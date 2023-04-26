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

    public GroupVelocityObstacle(Agent origin, List<Agent> others, NavigationMap navigationMap) {
        // Отсортированные против часовой стрелки VO
        _origin = origin;
        _velocityObstacles = SortAgents(origin, others).stream().map(
                b -> new GenericVelocityObstacle(_origin, b)
        ).collect(Collectors.toCollection(LinkedList::new));
        _map = navigationMap;
    }

    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        Vector2D velocityOnMap = _origin.getPosition().add(point);
        if (velocityOnMap.getX() <= _origin.radius || velocityOnMap.getX() >= (_map.sizeX - _origin.radius) ||
                velocityOnMap.getY() <= _origin.radius || velocityOnMap.getY() >= (_map.sizeY - _origin.radius))
            return true;
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
            //velocities.add(vel.scalarMultiply(0.5d));
        }
        /*for (GenericVelocityObstacle obstacle: _velocityObstacles) {
            velocities = Stream.concat(velocities.stream(),
                    obstacle.FindVelocityOutsideVelocityObstacle(currentVelocity, IVelocityObstacle.VelocityObstacleSide.LEFT).stream())
            .collect(Collectors.toList());
            //velocities = Stream.concat(velocities.stream(),
            //        obstacle.FindVelocityOutsideVelocityObstacle(currentVelocity, IVelocityObstacle.VelocityObstacleSide.RIGHT).stream())
            //        .collect(Collectors.toList());
        }*/
        // filter out of screen
        velocities = velocities.stream().filter(vel -> {
            Vector2D velocityOnMap = _origin.getPosition().add(vel);
            return (velocityOnMap.getX() > _origin.radius && velocityOnMap.getX() < (_map.sizeX - _origin.radius) &&
            velocityOnMap.getY() > _origin.radius && velocityOnMap.getY() < (_map.sizeY - _origin.radius));
        }).collect(Collectors.toList());
        // filter for static obstacles
        List<GenericVelocityObstacle> staticVelocityObstacles = _velocityObstacles.stream()
                .filter(obs -> obs.type() == BaseObstacle.VelocityObstacleType.STATIC)
                .collect(Collectors.toList());
        // filter for all
        velocities = velocities.stream().filter(vel -> {
            for (GenericVelocityObstacle obstacle: staticVelocityObstacles) {
                if (obstacle.IsCollideWithVelocityObstacle(vel))
                    return false;
            }
            return true;
            //return !IsCollideWithVelocityObstacle(vel);
        }).collect(Collectors.toList());

        /*List<Map.Entry<Vector2D, Integer>> velocityAndCollisionsCount = new ArrayList<>();
        for (Vector2D vel:velocities) {
            velocityAndCollisionsCount.add(new AbstractMap.SimpleEntry<>(vel, CollisionsCount(vel)));
        }
        int min = velocityAndCollisionsCount.stream()
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .get().getValue();
        velocities = velocityAndCollisionsCount.stream()
                .filter(entry -> entry.getValue() == min)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (min != 0)
            System.out.println("debug pause");
        //velocities = velocities.stream().filter(vel -> !IsCollideWithVelocityObstacle(vel)).collect(Collectors.toList());*/
        //Optional<Vector2D> bestVelocity = velocities.stream().min(Comparator.comparingDouble(vel -> Vector2D.distanceSq(currentVelocity, vel)));
        /*Optional<Vector2D> maxVelocity = velocities.stream().max(Comparator.comparingDouble(Vector2D::getNorm));
        Vector2D maxVelocityValue;
        if (maxVelocity.isPresent())
            maxVelocityValue = maxVelocity.get();
        else
            return Vector2D.ZERO;
        velocities = velocities.stream().filter(vel -> vel.getNorm() == maxVelocityValue.getNorm()).collect(Collectors.toList());*/
        Optional<Vector2D> bestVelocity = velocities.stream().min(Comparator.comparingDouble(vel -> Math.abs(GetAngleBetweenVectors(currentVelocity, vel))));
        if (bestVelocity.isPresent())
            return bestVelocity.get();
        else
            return Vector2D.ZERO;
            //throw new NoSuchElementException("Can't find best velocity");
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
