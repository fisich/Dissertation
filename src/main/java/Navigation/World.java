package Navigation;

import Navigation.Map.NavigationMap;
import Navigation.Map.NavigationMapModel;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class World {
    private final NavigationMap _map;
    private final List<Agent> _agents;
    private final NavigationMapModel _mapModel;

    public World(int mapTileSize, int mapTilesX, int mapTilesY) {
        _map = new NavigationMap(mapTileSize, mapTilesX, mapTilesY);
        _mapModel = _map.getModel();
        _agents = new ArrayList<>();
    }

    public List<Agent> agents()
    {
        return _agents;
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
        return new Vector2D(x * _mapModel.getTileSize(),
                y * _mapModel.getTileSize());
    }

    public Vector2D ToMapPoint2D(Vector2D pos)
    {
        return FromWorldVec2DToMapVec2D(pos.getX(), pos.getY());
    }

    public Vector2D FromWorldVec2DToMapVec2D(double x, double y)
    {
        int posX = (int)(Math.ceil(x / _mapModel.getTileSize()) - 1);
        int posY = (int)(Math.ceil(y / _mapModel.getTileSize()) - 1);
        return new Vector2D(posX,posY);
    }

    public void SendTicks() {
        for (Agent agent: _agents) {
            agent.Tick(60);
        }
    }

    public NavigationMap getMap()
    {
        return _map;
    }

    public NavigationMapModel getMapModel()
    {
        return _mapModel;
    }

    public List<Vector2D> GetMapTilesPositionAroundAgent(Agent agent)
    {
        Vector2D mapPosition = ToMapPoint2D(agent.getPosition());
        int leftX = (int) (mapPosition.getX() - agent.radius - agent.MaxVelocity/_mapModel.getTileSize());
        leftX = Math.max(0, leftX);
        int rightX = (int) (mapPosition.getX() + agent.radius + agent.MaxVelocity/_mapModel.getTileSize());
        rightX = Math.min(_mapModel.getTilesX(), rightX);
        int topY = (int) (mapPosition.getY() - agent.radius - agent.MaxVelocity/_mapModel.getTileSize());
        topY = Math.max(0, topY);
        int bottomY = (int) (mapPosition.getY() + agent.radius + agent.MaxVelocity/_mapModel.getTileSize());
        bottomY = Math.min(_mapModel.getTilesY(), bottomY);
        List<Vector2D> mapTilesPos = new ArrayList<>();

        for (int i = leftX; i < rightX; i++)
        {
            for (int j = topY; j < bottomY; j++)
                if (_map.getTile(i,j).getPassPrice() < 0) {
                    mapTilesPos.add(new Vector2D(i,j));
                }
        }
        return mapTilesPos;
    }
}