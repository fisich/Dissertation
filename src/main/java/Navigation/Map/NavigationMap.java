package Navigation.Map;

import MathExtensions.Vector2DExtension;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import static MathExtensions.Vector2DExtension.getLineEquation;

public class NavigationMap {
    private final NavigationMapTileInfo[][] mapTilesInfo;
    private final NavigationMapModel mapModel;

    public NavigationMap(int mapTileSize, int tilesX, int tilesY) {
        mapModel = new NavigationMapModel(mapTileSize, tilesX, tilesY);
        this.mapTilesInfo = new NavigationMapTileInfo[tilesX][tilesY];
        clearMapTilesInfo();
    }

    public void clearMapTilesInfo() {
        for (int i = 0; i < mapModel.getTilesX(); i++) {
            for (int j = 0; j < mapModel.getTilesY(); j++) {
                mapTilesInfo[i][j] = new NavigationMapTileInfo();
            }
        }
    }

    public boolean isPathBetweenPointsClear(Vector2D start, Vector2D end) {
        Vector2D _start = start, _end = end;
        Vector2DExtension.LineEquation equation = getLineEquation(start, end);
        if (Math.abs(equation.M) < 1) {
            if (start.getX() > end.getX()) {
                _start = end;
                _end = start;
            }
            for (int x = (int) _start.getX(); x < (int) _end.getX(); x++) {
                int y = (int) Math.ceil(x * equation.M + equation.B);
                y = y < mapModel.getTilesY() ? y : y - 1;
                if (mapTilesInfo[x][y].getPassPrice() < 0)
                    return false;
            }
        } else {
            if (start.getY() > end.getY()) {
                _start = end;
                _end = start;
            }
            for (int y = (int) _start.getY(); y < (int) _end.getY(); y++) {
                int x = (int) Math.ceil((y - equation.B) / equation.M);
                x = x < mapModel.getTilesX() ? x : x - 1;
                if (mapTilesInfo[x][y].getPassPrice() < 0)
                    return false;
            }
        }
        return true;
    }

    public void updateTileInfo(int posX, int posY, Color color, double passPrice) {
        if (mapTilesInfo[posX][posY] == null)
            mapTilesInfo[posX][posY] = new NavigationMapTileInfo(color, passPrice);
        else
            mapTilesInfo[posX][posY].UpdateInfo(color, passPrice);
    }

    public NavigationMapTileInfo getTileInfo(int x, int y) {
        return mapTilesInfo[x][y];
    }

    public NavigationMapModel getMapModel() {
        return mapModel;
    }
}
