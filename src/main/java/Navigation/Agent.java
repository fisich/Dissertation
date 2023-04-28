package Navigation;

import Navigation.Map.NavigationMapTileInfo;
import Navigation.PathFinding.AStarPathFinding;
import Navigation.PathFinding.PathProcessing;
import Navigation.VelocityObstacle.BaseObstacle;
import Navigation.VelocityObstacle.GroupVelocityObstacle;
import Navigation.VelocityObstacle.StaticVelocityObstacle;
import javafx.scene.paint.Color;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.MathArrays;

import java.util.*;
import java.util.stream.Collectors;

public class Agent {
    private volatile Vector2D _position;
    private Vector2D _goalVelocity;
    private Vector2D _currentVelocity;
    public double MaxVelocity = 50;
    public final double radius;
    public  Color color;
    private final World _worldRef;
    public double _viewRadius;
    public final boolean _draw;
    private GroupVelocityObstacle _VO = null;
    private Queue<Vector2D> route = new LinkedList<>();
    private boolean hasCompleteMovement = true;
    private long ticksToUseBestVelocity;
    private long _movementStartTime;
    public long movementTime;
    public List<StaticVelocityObstacle> mapObstacles;
    public double pathLength = 0;


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
            AStarPathFinding pathFinding = new AStarPathFinding(_worldRef);
            List<Vector2D> tempRoute = pathFinding.findRoute(this, new Vector2D(posX, posY));
            tempRoute = tempRoute.stream().map(_worldRef::ToCenterOfWorldPoint2D).collect(Collectors.toList());
            route = new LinkedList<>(PathProcessing.StraightenThePath(tempRoute));
            hasCompleteMovement = false;
            ticksToUseBestVelocity = System.currentTimeMillis();
            _movementStartTime = System.currentTimeMillis();
        }
    }

    public void Tick(int FPS) {
        if (route.isEmpty()) {
            hasCompleteMovement = true;
            _currentVelocity = _goalVelocity = Vector2D.ZERO;
            return;
        }
        Vector2D nextPoint = route.peek();
        if (isPositionReached(nextPoint)) {
            route.poll();
            if(route.isEmpty()) {
                hasCompleteMovement = true;
                _currentVelocity = _goalVelocity = Vector2D.ZERO;
                movementTime = System.currentTimeMillis() - _movementStartTime;
                return;
            }
            else
                nextPoint = route.peek();
        }
        _goalVelocity = nextPoint.subtract(_position).normalize().scalarMultiply(MaxVelocity);
        List<Agent> agents = GetAgentsAround();
        _VO = new GroupVelocityObstacle(this, agents, _worldRef.map, GetStaticObstacleAround());
        if(_currentVelocity.getNorm() == 0)
            _currentVelocity = _goalVelocity;
        if (!_VO.IsCollideWithVelocityObstacle(_goalVelocity)) {
            if (System.currentTimeMillis() - ticksToUseBestVelocity > 10) {
                _currentVelocity = _goalVelocity;
            }
        } else {
            ticksToUseBestVelocity = System.currentTimeMillis();
            if (_VO.IsCollideWithVelocityObstacle(_currentVelocity)) {
                _currentVelocity = _VO.FindBestVelocityOutsideObstacles(_currentVelocity);
            }
        }
        Vector2D posMove = _currentVelocity.scalarMultiply(1d / FPS);
        _position = _position.add(posMove);
        pathLength += posMove.getNorm();
    }

    private boolean isPositionReached(Vector2D point)
    {
        return point.subtract(_position).getNorm() <= radius / 2;
    }

    private List<Agent> GetAgentsAround()
    {
        return _worldRef.agents.stream()
                .filter(agent -> agent != this &&
                        agent.getPosition()
                                .subtract(getPosition())
                                .getNorm() < _viewRadius)
                .collect(Collectors.toList());
    }

    private List<StaticVelocityObstacle> GetStaticObstacleAround()
    {
        Vector2D mapPosition = _worldRef.ToMapPoint2D(getPosition());
        int leftX = (int) (mapPosition.getX() - radius - MaxVelocity/_worldRef.map.mapTileSize);
        leftX = Math.max(0, leftX);
        int rightX = (int) (mapPosition.getX() + radius + MaxVelocity/_worldRef.map.mapTileSize);
        rightX = Math.min(_worldRef.map.tilesX, rightX);
        int topY = (int) (mapPosition.getY() - radius - MaxVelocity/_worldRef.map.mapTileSize);
        topY = Math.max(0, topY);
        int bottomY = (int) (mapPosition.getY() + radius + MaxVelocity/_worldRef.map.mapTileSize);
        bottomY = Math.min(_worldRef.map.tilesY, bottomY);
        mapObstacles = new ArrayList<>();

        for (int i = leftX; i < rightX; i++)
        {
            for (int j = topY; j < bottomY; j++)
                if (_worldRef.map.tiles[i][j].getPassPrice() < 0) {
                    Vector2D topLeftPos = _worldRef.FromMapVec2DToWorldPoint2D(i, j)
                            .subtract(getPosition());
                    mapObstacles.add(new StaticVelocityObstacle(topLeftPos, radius));
                    mapObstacles.add(new StaticVelocityObstacle(topLeftPos.add(new Vector2D(0, _worldRef.map.mapTileSize)), radius));
                    mapObstacles.add(new StaticVelocityObstacle(topLeftPos.add(new Vector2D(_worldRef.map.mapTileSize, 0)), radius));
                    mapObstacles.add(new StaticVelocityObstacle(topLeftPos.add(new Vector2D(_worldRef.map.mapTileSize, _worldRef.map.mapTileSize)), radius));
                }
        }
        return mapObstacles;
    }

    public static double angleBetweenVectors(Vector2D v1, Vector2D v2) throws MathArithmeticException{
        double normProduct = v1.getNorm() * v2.getNorm();
        if (normProduct == 0.0D) {
            throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
        }
        double dotProduct = v1.dotProduct(v2);
        double angle = Math.acos(dotProduct / normProduct);
        // определение знака скалярного произведения
        double crossProduct = MathArrays.linearCombination(v1.getX(), v2.getY(), -v1.getY(), v2.getX());
        if (crossProduct < 0) {
            angle = -angle;
        }
        return angle;
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

    public GroupVelocityObstacle GetVelocityObstacle() { return _VO; }
}
