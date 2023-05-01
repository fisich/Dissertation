package Navigation.VelocityObstacle;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import static MathExtensions.Vector2DExtension.CalculateDistanceToLine;
import static MathExtensions.Vector2DExtension.GetLinesCross;

public abstract class BaseObstacle implements IVelocityObstacle {
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

    public Vector2D GetCrossPointWithClosestSide(Vector2D currentVelocity)
    {
        if (CalculateDistanceToLine(currentVelocity, relativeObstaclePos(), relativeObstaclePos().add(leftSide()))
                < CalculateDistanceToLine(currentVelocity, relativeObstaclePos(), relativeObstaclePos().add(rightSide())))
        {
            return GetLinesCross(Vector2D.ZERO, currentVelocity,
                    relativeObstaclePos(), relativeObstaclePos().add(leftSide()));
        }
        else
        {
            return GetLinesCross(Vector2D.ZERO, currentVelocity,
                    relativeObstaclePos(), relativeObstaclePos().add(rightSide()));
        }
    }

    public abstract VelocityObstacleType getType();

    public enum VelocityObstacleType
    {
        STATIC,
        DYNAMIC
    }
}