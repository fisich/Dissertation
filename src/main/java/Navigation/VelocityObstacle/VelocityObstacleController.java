package Navigation.VelocityObstacle;

import Navigation.Agent;
import Navigation.Map.NavigationMap;
import Navigation.Map.NavigationMapModel;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static MathExtensions.Vector2DExtension.RotateVector;

public class VelocityObstacleController {
    private List<DynamicVelocityObstacle> _dynamicObstacles;
    private List<StaticVelocityObstacle> _staticObstacles;
    private final NavigationMapModel _mapModel;
    private final Agent _agent;

    public VelocityObstacleController(Agent origin, List<Agent> others, NavigationMapModel mapModel, List<StaticVelocityObstacle> staticObstacles) {
        List<BaseObstacle> agentObstacles = others.stream().map(
                b -> VelocityObstacleBuilder.build(origin, b)
        ).collect(Collectors.toList());
        _dynamicObstacles = new ArrayList<>();
        _staticObstacles = new ArrayList<>();
        for (BaseObstacle obstacle: agentObstacles) {
            switch (obstacle.getType())
            {
                case DYNAMIC:
                    _dynamicObstacles.add((DynamicVelocityObstacle) obstacle);
                    break;
                case STATIC:
                    _staticObstacles.add((StaticVelocityObstacle) obstacle);
                    break;
                default:
                    throw new IllegalStateException("Unexpected VelocityObstacleType " + obstacle.getType());
            }
        }
        _staticObstacles = Stream.concat(_staticObstacles.stream(), staticObstacles.stream()).collect(Collectors.toList());
        _mapModel = mapModel;
        _agent = origin;
    }

    /**
     * Check if agent can move freely with current
     * @param velocity Agent's estimated velocity
     * @return true if there is no problem with map bounds, static and dynamic obstacle,
     * otherwise false
     */
    public boolean IsVelocityAvailableForAgent(Vector2D velocity) {
        if (!IsVelocityInsideMapBounds(velocity))
            return false;
        if (IsCollideWithStaticObstacles(velocity))
            return false;
        for (DynamicVelocityObstacle obstacle: _dynamicObstacles) {
            if (obstacle.IsVelocityCollideWithObstacle(velocity))
                return false;
        }
        return true;
    }

    /**
     * Check if velocity inside map bounds at agent position
     * @param velocity Agent's estimated velocity
     * @return true if velocity inside map bounds, otherwise false
     */
    private boolean IsVelocityInsideMapBounds(Vector2D velocity)
    {
        Vector2D velocityOnMap = _agent.getPosition().add(velocity);
        return (velocityOnMap.getX() > _agent.radius) && (velocityOnMap.getX() < (_mapModel.sizeX() - _agent.radius)) &&
                (velocityOnMap.getY() > _agent.radius) && (velocityOnMap.getY() < (_mapModel.sizeY() - _agent.radius));
    }

    /**
     * Check if velocity collides with some of static obstacle
     * @param velocity Agent's estimated velocity
     * @return false if velocity not collide with any of static obstacle,
     * otherwise true
     */
    private boolean IsCollideWithStaticObstacles(Vector2D velocity)
    {
        for (StaticVelocityObstacle obstacle: _staticObstacles)
        {
            if (obstacle.IsVelocityCollideWithObstacle(velocity))
                return true;
        }
        return false;
    }

    private int CollisionsCount(Vector2D velocity)
    {
        int count = 0;
        for (DynamicVelocityObstacle obstacle: _dynamicObstacles) {
            if (obstacle.IsVelocityCollideWithObstacle(velocity))
                count++;
        }
        return count;
    }

    public Vector2D FindBestVelocityOutsideObstacles(Vector2D currentVelocity)
    {
        List<Vector2D> velocities = new ArrayList<>();
        for (int i = 1; i < 24; i++)
        {
            Vector2D vel = RotateVector(currentVelocity, 0.261799 * i);
            velocities.add(vel);
            velocities.add(vel.scalarMultiply(0.5d));
            velocities.add(vel.scalarMultiply(0.25d));
        }
        // filter out of screen
        velocities = velocities.stream().filter(this::IsVelocityInsideMapBounds).collect(Collectors.toList());

        // filter for all static obstacles
        velocities = velocities.stream().filter(vel -> !IsCollideWithStaticObstacles(vel)).collect(Collectors.toList());
        if (velocities.isEmpty())
            return Vector2D.ZERO;

        // Calculate velocities for dynamics
        List<Map.Entry<Vector2D, Integer>> velocityAndCollisionsCount = new ArrayList<>();
        for (Vector2D vel:velocities) {
            velocityAndCollisionsCount.add(new AbstractMap.SimpleEntry<>(vel, CollisionsCount(vel)));
        }

        double minCollision = velocityAndCollisionsCount.stream()
            .min(Comparator.comparingInt(Map.Entry::getValue))
            .get().getValue();

        velocities = velocities.stream().filter(vel -> CollisionsCount(vel) <= minCollision).collect(Collectors.toList());

        List<Map.Entry<Vector2D, Double>> velocityAndPenaltyScore = new ArrayList<>();
        for (Vector2D vel: velocities)
        {
            double distScore = Vector2D.distance(currentVelocity, vel);
            if (minCollision == 0)
            {
                distScore -= vel.getNorm();
                velocityAndPenaltyScore.add(new AbstractMap.SimpleEntry<>(vel, distScore));
            }
            else
            {
                boolean added = false;
                for (DynamicVelocityObstacle obstacle: _dynamicObstacles)
                {
                    double timeToCollideScore = 0;
                    if (obstacle.IsVelocityCollideWithObstacle(vel))
                    {
                        Vector2D collisionPoint = obstacle.GetCrossPointWithClosestSide(vel);
                        timeToCollideScore = vel.getNorm() * 15 / collisionPoint.getNorm();
                    }
                    velocityAndPenaltyScore.add(new AbstractMap.SimpleEntry<>(vel,
                            timeToCollideScore + distScore));
                    added = true;
                }
                if (!added)
                    velocityAndPenaltyScore.add(new AbstractMap.SimpleEntry<>(vel, distScore));
            }
        }
        Optional<Vector2D> bestVelocity = velocityAndPenaltyScore.stream().min(Comparator.comparingDouble(Map.Entry::getValue)).map(Map.Entry::getKey);
        return bestVelocity.orElse(Vector2D.ZERO);
    }
}
