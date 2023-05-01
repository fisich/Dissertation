package Navigation.PathFinding;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import Navigation.World;

import java.util.*;

public class AStarPathFinding {
    private final World world;
    private Map<PathNode, PathNode> visitedNodes; // to, from
    private double _agentMapRadius;
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
            return new ArrayList<>();
        }
        _agentMapRadius = a.radius / (double) world.map.mapTileSize;
        Queue<PathNode> queue = new PriorityQueue<>(priceComparator);
        visitedNodes = new HashMap<>();
        //visitedNodes.put(startNode, null);
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
        int leftX = (source.position.getX() - _agentMapRadius - 1) >= 0 ? -1 : 0;
        int rightX = (source.position.getX() + _agentMapRadius + 1) <= world.map.tilesX ? 1 : 0;
        int topY = (source.position.getY() - _agentMapRadius - 1) >= 0 ? -1 : 0;
        int bottomY = (source.position.getY() + _agentMapRadius + 1) <= world.map.tilesY ? 1 : 0;
        for (int i = leftX; i <= rightX; i++ )
        {
            for (int j = topY; j <= bottomY; j++)
            {
                if (i != 0 || j != 0)  {
                    double price = 1;
                    if (i != 0 && j != 0)
                        price = Math.sqrt(2);
                    Vector2D neighborPos = source.position.add(new Vector2D(i, j));
                    if (!IsAgentCollideAtPosition(neighborPos)) {
                        PathNode neighborNode = new PathNode(source.price + price
                                + GetHeuristicLength(neighborPos, _mapDestination) * 2, neighborPos);
                        //if (!visitedNodes.containsKey(neighborNode))
                            neighborNodes.add(neighborNode);
                    }
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
        for (int i = (int) (positionToCheck.getX() - Math.ceil(_agentMapRadius)); i < (int) (positionToCheck.getX() + Math.ceil(_agentMapRadius)); i++)
        {
            if (i < 0 || i > world.map.tilesX)
                return true;
            for (int j = (int)(positionToCheck.getY() - Math.ceil(_agentMapRadius)); j < (int) (positionToCheck.getY() + Math.ceil(_agentMapRadius)); j++)
            {
                if (j < 0 || j > world.map.tilesY)
                    return true;
                if (Vector2D.distance(positionToCheck, new Vector2D(i, j)) <= Math.ceil(_agentMapRadius))
                {
                    if (world.map.tiles[i][j].getPassPrice() < 0)
                        return true;
                }
            }
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
            if (o == null || getClass() != o.getClass()) return false;
            PathNode that = (PathNode) o;
            return this.position.equals(that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(position);
        }
    }
}
