package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.time.LocalDateTime;
import java.util.List;

public class DynamicVelocityObstacle extends BaseObstacle{

    public final Vector2D dynamicObstacleVelocity;
    public final Vector2D leftSide, rightSide;

    public DynamicVelocityObstacle(Agent A, Agent B) {
        type = VelocityObstacleType.DYNAMIC;
        dynamicObstacleVelocity = B.getVelocity();
        double minkowskiRadius = (B.radius * 1.05d + A.radius);
        double minkowskiRadiusSq = minkowskiRadius * minkowskiRadius;
        Vector2D relativeObstaclePos = B.getPosition().subtract(A.getPosition());
        double distanceBetweenAgentsSq = Vector2D.distanceSq(B.getPosition(), A.getPosition());
        double distanceBetweenAgents = FastMath.sqrt(distanceBetweenAgentsSq);
        if (distanceBetweenAgentsSq < minkowskiRadiusSq) {
            System.out.println("Tricky collision: inside");
        }
        // Рассматриваем прямоугольный треугольник, чтобы найти касательную и угол ее наклона слева и справа для
        // формирования области VO.
        double tangentLength = FastMath.sqrt(Math.abs(distanceBetweenAgentsSq - minkowskiRadiusSq));
        double sin = minkowskiRadius / distanceBetweenAgents;
        double cos = tangentLength / distanceBetweenAgents;
        double triangleHeight = distanceBetweenAgents + minkowskiRadius;
        // Находим сторону равнобедренного треугольника, в который вписана окружность (сумма Минковского)
        // Длину получаем через cos
        Vector2D tangentVec = relativeObstaclePos.normalize().scalarMultiply(triangleHeight / cos);
        rightSide = new Vector2D(tangentVec.getX() * cos - tangentVec.getY() * sin,
                tangentVec.getX() * sin + tangentVec.getY() * cos);
        if (rightSide.isNaN())
            System.out.println("rightside is Nan");
        leftSide = new Vector2D(tangentVec.getX() * cos + tangentVec.getY() * sin,
                - tangentVec.getX() * sin + tangentVec.getY() * cos);
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        return IsPointInsideTriangle(point, Vector2D.ZERO, leftSide, rightSide);
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

    private static double sign(Vector2D p1, Vector2D p2, Vector2D p3)
    {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    @Override
    public Vector2D FindVelocityOutSideVelocityObstacle(Agent agent) {

        Vector2D agentVelocity = agent.getVelocity();
        Vector2D VOClosestSide;
        Vector2D approachVelocity = dynamicObstacleVelocity.subtract(agentVelocity);
        double distanceToLeft = leftSide.add(dynamicObstacleVelocity).getNorm();
        double distanceToRight = rightSide.add(dynamicObstacleVelocity).getNorm();
        boolean useOnlyDeviatedVelocity = false;
        // Находится ли агент внутри области VO
        if (IsCollideWithVelocityObstacle(dynamicObstacleVelocity.negate()))
        {
            //Если да - ищем ближайшую сторону VO к текущей скорости и стремимся покинуть ее.
            if (Vector2D.distance(leftSide, agentVelocity) < Vector2D.distance(rightSide, agentVelocity))
                VOClosestSide = leftSide;
            else
                VOClosestSide = rightSide;
            useOnlyDeviatedVelocity = true;
        }
        // Агент примерно равноудален от обоих концов VO, выбираем сторону взависимости от его скорости
        else if ( Math.abs(distanceToLeft - distanceToRight) < Math.min(distanceToLeft, distanceToRight)*0.02d)
        {
            if (Vector2D.distance(leftSide, approachVelocity) < Vector2D.distance(rightSide, approachVelocity))
                VOClosestSide = leftSide;
            else
                VOClosestSide = rightSide;
        }
        // Если он явно находится с какой-то из сторон, выбираем ближайшую
        else if (distanceToLeft < distanceToRight)
            VOClosestSide = leftSide;
        // Агент ближе к правой стороне и снаружи
        else
            VOClosestSide = rightSide;
        if (VOClosestSide.isNaN())
            System.out.println("VO side is nan");
        Vector2D deviatedVelocity = CalculateDeviatedVelocity(VOClosestSide, agentVelocity);
        if (useOnlyDeviatedVelocity)
        {
            return deviatedVelocity;
        }
        Vector2D straightVelocity = CalculateStraightVelocity(VOClosestSide, agentVelocity);
        return deviatedVelocity.add(straightVelocity).scalarMultiply(0.5d);
    }

    private Vector2D CalculateDeviatedVelocity(Vector2D VOClosestSide, Vector2D agentVelocity)
    {
        // Проверяем, есть ли такой поворот с текущей скоростью, который позволит выйти из VO
        // Для этого нужно для окружности радиусом Velocity определить касание с стороной VOClosestSide
        double m = VOClosestSide.getY() / VOClosestSide.getX();
        // b = y - ax
        double b = VOClosestSide.add(dynamicObstacleVelocity).getY() - m * VOClosestSide.add(dynamicObstacleVelocity).getX();
        // Решаем систему окружность x^2+y^2=R^2 и прямая y = mx+b
        // D = (mb)^2-(1+m^2)(b^2-R^2)
        double mb = m*b;
        double m_m_plus1 = m*m + 1;
        double agentVelocityLength = agentVelocity.getNorm();
        double discriminant = mb * mb-(m_m_plus1)*(b*b - agentVelocityLength * agentVelocityLength);
        // Поворот с текущей скоростью не найден
        if (discriminant < 0)
        {
            // Тогда нужно отклоняться в сторону перпендикуляра, как самый быстрый способ выйти из препятствия
            // y = -1/a*x - уравнение перпендикуляра
            // Точка пересечения перпендикуляра с прямой
            // -x/m=mx+b -> x = -mb/(m^2+1)
            double normalX = -mb / m_m_plus1;
            double normalY = m * normalX + b;
            return new Vector2D(normalX, normalY).normalize().scalarMultiply(agentVelocityLength);
        }
        else
        {
            // Поворот найден, вычисляем точки пересечения
            double discriminantSqrt = FastMath.sqrt(discriminant);
            if (Double.isNaN(discriminantSqrt))
                System.out.println("discr is Nan");
            double x1 = (-mb + discriminantSqrt)/m_m_plus1;
            double y1 = m*x1+b;
            Vector2D firstDeviatedVelocity = new Vector2D(x1, y1);
            double x2 = (-mb - discriminantSqrt)/m_m_plus1;
            double y2 = m*x2+b;
            Vector2D secondDeviatedVelocity = new Vector2D(x2, y2);
            // Если первая точка ближе к текущей скорости, отклоняемся в ее сторону
            if (Vector2D.distanceSq(firstDeviatedVelocity, agentVelocity) < Vector2D.distanceSq(secondDeviatedVelocity, agentVelocity))
            {
                return firstDeviatedVelocity;
            }
            // Иначе отклоняемся в сторону второй
            else
            {
                return secondDeviatedVelocity;
            }
        }
    }

    private Vector2D CalculateStraightVelocity(Vector2D VOClosestSide, Vector2D agentVelocity)
    {
        // Уравнение ближайшей стороны конуса y = mx + b
        // m = (y1-y0)/(x1-x0)
        double m1 = VOClosestSide.getY() / VOClosestSide.getX();
        // b = y - ax
        double b1 = VOClosestSide.add(dynamicObstacleVelocity).getY() - m1 * VOClosestSide.add(dynamicObstacleVelocity).getX();
        // Уравнение прямой, характеризующей скорость
        double m2 = agentVelocity.getY() / agentVelocity.getX();
        // Так как считаем относительно агента, скорость не смещена -> b = 0
        double b2 = 0;
        // Точка пересечения текущей скорости и стороны VO
        double straightVelX = -(b2 - b1) / (m1 - m2);
        double straightVelY =  (m2 * straightVelX - b2);
        Vector2D straightVelocity = new Vector2D(straightVelX, straightVelY);
        // Если точка пересечения снаружи, то изменением скорости избежать столкновения не удастся
        if (isPointOnLine(straightVelocity, Vector2D.ZERO, VOClosestSide))
            return agentVelocity;
        else
            return straightVelocity;
    }

    public static boolean isPointOnLine(Vector2D p, Vector2D a, Vector2D b) {
        if (a.getX() > b.getX()) {
            // меняем местами точки a и b
            Vector2D tmp = a;
            a = b;
            b = tmp;
        }
        double crossProduct = (p.getY() - a.getY()) * (b.getX() - a.getX()) - (p.getX() - a.getX()) * (b.getY() - a.getY());
        if (Math.abs(crossProduct) > 0.000001) {
            return false; // точка p не лежит на прямой, проходящей через точки a и b
        }
        double dotProduct = (p.getX() - a.getX()) * (b.getX() - a.getX()) + (p.getY() - a.getY())*(b.getY() - a.getY());
        if (dotProduct < 0) {
            return false; // точка p находится слева от точки a
        }
        double squaredLengthBA = Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2);
        if (dotProduct > squaredLengthBA) {
            return false; // точка p находится справа от точки b
        }
        return true;
    }
}
