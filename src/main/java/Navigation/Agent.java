package Navigation;

import Navigation.PathFinding.AStarPathFinding;
import Navigation.PathFinding.PathProcessing;
import Navigation.VelocityObstacle.BaseObstacle;
import Navigation.VelocityObstacle.DynamicVelocityObstacle;
import Navigation.VelocityObstacle.StaticVelocityObstacle;
import Navigation.VelocityObstacle.VelocityObstacleController;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Agent {
    private Vector2D position;
    private Vector2D goalVelocity;
    private Vector2D currentVelocity;
    public final double maxVelocity = 40;
    public final double radius;
    public final Color color;
    private final VirtualEnvironment virtualEnvironment;
    private final double viewRadius;
    private VelocityObstacleController VO;
    private Queue<Vector2D> route = new LinkedList<>();
    private Vector2D targetPoint;
    public boolean hasCompleteMovement = true;

    private boolean calculateInfo = true;
    private long movementStartTime;
    public long movementTime;
    public double pathLength = 0;
    public double velocityDeviation = 0;
    public int measureNumber = 0;

    public Agent(double posX, double posY, double radius, Color color, VirtualEnvironment virtualEnvironment) {
        currentVelocity = new Vector2D(0, 0);
        this.radius = radius;
        this.color = color;
        position = new Vector2D(posX, posY);
        this.virtualEnvironment = virtualEnvironment;
        goalVelocity = currentVelocity = new Vector2D(0, 0);
        viewRadius = maxVelocity * 3;
        targetPoint = position;
    }

    public void moveTo(double posX, double posY) {
        if (hasCompleteMovement) {
            targetPoint = new Vector2D(posX, posY);
            if (getRoute(targetPoint)) {
                hasCompleteMovement = false;
                movementStartTime = System.currentTimeMillis();
                calculateInfo = true;
            }
            else
            {
                targetPoint = position;
            }
        }
    }

    private boolean getRoute(Vector2D target) {
        AStarPathFinding pathFinding = new AStarPathFinding(virtualEnvironment);
        List<Vector2D> tempRoute = pathFinding.findRoute(this, target);
        if (tempRoute == null)
            return false;
        tempRoute = tempRoute.subList(0, tempRoute.size() - 1).stream().map(virtualEnvironment::toCenterOfMapCoordinate2D).collect(Collectors.toList());
        tempRoute.add(target);
        route = new LinkedList<>(PathProcessing.StraightenThePath(tempRoute));
        return true;
    }

    public void tick(int FPS) {
        Vector2D nextPoint;
        // if route is empty agent should keep his position
        if (route.isEmpty()) {
            VO = null;
            nextPoint = targetPoint;
            hasCompleteMovement = true;
            calculateInfo = false;
        }
        else {
            hasCompleteMovement = false;
            nextPoint = route.peek();
        }

        // check if agent can reach some of the next points during obstacle avoidance
        if (route.size() > 1) {
            List<Vector2D> tempRoute = new ArrayList<>(route);
            List<Vector2D> finalTempRoute = tempRoute;
            int availableRouteNodeIndex = IntStream.range(1, route.size()).filter(i -> virtualEnvironment.getMap()
                    .isPathBetweenPointsClear(virtualEnvironment.toMapCoordinate2D(getPosition()),
                            virtualEnvironment.toMapCoordinate2D(finalTempRoute.get(i)))).reduce((a, b) -> b).orElse(-1);
            // if found reachable point, then move to it and skip other part of route
            if (availableRouteNodeIndex > 0) {
                nextPoint = tempRoute.get(availableRouteNodeIndex);
                tempRoute = tempRoute.subList(availableRouteNodeIndex, tempRoute.size());
                route = new LinkedList<>(tempRoute);
            }
        }
        // if agent has lost line of sight of the point, then recalculate the route
        if (!virtualEnvironment.getMap().isPathBetweenPointsClear(virtualEnvironment.toMapCoordinate2D(getPosition()), virtualEnvironment.toMapCoordinate2D(nextPoint))) {
            getRoute(targetPoint);
            nextPoint = route.peek();
        }
        goalVelocity = nextPoint.subtract(position);
        //if (goalVelocity.getNorm() > maxVelocity)
            goalVelocity = goalVelocity.normalize().scalarMultiply(maxVelocity);
        //if (goalVelocity.getNorm() < maxVelocity * 0.25)
        //    goalVelocity = goalVelocity.normalize().scalarMultiply(maxVelocity * 0.25d);
        List<Agent> agents = getAgentsAround();
        VO = new VelocityObstacleController(this, agents, virtualEnvironment, getStaticObstaclesAround());
        if (currentVelocity.getNorm() == 0) {
            currentVelocity = goalVelocity;
        }
        if (VO.isVelocityAvailableForAgent(goalVelocity)) {
            currentVelocity = goalVelocity;
        } else {
            currentVelocity = VO.findBestVelocityOutsideObstacles(this);
        }
        Vector2D posMove = currentVelocity.scalarMultiply(1d / FPS);
        position = position.add(posMove);
        pathLength += posMove.getNorm();
        velocityDeviation += Vector2D.distance(goalVelocity, currentVelocity);
        measureNumber += 1;
        if (isPositionReached(nextPoint)) {
            route.poll();
            if (route.isEmpty()) {
                if (calculateInfo) {
                    hasCompleteMovement = true;
                    calculateInfo = false;
                    movementTime = System.currentTimeMillis() - movementStartTime;
                    System.out.println("Path length: " + pathLength);
                    System.out.println("Movement time " + movementTime);
                    System.out.println("Average velocity deviation " + (velocityDeviation / (double) measureNumber));
                }
            }
        }
    }

    private boolean isPositionReached(Vector2D point) {
        if (Vector2D.distance(point, targetPoint) < virtualEnvironment.getMapModel().getTileSize()) {
            return Vector2D.distance(point, getPosition()) < 0.1d;
        }
        return Vector2D.distance(point, getPosition()) <= 1.5 * radius;
    }

    private List<Agent> getAgentsAround() {
        double searchRadius;
        if (hasCompleteMovement)
            searchRadius = radius * 3;
        else
            searchRadius = viewRadius;
        return virtualEnvironment.agents().stream()
                .filter(agent -> agent != this &&
                        Vector2D.distance(agent.getPosition(), this.getPosition()) <= searchRadius)
                .collect(Collectors.toList());
    }

    private List<StaticVelocityObstacle> getStaticObstaclesAround() {
        List<StaticVelocityObstacle> mapObstacles = new ArrayList<>();
        virtualEnvironment.getMapTilesPositionAroundAgent(this).forEach(vel -> {
            Vector2D topLeftPos = virtualEnvironment.toScreenCoordinate2D(vel)
                    .subtract(getPosition());
            double offset = virtualEnvironment.getMapModel().getTileSize();
            mapObstacles.add(new StaticVelocityObstacle(topLeftPos, radius));
            mapObstacles.add(new StaticVelocityObstacle(topLeftPos.add(new Vector2D(0, offset)), radius));
            mapObstacles.add(new StaticVelocityObstacle(topLeftPos.add(new Vector2D(offset, 0)), radius));
            mapObstacles.add(new StaticVelocityObstacle(topLeftPos.add(new Vector2D(offset, offset)), radius));
        });
        return mapObstacles;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return currentVelocity;
    }

    public Vector2D getGoalVelocity() {
        return goalVelocity;
    }

    public List<DynamicVelocityObstacle> getDynamicObstacles() {
        if (VO != null)
            return VO.getDynamicObstacles();
        else
            return new ArrayList<>();
    }

    public List<BaseObstacle> getStaticObstacles() {
        if (VO != null)
            return VO.getStaticObstacles();
        else
            return new ArrayList<>();
    }
}
