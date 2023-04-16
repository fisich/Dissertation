package Navigation.Map;

import javafx.scene.paint.Color;

public class NavigationMap {
    public final int mapTileSize;
    public final int sizeX, sizeY;
    public final NavigationMapTileInfo[][] tiles;

    public NavigationMap(int mapTileSize, int sizeX, int sizeY) {
        this.mapTileSize = mapTileSize;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        tiles = new NavigationMapTileInfo[sizeX][sizeY];
        ClearMapTilesInfo();
    }

    public void ClearMapTilesInfo()
    {
        for (int i = 0; i < sizeX; i++){
            for (int j = 0; j < sizeY; j++){
                tiles[i][j] = new NavigationMapTileInfo();
            }
        }
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
