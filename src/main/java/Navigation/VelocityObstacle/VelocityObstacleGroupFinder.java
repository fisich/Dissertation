package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.MathArrays;

import java.util.*;
import java.util.stream.Collectors;

public class VelocityObstacleGroupFinder {

    public Vector2D leftSideObstacleVelocity, rightSideObstacleVelocity;
    public Vector2D leftSide, rightSide;
    private Agent agent;

    public VelocityObstacleGroupFinder(Agent A, List<Agent> others)
    {
        agent = A;
        List<Agent> sorted = SortAgents(others);
        List<VelocityObstacle> obstaclesGroup = new ArrayList<>();
        for (Agent b: sorted)
        {
            VelocityObstacle vo = new VelocityObstacle(A, b);

        }
    }

    private List<Agent> SortAgents(List<Agent> agents)
    {
        final List<Map.Entry<Agent, Double>> angles = new ArrayList<>();
        agents.forEach(a -> {
            angles.add(new AbstractMap.SimpleEntry<>(a,
                            angleBetweenVectors(agent.getVelocity(),
                                    a.getPosition().subtract(agent.getPosition()).normalize()
                            )
                    )
            );
        });
        angles.sort(Comparator.comparingDouble(Map.Entry::getValue));
        List<Agent> sortedAgents = angles.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        return sortedAgents;
    }

    public static double angleBetweenVectors(Vector2D v1, Vector2D v2) throws MathArithmeticException {
        double normProduct = v1.getNorm() * v2.getNorm();
        if (normProduct == 0.0D) {
            throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
        }
        double dotProduct = v1.dotProduct(v2);
        double angle = Math.acos(dotProduct / normProduct);
        // определение знака скалярного произведения
        double crossProduct = MathArrays.linearCombination(v1.getX(), v2.getY(), -v1.getY(), v2.getX());
        if (crossProduct < 0) {
            angle = -angle;
        }
        return angle;
    }
}
