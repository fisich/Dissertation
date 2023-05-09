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
        // Строим область VO
        // Длина касательной к окружности
        double tangentLength = FastMath.sqrt(distanceBetweenAgentsSq - minkowskiRadiusSq);
        double sin = minkowskiRadius / distanceBetweenAgents;
        double cos = tangentLength / distanceBetweenAgents;
        // Высота треугольника
        double triangleHeight = distanceBetweenAgents + minkowskiRadius;
        // Вектор стороны равнобедренного треугольника вдоль касательной
        // Через cos получаем длину стороны так, чтобы область суммы Минковского была вписана в треугольник VO
        Vector2D _relativeBPosition = obstacleAgent.getPosition().subtract(origin.getPosition());
        Vector2D tangentVec = _relativeBPosition.normalize().scalarMultiply(triangleHeight / cos);
        // Через поворот получаем вектор левой и правой сторон
        // Каждая из них расположена относительно агента origin
        Vector2D rightSide = rotateVector(tangentVec, -sin, cos);
        Vector2D leftSide = rotateVector(tangentVec, sin, cos);
        // Согласно алгоритмам вычисляем опорную точку области препятствий
        // VO
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
                    // Левая сторона ближе, делаем правую менее привлекательной
                    relativeObstaclePos = getLinesCrossPoint(VOrelativeObstaclePos, VOrelativeObstaclePos.add(rightSide),
                            RVOrelativeObstaclePos, RVOrelativeObstaclePos.add(leftSide));
                } else {
                    // Правая сторона дальше, делаем левую менее привлекательной
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