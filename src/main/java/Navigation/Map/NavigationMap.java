package Navigation.Map;

import javafx.scene.paint.Color;

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
