package Application.Controllers;

import Navigation.World;
import Patterns.Observer.IMouseEventReceiver;
import Patterns.Observer.MouseEventSender;
import Rendering.WorldRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

public class MainWindowController extends MouseEventSender {

    private final Stage rootStage;
    private World _world;
    private WorldRenderer _renderer;
    @FXML
    public Canvas mapCanvas;

    public MainWindowController(World world) throws IOException {
        rootStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainScene.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        rootStage.setScene(new Scene(root));
        _world = world;
        mapCanvas.setWidth(_world.map.sizeX * _world.map.mapTileSize);
        mapCanvas.setHeight(_world.map.sizeY * _world.map.mapTileSize);
    }

    public void start()
    {
        rootStage.show();
        _renderer = new WorldRenderer(_world, mapCanvas);
        AddObserver(_renderer);

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                _renderer.Redraw();
            }
        }.start();
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void initialize()
    {
        mapCanvas.setOnMouseClicked(event -> mouseDrawOnCanvas(event));
    }

    public void mouseDrawOnCanvas(MouseEvent mouseEvent) {
        NotifyObservers(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void NotifyObservers(double x, double y) {
        for (IMouseEventReceiver observer: observers) {
            observer.Update(x, y);
        }
    }
}
