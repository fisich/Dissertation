package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import static MathExtensions.Vector2DExtension.isVector2DCrossCircle;

public class StaticVelocityObstacle extends BaseObstacle {

    public StaticVelocityObstacle(Agent origin, Agent obstacleAgent) {
        setup(obstacleAgent.getPosition().subtract(origin.getPosition()), origin.radius + obstacleAgent.radius);
    }

    public StaticVelocityObstacle(Vector2D relativePosition, double radius) {
        setup(relativePosition, radius);
    }

    private void setup(Vector2D relativePosition, double radius) {
        minkowskiRadius = radius;
        relativeObstaclePos = relativePosition;
        if (relativeObstaclePos.getNorm() <= minkowskiRadius)
            throw new RuntimeException("Error, agent inside static velocity");
    }

    @Override
    public boolean isVelocityCollide(Vector2D velocity) {
        return isVector2DCrossCircle(Vector2D.ZERO, velocity, relativeObstaclePos, minkowskiRadius);
    }

    @Override
    public VelocityObstacleType getType() {
        return VelocityObstacleType.STATIC;
    }
}