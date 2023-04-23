package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public abstract class BaseObstacle implements IVelocityObstacle {
    protected VelocityObstacleType _type;
    protected Vector2D _leftSide, _rightSide;
    protected Vector2D _relativeObstaclePos;

    public Vector2D leftSide()
    {
        return _leftSide;
    }

    public Vector2D rightSide()
    {
        return _rightSide;
    }

    public Vector2D relativeObstaclePos() { return  _relativeObstaclePos; }

    public abstract Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity, VelocityObstacleSide side);

    public enum VelocityObstacleSide
    {
        LEFT,
        RIGHT
    }

    public enum VelocityObstacleType
    {
        STATIC,
        DYNAMIC,
        GROUP
    }

    public VelocityObstacleType type()
    {
        return _type;
    }
}