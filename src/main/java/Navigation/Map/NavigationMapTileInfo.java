package Navigation.Map;

import javafx.scene.paint.Color;

public class NavigationMapTileInfo {
    private Color color;
    private double passPrice;

    public NavigationMapTileInfo() {
        color = Color.WHITE;
        passPrice = 0;
    }

    public NavigationMapTileInfo(Color color, double passPrice) {
        this.color = color;
        this.passPrice = passPrice;
    }

    public void UpdateInfo(Color color, double passPrice) {
        this.color = color;
        this.passPrice = passPrice;
    }

    public Color getColor() {
        return color;
    }

    public double getPassPrice() {
        return passPrice;
    }
}