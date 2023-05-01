package Navigation.Map;

import MathExtensions.Vector2DExtension;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import static MathExtensions.Vector2DExtension.GetLineEquation;

public class NavigationMap {
    public final int mapTileSize;
    public final int tilesX, tilesY;
    public final NavigationMapTileInfo[][] tiles;
    public final double sizeX, sizeY;

    public NavigationMap(int mapTileSize, int tilesX, int tilesY) {
        this.mapTileSize = mapTileSize;
        this.tilesX = tilesX;
        this.tilesY = tilesY;
        this.tiles = new NavigationMapTileInfo[tilesX][tilesY];
        this.sizeX = tilesX * mapTileSize;
        this.sizeY = tilesY * mapTileSize;
        ClearMapTilesInfo();
    }

    public void ClearMapTilesInfo()
    {
        for (int i = 0; i < tilesX; i++){
            for (int j = 0; j < tilesY; j++){
                tiles[i][j] = new NavigationMapTileInfo();
            }
        }
    }

    public boolean IsPathBetweenLinesClear(Vector2D start, Vector2D end)
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
}
