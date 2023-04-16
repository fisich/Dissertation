package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public abstract class BaseObstacle {
    public VelocityObstacleType type;

    protected abstract boolean IsCollideWithVelocityObstacle(Vector2D point);

    protected abstract Vector2D FindVelocityOutSideVelocityObstacle(Agent agent);

    public enum VelocityObstacleType
    {
        DYNAMIC,
        STATIC
    }
}