package Navigation.VelocityObstacle;

import Navigation.Agent;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.time.LocalDateTime;

public class GenericVelocityObstacle extends BaseObstacle{
    private BaseObstacle obstacle;
    private Color agentColor;

    public GenericVelocityObstacle(Agent A, Agent B) {
        agentColor = A.color;
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

    @Override
    public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity) {
        if(currentVelocity.isNaN())
            System.out.println("Generic VO current velocity is nan");
        Vector2D velocity = obstacle.FindVelocityOutsideVelocityObstacle(currentVelocity);
        if (velocity.isNaN())
            System.out.println("New velocity is NaN");
        if (velocity.getNorm() > 51)
            System.out.println("New velocity is high " + velocity.getNorm() + " " + obstacle.type());
        return velocity;
    }

    @Override
    public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity, VelocityObstacleSide side)
    {
        return obstacle.FindVelocityOutsideVelocityObstacle(currentVelocity, side);
    }

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