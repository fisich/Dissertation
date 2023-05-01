package Navigation.VelocityObstacle;

import Navigation.Agent;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.time.LocalDateTime;

public class GenericVelocityObstacle extends BaseObstacle {
    private BaseObstacle obstacle;

    public GenericVelocityObstacle(Agent A, Agent B) {
        Vector2D dynamicObstacleVelocity = B.getVelocity();
        double minkowskiRadius = (B.radius + A.radius);
        double minkowskiRadiusSq = minkowskiRadius * minkowskiRadius;
        double distanceBetweenAgentsSq = Vector2D.distanceSq(B.getPosition(), A.getPosition());
        // Скорость такая, что считаем статичным
        if (dynamicObstacleVelocity.getNorm() < 0.01 || A.getPosition().subtract(B.getPosition()).getNorm() <= 2.1d * Math.max(A.radius, B.radius)) {
            obstacle = new StaticVelocityObstacle(A, B);
        }
        // TODO: Когда внутри VO, считаем ли мы объект как Static????
        else if (distanceBetweenAgentsSq < minkowskiRadiusSq) {
            obstacle = new StaticVelocityObstacle(A, B);
            System.out.println("Tricky collision: " + (FastMath.sqrt(minkowskiRadiusSq - distanceBetweenAgentsSq)) + "  " + LocalDateTime.now().toString());
        } else {
            obstacle = new DynamicVelocityObstacle(A, B);
        }
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        return obstacle.IsCollideWithVelocityObstacle(point);
    }

    public Vector2D GetCrossPointWithClosestSide(Vector2D currentVelocity) {
        return obstacle.GetCrossPointWithClosestSide(currentVelocity);
    }

    @Override
    public Vector2D leftSide() {
        return obstacle.leftSide();
    }

    @Override
    public Vector2D rightSide() {
        return obstacle.rightSide();
    }

    @Override
    public Vector2D relativeObstaclePos() {
        return obstacle.relativeObstaclePos();
    }

    @Override
    public VelocityObstacleType getType() {
        return obstacle.getType();
    }
}