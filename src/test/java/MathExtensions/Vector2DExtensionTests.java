package MathExtensions;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import static MathExtensions.Vector2DExtension.*;
import static org.junit.Assert.*;

public class Vector2DExtensionTests {
    @Test
    public void RotateVectorByAngleTest()
    {
        Vector2D original = new Vector2D(0, 1);
        Vector2D rotated = RotateVector(original, Math.PI / 2);
        assertTrue(new Vector2D(1, 0).equals(rotated));
        rotated = RotateVector(original, Math.PI);
        assertTrue(original.negate().equals(rotated));
        rotated = RotateVector(original, -Math.PI / 2);
        assertTrue(new Vector2D(-1, 0).equals(rotated));
        original = new Vector2D(0.70710678118, 0.70710678118);
        rotated = RotateVector(original, Math.PI * 0.75d);
        assertTrue(new Vector2D(0, -1).equals(rotated));
    }

    @Test
    public void GetAngleBetweenVectorsTest()
    {
        Vector2D first = new Vector2D(0, 1);
        Vector2D second = new Vector2D(1, 0);
        assertEquals(Math.PI/2, GetAngleBetweenVectors(first, second), 0.00001);

        first = new Vector2D(0.70710678118, 0.70710678118);
        second = new Vector2D(0, -1);
        assertEquals(Math.PI * 0.75d, GetAngleBetweenVectors(first, second), 0.00001);

        first = new Vector2D(0.70710678118, 0.70710678118);
        second = new Vector2D(-1, 0);
        assertEquals(- Math.PI * 0.75d, GetAngleBetweenVectors(first, second), 0.00001);

        first = new Vector2D(-1, -1);
        second = new Vector2D(1, -1);
        assertEquals(-Math.PI/2, GetAngleBetweenVectors(first, second), 0.00001);
        assertEquals(Math.PI/2, GetAngleBetweenVectors(second, first), 0.00001);

        second = new Vector2D(1, 1);
        assertEquals(Math.PI, GetAngleBetweenVectors(first, second), 0.00001);
    }

    @Test
    public void RotateAndCheckAngleBetweenVectorsTest()
    {
        Vector2D first = new Vector2D(0.70710678118, 0.70710678118);
        Vector2D second = new Vector2D(0, -1);
        double angle = GetAngleBetweenVectors(first, second);
        Vector2D firstRotated = RotateVector(first, angle);
        assertTrue(firstRotated.equals(second));
    }

    @Test
    public void RotateVectorBySinCosTest()
    {
        double angle = Math.PI/ 2;
        Vector2D original = new Vector2D(0, 1);
        Vector2D rotated = RotateVector(original, FastMath.sin(angle), FastMath.cos(angle));
        assertTrue(new Vector2D(1, 0).equals(rotated));
        angle = Math.PI;
        rotated = RotateVector(original, FastMath.sin(angle), FastMath.cos(angle));
        assertTrue(original.negate().equals(rotated));
        angle = -Math.PI / 2;
        rotated = RotateVector(original, FastMath.sin(angle), FastMath.cos(angle));
        assertTrue(new Vector2D(-1, 0).equals(rotated));
        original = new Vector2D(0.70710678118, 0.70710678118);
        angle = Math.PI * 0.75d;
        rotated = RotateVector(original, FastMath.sin(angle), FastMath.cos(angle));
        assertTrue(new Vector2D(0, -1).equals(rotated));
    }

    @Test
    public void GetLineEquationTest()
    {
        Vector2D start = new Vector2D(2,2);
        Vector2D end = new Vector2D(4,4);
        LineEquation equation = GetLineEquation(start, end);
        assertTrue(equation.M == 1 && equation.B == 0);

        start = new Vector2D(1,1);
        end = new Vector2D(5,3);
        equation = GetLineEquation(start, end);
        assertTrue(equation.M == 0.5 && equation.B == 0.5);

        start = new Vector2D(9,1);
        end = new Vector2D(5,7);
        equation = GetLineEquation(start, end);
        assertTrue(equation.M == - 1.5 && equation.B == 14.5);

        start = new Vector2D(-4,4);
        end = new Vector2D(-1,2);
        equation = GetLineEquation(start, end);
        assertEquals(equation.M, -2 / 3.0d, 0.000001);
        assertEquals(equation.B, 4/3.0d, 0.000001);
    }

    @Test
    public void GetLinesCrossTest()
    {
        Vector2D start1 = new Vector2D(1,1);
        Vector2D end1 = new Vector2D(5,3);
        Vector2D start2 = new Vector2D(9,1);
        Vector2D end2 = new Vector2D(5,7);
        Vector2D cross = GetLinesCross(start1, end1, start2, end2);
        assertTrue(cross.equals(new Vector2D(7,4)));

        start1 = new Vector2D(-3,-5);
        end1 = new Vector2D(-2,-4);
        start2 = new Vector2D(-1,2);
        end2 = new Vector2D(-4,4);
        cross = GetLinesCross(start1, end1, start2, end2);
        assertTrue(cross.equals(new Vector2D(2,0)));
    }

    @Test
    public void IsPointOnLineTest()
    {
        assertTrue(IsPointOnLine(new Vector2D(1.5, 3), Vector2D.ZERO, new Vector2D(3, 6)));

        assertFalse(IsPointOnLine(new Vector2D(2000,3000), new Vector2D(6000, -1000), new Vector2D(-3000, 7000)));

        LineEquation equation = GetLineEquation(new Vector2D(6000, -1000), new Vector2D(-3000, 7000));
        assertTrue(IsPointOnLine(new Vector2D(2000, equation.M * 2000 + equation.B), new Vector2D(6000, -1000), new Vector2D(-3000, 7000)));
    }
}
