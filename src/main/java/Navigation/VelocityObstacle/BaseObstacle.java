package Navigation.VelocityObstacle;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public abstract class BaseObstacle{
    protected double minkowskiRadius;
    protected Vector2D _relativeObstaclePos;

    public Vector2D getRelativePos() { return  _relativeObstaclePos; }

    public abstract VelocityObstacleType getType();

    public abstract boolean IsVelocityCollideWithObstacle(Vector2D velocity);

    public enum VelocityObstacleType
    {
        STATIC,
        DYNAMIC
    }
}