package Navigation.Map;

import javafx.scene.paint.Color;

public class NavigationMapTileInfo {
    private Color color;
    private float passPrice;

    public NavigationMapTileInfo()
    {
        color = Color.LIGHTGRAY;
        passPrice = 0;
    }

    public NavigationMapTileInfo(Color color, float passPrice) {
        this.color = color;
        this.passPrice = passPrice;
    }

    public void UpdateInfo(Color color, float passPrice) {
        this.color = color;
        this.passPrice = passPrice;
    }

    public Color getColor()
    {
        return color;
    }

    public float getPassPrice() {
        return passPrice;
    }
}