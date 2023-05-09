package Navigation.Map;

public class NavigationMapModel {
    private final int mapTileSize;
    private final int tilesX, tilesY;
    private final double sizeX, sizeY;

    public NavigationMapModel(int mapTileSize, int tilesX, int tilesY) {
        this.mapTileSize = mapTileSize;
        this.tilesX = tilesX;
        this.tilesY = tilesY;
        this.sizeX = tilesX * mapTileSize;
        this.sizeY = tilesY * mapTileSize;
    }

    public int getTileSize() {
        return mapTileSize;
    }

    public int getTilesX() {
        return tilesX;
    }

    public int getTilesY() {
        return tilesY;
    }

    public double sizeX() {
        return sizeX;
    }

    public double sizeY() {
        return sizeY;
    }
}
