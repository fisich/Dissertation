package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;
import static MathExtensions.Vector2DExtension.*;

public class DynamicVelocityObstacle extends BaseObstacle{

    private final Vector2D _leftSide, _rightSide;
    private final Vector2D _relativeLeftSide, _relativeRightSide;

    public DynamicVelocityObstacle(Agent A, Agent B) {
        minkowskiRadius = (A.radius + B.radius);
        double minkowskiRadiusSq = minkowskiRadius * minkowskiRadius;
        double distanceBetweenAgentsSq = Vector2D.distanceSq(A.getPosition(), B.getPosition());
        if (distanceBetweenAgentsSq < minkowskiRadiusSq) {
            throw new RuntimeException("Error, inside dynamic obstacle");
        }
        double distanceBetweenAgents = FastMath.sqrt(distanceBetweenAgentsSq);
        // Строим область VO
        // Длина касательной к окружности
        double tangentLength = FastMath.sqrt(Math.abs(distanceBetweenAgentsSq - minkowskiRadiusSq));
        double sin = minkowskiRadius / distanceBetweenAgents;
        double cos = tangentLength / distanceBetweenAgents;
        // Высота треугольника
        double triangleHeight = distanceBetweenAgents + minkowskiRadius;
        // Вектор стороны равнобедренного треугольника вдоль касательной
        // Через cos получаем длину стороны так, чтобы область суммы Минковского была вписана в треугольник VO
        Vector2D _relativeBPosition = B.getPosition().subtract(A.getPosition());
        Vector2D tangentVec = _relativeBPosition.normalize().scalarMultiply(triangleHeight / cos);
        // Через поворот получаем вектор левой и правой сторон
        // Каждая из них расположена относительно агента A
        _rightSide = RotateVector(tangentVec, -sin, cos);
        _leftSide = RotateVector(tangentVec, sin, cos);
        // Согласно алгоритмам вычисляем опорную точку области препятствий
        // VO
        Vector2D VOrelativeObstaclePos = B.getVelocity();
        // RVO
        Vector2D RVOrelativeObstaclePos = B.getVelocity().add(A.getVelocity()).scalarMultiply(0.5d);
        // HRVO
        if (CalculateDistanceToLine(A.getVelocity(), B.getVelocity(), B.getVelocity().add(_leftSide))
        < CalculateDistanceToLine(A.getVelocity(), B.getVelocity(), B.getVelocity().add(_rightSide)))
        {
            // Левая сторона ближе, делаем ее менее привлекательной
            _relativeObstaclePos = GetLinesCross(VOrelativeObstaclePos, VOrelativeObstaclePos.add(_leftSide),
                    RVOrelativeObstaclePos, RVOrelativeObstaclePos.add(_rightSide));
        }
        else
        {
            // Правая сторона ближе, делаем ее менее привлекательной
            _relativeObstaclePos = GetLinesCross(VOrelativeObstaclePos, VOrelativeObstaclePos.add(_rightSide),
                    RVOrelativeObstaclePos, RVOrelativeObstaclePos.add(_leftSide));
        }
        _relativeObstaclePos = RVOrelativeObstaclePos;
        // Получаем положение сторон для VO относительно скорости препятствия
        _relativeLeftSide = getRelativePos().add(_leftSide);
        _relativeRightSide = getRelativePos().add(_rightSide);
    }

    public Vector2D GetCrossPointWithClosestSide(Vector2D currentVelocity)
    {
        if (CalculateDistanceToLine(currentVelocity, _relativeObstaclePos, _relativeLeftSide)
                < CalculateDistanceToLine(currentVelocity, _relativeObstaclePos,_relativeRightSide))
        {
            return GetLinesCross(Vector2D.ZERO, currentVelocity,
                    _relativeObstaclePos, _relativeLeftSide);
        }
        else
        {
            return GetLinesCross(Vector2D.ZERO, currentVelocity,
                    _relativeObstaclePos, _relativeRightSide);
        }
    }

    @Override
    public boolean IsVelocityCollideWithObstacle(Vector2D velocity) {
        return IsPointInsideTriangle(velocity, _relativeObstaclePos,
                _relativeLeftSide, _relativeRightSide);
    }

    private static boolean IsPointInsideTriangle(Vector2D point, Vector2D A, Vector2D B, Vector2D C)
    {
        // https://www.cyberforum.ru/algorithms/thread144722.html
        double first = sign(point, A, B);
        double second = sign(point, C, A);
        boolean has_neg = (first < 0) || (second < 0);
        boolean has_pos = (first > 0) || (second > 0);

        return !(has_neg && has_pos);
    }

    private static double sign(Vector2D p1, Vector2D p2, Vector2D p3)
    {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    @Override
    public VelocityObstacleType getType()
    {
        return VelocityObstacleType.DYNAMIC;
    }
}