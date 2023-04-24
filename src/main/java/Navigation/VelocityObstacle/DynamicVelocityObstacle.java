package Navigation.VelocityObstacle;

import MathExtensions.Vector2DExtension;
import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static MathExtensions.Vector2DExtension.*;

public class DynamicVelocityObstacle extends BaseObstacle{
    public final DynamicObstacleType dynamicType;

    public DynamicVelocityObstacle(BaseObstacle VORight, BaseObstacle VOLeft)
    {
        _type = VelocityObstacleType.GROUP;
        Vector2D relativeVOLeft = VOLeft.leftSide().add(VOLeft.relativeObstaclePos());
        Vector2D relativeVORight = VORight.rightSide().add(VORight.relativeObstaclePos());
        _relativeObstaclePos = GetLinesCross(VOLeft.relativeObstaclePos(), relativeVOLeft,
                VORight.relativeObstaclePos(), relativeVORight);
        _leftSide = relativeVOLeft.subtract(_relativeObstaclePos);
        _rightSide = relativeVORight.subtract(_relativeObstaclePos);
        double angle = GetAngleBetweenVectors(_rightSide, _leftSide);
        if (angle <= 0 || angle >= Math.PI)
            dynamicType = DynamicObstacleType.WIDE;
        else
            dynamicType = DynamicObstacleType.NARROW;
    }

    public DynamicVelocityObstacle(Agent A, Agent B) {
        _relativeObstaclePos = B.getVelocity().add(A.getVelocity()).scalarMultiply(0.5d);
        double minkowskiRadius = (B.radius + A.radius);
        double minkowskiRadiusSq = minkowskiRadius * minkowskiRadius;
        Vector2D relativeObstaclePos = B.getPosition().subtract(A.getPosition());
        double distanceBetweenAgentsSq = Vector2D.distanceSq(B.getPosition(), A.getPosition());
        double distanceBetweenAgents = FastMath.sqrt(distanceBetweenAgentsSq);
        if (distanceBetweenAgentsSq < minkowskiRadiusSq) {
            throw new IllegalStateException("Tricky collision: inside dynamic");
        }
        // Рассматриваем прямоугольный треугольник, чтобы найти касательную и угол ее наклона слева и справа для
        // формирования области VO.
        double tangentLength = 0;
        if (distanceBetweenAgents > minkowskiRadius)
            tangentLength = FastMath.sqrt(Math.abs(distanceBetweenAgentsSq - minkowskiRadiusSq));
        double sin = minkowskiRadius / distanceBetweenAgents;
        double cos = tangentLength / distanceBetweenAgents;
        double triangleHeight = distanceBetweenAgents + minkowskiRadius;
        // Находим сторону равнобедренного треугольника, в который вписана окружность (сумма Минковского)
        // Длину получаем через cos
        Vector2D tangentVec = relativeObstaclePos.normalize().scalarMultiply(triangleHeight / cos);
        _rightSide = RotateVector(tangentVec, -sin, cos);
        _leftSide = RotateVector(tangentVec, sin, cos);
        if (_leftSide.isNaN() || _rightSide.isNaN())
            System.out.println("Dynamic VO side is Nan");
        if (GetAngleBetweenVectors(_rightSide, _leftSide) <= 0)
            dynamicType = DynamicObstacleType.WIDE;
        else
            dynamicType = DynamicObstacleType.NARROW;
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        if (dynamicType == DynamicObstacleType.WIDE)
            return IsPointOutsideTriangle(point);
        else
            // Честный подсчет, преобразовывать точку point не надо
            return IsPointInsideTriangle(point, _relativeObstaclePos,
                    _leftSide.add(_relativeObstaclePos),
                    _rightSide.add(_relativeObstaclePos));
    }

    private static boolean IsPointInsideTriangle(Vector2D point, Vector2D A, Vector2D B, Vector2D C)
    {
        // https://www.cyberforum.ru/algorithms/thread144722.html
        double first = sign(point, A, B);
        double second = sign(point, B, C);
        double third = sign(point, C, A);
        boolean has_neg = (first < 0) || (second < 0) || (third < 0);
        boolean has_pos = (first > 0) || (second > 0) || (third > 0);

        return !(has_neg && has_pos);
    }

    private boolean IsPointOutsideTriangle(Vector2D point)
    {
        double angleBetweenLeftRight = GetAngleBetweenVectors(_leftSide, _rightSide);
        double angleBetweenLeftPoint = GetAngleBetweenVectors(_leftSide, point.subtract(_relativeObstaclePos));
        if (angleBetweenLeftPoint < 0)
            return true;
        return angleBetweenLeftPoint > angleBetweenLeftRight;
    }

    private static double sign(Vector2D p1, Vector2D p2, Vector2D p3)
    {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    @Override
    public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity) {
        if (currentVelocity.isNaN())
            System.out.println("Find new Vel dynamic is Nan");
        VelocityObstacleSide side = chooseClosestSide(currentVelocity);
        return FindVelocityOutsideVelocityObstacle(currentVelocity, side);
    }

    private VelocityObstacleSide chooseClosestSide(Vector2D currentVelocity)
    {
        VelocityObstacleSide side;
        Vector2D approachVelocity = currentVelocity.subtract(_relativeObstaclePos); // 100% fixed
        double distanceToLeft = _leftSide.add(_relativeObstaclePos).getNorm();
        double distanceToRight = _rightSide.add(_relativeObstaclePos).getNorm();
        // Если агент примерно равноудален от обоих концов VO или находится внутри VO
        // то выбираем сторону взависимости от его скорости
        if (Math.abs(distanceToLeft - distanceToRight) < Math.min(distanceToLeft, distanceToRight) * 0.02d
                || IsCollideWithVelocityObstacle(Vector2D.ZERO) && dynamicType == DynamicObstacleType.NARROW)
        {
            if (Vector2D.distance(_leftSide, approachVelocity) < Vector2D.distance(_rightSide, approachVelocity)) {
                side = VelocityObstacleSide.LEFT;
            }
            else {
                side = VelocityObstacleSide.RIGHT;
            }
        }
        // Если он снаружи и явно находится с какой-то из сторон, то ближайшую выбираем на основе местоположения
        else {
            Vector2D normal1 = CalculateNormalVelocity(_leftSide);
            Vector2D normal2 = CalculateNormalVelocity(_rightSide);
            if (!IsPointOnLine(normal1, _relativeObstaclePos, _leftSide.add(_relativeObstaclePos)))
                side = VelocityObstacleSide.RIGHT;
            else {
                if (!IsPointOnLine(normal2, _relativeObstaclePos, _rightSide.add(_relativeObstaclePos))) {
                    side = VelocityObstacleSide.LEFT;
                } else {
                    if (normal1.getNorm() < normal2.getNorm())
                        side = VelocityObstacleSide.LEFT;
                    else
                        side = VelocityObstacleSide.RIGHT;
                }
            }
        }
        return side;
    }

    @Override
    public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity, VelocityObstacleSide side)
    {
        if(!IsCollideWithVelocityObstacle(currentVelocity))
            return currentVelocity;
        if (currentVelocity.isNaN())
            System.out.println("Why current velocity is nan");
        Vector2D VOside;
        switch (side)
        {
            case LEFT:
                VOside = _leftSide;
                break;
            case RIGHT:
                VOside = _rightSide;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + side);
        }
        // Если VO не широкая, то
        // 1 - Если мы внутри VO, то отклоняемся к ближайшей стороне, либо в сторону перпендикуляра TODO: Может вообще назад
        // 2 - Если мы снаружи VO, то берем скорость по прямой и по касательной
        // TODO: Может тут можно учесть идеальную скорость, чтоб понять чему отдавать предпочтение
        //  отклонению или прямой скорости
        switch (dynamicType)
        {
            case NARROW:
                System.out.println("Narrow");
                // Получаем отклоненную скорость
                Vector2D deviatedVelocity = CalculateDeviatedVelocity(VOside, currentVelocity);
                if (IsCollideWithVelocityObstacle(Vector2D.ZERO))
                {
                    if (deviatedVelocity.equals(Vector2D.ZERO))
                    {
                        System.out.println("deviated not helps");
                        Vector2D normalizedVelocity = CalculateNormalVelocity(VOside);
                        if (normalizedVelocity.getNorm() > currentVelocity.getNorm())
                            normalizedVelocity = normalizedVelocity.normalize().scalarMultiply(currentVelocity.getNorm());
                        return normalizedVelocity;
                    }
                    else
                    {
                        double angle = GetAngleBetweenVectors(currentVelocity, deviatedVelocity);
                        if (angle < 0)
                            deviatedVelocity = RotateVector(deviatedVelocity, -0.1d);
                        else
                            deviatedVelocity = RotateVector(deviatedVelocity, 0.1d);
                        if (IsCollideWithVelocityObstacle(deviatedVelocity))
                        {
                            System.out.println("Definitely problem with narrow VO");
                        }
                        return deviatedVelocity;
                    }
                }
                else
                {
                    if (deviatedVelocity.equals(Vector2D.ZERO))
                    {
                        System.out.println("deviated velocity not works");
                    }
                    else {
                        double angle = GetAngleBetweenVectors(currentVelocity, deviatedVelocity);
                        if (angle < 0)
                            deviatedVelocity = RotateVector(deviatedVelocity, -0.1d);
                        else
                            deviatedVelocity = RotateVector(deviatedVelocity, 0.1d);
                    }
                    if (IsCollideWithVelocityObstacle(deviatedVelocity))
                        System.out.println("wrong deviated vel in narrow");
                    Vector2D straightVelocity = CalculateStraightVelocity(VOside, currentVelocity).scalarMultiply(0.9d);
                    if (straightVelocity.equals(Vector2D.ZERO))
                    {
                        System.out.println("skip straight");
                        return deviatedVelocity;
                    }
                    else
                    {
                        if (IsCollideWithVelocityObstacle(straightVelocity))
                        {
                            System.out.println("rel " + relativeObstaclePos());
                            System.out.println("lef " + leftSide().add(relativeObstaclePos()));
                            System.out.println("rig " + rightSide().add(relativeObstaclePos()));
                            System.out.println("cur " + currentVelocity);
                            System.out.println("str " + straightVelocity);
                            System.out.println("");
                            return deviatedVelocity;
                        }
                        else
                            return deviatedVelocity.add(straightVelocity).scalarMultiply(0.5d);
                    }
                }
            case WIDE:
                // Inside
                if (IsCollideWithVelocityObstacle(Vector2D.ZERO))
                {
                    System.out.println("Inside wide");
                    System.out.println("rel " + relativeObstaclePos());
                    System.out.println("lef " + leftSide().add(relativeObstaclePos()));
                    System.out.println("rig " + rightSide().add(relativeObstaclePos()));
                    System.out.println("cur " + currentVelocity);
                    deviatedVelocity = VOside.normalize().scalarMultiply(currentVelocity.getNorm());
                    Vector2D normalVelocity = CalculateNormalVelocity(VOside).normalize()
                            .scalarMultiply(currentVelocity.getNorm());
                    //deviatedVelocity = CalculateDeviatedVelocity(VOside, normalVelocity, VOside);
                    if (!deviatedVelocity.equals(Vector2D.ZERO))
                    {
                        double angle = GetAngleBetweenVectors(currentVelocity, deviatedVelocity);
                        if (angle < 0)
                            deviatedVelocity = RotateVector(deviatedVelocity, -0.1d);
                        else
                            deviatedVelocity = RotateVector(deviatedVelocity, 0.1d);
                    }
                    else
                    {
                        System.out.println("Oops");
                        return normalVelocity;
                    }
                    if (IsCollideWithVelocityObstacle(deviatedVelocity)) {
                        System.out.println("think better");
                        IsCollideWithVelocityObstacle(deviatedVelocity);
                    }
                    return deviatedVelocity;
                }
                // Outside
                else
                {
                    System.out.println("outside wide");
                    deviatedVelocity = VOside.normalize().scalarMultiply(currentVelocity.getNorm());
                    //Vector2D normal = CalculateNormalVelocity(VOside);
                    //if (IsPointOnLine(normal, _relativeObstaclePos, VOside.add(_relativeObstaclePos)))
                    //    deviatedVelocity = deviatedVelocity.add(normal.scalarMultiply(0.9d)).scalarMultiply(0.5d);
                    if (deviatedVelocity.equals(Vector2D.ZERO))
                    {
                        System.out.println("deviated velocity not works");
                    }
                    else {
                        double angle = GetAngleBetweenVectors(currentVelocity, deviatedVelocity);
                        if (angle < 0)
                            deviatedVelocity = RotateVector(deviatedVelocity, -0.1d);
                        else
                            deviatedVelocity = RotateVector(deviatedVelocity, 0.1d);
                    }
                    if (IsCollideWithVelocityObstacle(deviatedVelocity)) {
                        System.out.println("rel " + relativeObstaclePos());
                        System.out.println("lef " + leftSide().add(relativeObstaclePos()));
                        System.out.println("rig " + rightSide().add(relativeObstaclePos()));
                        System.out.println("cur " + currentVelocity);
                        System.out.println("new " + deviatedVelocity);
                        IsCollideWithVelocityObstacle(deviatedVelocity);
                    }
                    return deviatedVelocity;
                }
            default:
                throw new IllegalStateException("Unexpected value: " + dynamicType);
        }
    }


    private Vector2D CalculateDeviatedVelocity(Vector2D VOClosestSide, Vector2D agentVelocity, Vector2D priority)
    {
        // Проверяем, есть ли такой поворот с текущей скоростью, который позволит выйти из VO
        // Для этого нужно для окружности радиусом Velocity определить касание с стороной VOClosestSide
        Vector2DExtension.LineEquation equation = GetLineEquation(_relativeObstaclePos, VOClosestSide.add(_relativeObstaclePos));
        // Решаем систему окружность x^2+y^2=R^2 и прямая y = mx+b
        // D = (mb)^2-(1+m^2)(b^2-R^2)
        double mb = equation.M * equation.B;
        double m_m_plus1 = equation.M * equation.M + 1;
        double agentVelocityLength = agentVelocity.getNorm();
        double discriminant = mb * mb - (m_m_plus1) * (equation.B*equation.B - agentVelocityLength * agentVelocityLength);
        // Поворот с текущей скоростью не найден
        if (discriminant < 0)
        {
            return Vector2D.ZERO;
        }
        else
        {
            // Поворот найден, вычисляем точки пересечения
            double discriminantSqrt = FastMath.sqrt(discriminant);
            if (Double.isNaN(discriminantSqrt))
                System.out.println("discr is Nan");
            double x1 = (-mb + discriminantSqrt)/m_m_plus1;
            double y1 = equation.M*x1+equation.B;
            Vector2D firstDeviatedVelocity = new Vector2D(x1, y1);
            double x2 = (-mb - discriminantSqrt)/m_m_plus1;
            double y2 = equation.M*x2+equation.B;
            Vector2D secondDeviatedVelocity = new Vector2D(x2, y2);
            Optional<Vector2D> result;
            List<Vector2D> velocities = new ArrayList<>();
            velocities.add(firstDeviatedVelocity);
            velocities.add(secondDeviatedVelocity);
            velocities = velocities.stream().filter(vel -> IsPointOnLine(vel, _relativeObstaclePos, VOClosestSide.add(_relativeObstaclePos)))
                    .collect(Collectors.toList());
            result = velocities.stream().min(Comparator.comparingDouble(a -> Vector2D.distanceSq(priority, a)));
            return result.orElse(Vector2D.ZERO);
        }
    }

    private Vector2D CalculateDeviatedVelocity(Vector2D VOClosestSide, Vector2D agentVelocity)
    {
        return CalculateDeviatedVelocity(VOClosestSide, agentVelocity, agentVelocity);
    }

    private Vector2D CalculateNormalVelocity(Vector2D VOClosestSide)
    {
        Vector2DExtension.LineEquation equation = GetLineEquation(_relativeObstaclePos, VOClosestSide.add(_relativeObstaclePos));
        // y = -1/m*x - уравнение перпендикуляра
        // Точка пересечения перпендикуляра с прямой
        // -x/m=mx+b -> x = -mb/(m^2+1) 100% OK
        double mb = equation.M * equation.B;
        double m_m_plus1 = equation.M * equation.M + 1;
        double normalX = -mb / m_m_plus1;
        double normalY = equation.M * normalX + equation.B;
        return new Vector2D(normalX, normalY);
    }

    private Vector2D CalculateStraightVelocity(Vector2D VOClosestSide, Vector2D agentVelocity)
    {
        Vector2D straightVelocity = GetLinesCross(_relativeObstaclePos, VOClosestSide.add(_relativeObstaclePos),
                Vector2D.ZERO, agentVelocity);
        // Если точка пересечения снаружи, то изменением скорости избежать столкновения не удастся
        if (IsPointOnLine(straightVelocity, _relativeObstaclePos, VOClosestSide.add(_relativeObstaclePos))) {
            return straightVelocity;
        }
        else {
            System.out.println("Return default velocity");
            return Vector2D.ZERO;
        }
    }

    public enum DynamicObstacleType
    {
        NARROW,
        WIDE
    }

    @Override
    public VelocityObstacleType type()
    {
        return VelocityObstacleType.DYNAMIC;
    }
}
