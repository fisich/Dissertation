package Navigation.VelocityObstacle;

import MathExtensions.Vector2DExtension;
import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.util.*;

import static MathExtensions.Vector2DExtension.*;

public class DynamicVelocityObstacle extends BaseObstacle{

    private final double _minkowskiRadius;
    private final Vector2D _relativeBPosition;

    public DynamicVelocityObstacle(Agent A, Agent B) {
        // RVO
        _relativeObstaclePos = B.getVelocity();//.add(A.getVelocity()).scalarMultiply(0.5d);
        _minkowskiRadius = (B.radius + A.radius);
        double minkowskiRadiusSq = _minkowskiRadius * _minkowskiRadius;
        _relativeBPosition = B.getPosition().subtract(A.getPosition());
        double distanceBetweenAgentsSq = Vector2D.distanceSq(B.getPosition(), A.getPosition()); // TODO: vecBetweenAgents?
        double distanceBetweenAgents = FastMath.sqrt(distanceBetweenAgentsSq);
        if (distanceBetweenAgentsSq < minkowskiRadiusSq) {
            throw new RuntimeException("Tricky collision: inside dynamic");
        }
        // Рассматриваем прямоугольный треугольник, чтобы найти касательную и угол ее наклона слева и справа для
        // формирования области VO.
        double tangentLength = 0;
        if (distanceBetweenAgents > _minkowskiRadius)
            tangentLength = FastMath.sqrt(Math.abs(distanceBetweenAgentsSq - minkowskiRadiusSq));
        double sin = _minkowskiRadius / distanceBetweenAgents;
        double cos = tangentLength / distanceBetweenAgents;
        double triangleHeight = distanceBetweenAgents + _minkowskiRadius;
        // Находим сторону равнобедренного треугольника, в который вписана окружность (сумма Минковского)
        // Длину получаем через cos
        Vector2D tangentVec = _relativeBPosition.normalize().scalarMultiply(triangleHeight / cos);
        _rightSide = RotateVector(tangentVec, -sin, cos);
        _leftSide = RotateVector(tangentVec, sin, cos);
        if (_leftSide.isNaN() || _rightSide.isNaN())
            System.out.println("Dynamic VO side is Nan");
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
            // Честный подсчет, преобразовывать точку point не надо
            return IsPointInsideTriangle(point, _relativeObstaclePos,
                    _leftSide.add(_relativeObstaclePos),
                    _rightSide.add(_relativeObstaclePos));
    }

    private static boolean IsPointInsideTriangle(Vector2D point, Vector2D A, Vector2D B, Vector2D C)
    {
        // TODO: переделать через углы (IsPointOutsideTriangle)
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

    /*@Override
    public List<Vector2D> FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity, VelocityObstacleSide side)
    {
        if(!IsCollideWithVelocityObstacle(currentVelocity)) {
            return Collections.singletonList(currentVelocity);
            //throw new RuntimeException("Current velocity is outside obstacle, why we should calculate");
        }
        List<Vector2D> velocities = new ArrayList<>();
        for (int i = 0; i < 24; i++)
        {
            velocities.add(RotateVector(currentVelocity, 0.261799 * i));
        }
        return velocities;
        /*if (currentVelocity.isNaN())
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

        // Получаем отклоненную скорость
        List<Vector2D> deviatedVelocities = CalculateDeviatedVelocity(VOside, currentVelocity);
        // Inside
        if (IsCollideWithVelocityObstacle(Vector2D.ZERO))
        {
            Vector2D normalVelocity = CalculateNormalVelocity(VOside, currentVelocity);
            deviatedVelocities = deviatedVelocities.stream().map(vel ->
            {
                double angle = GetAngleBetweenVectors(currentVelocity, vel);
                if (angle <= 0 && angle > -FastMath.PI/2)
                    return RotateVector(vel, -0.1d);
                else if (angle <= -FastMath.PI/2 && FastMath.abs(angle) < FastMath.PI)
                    return RotateVector(vel, 0.1d);
                else if (angle > 0 && angle <= FastMath.PI/2)
                    return RotateVector(vel, 0.1d);
                else
                    return RotateVector(vel, -0.1d);
            }).collect(Collectors.toList());
            //deviatedVelocities = deviatedVelocities.stream().filter(vel -> !IsVecCrossCircle(currentVelocity, _relativeBPosition, _minkowskiRadius)).collect(Collectors.toList());
            if (deviatedVelocities.isEmpty())
            {
                return Collections.singletonList(normalVelocity);
            }
            else
            {
                deviatedVelocities.add(normalVelocity);
                return deviatedVelocities;
            }
        }
        // Outside
        else
        {
            deviatedVelocities = deviatedVelocities.stream().map(vel ->
            {
                if (IsPointOnLine(vel, _relativeObstaclePos, VOside.add(_relativeObstaclePos)))
                {
                    VelocityObstacleSide closestSide;
                    // Ближайшая сторона - левая
                    if (_leftSide.add(_relativeObstaclePos).getNormSq() < _rightSide.add(_relativeObstaclePos).getNormSq())
                        closestSide = VelocityObstacleSide.LEFT;
                    // Ближайшая сторона - правая
                    else
                        closestSide = VelocityObstacleSide.RIGHT;
                    // Скорости всегда выходят крайними, поэтому не может быть такого, что доворачивание ведет к столкновению
                    // Текущая сторона - ближняя
                    if (side == closestSide)
                    {
                        double angle = GetAngleBetweenVectors(currentVelocity, vel);
                        if (angle < 0)
                            return RotateVector(vel, -0.1d);
                        else
                            return RotateVector(vel, 0.1d);
                    }
                    //Расчитывали по дальней стороне
                    else
                    {
                        double angle = GetAngleBetweenVectors(currentVelocity, vel);
                        if (angle < 0)
                            return RotateVector(vel, 0.1d);
                        else
                            return RotateVector(vel, -0.1d);
                    }
                }
                return vel;
            }).collect(Collectors.toList());
            return deviatedVelocities;
        }
    }*/


    private List<Vector2D> CalculateDeviatedVelocity(Vector2D VOClosestSide, Vector2D currentVelocity)
    {
        // Проверяем, есть ли такой поворот с текущей скоростью, который позволит выйти из VO
        // Для этого нужно для окружности радиусом Velocity определить касание с стороной VOClosestSide
        Vector2DExtension.LineEquation equation = GetLineEquation(_relativeObstaclePos, VOClosestSide.add(_relativeObstaclePos));
        // Решаем систему окружность x^2+y^2=R^2 и прямая y = mx+b
        // D = (mb)^2-(1+m^2)(b^2-R^2)
        double mb = equation.M * equation.B;
        double m_m_plus1 = equation.M * equation.M + 1;
        double agentVelocityLength = currentVelocity.getNorm();
        double discriminant = mb * mb - (m_m_plus1) * (equation.B*equation.B - agentVelocityLength * agentVelocityLength);
        // Поворот с текущей скоростью не найден
        if (discriminant < 0)
        {
            return new ArrayList<>();
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
            List<Vector2D> velocities = new ArrayList<>();
            velocities.add(firstDeviatedVelocity);
            velocities.add(secondDeviatedVelocity);
            return velocities;
        }
    }

    private Vector2D CalculateNormalVelocity(Vector2D VOClosestSide, Vector2D currentVelocity)
    {
        Vector2DExtension.LineEquation equation = GetLineEquation(_relativeObstaclePos, VOClosestSide.add(_relativeObstaclePos));
        // y = -1/m*x - уравнение перпендикуляра
        // Точка пересечения перпендикуляра с прямой
        // -x/m=mx+b -> x = -mb/(m^2+1) 100% OK
        double mb = equation.M * equation.B;
        double m_m_plus1 = equation.M * equation.M + 1;
        double normalX = -mb / m_m_plus1;
        double normalY = equation.M * normalX + equation.B;
        return new Vector2D(normalX, normalY).normalize().scalarMultiply(currentVelocity.getNorm());
    }

    @Override
    public VelocityObstacleType type()
    {
        return VelocityObstacleType.DYNAMIC;
    }
}