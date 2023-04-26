package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.util.Collections;
import java.util.List;

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
            throw new RuntimeException("Error, agent inside static velocity");
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

/*    @Override
    public List<Vector2D> FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity, VelocityObstacleSide side) {
        switch (side)
        {
            case LEFT:
                return Collections.singletonList(RotateVector(_leftSide, 0.1d));
            case RIGHT:
                return Collections.singletonList(RotateVector(_rightSide, -0.1d));
            default:
                throw new IllegalStateException("Unexpected value: " + side);
        }
    }*/

    @Override
    public VelocityObstacleType type()
    {
        return VelocityObstacleType.STATIC;
    }
}