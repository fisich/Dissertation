package Rendering;

import Navigation.Agent;
import Navigation.VelocityObstacle.BaseObstacle;
import Navigation.VelocityObstacle.DynamicVelocityObstacle;
import Navigation.VelocityObstacle.VelocityObstacle;
import Navigation.VelocityObstacle.BaseObstacle.VelocityObstacleType;
import Navigation.World;
import Patterns.Observer.IMouseEventReceiver;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class WorldRenderer implements IMouseEventReceiver {
    private final Canvas _canvas;
    private final World _world;

    public WorldRenderer(World world, Canvas canvas) {
        this._world = world;
        this._canvas = canvas;
        _world.renderer = this;
    }

    public void Redraw()
    {
        GraphicsContext gc = _canvas.getGraphicsContext2D();
        for (int i = 0; i < _world.map.sizeX; i++)
        {
            for (int j = 0; j < _world.map.sizeY; j++)
            {
                gc.setFill(_world.map.tiles[i][j].getColor());
                Vector2D pos = _world.FromMapVec2DToWorldPoint2D(i, j);
                gc.fillRect(pos.getX(), pos.getY(), _world.map.mapTileSize, _world.map.mapTileSize);
            }
        }
        for (Agent agent: _world.agents) {
            if (agent.getPosition().getX() > _canvas.getWidth() || agent.getPosition().getY() > _canvas.getHeight())
                System.out.println("out of screen");
            if (agent.getPosition().getX() < 0 || agent.getPosition().getY() < 0)
                System.out.println("out of screen");
            if (agent.getPosition().isNaN())
                System.out.println("agent pos is nan");
            if (!agent._draw) {
                //gc.setFill(Color.YELLOW);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeOval(agent.getPosition().getX() - agent.radius * 2, agent.getPosition().getY() - agent.radius * 2, agent.radius * 4, agent.radius * 4);
            }
            gc.setFill(agent.color);
            gc.fillOval(agent.getPosition().getX() - agent.radius, agent.getPosition().getY() - agent.radius, agent.radius * 2, agent.radius * 2);
            DrawLine(agent.getPosition(), agent.getPosition().add(agent.getGoalVelocity()), Color.BLUE, 8);
            DrawLine(agent.getPosition(), agent.getPosition().add(agent.getVelocity()), Color.RED, 4);
            VelocityObstacle agentVO = agent.GetVelocityObstacle();
            if (agentVO != null && agent._draw) {
                if (agentVO.type == VelocityObstacleType.DYNAMIC) {
                    try {
                        DynamicVelocityObstacle dVO = (DynamicVelocityObstacle) agentVO.obstacle;
                        DrawLine(agent.getPosition().add(dVO.dynamicObstacleVelocity),
                                dVO.leftSide.add(dVO.dynamicObstacleVelocity).add(agent.getPosition()));
                        DrawLine(agent.getPosition().add(dVO.dynamicObstacleVelocity),
                                dVO.rightSide.add(dVO.dynamicObstacleVelocity).add(agent.getPosition()));
                        DrawLine(agent.getPosition().add(dVO.dynamicObstacleVelocity.add(dVO.leftSide)),
                                dVO.rightSide.add(dVO.dynamicObstacleVelocity).add(agent.getPosition()));
                    }
                    catch (ClassCastException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            if (agent._draw)
            {
                //DrawLine(agent.getPosition(), agent.getPosition().add(agent.straightVelocity), Color.YELLOW, 3);
                //DrawLine(agent.getPosition(), agent.getPosition().add(agent.maxSpeedVelocity), Color.LIME, 3);
            }
        }
    }

    public void DrawLine(Vector2D start, Vector2D end)
    {
        DrawLine(start, end, Color.BLACK, 1);
    }

    public void DrawLine(Vector2D start, Vector2D end, Color color, int width)
    {
        GraphicsContext gc = _canvas.getGraphicsContext2D();
        gc.setStroke(color);
        gc.setLineWidth(width);
        gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }

    @Override
    public void Update(double x, double y) {
        //MapVec2D mapTile = _world.FromWorldVec2DToMapVec2D(x, y);
        //_world.map.UpdateTile(mapTile.x, mapTile.y, Color.RED, 0);
    }
}
