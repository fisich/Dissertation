package Navigation.VelocityObstacle;

import Navigation.Agent;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupVelocityObstacle implements IVelocityObstacle {

    List<BaseObstacle> _orderedObstacles = new ArrayList<>();

    public GroupVelocityObstacle(List<GenericVelocityObstacle> orderedObstacles) {
        List<GenericVelocityObstacle> dynamicVOs = new ArrayList<>();
        for (GenericVelocityObstacle obstacle : orderedObstacles) {
            if (obstacle.type() == BaseObstacle.VelocityObstacleType.DYNAMIC)
            {
                dynamicVOs.add(obstacle);
            }
            else
            {
                if (!dynamicVOs.isEmpty()) {
                    if (dynamicVOs.size() == 1)
                        _orderedObstacles.add(dynamicVOs.get(0));
                    else
                        _orderedObstacles.add(new DynamicVelocityObstacle(dynamicVOs.get(0),
                                dynamicVOs.get(dynamicVOs.size() - 1)));
                    dynamicVOs.clear();
                }
                _orderedObstacles.add(obstacle);
            }
        }
        if (!dynamicVOs.isEmpty())
            if (dynamicVOs.size() == 1)
                _orderedObstacles.add(dynamicVOs.get(0));
            else
                _orderedObstacles.add(new DynamicVelocityObstacle(dynamicVOs.get(0),
                        dynamicVOs.get(dynamicVOs.size() - 1)));
        // На этом этапе мы склеили все динамические препятствия в одно, оставили между ними статические, если есть
    }

    @Override
    public boolean IsCollideWithVelocityObstacle(Vector2D point) {
        for (BaseObstacle obstacle: _orderedObstacles) {
            if (obstacle.IsCollideWithVelocityObstacle(point))
                return true;
        }
        return false;
    }

    @Override
    public Vector2D FindVelocityOutsideVelocityObstacle(Vector2D currentVelocity) {
        // TODO: СНАЧАЛА ОБЯЗАТЕЛЬНО БЕРЕМ СКОРОСТЬ ОТ-НО САМОГО ПРАВОГО, ЕГО ПРАВОЙ СТОРОНЫ
        //  2. ЗАТЕМ против часовой стрелки проверяем с кем есть коллизия и для него рассчитываем оптимальную скорость
        //  3. ПЕРЕЗАПУСКАЕМ ЦИКЛ И ДЛЯ СКОРОСТИ из п.2 ПРОВЕРЯЕМ КОЛЛИЗИЮ
        //  3а если коллизия с тем, для кого считали в прошлый раз - ПОДУМАТЬ, резолвим скорость между ними?
        //  3б если коллизия не с прошлым препятствием, все как в шаге 2.
        //  4. когда цикл закончится у нас будет скорость слева либо 3а, либо по 3б
        //  5. В конце посмотреть какая скорость ближе к идеальной и взять ее, profit.
        if (_orderedObstacles.size() == 1) {
            if (_orderedObstacles.get(0).IsCollideWithVelocityObstacle(currentVelocity))
            {
                Vector2D result = _orderedObstacles.get(0).FindVelocityOutsideVelocityObstacle(currentVelocity);
                if (IsCollideWithVelocityObstacle(result))
                    System.out.println("single VO error");
                return result;
            }
            else {
                System.out.println("returned current vel");
                return currentVelocity;
            }
        }
        int[] prevObstacle = new int[_orderedObstacles.size()];
        int prevObstacleIndex = 0;
        Arrays.fill(prevObstacle, -1);
        boolean isFindNewBestVelocity = true;
        Vector2D bestVelocity = _orderedObstacles.get(0).FindVelocityOutsideVelocityObstacle(currentVelocity);
        while (isFindNewBestVelocity)
        {
            isFindNewBestVelocity = false;
            for (int i = 0; i < _orderedObstacles.size(); i++) {
                if (_orderedObstacles.get(i).IsCollideWithVelocityObstacle(bestVelocity)) {
                    // Если мы попали в предыдущую VO из текущей, значит мы колеблемся
                    if (prevObstacle[prevObstacleIndex] == i) {
                        if (i != prevObstacleIndex)
                            System.out.println("Resolve collision problem");
                        return _orderedObstacles.get(i).FindVelocityOutsideVelocityObstacle(bestVelocity, BaseObstacle.VelocityObstacleSide.RIGHT);
                    } else {
                        prevObstacle[i] = prevObstacleIndex;
                        bestVelocity = _orderedObstacles.get(i).FindVelocityOutsideVelocityObstacle(bestVelocity);
                        if (bestVelocity.isNaN())
                            System.out.println("Best Velocity is Nan");
                        prevObstacleIndex = i;
                        isFindNewBestVelocity = true;
                        break;
                    }
                }
            }
        }
        if (IsCollideWithVelocityObstacle(bestVelocity))
            System.out.println("outer agent problem");
        return bestVelocity;
    }
}
