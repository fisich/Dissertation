package Navigation.VelocityObstacle;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public interface IVelocityObstacle {
    boolean IsCollideWithVelocityObstacle(Vector2D point);

    enum VelocityObstacleSide
    {
        LEFT,
        RIGHT
    }
}