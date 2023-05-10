package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import static MathExtensions.Vector2DExtension.*;

public class DynamicVelocityObstacle extends BaseObstacle {

    private final Vector2D relativeLeftSide, relativeRightSide;

    public DynamicVelocityObstacle(Agent origin, Agent obstacleAgent, VelocityObstacleAlgorithm algorithm) {
        minkowskiRadius = (origin.radius + obstacleAgent.radius);
        double minkowskiRadiusSq = minkowskiRadius * minkowskiRadius;
        double distanceBetweenAgentsSq = Vector2D.distanceSq(origin.getPosition(), obstacleAgent.getPosition());
        if (distanceBetweenAgentsSq < minkowskiRadiusSq) {
            throw new RuntimeException("Error, inside dynamic obstacleAgent");
        }
        double distanceBetweenAgents = FastMath.sqrt(distanceBetweenAgentsSq);
        // Calculate VO
        // Calculate length of tangent line to circle of minkowski sum
        double tangentLength = FastMath.sqrt(distanceBetweenAgentsSq - minkowskiRadiusSq);
        double sin = minkowskiRadius / distanceBetweenAgents;
        double cos = tangentLength / distanceBetweenAgents;
        double triangleHeight = distanceBetweenAgents + minkowskiRadius;
        // Get vector for triangle side towards tangent line
        Vector2D _relativeBPosition = obstacleAgent.getPosition().subtract(origin.getPosition());
        // Use cos to get length so that the area of the Minkowski sum is inscribed in the triangle VO
        Vector2D tangentVec = _relativeBPosition.normalize().scalarMultiply(triangleHeight / cos);
        // Rotate vector so we get sides of triangle
        Vector2D rightSide = rotateVector(tangentVec, -sin, cos);
        Vector2D leftSide = rotateVector(tangentVec, sin, cos);
        switch (algorithm) {
            case VELOCITY_OBSTACLE:
                relativeObstaclePos = obstacleAgent.getVelocity();
                break;
            case RECIPROCAL_VELOCITY_OBSTACLE:
                relativeObstaclePos = obstacleAgent.getVelocity().add(origin.getVelocity()).scalarMultiply(0.5d);
                break;
            case HYBRID_RECIPROCAL_VELOCITY_OBSTACLE:
                Vector2D VOrelativeObstaclePos = obstacleAgent.getVelocity();
                Vector2D RVOrelativeObstaclePos = obstacleAgent.getVelocity().add(origin.getVelocity()).scalarMultiply(0.5d);
                if (getDistanceToLine(origin.getVelocity(), RVOrelativeObstaclePos, RVOrelativeObstaclePos.add(leftSide))
                        < getDistanceToLine(origin.getVelocity(), RVOrelativeObstaclePos, RVOrelativeObstaclePos.add(rightSide))) {
                    // Left line closer, so make the right one less attractive
                    relativeObstaclePos = getLinesCrossPoint(VOrelativeObstaclePos, VOrelativeObstaclePos.add(rightSide),
                            RVOrelativeObstaclePos, RVOrelativeObstaclePos.add(leftSide));
                } else {
                    // Right line closer, so make the left one less attractive
                    relativeObstaclePos = getLinesCrossPoint(VOrelativeObstaclePos, VOrelativeObstaclePos.add(leftSide),
                            RVOrelativeObstaclePos, RVOrelativeObstaclePos.add(rightSide));
                }
                break;
        }
        relativeLeftSide = relativeObstaclePos.add(leftSide);
        relativeRightSide = relativeObstaclePos.add(rightSide);
    }

    public Vector2D getCrossPointWithClosestSide(Vector2D currentVelocity) {
        if (getDistanceToLine(currentVelocity, relativeObstaclePos, relativeLeftSide)
                < getDistanceToLine(currentVelocity, relativeObstaclePos, relativeRightSide)) {
            return getLinesCrossPoint(Vector2D.ZERO, currentVelocity, relativeObstaclePos, relativeLeftSide);
        } else {
            return getLinesCrossPoint(Vector2D.ZERO, currentVelocity, relativeObstaclePos, relativeRightSide);
        }
    }

    @Override
    public boolean isVelocityCollide(Vector2D velocity) {
        return isPointInsideTriangle(velocity, relativeObstaclePos,
                relativeLeftSide, relativeRightSide);
    }

    private static boolean isPointInsideTriangle(Vector2D point, Vector2D A, Vector2D B, Vector2D C) {
        double first = pseudoScalarProduct(point, A, B);
        double second = pseudoScalarProduct(point, C, A);
        boolean has_neg = (first < 0) || (second < 0);
        boolean has_pos = (first > 0) || (second > 0);

        return !(has_neg && has_pos);
    }

    private static double pseudoScalarProduct(Vector2D p1, Vector2D p2, Vector2D p3) {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    @Override
    public VelocityObstacleType getType() {
        return VelocityObstacleType.DYNAMIC;
    }

    public Vector2D getRelativeLeftSide() {
        return relativeLeftSide;
    }

    public Vector2D getRelativeRightSide() {
        return relativeRightSide;
    }
}