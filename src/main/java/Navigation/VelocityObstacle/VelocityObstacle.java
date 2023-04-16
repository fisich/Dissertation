package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.time.LocalDateTime;

public class VelocityObstacle extends BaseObstacle{
    public final BaseObstacle obstacle;

    public VelocityObstacle(Agent A, Agent B) {
        Vector2D dynamicObstacleVelocity = B.getVelocity();
        double minkowskiRadius = (B.radius * 1.05d + A.radius);
        double minkowskiRadiusSq = minkowskiRadius * minkowskiRadius;
        double distanceBetweenAgentsSq = Vector2D.distanceSq(B.getPosition(), A.getPosition());
        // Скорость такая, что считаем статичным
        if (dynamicObstacleVelocity.getNorm() < 1)
        {
            type = VelocityObstacleType.STATIC;
            obstacle = new StaticVelocityObstacle(A, B);
        }
        // TODO: Когда внутри VO, считаем ли мы объект как Static????
        else if (distanceBetweenAgentsSq < minkowskiRadiusSq)
        {
            type = VelocityObstacleType.STATIC;
            obstacle = new StaticVelocityObstacle(A, B);
            System.out.println("Tricky collision: " + (FastMath.sqrt(minkowskiRadiusSq - distanceBetweenAgentsSq)) + "  " + LocalDateTime.now().toString());
        }
        else
        {
            type = VelocityObstacleType.DYNAMIC;
            obstacle = new DynamicVelocityObstacle(A, B);
        }
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        return obstacle.IsCollideWithVelocityObstacle(point);
    }

    @Override
    public Vector2D FindVelocityOutSideVelocityObstacle(Agent agent) {
        return obstacle.FindVelocityOutSideVelocityObstacle(agent);
    }
}