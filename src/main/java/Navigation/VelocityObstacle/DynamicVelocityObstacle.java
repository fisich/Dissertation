package Navigation.VelocityObstacle;

import MathExtensions.Vector2DExtension;
import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.util.*;

import static MathExtensions.Vector2DExtension.*;

public class DynamicVelocityObstacle extends BaseObstacle{

    private final double _minkowskiRadius;

    public DynamicVelocityObstacle(Agent A, Agent B) {
        _minkowskiRadius = (B.radius + A.radius);
        double minkowskiRadiusSq = _minkowskiRadius * _minkowskiRadius;
        double distanceBetweenAgentsSq = Vector2D.distanceSq(B.getPosition(), A.getPosition());
        double distanceBetweenAgents = FastMath.sqrt(distanceBetweenAgentsSq);
        if (distanceBetweenAgentsSq < minkowskiRadiusSq) {
            throw new RuntimeException("Tricky collision: inside dynamic");
        }
        // Рассматриваем прямоугольный треугольник, чтобы найти касательную и угол ее наклона слева и справа для
        // формирования области VO.
        double tangentLength = 0;
        if (distanceBetweenAgents > _minkowskiRadius) {
            tangentLength = FastMath.sqrt(Math.abs(distanceBetweenAgentsSq - minkowskiRadiusSq));
        }
        double sin = _minkowskiRadius / distanceBetweenAgents;
        double cos = tangentLength / distanceBetweenAgents;
        double triangleHeight = distanceBetweenAgents + _minkowskiRadius;
        // Находим сторону равнобедренного треугольника, в который вписана окружность (сумма Минковского)
        // Длину получаем через cos
        Vector2D _relativeBPosition = B.getPosition().subtract(A.getPosition());
        Vector2D tangentVec = _relativeBPosition.normalize().scalarMultiply(triangleHeight / cos);
        _rightSide = RotateVector(tangentVec, -sin, cos);
        _leftSide = RotateVector(tangentVec, sin, cos);
        // VO
        //Vector2D VOrelativeObstaclePos = B.getVelocity();
        // RVO
        Vector2D RVOrelativeObstaclePos = B.getVelocity().add(A.getVelocity()).scalarMultiply(0.5d);
        // HRVO
        /*if (CalculateDistanceToLine(A.getVelocity(), B.getVelocity(), B.getVelocity().add(leftSide()))
        < CalculateDistanceToLine(A.getVelocity(), B.getVelocity(), B.getVelocity().add(rightSide())))
        {
            // Левая сторона ближе, делаем ее менее привлекательной
            _relativeObstaclePos = GetLinesCross(VOrelativeObstaclePos, VOrelativeObstaclePos.add(leftSide()),
                    RVOrelativeObstaclePos, RVOrelativeObstaclePos.add(rightSide()));
        }
        else
        {
            // Правая сторона ближе, делаем ее менее привлекательной
            _relativeObstaclePos = GetLinesCross(VOrelativeObstaclePos, VOrelativeObstaclePos.add(rightSide()),
                    RVOrelativeObstaclePos, RVOrelativeObstaclePos.add(leftSide()));
        }*/
        _relativeObstaclePos = RVOrelativeObstaclePos;
    }

    public Vector2D GetCrossPointWithClosestSide(Vector2D currentVelocity)
    {
        if (CalculateDistanceToLine(currentVelocity, _relativeObstaclePos, _relativeObstaclePos.add(leftSide()))
                < CalculateDistanceToLine(currentVelocity, _relativeObstaclePos, _relativeObstaclePos.add(rightSide())))
        {
            return GetLinesCross(Vector2D.ZERO, currentVelocity,
                    _relativeObstaclePos, _relativeObstaclePos.add(leftSide()));
        }
        else
        {
            return GetLinesCross(Vector2D.ZERO, currentVelocity,
                    _relativeObstaclePos, _relativeObstaclePos.add(rightSide()));
        }
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        // Честный подсчет, преобразовывать точку point не надо
        return IsPointInsideTriangle(point, _relativeObstaclePos,
                _leftSide.add(_relativeObstaclePos),
                _rightSide.add(_relativeObstaclePos));
    }

    private static boolean IsPointInsideTriangle(Vector2D point, Vector2D A, Vector2D B, Vector2D C)
    {
        // https://www.cyberforum.ru/algorithms/thread144722.html
        double first = sign(point, A, B);
        double second = sign(point, C, A);
        //double third = sign(point, C, A);
        boolean has_neg = (first < 0) || (second < 0);// || (third < 0);
        boolean has_pos = (first > 0) || (second > 0); //|| (third > 0);

        return !(has_neg && has_pos);
    }

    private boolean IsPointOutsideTriangle(Vector2D point)
    {
        double angleBetweenLeftRight = GetAngleBetweenVectors(_leftSide, _rightSide);
        double angleBetweenLeftPoint = GetAngleBetweenVectors(_leftSide, point.subtract(_relativeObstaclePos));
        if (angleBetweenLeftPoint < 0)
            return true;
        return angleBetweenLeftPoint > angleBetweenLeftRight;
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