package Navigation.PathFinding;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import Navigation.VirtualEnvironment;

import java.util.*;

public class AStarPathFinding {
    private final VirtualEnvironment mVirtualEnvironment;
    private Map<PathNode, PathNode> visitedNodes; // to, from
    private double mapCoordinateAgentRadius;
    private Vector2D mapCoordinateDestination;

    public AStarPathFinding(VirtualEnvironment mVirtualEnvironment) {
        this.mVirtualEnvironment = mVirtualEnvironment;
    }

    public List<Vector2D> findRoute(Agent agent, Vector2D destination) {
        PathNode startNode = new PathNode(0, mVirtualEnvironment.toMapCoordinate2D(agent.getPosition()));
        mapCoordinateDestination = mVirtualEnvironment.toMapCoordinate2D(destination);
        if (startNode.mapCoordinatePosition.equals(mapCoordinateDestination)) {
            List<Vector2D> route = new ArrayList<>();
            route.add(mapCoordinateDestination);
            return route;
        }
        mapCoordinateAgentRadius = agent.radius / (double) mVirtualEnvironment.getMapModel().getTileSize();
        Queue<PathNode> queue = new PriorityQueue<>(priceComparator);
        visitedNodes = new HashMap<>();
        PathNode destinationNode = null;
        queue.add(startNode);
        while (queue.size() > 0) {
            PathNode current = queue.poll();
            if (current.mapCoordinatePosition.equals(mapCoordinateDestination)) {
                destinationNode = current;
                break;
            }
            for (PathNode neighbor : getAvailableNeighborNodes(current)) {
                if (markAndCheckPriceForVisitedNode(current, neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        if (destinationNode == null)
            return null;
        List<Vector2D> pathNodes = new ArrayList<>();
        pathNodes.add(destinationNode.mapCoordinatePosition);
        PathNode temp = visitedNodes.get(destinationNode);
        while (temp.price > 0) {
            pathNodes.add(temp.mapCoordinatePosition);
            temp = visitedNodes.get(temp);
        }
        Collections.reverse(pathNodes);
        return pathNodes;
    }

    private boolean markAndCheckPriceForVisitedNode(PathNode nodeFrom, PathNode nodeTo) {
        boolean isNewPathNodeProfitable = false;
        if (visitedNodes.containsKey(nodeTo)) {
            if (visitedNodes.get(nodeTo).price > nodeFrom.price) {
                visitedNodes.put(nodeTo, nodeFrom);
                isNewPathNodeProfitable = true;
            }
        } else {
            visitedNodes.put(nodeTo, nodeFrom);
            isNewPathNodeProfitable = true;
        }
        return isNewPathNodeProfitable;
    }

    private List<PathNode> getAvailableNeighborNodes(PathNode source) {
        List<PathNode> neighborNodes = new ArrayList<>();
        int leftX = (source.mapCoordinatePosition.getX() - mapCoordinateAgentRadius - 1) >= 0 ? -1 : 0;
        int rightX = (source.mapCoordinatePosition.getX() + mapCoordinateAgentRadius + 1) <= mVirtualEnvironment.getMapModel().getTilesX() ? 1 : 0;
        int topY = (source.mapCoordinatePosition.getY() - mapCoordinateAgentRadius - 1) >= 0 ? -1 : 0;
        int bottomY = (source.mapCoordinatePosition.getY() + mapCoordinateAgentRadius + 1) <= mVirtualEnvironment.getMapModel().getTilesY() ? 1 : 0;
        for (int i = leftX; i <= rightX; i++) {
            for (int j = topY; j <= bottomY; j++) {
                if (i != 0 || j != 0) {
                    double price = 1;
                    if (i != 0 && j != 0)
                        price = Math.sqrt(2);
                    Vector2D neighborPos = source.mapCoordinatePosition.add(new Vector2D(i, j));
                    if (!isAgentCollideAtPosition(neighborPos)) {
                        PathNode neighborNode = new PathNode(source.price + price
                                + getHeuristicLength(neighborPos, mapCoordinateDestination) * 2, neighborPos);
                        if (!visitedNodes.containsKey(neighborNode))
                            neighborNodes.add(neighborNode);
                    }
                }
            }
        }
        return neighborNodes;
    }

    private double getHeuristicLength(Vector2D a, Vector2D b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private boolean isAgentCollideAtPosition(Vector2D positionToCheck) {
        for (int i = (int) (positionToCheck.getX() - Math.ceil(mapCoordinateAgentRadius)); i < (int) (positionToCheck.getX() + Math.ceil(mapCoordinateAgentRadius)); i++) {
            if (i < 0 || i > mVirtualEnvironment.getMapModel().getTilesX())
                return true;
            for (int j = (int) (positionToCheck.getY() - Math.ceil(mapCoordinateAgentRadius)); j < (int) (positionToCheck.getY() + Math.ceil(mapCoordinateAgentRadius)); j++) {
                if (j < 0 || j > mVirtualEnvironment.getMapModel().getTilesY())
                    return true;
                if (Vector2D.distance(positionToCheck, new Vector2D(i, j)) <= Math.ceil(mapCoordinateAgentRadius)) {
                    if (mVirtualEnvironment.getMap().getTileInfo(i, j).getPassPrice() < 0)
                        return true;
                }
            }
        }
        return false;
    }

    private final static Comparator<PathNode> priceComparator = (o1, o2) -> (int) (o1.price - o2.price);

    private static class PathNode {
        public double price;
        public Vector2D mapCoordinatePosition;

        public PathNode(double price, Vector2D mapCoordinatePosition) {
            this.price = price;
            this.mapCoordinatePosition = mapCoordinatePosition;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PathNode that = (PathNode) o;
            return this.mapCoordinatePosition.equals(that.mapCoordinatePosition);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mapCoordinatePosition);
        }
    }
}
