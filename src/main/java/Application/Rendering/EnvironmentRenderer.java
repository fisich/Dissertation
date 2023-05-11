package Application.Rendering;

import Navigation.Agent;
import Navigation.VelocityObstacle.BaseObstacle;
import Navigation.VelocityObstacle.DynamicVelocityObstacle;
import Navigation.VirtualEnvironment;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

public class EnvironmentRenderer{
    private final Canvas mCanvas;
    private final VirtualEnvironment mVirtualEnvironment;
    private final GraphicsContext mGraphicContext;
    public boolean drawVelocities = false, drawStaticObstacles = false, drawDynamicObstacles = false;

    public EnvironmentRenderer(VirtualEnvironment virtualEnvironment, Canvas canvas) {
        this.mVirtualEnvironment = virtualEnvironment;
        this.mCanvas = canvas;
        mGraphicContext = mCanvas.getGraphicsContext2D();
    }

    public void render()
    {
        for (int i = 0; i < mVirtualEnvironment.getMapModel().getTilesX(); i++)
        {
            for (int j = 0; j < mVirtualEnvironment.getMapModel().getTilesY(); j++)
            {
                mGraphicContext.setFill(mVirtualEnvironment.getMap().getTileInfo(i,j).getColor());
                Vector2D pos = mVirtualEnvironment.fromMapToScreenCoordinate2D(i, j);
                mGraphicContext.fillRect(pos.getX(), pos.getY(), mVirtualEnvironment.getMapModel().getTileSize(), mVirtualEnvironment.getMapModel().getTileSize());
            }
        }
        for (Agent agent: mVirtualEnvironment.agents()) {
            if (agent.getPosition().getX() > mCanvas.getWidth() || agent.getPosition().getY() > mCanvas.getHeight())
                throw new IllegalStateException("Agent out of screen");
            if (agent.getPosition().getX() < 0 || agent.getPosition().getY() < 0)
                throw new IllegalStateException("Agent out of screen");
            mGraphicContext.setFill(agent.color);
            mGraphicContext.fillOval(agent.getPosition().getX() - agent.radius, agent.getPosition().getY() - agent.radius, agent.radius * 2, agent.radius * 2);
            if (drawStaticObstacles) {
                List<BaseObstacle> staticVelocityObstacles = agent.getStaticObstacles();
                for (BaseObstacle obstacle: staticVelocityObstacles) {
                    mGraphicContext.setStroke(Color.BLACK);
                    mGraphicContext.setLineWidth(1);
                    Vector2D obstaclePos = agent.getPosition().add(obstacle.getRelativeObstaclePos());
                    mGraphicContext.strokeOval(obstaclePos.getX() - obstacle.getMinkowskiRadius(),
                            obstaclePos.getY() - obstacle.getMinkowskiRadius(),
                            obstacle.getMinkowskiRadius() * 2,
                            obstacle.getMinkowskiRadius() * 2);
                }
            }
            if (drawDynamicObstacles)
            {
                List<DynamicVelocityObstacle> dynamicVelocityObstacles = agent.getDynamicObstacles();
                for (DynamicVelocityObstacle obstacle: dynamicVelocityObstacles)
                {
                    Vector2D obstaclePos = agent.getPosition().add(obstacle.getRelativeObstaclePos());
                    drawLine(obstaclePos, agent.getPosition().add(obstacle.getRelativeLeftSide()));
                    drawLine(obstaclePos, agent.getPosition().add(obstacle.getRelativeRightSide()));
                }
            }
            if (drawVelocities) {
                drawLine(agent.getPosition(), agent.getPosition().add(agent.getGoalVelocity()), Color.BLUE, 8);
                drawLine(agent.getPosition(), agent.getPosition().add(agent.getVelocity()), Color.RED, 4);
            }
        }
    }

    private void drawLine(Vector2D start, Vector2D end)
    {
        drawLine(start, end, Color.BLACK, 1);
    }

    private void drawLine(Vector2D start, Vector2D end, Color color, int width)
    {
        mGraphicContext.setStroke(color);
        mGraphicContext.setLineWidth(width);
        mGraphicContext.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }
}