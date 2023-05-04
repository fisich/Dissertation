package Navigation;

import Navigation.PathFinding.AStarPathFinding;
import Navigation.PathFinding.PathProcessing;
import Navigation.VelocityObstacle.StaticVelocityObstacle;
import Navigation.VelocityObstacle.VelocityObstacleController;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.stream.Collectors;

public class Agent {
    private volatile Vector2D _position;
    private Vector2D _goalVelocity;
    private Vector2D _currentVelocity;
    public final double MaxVelocity = 30;
    public final double radius;
    public  Color color;
    private final World _worldRef;
    public double _viewRadius;
    public final boolean _draw;
    private VelocityObstacleController _VO = null;
    public Queue<Vector2D> route = new LinkedList<>();
    private Vector2D targetPoint;
    private boolean hasCompleteMovement = true;
    private long ticksToUseBestVelocity;

    private long _movementStartTime;
    public long movementTime;
    public double pathLength = 0;
    public double velocityDeviation = 0;
    public int measureNumber = 0;

    public Agent(double posX, double posY, double radius, Color color, World worldRef, boolean DRAW) {
        _currentVelocity = new Vector2D(0, 0);
        this.radius = radius;
        this.color = color;
        _position = new Vector2D(posX, posY);
        this._worldRef = worldRef;
        _goalVelocity = _currentVelocity = new Vector2D(0,0);
        _viewRadius = MaxVelocity*3;
        _draw = DRAW;
    }

    public void MoveTo(double posX, double posY)
    {
        if(hasCompleteMovement)
        {
            targetPoint = new Vector2D(posX, posY);
            GetRoute(targetPoint);
            hasCompleteMovement = false;
            ticksToUseBestVelocity = System.currentTimeMillis();
            _movementStartTime = System.currentTimeMillis();
        }
    }

    private void GetRoute(Vector2D target)
    {
        AStarPathFinding pathFinding = new AStarPathFinding(_worldRef);
        List<Vector2D> tempRoute = pathFinding.findRoute(this, target);
        tempRoute = tempRoute.subList(0, tempRoute.size() - 1).stream().map(_worldRef::ToCenterOfWorldPoint2D).collect(Collectors.toList());
        tempRoute.add(target);
        route = new LinkedList<>(PathProcessing.StraightenThePath(tempRoute));
    }

    public void Tick(int FPS) {
        if (route.isEmpty()) {
            hasCompleteMovement = true;
            _currentVelocity = _goalVelocity = Vector2D.ZERO;
            return;
        }
        Vector2D nextPoint = route.peek();
        if (!_worldRef.getMap().IsPathAtLineClear(_worldRef.ToMapPoint2D(getPosition()), _worldRef.ToMapPoint2D(nextPoint)))
        {
            GetRoute(targetPoint);
            nextPoint = route.peek();
        }
        if (route.isEmpty()){
            hasCompleteMovement = true;
            _currentVelocity = _goalVelocity = Vector2D.ZERO;
            return;
        }
        if (isPositionReached(nextPoint)) {
            route.poll();
            if(route.isEmpty()) {
                hasCompleteMovement = true;
                _currentVelocity = _goalVelocity = Vector2D.ZERO;
                movementTime = System.currentTimeMillis() - _movementStartTime;
                System.out.println("Path length: " + pathLength + " time " + movementTime);
                System.out.println("Average velocity deviation " + (velocityDeviation / (double) measureNumber));
                return;
            }
            else
                nextPoint = route.peek();
        }
        _goalVelocity = nextPoint.subtract(_position);
        if (_goalVelocity.getNorm() > MaxVelocity)
            _goalVelocity = _goalVelocity.normalize().scalarMultiply(MaxVelocity);
        if (_goalVelocity.getNorm() < MaxVelocity * 0.25d)
            _goalVelocity = _goalVelocity.normalize().scalarMultiply(MaxVelocity * 0.25d);
        List<Agent> agents = GetAgentsAround();
        _VO = new VelocityObstacleController(this, agents, _worldRef.getMapModel(), GetStaticObstacleAround());
        if(_currentVelocity.getNorm() == 0) {
            _currentVelocity = _goalVelocity;
        }
        if (_VO.IsVelocityAvailableForAgent(_goalVelocity)) {
            if (System.currentTimeMillis() - ticksToUseBestVelocity > 10) {
                _currentVelocity = _goalVelocity;
            }
        } else {
            ticksToUseBestVelocity = System.currentTimeMillis();
            if (!_VO.IsVelocityAvailableForAgent(_currentVelocity)) {
                _currentVelocity = _VO.FindBestVelocityOutsideObstacles(_goalVelocity);
            }
        }
        Vector2D posMove = _currentVelocity.scalarMultiply(1d / FPS);
        _position = _position.add(posMove);
        pathLength += posMove.getNorm();
        velocityDeviation += Vector2D.distance(_goalVelocity, _currentVelocity);
        measureNumber += 1;
    }

    private boolean isPositionReached(Vector2D point)
    {
        if (Vector2D.distance(point, targetPoint) < _worldRef.getMapModel().getTileSize())
        {
            return Vector2D.distance(point, getPosition()) < 0.1d;
        }
        return Vector2D.distance(point, getPosition()) <= 1.5 * radius;
    }

    private List<Agent> GetAgentsAround()
    {
        return _worldRef.agents().stream()
                .filter(agent -> agent != this &&
                        agent.getPosition()
                                .subtract(getPosition())
                                .getNorm() < _viewRadius)
                .collect(Collectors.toList());
    }

    private List<StaticVelocityObstacle> GetStaticObstacleAround()
    {
        List<Vector2D> mapObstaclePos = _worldRef.GetMapTilesPositionAroundAgent(this);
        List<StaticVelocityObstacle> mapObstacles = new ArrayList<>();
        for (Vector2D mapTilePos: mapObstaclePos) {
            Vector2D topLeftPos = _worldRef.ToWorldPoint2D(mapTilePos)
                    .subtract(getPosition());
            mapObstacles.add(new StaticVelocityObstacle(topLeftPos, radius));
            mapObstacles.add(new StaticVelocityObstacle(topLeftPos.add(new Vector2D(0, _worldRef.getMapModel().getTileSize())), radius));
            mapObstacles.add(new StaticVelocityObstacle(topLeftPos.add(new Vector2D(_worldRef.getMapModel().getTileSize(), 0)), radius));
            mapObstacles.add(new StaticVelocityObstacle(topLeftPos.add(new Vector2D(_worldRef.getMapModel().getTileSize(), _worldRef.getMapModel().getTileSize())), radius));
        }
        return mapObstacles;
    }

    public Vector2D getPosition()
    {
        return new Vector2D(_position.getX(), _position.getY());
    }

    public Vector2D getVelocity()
    {
        return new Vector2D(_currentVelocity.getX(), _currentVelocity.getY());
    }

    public Vector2D getGoalVelocity() {return new Vector2D(_goalVelocity.getX(), _goalVelocity.getY());}

}
