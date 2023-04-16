package Navigation;

import Navigation.PathFinding.AStarPathFinding;
import Navigation.PathFinding.PathProcessing;
import Navigation.VelocityObstacle.VelocityObstacle;
import javafx.scene.paint.Color;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.MathArrays;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Agent {
    private volatile Vector2D _position;
    private Vector2D _goalVelocity;
    private Vector2D _currentVelocity;
    public double MaxVelocity = 50;
    public final double radius;
    public  Color color;
    private World _worldRef;
    public double _viewRadius;
    public final boolean _draw;
    private VelocityObstacle _VO = null;
    private Queue<Vector2D> route = new LinkedList<>();
    private boolean hasCompleteMovement = true;

    public Vector2D straightVelocity = new Vector2D(0,0);
    public Vector2D maxSpeedVelocity = new Vector2D(0,0);

    public Agent(double posX, double posY, double radius, Color color, World worldRef, boolean DRAW) {
        _currentVelocity = new Vector2D(0, 0);
        this.radius = radius;
        this.color = color;
        _position = new Vector2D(posX, posY);
        this._worldRef = worldRef;
        _goalVelocity = new Vector2D(0,0);
        _viewRadius = radius*4;
        _draw = DRAW;
    }

    public void MoveTo(double posX, double posY)
    {
        if(hasCompleteMovement)
        {
            AStarPathFinding pathFinding = new AStarPathFinding(_worldRef);
            List<Vector2D> tempRoute = pathFinding.findRoute(this, new Vector2D(posX, posY));
            tempRoute = tempRoute.stream().map(pos -> _worldRef.ToCenterOfWorldPoint2D(pos)).collect(Collectors.toList());
            route = new LinkedList<>(PathProcessing.StraightenThePath(tempRoute));
            hasCompleteMovement = false;
        }
    }

    public void Tick(int FPS) throws Exception {
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
                return;
            }
            else
                nextPoint = route.peek();
        }
        _goalVelocity = nextPoint.subtract(_position).normalize().scalarMultiply(MaxVelocity);
        List<Agent> agents = GetAgentsAround();
        if (agents.size() > 0) {
            if (color == Color.BROWN)
            {
                System.out.println(agents.get(0).color == Color.AZURE);
            }
            _VO = new VelocityObstacle(this, agents.get(0));
            if (!_VO.IsCollideWithVelocityObstacle(_goalVelocity.subtract(agents.get(0).getVelocity()))){
                _currentVelocity = _goalVelocity;
            }
            else
            {
                if (_VO.IsCollideWithVelocityObstacle(_currentVelocity.subtract(agents.get(0).getVelocity())))
                    _currentVelocity = _VO.FindVelocityOutSideVelocityObstacle(this).add(_currentVelocity).scalarMultiply(0.5d);
            }
        }
        else
        {
            _currentVelocity = _goalVelocity;
        }
        _position = _position.add(_currentVelocity.scalarMultiply(1d / FPS));
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
                                .getNorm() < _viewRadius + agent.radius)
                .collect(Collectors.toList());
    }

    private List<Agent> SortAgents(List<Agent> agents)
    {
        final List<Map.Entry<Agent, Double>> angles = new ArrayList<>();
        agents.forEach(a -> {
            angles.add(new AbstractMap.SimpleEntry<>(a,
                    angleBetweenVectors(_currentVelocity,
                            a.getPosition().subtract(getPosition()).normalize()
                    )
                )
            );
        });
        angles.sort(Comparator.comparingDouble(Map.Entry::getValue));
        List<Agent> sortedAgents = angles.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        return sortedAgents;
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

    public VelocityObstacle GetVelocityObstacle() { return _VO; }
}
