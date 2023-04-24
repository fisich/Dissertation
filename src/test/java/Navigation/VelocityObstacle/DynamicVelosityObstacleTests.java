package Navigation.VelocityObstacle;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.junit.Test;

import static MathExtensions.Vector2DExtension.GetLinesCross;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;

public class DynamicVelosityObstacleTests {
    @Test
    public void CheckMerge()
    {
        MockVelocityObstacle right = new MockVelocityObstacle(new Vector2D(0, -150),
                new Vector2D(100, -100),
                new Vector2D(0,-50));
        MockVelocityObstacle left = new MockVelocityObstacle(new Vector2D(-65, -140),
                new Vector2D(65, -140),
                new Vector2D(15,0));
        DynamicVelocityObstacle obs = new DynamicVelocityObstacle(right, left);
        Vector2D cross = GetLinesCross(right.relativeObstaclePos(), right.rightSide().add(right.relativeObstaclePos()),
            left.relativeObstaclePos(), left.leftSide().add(left.relativeObstaclePos()));
        assertTrue(obs.relativeObstaclePos().equals(new Vector2D(-5.609757,-44.390243)));
        assertTrue(obs.leftSide().add(obs.relativeObstaclePos()).equals(left.leftSide().add(left.relativeObstaclePos())));
        assertTrue(obs.rightSide().add(obs.relativeObstaclePos()).equals(right.rightSide().add(right.relativeObstaclePos())));
        assertSame(obs.dynamicType, DynamicVelocityObstacle.DynamicObstacleType.NARROW);
    }

    public class MockVelocityObstacle extends BaseObstacle
    {
        public MockVelocityObstacle(Vector2D leftSide, Vector2D rightSide, Vector2D relativePoint)
        {
            _leftSide = leftSide;
            _rightSide = rightSide;
            _relativeObstaclePos = relativePoint;
        }
        @Override
        public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity, VelocityObstacleSide side) {
            return null;
        }

        @Override
        public boolean IsCollideWithVelocityObstacle(Vector2D point) {
            return false;
        }

        @Override
        public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity) {
            return null;
        }
    }
}
