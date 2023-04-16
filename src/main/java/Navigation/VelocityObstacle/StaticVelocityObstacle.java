package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

public class StaticVelocityObstacle extends BaseObstacle{
    private final double minkowskiRadius;
    private final Vector2D relativeObstaclePos;

    public StaticVelocityObstacle(Agent A, Agent B) {
        type = VelocityObstacleType.STATIC;
        minkowskiRadius = (B.radius * 1.05d + A.radius);
        relativeObstaclePos = B.getPosition().subtract(A.getPosition());
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        return IsVecCrossCircle(point, relativeObstaclePos, minkowskiRadius);
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
        double m = vec.getY() / vec.getX();
        double xC_plus_m_yC = circleCenter.getX() + m * circleCenter.getY();
        double discriminant = (xC_plus_m_yC * xC_plus_m_yC) - (1 + m*m)*(circleCenter.getX() * circleCenter.getX()
                + circleCenter.getY() * circleCenter.getY() - radius * radius);
        double accuracy = 0.25;
        if (discriminant < accuracy) {
            return false;
        }
        return discriminant > 0;
    }

    @Override
    public Vector2D FindVelocityOutSideVelocityObstacle(Agent agent) {
        double distanceBetweenAgents = relativeObstaclePos.getNorm();
        // Длина касательной к окружности
        double tangentLength = FastMath.sqrt(relativeObstaclePos.getNormSq() - minkowskiRadius * minkowskiRadius);
        // sin и cos для поворота прямой, соединяющей центры агента и препятствия в сторону касательной
        double sin = minkowskiRadius / distanceBetweenAgents;
        double cos = tangentLength / distanceBetweenAgents;
        // Текущая величина скорости
        double velValue = agent.getVelocity().getNorm();
        Vector2D tangentVec = relativeObstaclePos.normalize().scalarMultiply(velValue);

        Vector2D rightSide = new Vector2D(tangentVec.getX() * cos - tangentVec.getY() * sin,
                tangentVec.getX() * sin + tangentVec.getY() * cos);
        Vector2D leftSide = new Vector2D(tangentVec.getX() * cos + tangentVec.getY() * sin,
                - tangentVec.getX() * sin + tangentVec.getY() * cos);
        //if (leftSide.isNaN() || rightSide.isNaN())
        //    return null;
        if (Vector2D.distanceSq(leftSide, agent.getVelocity()) < Vector2D.distanceSq(rightSide, agent.getVelocity()))
            return leftSide;
        else
            return rightSide;
    }
}
