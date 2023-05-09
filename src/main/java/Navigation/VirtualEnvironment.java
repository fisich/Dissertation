package Navigation;

import Navigation.Map.NavigationMap;
import Navigation.Map.NavigationMapModel;
import Navigation.VelocityObstacle.VelocityObstacleAlgorithm;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class VirtualEnvironment {
    private final NavigationMap map;
    private final List<Agent> agents;
    private final NavigationMapModel mapModel;
    private VelocityObstacleAlgorithm algorithm;

    public VirtualEnvironment(int mapTileSize, int mapTilesX, int mapTilesY, VelocityObstacleAlgorithm algorithm) {
        map = new NavigationMap(mapTileSize, mapTilesX, mapTilesY);
        mapModel = map.getMapModel();
        agents = new ArrayList<>();
        this.algorithm = algorithm;
    }

    public void setAlgorithm(VelocityObstacleAlgorithm algorithm)
    {
        this.algorithm = algorithm;
    }

    public VelocityObstacleAlgorithm getAlgorithm() { return algorithm; }

    public List<Agent> agents()
    {
        return agents;
    }

    public Vector2D toScreenCoordinate2D(Vector2D pos)
    {
        return fromMapToScreenCoordinate2D(pos.getX(), pos.getY());
    }

    public Vector2D toCenterOfMapCoordinate2D(Vector2D pos)
    {
        return fromMapToScreenCoordinate2D(pos.getX() + 0.5, pos.getY() + 0.5);
    }

    public Vector2D fromMapToScreenCoordinate2D(double x, double y)
    {
        return new Vector2D(x * mapModel.getTileSize(),
                y * mapModel.getTileSize());
    }

    public Vector2D toMapCoordinate2D(Vector2D pos)
    {
        return fromScreenToMapCoordinate2D(pos.getX(), pos.getY());
    }

    public Vector2D fromScreenToMapCoordinate2D(double x, double y)
    {
        int posX = (int)(Math.ceil(x / mapModel.getTileSize()) - 1);
        int posY = (int)(Math.ceil(y / mapModel.getTileSize()) - 1);
        return new Vector2D(posX,posY);
    }

    public void tickAgents() {
        for (Agent agent: agents) {
            agent.tick(60);
        }
    }

    public NavigationMap getMap()
    {
        return map;
    }

    public NavigationMapModel getMapModel()
    {
        return mapModel;
    }

    public List<Vector2D> getMapTilesPositionAroundAgent(Agent agent)
    {
        Vector2D mapPosition = toMapCoordinate2D(agent.getPosition());
        double agentMapRadius = Math.ceil(agent.radius / mapModel.getTileSize());
        double agentMaxVelocityAtMap = agent.maxVelocity / mapModel.getTileSize();
        int leftX = (int) (mapPosition.getX() - agentMapRadius - agentMaxVelocityAtMap);
        leftX = Math.max(0, leftX);
        int rightX = (int) (mapPosition.getX() + agentMapRadius + agentMaxVelocityAtMap);
        rightX = Math.min(mapModel.getTilesX(), rightX);
        int topY = (int) (mapPosition.getY() - agentMapRadius - agentMaxVelocityAtMap);
        topY = Math.max(0, topY);
        int bottomY = (int) (mapPosition.getY() + agentMapRadius + agentMaxVelocityAtMap);
        bottomY = Math.min(mapModel.getTilesY(), bottomY);
        List<Vector2D> mapTilesPos = new ArrayList<>();

        for (int i = leftX; i < rightX; i++)
        {
            for (int j = topY; j < bottomY; j++)
                if (map.getTileInfo(i,j).getPassPrice() < 0) {
                    mapTilesPos.add(new Vector2D(i,j));
                }
        }
        return mapTilesPos;
    }
}