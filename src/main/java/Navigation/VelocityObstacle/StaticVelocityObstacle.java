package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import static MathExtensions.Vector2DExtension.*;

public class StaticVelocityObstacle extends BaseObstacle{
    public final double minkowskiRadius;

    public StaticVelocityObstacle(Agent A, Agent B) {
        _relativeObstaclePos = B.getPosition().subtract(A.getPosition());
        minkowskiRadius = (B.radius + A.radius);
        double distanceBetweenAgents = _relativeObstaclePos.getNorm();
        if (distanceBetweenAgents <=  minkowskiRadius)
            throw new RuntimeException("Error, agent inside static velocity");
    }

    public StaticVelocityObstacle(Vector2D position, double radius)
    {
        minkowskiRadius = radius;
        _relativeObstaclePos = position;
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        return IsVecCrossCircle(Vector2D.ZERO, point, _relativeObstaclePos, minkowskiRadius);
    }

    @Override
    public VelocityObstacleType getType()
    {
        return VelocityObstacleType.STATIC;
    }
}