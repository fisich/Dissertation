package Navigation.Map;

import MathExtensions.Vector2DExtension;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import static MathExtensions.Vector2DExtension.GetLineEquation;

public class NavigationMap {
    private final NavigationMapTileInfo[][] tiles;
    private final NavigationMapModel _mapModel;

    public NavigationMap(int mapTileSize, int tilesX, int tilesY) {
        _mapModel = new NavigationMapModel(mapTileSize, tilesX, tilesY);
        this.tiles = new NavigationMapTileInfo[tilesX][tilesY];
        ClearMapTilesInfo();
    }

    public void ClearMapTilesInfo()
    {
        for (int i = 0; i < _mapModel.getTilesX(); i++){
            for (int j = 0; j < _mapModel.getTilesY(); j++){
                tiles[i][j] = new NavigationMapTileInfo();
            }
        }
    }

    public boolean IsPathAtLineClear(Vector2D start, Vector2D end)
    {
        Vector2D _start = start, _end = end;
        Vector2DExtension.LineEquation equation = GetLineEquation(start, end);
        if (Math.abs(equation.M) < 1)
        {
            if (start.getX() > end.getX())
            {
                _start = end; _end = start;
            }
            for (int x = (int) _start.getX(); x < _end.getX(); x++ )
            {
                if(tiles[x][(int)Math.ceil(x*equation.M + equation.B)].getPassPrice() < 0)
                    return false;
            }
        }
        else
        {
            if (start.getY() > end.getY())
            {
                _start = end; _end = start;
            }
            for (int y = (int) _start.getY(); y < _end.getY(); y++)
            {
                if(tiles[(int) Math.ceil((y-equation.B)/equation.M)][y].getPassPrice() < 0)
                    return false;
            }
        }
        return true;
    }

    public void UpdateTile(int posX, int posY, Color color, float passPrice)
    {
        if (tiles[posX][posY] == null)
        {
            tiles[posX][posY] = new NavigationMapTileInfo(color, passPrice);
        }
        else
        {
            tiles[posX][posY].UpdateInfo(color, passPrice);
        }
    }

    public NavigationMapTileInfo getTile(int x, int y)
    {
        return tiles[x][y];
    }

    public NavigationMapModel getModel() { return _mapModel; }
}
