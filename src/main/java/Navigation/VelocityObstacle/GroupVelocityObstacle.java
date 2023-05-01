package Navigation.VelocityObstacle;

import Navigation.Agent;
import Navigation.Map.NavigationMap;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static MathExtensions.Vector2DExtension.GetAngleBetweenVectors;
import static MathExtensions.Vector2DExtension.RotateVector;

public class GroupVelocityObstacle {
    private List<GenericVelocityObstacle> _velocityObstacles;
    private NavigationMap _map;
    private Agent _origin;
    private List<StaticVelocityObstacle> _mapObstacles;

    public GroupVelocityObstacle(Agent origin, List<Agent> others, NavigationMap navigationMap, List<StaticVelocityObstacle> obstacles) {
        // Отсортированные против часовой стрелки VO
        _origin = origin;
        _velocityObstacles = SortAgents(origin, others).stream().map(
                b -> new GenericVelocityObstacle(_origin, b)
        ).collect(Collectors.toCollection(LinkedList::new));
        _map = navigationMap;
        _mapObstacles = obstacles;
    }

    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        Vector2D velocityOnMap = _origin.getPosition().add(point);
        if (velocityOnMap.getX() <= _origin.radius || velocityOnMap.getX() >= (_map.sizeX - _origin.radius) ||
                velocityOnMap.getY() <= _origin.radius || velocityOnMap.getY() >= (_map.sizeY - _origin.radius))
            return true;
        for (StaticVelocityObstacle obstacle: _mapObstacles)
        {
            if (obstacle.IsCollideWithVelocityObstacle(point))
                return true;
        }
        for (GenericVelocityObstacle obstacle: _velocityObstacles) {
            if (obstacle.IsCollideWithVelocityObstacle(point))
                return true;
        }
        return false;
    }

    private int CollisionsCount(Vector2D point)
    {
        int count = 0;
        for (GenericVelocityObstacle obstacle: _velocityObstacles) {
            if (obstacle.IsCollideWithVelocityObstacle(point))
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
        velocities = velocities.stream().filter(vel -> {
            Vector2D velocityOnMap = _origin.getPosition().add(vel);
            return (velocityOnMap.getX() > _origin.radius && velocityOnMap.getX() < (_map.sizeX - _origin.radius) &&
            velocityOnMap.getY() > _origin.radius && velocityOnMap.getY() < (_map.sizeY - _origin.radius));
        }).collect(Collectors.toList());

        // filter for all static obstacles
        List<BaseObstacle> staticVelocityObstacles = Stream.concat(_velocityObstacles.stream()
                .filter(obs -> obs.getType() == BaseObstacle.VelocityObstacleType.STATIC), _mapObstacles.stream())
                .collect(Collectors.toList());
        velocities = velocities.stream().filter(vel -> {
            for (BaseObstacle obstacle: staticVelocityObstacles) {
                if (obstacle.IsCollideWithVelocityObstacle(vel))
                    return false;
            }
            return true;
        }).collect(Collectors.toList());
        if (velocities.isEmpty())
            return Vector2D.ZERO;

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
                velocityAndPenaltyScore.add(new AbstractMap.SimpleEntry(vel, distScore));
            }
            else
            {
                boolean added = false;
                for (GenericVelocityObstacle obs: _velocityObstacles)
                {
                    double timeToCollideScore = 0;
                    if (obs.IsCollideWithVelocityObstacle(vel))
                    {
                        Vector2D collisionPoint = obs.GetCrossPointWithClosestSide(vel);
                        timeToCollideScore = vel.getNorm() * 15 / collisionPoint.getNorm();
                    }
                    velocityAndPenaltyScore.add(new AbstractMap.SimpleEntry(vel,
                            timeToCollideScore + distScore));
                    added = true;
                }
                if (!added)
                    velocityAndPenaltyScore.add(new AbstractMap.SimpleEntry(vel, distScore));
            }
        }
        Optional<Vector2D> bestVelocity = velocityAndPenaltyScore.stream().min(Comparator.comparingDouble(Map.Entry::getValue)).map(Map.Entry::getKey);
        return bestVelocity.orElse(Vector2D.ZERO);
    }

    public List<GenericVelocityObstacle> GetObstacles()
    {
        return _velocityObstacles;
    }

    private List<Agent> SortAgents(Agent origin, List<Agent> agents)
    {
        final List<Map.Entry<Agent, Double>> agentsAndRelativeAngles = new ArrayList<>();
        agents.forEach(a -> {
            agentsAndRelativeAngles.add(new AbstractMap.SimpleEntry<>(a,
                            GetAngleBetweenVectors(origin.getGoalVelocity(),
                                    a.getPosition().subtract(origin.getPosition()).normalize()
                            )
                    )
            );
        });
        agentsAndRelativeAngles.sort(Comparator.comparingDouble(Map.Entry::getValue));
        return agentsAndRelativeAngles.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
