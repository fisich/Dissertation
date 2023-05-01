package Navigation;

import Navigation.Map.NavigationMap;
import Application.Rendering.WorldRenderer;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class World {
    public final NavigationMap map;
    public List<Agent> agents;
    public WorldRenderer renderer;

    public World(int mapTileSize, int mapTilesX, int mapTilesY) {
        map = new NavigationMap(mapTileSize, mapTilesX, mapTilesY);
        agents = new ArrayList<>();
    }

    public Vector2D ToWorldPoint2D(Vector2D pos)
    {
        return FromMapVec2DToWorldPoint2D(pos.getX(), pos.getY());
    }

    public Vector2D ToCenterOfWorldPoint2D(Vector2D pos)
    {
        return FromMapVec2DToWorldPoint2D(pos.getX() + 0.5, pos.getY() + 0.5);
    }

    public Vector2D FromMapVec2DToWorldPoint2D(double x, double y)
    {
        return new Vector2D(x * map.mapTileSize, y * map.mapTileSize);
    }

    public Vector2D ToMapPoint2D(Vector2D pos)
    {
        return FromWorldVec2DToMapVec2D(pos.getX(), pos.getY());
    }

    public Vector2D FromWorldVec2DToMapVec2D(double x, double y)
    {
        int posX = (int)(Math.ceil(x / map.mapTileSize) - 1);
        int posY = (int)(Math.ceil(y / map.mapTileSize) - 1);
        return new Vector2D(posX,posY);
    }

    public void SendTicks() {
        for (Agent agent: agents) {
            agent.Tick(60);
        }
    }
}