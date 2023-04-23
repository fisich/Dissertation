package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static MathExtensions.Vector2DExtension.GetAngleBetweenVectors;
import static MathExtensions.Vector2DExtension.GetLinesCross;

public class VelocityObstacleGroupFinder implements IVelocityObstacle {

    private final List<GroupVelocityObstacle> _velocityObstacleGroups = new ArrayList<>();

    public List<BaseObstacle> GetObstacles()
    {
        List<BaseObstacle> result = new ArrayList<>();
        for (GroupVelocityObstacle group:
             _velocityObstacleGroups) {
            result = Stream.concat(result.stream(), group._orderedObstacles.stream()).collect(Collectors.toList());
        }
        return result;
    }

    public VelocityObstacleGroupFinder(Agent origin, List<Agent> others) {
        // Отсортированные против часовой стрелки VO
        Queue<GenericVelocityObstacle> orderedObstacles = SortAgents(origin, others).stream().map(
                b -> new GenericVelocityObstacle(origin, b)
        ).collect(Collectors.toCollection(LinkedList::new));
        // VO, которые будут принадлежать одной группе
        List<GenericVelocityObstacle> groupObstacles = new ArrayList<>();
        // Предыдущая VO
        GenericVelocityObstacle previous = orderedObstacles.poll();
        groupObstacles.add(previous);
        while (!orderedObstacles.isEmpty())
        {
            GenericVelocityObstacle current = orderedObstacles.poll();
            if (previous == null)
                System.out.println("Previous VO is null!");
            // Если текущая VO не сталкивается с прошлой, то заканчиваем построение группы, начинаем новую
            if (!IsVelocityObstaclesCollides(previous, current, origin.MaxVelocity))
            {
                _velocityObstacleGroups.add(new GroupVelocityObstacle(groupObstacles));
                groupObstacles.clear();
                groupObstacles.add(current);
            }
            else
            {
                groupObstacles.add(current);
            }
            previous = current;
        }
        if (!groupObstacles.isEmpty())
        {
            _velocityObstacleGroups.add(new GroupVelocityObstacle(groupObstacles));
        }
        // На этот момент мы имеем отдельные группы агентов
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        for (GroupVelocityObstacle obstacle: _velocityObstacleGroups) {
            if (obstacle.IsCollideWithVelocityObstacle(point))
                return true;
        }
        return false;
    }

    @Override
    public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity) {
        Optional<GroupVelocityObstacle> obstacle = _velocityObstacleGroups.stream()
                .filter(gr -> gr.IsCollideWithVelocityObstacle(currentVelocity))
                .findFirst();
        if (obstacle.isPresent())
            return obstacle.get().FindVelocityOutsideVelocityObstacle(currentVelocity);
        else
            return currentVelocity;
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

    private static boolean IsVelocityObstaclesCollides(BaseObstacle VORight, BaseObstacle VOLeft, double agentMaxVelocity)
    {
        Vector2D rightVOinnerSide = VORight.leftSide().add(VORight.relativeObstaclePos());
        Vector2D leftVOinnerSide = VOLeft.rightSide().add(VOLeft.relativeObstaclePos());
        double angleBetweenRightLeft = GetAngleBetweenVectors(VORight.rightSide(), VORight.leftSide());
        double angleBetweenRightRight = GetAngleBetweenVectors(VORight.rightSide(),
                leftVOinnerSide.subtract(VORight.relativeObstaclePos())
        );
        if (angleBetweenRightRight < angleBetweenRightLeft)
        {
            return true;
        }
        else
        {
            Vector2D innerLinesCross = GetLinesCross(VOLeft.relativeObstaclePos(), leftVOinnerSide,
                    VORight.relativeObstaclePos(), rightVOinnerSide);
            return innerLinesCross.getNorm() > agentMaxVelocity;
        }
    }
}
