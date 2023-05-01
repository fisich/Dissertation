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
        // Case 1
        Vector2D original = new Vector2D(0, 1);
        Vector2D rotated = RotateVector(original, Math.PI / 2);
        assertEquals(new Vector2D(1, 0), rotated);
        // Case 2
        rotated = RotateVector(original, Math.PI);
        assertEquals(original.negate(), rotated);
        // Case 3
        rotated = RotateVector(original, -Math.PI / 2);
        assertEquals(new Vector2D(-1, 0), rotated);
        // Case 4
        original = new Vector2D(0.70710678118, 0.70710678118);
        rotated = RotateVector(original, Math.PI * 0.75d);
        assertEquals(new Vector2D(0, -1), rotated);
    }

    @Test
    public void GetAngleBetweenVectorsTest()
    {
        // Case 1
        Vector2D first = new Vector2D(0, 1);
        Vector2D second = new Vector2D(1, 0);
        assertEquals(Math.PI/2, GetAngleBetweenVectors(first, second), 0.00001);
        // Case 2
        first = new Vector2D(0.70710678118, 0.70710678118);
        second = new Vector2D(0, -1);
        assertEquals(Math.PI * 0.75d, GetAngleBetweenVectors(first, second), 0.00001);
        // Case 3
        second = new Vector2D(-1, 0);
        assertEquals(- Math.PI * 0.75d, GetAngleBetweenVectors(first, second), 0.00001);
        // Case 4
        first = new Vector2D(-1, -1);
        second = new Vector2D(1, -1);
        assertEquals(-Math.PI/2, GetAngleBetweenVectors(first, second), 0.00001);
        // Case 5
        assertEquals(Math.PI/2, GetAngleBetweenVectors(second, first), 0.00001);
        // Case 6
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
        assertEquals(firstRotated, second);
    }

    @Test
    public void RotateVectorBySinCosTest()
    {
        // Case 1
        double angle = Math.PI/ 2;
        Vector2D original = new Vector2D(0, 1);
        Vector2D rotated = RotateVector(original, FastMath.sin(angle), FastMath.cos(angle));
        assertEquals(new Vector2D(1, 0), rotated);
        // Case 2
        angle = Math.PI;
        rotated = RotateVector(original, FastMath.sin(angle), FastMath.cos(angle));
        assertEquals(original.negate(), rotated);
        // Case 3
        angle = -Math.PI / 2;
        rotated = RotateVector(original, FastMath.sin(angle), FastMath.cos(angle));
        assertEquals(new Vector2D(-1, 0), rotated);
        // Case 4
        original = new Vector2D(0.70710678118, 0.70710678118);
        angle = Math.PI * 0.75d;
        rotated = RotateVector(original, FastMath.sin(angle), FastMath.cos(angle));
        assertEquals(new Vector2D(0, -1), rotated);
    }

    @Test
    public void GetLineEquationTest()
    {
        // Case 1
        Vector2D start = new Vector2D(2,2);
        Vector2D end = new Vector2D(4,4);
        LineEquation equation = GetLineEquation(start, end);
        assertTrue(equation.M == 1 && equation.B == 0);
        // Case 2
        start = new Vector2D(1,1);
        end = new Vector2D(5,3);
        equation = GetLineEquation(start, end);
        assertTrue(equation.M == 0.5 && equation.B == 0.5);
        // Case 3
        start = new Vector2D(9,1);
        end = new Vector2D(5,7);
        equation = GetLineEquation(start, end);
        assertTrue(equation.M == - 1.5 && equation.B == 14.5);
        // Case 4
        start = new Vector2D(-4,4);
        end = new Vector2D(-1,2);
        equation = GetLineEquation(start, end);
        assertEquals(equation.M, -2 / 3.0d, 0.0001);
        // Case 5
        assertEquals(equation.B, 4/3.0d, 0.0001);
    }

    @Test
    public void GetLinesCrossTest()
    {
        // Case 1
        Vector2D start1 = new Vector2D(1,1);
        Vector2D end1 = new Vector2D(5,3);
        Vector2D start2 = new Vector2D(9,1);
        Vector2D end2 = new Vector2D(5,7);
        Vector2D cross = GetLinesCross(start1, end1, start2, end2);
        assertEquals(cross, new Vector2D(7, 4));
        // Case 2
        start1 = new Vector2D(-3,-5);
        end1 = new Vector2D(-2,-4);
        start2 = new Vector2D(-1,2);
        end2 = new Vector2D(-4,4);
        cross = GetLinesCross(start1, end1, start2, end2);
        assertEquals(2, cross.getX(), 0.0001d);
        assertEquals(0, cross.getY(), 0.0001d);
    }

    @Test
    public void IsPointOnLineTest()
    {
        // Case 1
        assertTrue(IsPointOnLine(new Vector2D(1.5, 3), Vector2D.ZERO, new Vector2D(3, 6)));
        // Case 2
        assertFalse(IsPointOnLine(new Vector2D(2000,3000), new Vector2D(6000, -1000), new Vector2D(-3000, 7000)));
        // Case 3
        LineEquation equation = GetLineEquation(new Vector2D(6000, -1000), new Vector2D(-3000, 7000));
        assertTrue(IsPointOnLine(new Vector2D(2000, equation.M * 2000 + equation.B), new Vector2D(6000, -1000), new Vector2D(-3000, 7000)));
    }
}