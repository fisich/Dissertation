package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import static MathExtensions.Vector2DExtension.*;

public class StaticVelocityObstacle extends BaseObstacle{

    public StaticVelocityObstacle(Agent A, Agent B) {
        setupFields(B.getPosition().subtract(A.getPosition()), A.radius + B.radius);
    }

    public StaticVelocityObstacle(Vector2D relativePosition, double radius)
    {
        setupFields(relativePosition, radius);
    }

    private void setupFields(Vector2D relativePosition, double radius)
    {
        minkowskiRadius = radius;
        _relativeObstaclePos = relativePosition;
        if (_relativeObstaclePos.getNorm() <=  minkowskiRadius)
            throw new RuntimeException("Error, agent inside static velocity");
    }

    @Override
    public boolean IsVelocityCollideWithObstacle(Vector2D velocity) {
        return IsVecCrossCircle(Vector2D.ZERO, velocity, _relativeObstaclePos, minkowskiRadius);
    }

    @Override
    public VelocityObstacleType getType()
    {
        return VelocityObstacleType.STATIC;
    }
}