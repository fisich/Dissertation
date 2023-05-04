package Navigation.Map;

public class NavigationMapModel {
    private final int _mapTileSize;
    private final int _tilesX, _tilesY;
    private final double _sizeX, _sizeY;

    public NavigationMapModel(int mapTileSize, int tilesX, int tilesY) {
        this._mapTileSize = mapTileSize;
        this._tilesX = tilesX;
        this._tilesY = tilesY;
        this._sizeX = tilesX * mapTileSize;
        this._sizeY = tilesY * mapTileSize;
    }

    public int getTileSize() { return _mapTileSize; }

    public int getTilesX() { return _tilesX; }

    public int getTilesY() { return _tilesY; }

    public double sizeX() { return _sizeX; }

    public double sizeY() { return _sizeY; }
}
