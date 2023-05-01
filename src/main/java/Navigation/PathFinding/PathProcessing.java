package Navigation.PathFinding;

import Navigation.World;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PathProcessing {
    public static List<Vector2D> StraightenThePath(List<Vector2D> originalPath)
    {
        if (originalPath.size() <= 2)
            return originalPath;
        List<Vector2D> straightenPath = new ArrayList<>();
        AtomicReference<Vector2D> prevDirection = new AtomicReference<>(originalPath.get(1).subtract(originalPath.get(0)));
        AtomicReference<Vector2D> prevPosition = new AtomicReference<>(originalPath.get(0));
        straightenPath.add(prevPosition.get());
        originalPath.subList(1, originalPath.size()).forEach(pos -> {
            if (!pos.subtract(prevPosition.get()).equals(prevDirection.get()))
            {
                straightenPath.add(prevPosition.get());
            }
            prevDirection.set(pos.subtract(prevPosition.get()));
            prevPosition.set(pos);
        });
        straightenPath.add(originalPath.get(originalPath.size() - 1));
        return straightenPath;
    }
}
