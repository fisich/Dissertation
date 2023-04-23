package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public interface IVelocityObstacle {
    boolean IsCollideWithVelocityObstacle(Vector2D point);
    Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity);
}
