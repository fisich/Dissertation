package Navigation.VelocityObstacle;

import Navigation.Agent;

public class VelocityObstacleBuilder {

    public static BaseObstacle build(Agent A, Agent B, VelocityObstacleAlgorithm algorithm) {
        // Скорость такая, что считаем статичным
        if (B.getVelocity().getNorm() < 0.01 || A.getPosition().subtract(B.getPosition()).getNorm() <= 2.1d * Math.max(A.radius, B.radius)) {
            return new StaticVelocityObstacle(A, B);
        } else {
            return new DynamicVelocityObstacle(A, B, algorithm);
        }
    }
}