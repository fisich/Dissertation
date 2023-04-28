package MathExtensions;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;

public class Vector2DExtension {
    public static Vector2D GetLinesCross(Vector2D aStart, Vector2D aEnd, Vector2D bStart, Vector2D bEnd)
    {
        // Вычисления адаптированы под факт, что ось Y направлена вниз, X вправо
        LineEquation aLineEquation = GetLineEquation(aStart, aEnd);
        LineEquation bLineEquation = GetLineEquation(bStart, bEnd);
        double crossX = (bLineEquation.B - aLineEquation.B) / (aLineEquation.M - bLineEquation.M);
        double crossY = aLineEquation.M * crossX + aLineEquation.B;
        crossX = Math.round(crossX * 100000) / 100000d;
        crossY = Math.round(crossY * 100000) / 100000d;
        return new Vector2D(crossX, crossY);
    }

    /// Получение уравнения прямой
    public static LineEquation GetLineEquation(Vector2D start, Vector2D end)
    {
        Vector2D diff = end.subtract(start);
        double m = diff.getY() / diff.getX();
        m = Math.round(m * 100000) / 100000d;
        double b;
        if (start.equals(Vector2D.ZERO) || end.equals(Vector2D.ZERO))
            b = 0;
        else
            b = start.getY() - m * start.getX();
        b = Math.round(b * 100000) / 100000d;
        return new LineEquation(m, b);
    }

    public static double CalculateDistanceToLine(Vector2D point, Vector2D start, Vector2D end)
    {
        LineEquation equation = GetLineEquation(start, end);
        // y = -1/m*x - уравнение перпендикуляра
        // Точка пересечения перпендикуляра с прямой
        // -x/m=mx+b -> x = -mb/(m^2+1) 100% OK
        double mb = equation.M * equation.B;
        double m_m_plus1 = equation.M * equation.M + 1;
        double crossNormalX = -mb / m_m_plus1;
        double crossNormalY = equation.M * crossNormalX + equation.B;
        return Vector2D.distance(point, new Vector2D(crossNormalX, crossNormalY));
    }

    public static double GetAngleBetweenVectors(Vector2D v1, Vector2D v2) throws MathArithmeticException {
        double normProduct = v1.getNorm() * v2.getNorm();
        if (normProduct == 0.0D) {
            return Double.POSITIVE_INFINITY;
            //throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
        }
        double dotProduct = v1.dotProduct(v2);
        if (Math.abs(normProduct - dotProduct) < 0.00001d )
            return 0;
        double angle = Math.acos(dotProduct / normProduct);
        // определение знака скалярного произведения
        double crossProduct = MathArrays.linearCombination(v1.getX(), v2.getY(), -v1.getY(), v2.getX());
        if (crossProduct > 0) {
            angle = -angle;
        }
        return angle;
    }

    public static Vector2D RotateVector(Vector2D vec, double angle)
    {
        // Вычисления адаптированы под факт, что ось Y направлена вниз, X вправо
        return RotateVector(vec, FastMath.sin(angle), FastMath.cos(angle));
    }

    public static Vector2D RotateVector(Vector2D vec, double sin, double cos)
    {
        double rx = (vec.getX() * cos) - (-vec.getY() * sin);
        double ry = (-vec.getX() * sin) + (vec.getY() * cos);
        rx = Math.round(rx * 100000) / 100000d;
        ry = Math.round(ry * 100000) / 100000d;
        return new Vector2D(rx, ry);
    }

    public static boolean IsPointOnLine(Vector2D p, Vector2D a, Vector2D b) {
        double distance_a_b = Vector2D.distance(a, b);
        double distance_p_a = Vector2D.distance(p, a);
        double distance_p_b = Vector2D.distance(p, b);
        return Math.abs(distance_p_b + distance_p_a - distance_a_b) < 0.00001;
    }

    public static class LineEquation
    {
        public final double M;
        public final double B;

        public LineEquation(double m, double b) {
            M = m;
            B = b;
        }
    }

    public static boolean IsVecCrossCircle(Vector2D start, Vector2D end, Vector2D circleCenter, double radius)
    {
        if (Vector2D.distanceSq(end.subtract(start), circleCenter) <= radius * radius)
            return true;
        Vector2D diff = end.subtract(start);
        double a = diff.getX() * diff.getX() + diff.getY() * diff.getY();
        double b = 2 * (diff.getX() * (start.getX() - circleCenter.getX()) + diff.getY() * (start.getY() - circleCenter.getY()));
        double c = (start.getX() - circleCenter.getX()) * (start.getX() - circleCenter.getX())
                + (start.getY() - circleCenter.getY()) * (start.getY() - circleCenter.getY()) - radius * radius;
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return false;
        } else {
            double t1 = (-b + FastMath.sqrt(discriminant))/(2d*a);
            double t2 = (-b - FastMath.sqrt(discriminant))/(2d*a);
            if ((t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1)) {
                return true;
            } else {
                return false;
            }
        }
    }
}