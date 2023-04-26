package Navigation.VelocityObstacle;

import Navigation.Agent;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.time.LocalDateTime;
import java.util.List;

public class GenericVelocityObstacle extends BaseObstacle{
    private BaseObstacle obstacle;

    public GenericVelocityObstacle(Agent A, Agent B) {
        Vector2D dynamicObstacleVelocity = B.getVelocity();
        double minkowskiRadius = (B.radius + A.radius);
        double minkowskiRadiusSq = minkowskiRadius * minkowskiRadius;
        double distanceBetweenAgentsSq = Vector2D.distanceSq(B.getPosition(), A.getPosition());
        // Скорость такая, что считаем статичным
        if (dynamicObstacleVelocity.getNorm() < 0.01)
        {
            obstacle = new StaticVelocityObstacle(A, B);
        }
        // TODO: Когда внутри VO, считаем ли мы объект как Static????
        else if (distanceBetweenAgentsSq < minkowskiRadiusSq)
        {
            obstacle = new StaticVelocityObstacle(A, B);
            System.out.println("Tricky collision: " + (FastMath.sqrt(minkowskiRadiusSq - distanceBetweenAgentsSq)) + "  " + LocalDateTime.now().toString());
        }
        else
        {
            obstacle = new DynamicVelocityObstacle(A, B);
        }
        this._type = obstacle.type();
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        return obstacle.IsCollideWithVelocityObstacle(point);
    }

    /*@Override
    public List<Vector2D> FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity, VelocityObstacleSide side)
    {
        List<Vector2D> velocities = obstacle.FindVelocityOutsideVelocityObstacle(currentVelocity, side);
        if (velocities == null)
            System.out.println("Velocities is null");
        if (velocities.stream().anyMatch(v -> v.getNorm() > 51))
            System.out.println("Some of the new velocities is very high" + obstacle.type());
        return velocities;
    }*/

    @Override
    public Vector2D leftSide()
    {
        return obstacle.leftSide();
    }

    @Override
    public Vector2D rightSide()
    {
        return obstacle.rightSide();
    }

    @Override
    public Vector2D relativeObstaclePos()
    {
        return obstacle.relativeObstaclePos();
    }
}