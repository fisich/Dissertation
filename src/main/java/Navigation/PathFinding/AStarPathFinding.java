package Navigation.PathFinding;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
// import Navigation.Map.MapVec2D;
import Navigation.World;

import java.util.*;

public class AStarPathFinding {
    private final World world;
    private Map<PathNode, PathNode> visitedNodes; // to, from
    private int _agentMapRadius;
    private Vector2D _mapDestination;

    public AStarPathFinding(World worldRef) {
        this.world = worldRef;
    }

    public List<Vector2D> findRoute(Agent a, Vector2D destination)
    {
        PathNode startNode = new PathNode(0, world.ToMapPoint2D(a.getPosition()));
        _mapDestination = world.ToMapPoint2D(destination);
        if (startNode.position.equals(_mapDestination))
        {
            List<Vector2D> route = new ArrayList<>();
            route.add(_mapDestination);
            return new ArrayList<Vector2D>();
        }
        _agentMapRadius = (int) a.radius / world.map.mapTileSize;
        Queue<PathNode> queue = new PriorityQueue<>(priceComparator);
        visitedNodes = new HashMap<>();
        visitedNodes.put(startNode, null);
        PathNode destinationNode = null;
        queue.add(startNode);
        while (queue.size() > 0)
        {
            PathNode current = queue.poll();
            if(current.position.equals(_mapDestination)) {
                destinationNode = current;
                break;
            }
            for (PathNode neighbor: GetAvailableNeighborNodes(current)) {
                if (MarkAndCheckPriceForVisitedNode(current, neighbor))
                {
                    queue.add(neighbor);
                }
            }
        }
        if (destinationNode == null)
            return null;
        List<Vector2D> pathNodes = new ArrayList<>();
        pathNodes.add(destinationNode.position);
        PathNode temp = visitedNodes.get(destinationNode);
        while (temp.price > 0)
        {
            pathNodes.add(temp.position);
            temp = visitedNodes.get(temp);
        }
        Collections.reverse(pathNodes);
        return pathNodes;
    }

    private boolean MarkAndCheckPriceForVisitedNode(PathNode nodeFrom, PathNode nodeTo)
    {
        boolean isNewPathNodeProfitable = false;
        if (visitedNodes.containsKey(nodeTo))
        {
            if (visitedNodes.get(nodeTo).price > nodeFrom.price)
            {
                visitedNodes.put(nodeTo, nodeFrom);
                isNewPathNodeProfitable = true;
            }
        }
        else
        {
            visitedNodes.put(nodeTo, nodeFrom);
            isNewPathNodeProfitable = true;
        }
        return isNewPathNodeProfitable;
    }

    private List<PathNode> GetAvailableNeighborNodes(PathNode source)
    {
        List<PathNode> neighborNodes = new ArrayList<>();
        int availableMinX = (source.position.getX() - _agentMapRadius) >= 0 ? -1 : 0;
        int availableMaxX = (source.position.getX() + _agentMapRadius) < world.map.sizeX ? 1 : 0;
        int availableMinY = (source.position.getY() - _agentMapRadius) >= 0 ? -1 : 0;
        int availableMaxY = (source.position.getY() + _agentMapRadius) < world.map.sizeY ? 1 : 0;
        for (int i = availableMinX; i <= availableMaxX; i++ )
        {
            for (int j = availableMinY; j <= availableMaxY; j++)
            {
                if (i != 0 || j != 0) {
                    double price = 1;
                    if (i != 0 && j != 0)
                        price = Math.sqrt(2);
                    Vector2D neighborPos = source.position.add(new Vector2D(i, j));
                    if (!IsAgentCollideAtPosition(neighborPos))
                        neighborNodes.add(new PathNode(source.price + price
                                + GetHeuristicLength(neighborPos, _mapDestination) * 2, neighborPos));
                }
            }
        }
        return neighborNodes;
    }

    private double GetHeuristicLength(Vector2D a, Vector2D b)
    {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private boolean IsAgentCollideAtPosition(Vector2D positionToCheck)
    {
        int aroundArea = _agentMapRadius - 1;
        for (int i = -aroundArea; i <= aroundArea; i++)
        {
            double horizontalMapTilePosition = positionToCheck.getX() + i;
            if (horizontalMapTilePosition < 0 || horizontalMapTilePosition > world.map.sizeX)
                return true;
            if (world.map.tiles[(int)horizontalMapTilePosition][(int)positionToCheck.getY() + aroundArea].getPassPrice() < 0 ||
                    world.map.tiles[(int)horizontalMapTilePosition][(int)positionToCheck.getY() - aroundArea].getPassPrice() < 0)
                return true;
        }
        for (int j = -aroundArea + 1; j < aroundArea; j++)
        {
            double verticalMapTilePosition = positionToCheck.getY() + j;
            if (verticalMapTilePosition < 0 || verticalMapTilePosition > world.map.sizeY)
                return true;
            if (world.map.tiles[(int)positionToCheck.getX() + aroundArea][(int)verticalMapTilePosition].getPassPrice() < 0 ||
                    world.map.tiles[(int)positionToCheck.getX() - aroundArea][(int)verticalMapTilePosition].getPassPrice() < 0)
                return true;
        }
        return false;
    }

    private final static Comparator<PathNode> priceComparator = (o1, o2) -> (int) (o1.price - o2.price);

    private static class PathNode
    {
        public double price;
        public Vector2D position;

        public PathNode(double price, Vector2D position) {
            this.price = price;
            this.position = position;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PathNode that = (PathNode) o;
            return this.price == that.price && this.position.equals(that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(price, position);
        }
    }
}
