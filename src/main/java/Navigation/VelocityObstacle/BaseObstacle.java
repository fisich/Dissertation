package Navigation.VelocityObstacle;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public abstract class BaseObstacle {
    protected double minkowskiRadius;
    protected Vector2D relativeObstaclePos;

    protected abstract VelocityObstacleType getType();

    protected abstract boolean isVelocityCollide(Vector2D velocity);

    public enum VelocityObstacleType {
        STATIC,
        DYNAMIC
    }

    public double getMinkowskiRadius() {
        return minkowskiRadius;
    }

    public Vector2D getRelativeObstaclePos() {
        return relativeObstaclePos;
    }
}