package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import static MathExtensions.Vector2DExtension.*;

public class StaticVelocityObstacle extends BaseObstacle{
    private final double minkowskiRadius;

    public StaticVelocityObstacle(Agent A, Agent B) {
        _relativeObstaclePos = B.getPosition().subtract(A.getPosition());
        minkowskiRadius = (B.radius + A.radius);
        double distanceBetweenAgents = _relativeObstaclePos.getNorm();
        // Длина касательной к окружности
        double tangentLength = 0;
        if (distanceBetweenAgents > minkowskiRadius)
            tangentLength = FastMath.sqrt(distanceBetweenAgents * distanceBetweenAgents - minkowskiRadius * minkowskiRadius);
        else
            System.out.println("Static obstacle cross");
        // sin и cos для поворота прямой, соединяющей центры агента и препятствия в сторону касательной
        double sin = minkowskiRadius / distanceBetweenAgents;
        double cos = tangentLength / distanceBetweenAgents;
        // Текущая величина скорости
        double velValue = A.getVelocity().getNorm();
        Vector2D tangentVec = _relativeObstaclePos.normalize().scalarMultiply(velValue);
        _rightSide = RotateVector(tangentVec, -sin, cos);
        _leftSide = RotateVector(tangentVec, sin, cos);
        if (_leftSide.isNaN() || _rightSide.isNaN())
            System.out.println("ctor static");
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        return IsVecCrossCircle(point, _relativeObstaclePos, minkowskiRadius);
    }

    private static boolean IsVecCrossCircle(Vector2D vec, Vector2D circleCenter, double radius)
    {
        // Если движется прямо на препятствие (скорость внутри VO)
        if (Vector2D.distanceSq(vec, circleCenter) <= radius * radius)
            return true;
        // Если скорость вне VO и агент также находится вне VO, либо движется изнутри VO
        else if (Vector2D.distanceSq(vec, circleCenter) > vec.getNormSq())
            return false;
        // Иначе нужно проверить, что траектория движения пересекает область препятствий
        // Т.е. есть решения системы y = mx + b и (x-x_c)^2+(y-y_c)^2=r^2
        // Так как считаем относительно самого агента, его скорость не смещена -> b = 0
        // Нужно проверить, что D >= 0:
        // D = (2*x_c+2*m*y_c)^2 - 4(1+m^2)(x_c^2+y_c^2-r^2)
        // После оптицизации остается проверка:
        // (x_c+m*y_c)^2 >= (1+m^2)(x_c^2+y_c^2-r^2)
        double m = GetLineEquation(Vector2D.ZERO, vec).M;
        double xC_plus_m_yC = circleCenter.getX() + m * circleCenter.getY();
        double discriminant = (xC_plus_m_yC * xC_plus_m_yC) - (1 + m*m)*(circleCenter.getX() * circleCenter.getX()
                + circleCenter.getY() * circleCenter.getY() - radius * radius);
        return discriminant >= 0;
    }

    @Override
    public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity) {
        if (currentVelocity.isNaN())
            System.out.println("Find new Vel static is NaN");
        VelocityObstacleSide bestSide;
        if (Vector2D.distanceSq(_leftSide, currentVelocity) < Vector2D.distanceSq(_rightSide, currentVelocity))
            bestSide = VelocityObstacleSide.LEFT;
        else
            bestSide = VelocityObstacleSide.RIGHT;
        return FindVelocityOutsideVelocityObstacle(currentVelocity, bestSide);
    }

    @Override
    public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity, VelocityObstacleSide side) {
        Vector2D result;
        switch (side)
        {
            case LEFT:
                result = _leftSide;
                return RotateVector(result, 0.05d);
            case RIGHT:
                result = _rightSide;
                return RotateVector(result, -0.05d);
            default:
                throw new IllegalStateException("Unexpected value: " + side);
        }
    }

    @Override
    public VelocityObstacleType type()
    {
        return VelocityObstacleType.STATIC;
    }
}
