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
        crossX = Math.round(crossX * 1000000) / 1000000d;
        crossY = Math.round(crossY * 1000000) / 1000000d;
        return new Vector2D(crossX, crossY);
    }

    /// Получение уравнения прямой
    public static LineEquation GetLineEquation(Vector2D start, Vector2D end)
    {
        Vector2D diff = end.subtract(start);
        double m = diff.getY() / diff.getX();
        m = Math.round(m * 1000000) / 1000000d;
        double b;
        if (start.equals(Vector2D.ZERO) || end.equals(Vector2D.ZERO))
            b = 0;
        else
            b = start.getY() - m * start.getX();
        b = Math.round(b * 1000000) / 1000000d;
        return new LineEquation(m, b);
    }

    public static double GetAngleBetweenVectors(Vector2D v1, Vector2D v2) throws MathArithmeticException {
        double normProduct = v1.getNorm() * v2.getNorm();
        //if (normProduct == 0.0D) {
        //    throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
        //}
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
        rx = Math.round(rx * 1000000) / 1000000d;
        ry = Math.round(ry * 1000000) / 1000000d;
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
}
