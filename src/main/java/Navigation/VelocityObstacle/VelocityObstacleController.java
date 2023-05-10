package Navigation.VelocityObstacle;

import MathExtensions.Vector2DExtension;
import Navigation.Agent;
import Navigation.Map.NavigationMapModel;
import Navigation.VirtualEnvironment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VelocityObstacleController {
    private final List<DynamicVelocityObstacle> dynamicObstacles;
    private List<BaseObstacle> staticObstacles;
    private final NavigationMapModel mapModel;
    private final Agent origin;

    public VelocityObstacleController(Agent origin, List<Agent> otherAgents, VirtualEnvironment virtualEnvironment, List<StaticVelocityObstacle> mapObstacles) {
        List<BaseObstacle> agentObstacles = sortAgents(origin, otherAgents).stream().map(
                b -> VelocityObstacleBuilder.build(origin, b, virtualEnvironment.getAlgorithm())
        ).collect(Collectors.toList());
        dynamicObstacles = new ArrayList<>();
        this.staticObstacles = new ArrayList<>();
        for (BaseObstacle obstacle : agentObstacles) {
            switch (obstacle.getType()) {
                case DYNAMIC:
                    dynamicObstacles.add((DynamicVelocityObstacle) obstacle);
                    break;
                case STATIC:
                    this.staticObstacles.add(obstacle);
                    break;
                default:
                    throw new IllegalStateException("Unexpected VelocityObstacleType " + obstacle.getType());
            }
        }
        this.staticObstacles = Stream.concat(this.staticObstacles.stream(), mapObstacles.stream()).collect(Collectors.toList());
        mapModel = virtualEnvironment.getMapModel();
        this.origin = origin;
    }

    /**
     * Check if origin can move freely with current
     *
     * @param velocity Agent's estimated velocity
     * @return true if there is no problem with map bounds, static and dynamic obstacle,
     * otherwise false
     */
    public boolean isVelocityAvailableForAgent(Vector2D velocity) {
        if (!isVelocityInsideMapBounds(velocity))
            return false;
        if (isCollideWithStaticObstacles(velocity))
            return false;
        for (DynamicVelocityObstacle obstacle : dynamicObstacles) {
            if (obstacle.isVelocityCollide(velocity))
                return false;
        }
        return true;
    }

    /**
     * Check if velocity inside map bounds at origin mapCoordinatePosition
     *
     * @param velocity Agent's estimated velocity
     * @return true if velocity inside map bounds, otherwise false
     */
    private boolean isVelocityInsideMapBounds(Vector2D velocity) {
        Vector2D velocityOnMap = origin.getPosition().add(velocity);
        return (velocityOnMap.getX() > origin.radius) && (velocityOnMap.getX() < (mapModel.sizeX() - origin.radius)) &&
                (velocityOnMap.getY() > origin.radius) && (velocityOnMap.getY() < (mapModel.sizeY() - origin.radius));
    }

    /**
     * Check if velocity collides with some of static obstacle
     *
     * @param velocity Agent's estimated velocity
     * @return false if velocity not collide with any of static obstacle,
     * otherwise true
     */
    private boolean isCollideWithStaticObstacles(Vector2D velocity) {
        for (BaseObstacle obstacle : staticObstacles) {
            if (obstacle.isVelocityCollide(velocity))
                return true;
        }
        return false;
    }

    private List<Vector2D> filterVelocitiesByCollisionCount(List<Vector2D> velocities, int min) {
        List<Vector2D> result = new ArrayList<>();
        for (Vector2D vel : velocities) {
            int tempCount = 0;
            for (DynamicVelocityObstacle obstacle : dynamicObstacles) {
                if (obstacle.isVelocityCollide(vel))
                    tempCount++;
                if (tempCount > min)
                    break;
            }
            if (tempCount <= min)
                result.add(vel);
        }
        return result;
    }

    private int getMinCollisionsCount(List<Vector2D> velocities) {
        int min = velocities.size();
        for (Vector2D vel : velocities) {
            int tempCount = 0;
            for (DynamicVelocityObstacle obstacle : dynamicObstacles) {
                if (obstacle.isVelocityCollide(vel))
                    tempCount++;
                if (tempCount >= min)
                    break;
            }
            if (tempCount < min)
                min = tempCount;
        }
        return min;
    }

    public Vector2D findBestVelocityOutsideObstacles(Agent agent) {
        List<Vector2D> velocities = new ArrayList<>();
        for (int i = 1; i < 24; i++) {
            Vector2D vel = Vector2DExtension.rotateVector(new Vector2D(0, agent.maxVelocity), 0.261799 * i);
            velocities.add(vel);
            velocities.add(vel.scalarMultiply(0.5d));
            velocities.add(vel.scalarMultiply(0.25d));
        }
        // filter out of screen
        velocities = velocities.stream().filter(this::isVelocityInsideMapBounds).collect(Collectors.toList());

        // filter for all static obstacles
        velocities = velocities.stream().filter(vel -> !isCollideWithStaticObstacles(vel)).collect(Collectors.toList());
        if (velocities.isEmpty())
            return Vector2D.ZERO;

        int minCollision = getMinCollisionsCount(velocities);

        velocities = filterVelocitiesByCollisionCount(velocities, minCollision);

        List<Map.Entry<Vector2D, Double>> velocityAndPenaltyScore = new ArrayList<>();
        for (Vector2D vel : velocities) {
            double distScore = Vector2D.distance(agent.getGoalVelocity(), vel)
                    + Vector2D.distance(agent.getVelocity(), vel);
            if (minCollision == 0) {
                distScore -= vel.getNorm();
                velocityAndPenaltyScore.add(new AbstractMap.SimpleEntry<>(vel, distScore));
            } else {
                double timeToCollideScore = 0;
                for (DynamicVelocityObstacle obstacle : dynamicObstacles) {
                    if (obstacle.isVelocityCollide(vel)) {
                        Vector2D collisionPoint = obstacle.getCrossPointWithClosestSide(vel);
                        double timeToCollideScoreTemp = vel.getNorm() * agent.maxVelocity * 0.5d / collisionPoint.getNorm();
                        timeToCollideScore = FastMath.max(timeToCollideScore, timeToCollideScoreTemp);
                    }
                }
                velocityAndPenaltyScore.add(new AbstractMap.SimpleEntry<>(vel, distScore + timeToCollideScore));
            }
        }
        Optional<Vector2D> bestVelocity = velocityAndPenaltyScore.stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey);
        return bestVelocity.orElse(Vector2D.ZERO);
    }

    private List<Agent> sortAgents(Agent origin, List<Agent> agents) {
        final List<Map.Entry<Agent, Double>> agentsAndRelativeAngles = new ArrayList<>();
        agents.forEach(a -> agentsAndRelativeAngles.add(new AbstractMap.SimpleEntry<>(a,
                        Vector2DExtension.getAngleBetweenVectors(origin.getGoalVelocity(),
                                a.getPosition().subtract(origin.getPosition()).normalize()
                        )
                )
        ));
        agentsAndRelativeAngles.sort(Comparator.comparingDouble(Map.Entry::getValue));
        return agentsAndRelativeAngles.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<DynamicVelocityObstacle> getDynamicObstacles() {
        return dynamicObstacles;
    }

    public List<BaseObstacle> getStaticObstacles() {
        return staticObstacles;
    }
}
