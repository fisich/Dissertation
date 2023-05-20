package Application.Controllers;

import Application.Core.Scenario;
import Application.Rendering.EnvironmentRenderer;
import Navigation.VelocityObstacle.VelocityObstacleAlgorithm;
import Navigation.VirtualEnvironment;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.IOException;

public class MainWindowController{

    private final Stage mRootStage;
    private final VirtualEnvironment mVirtualEnvironment;
    private EnvironmentRenderer mEnvironmentRenderer;
    private boolean pause = true;

    @FXML
    private Canvas mapCanvas;
    @FXML
    private Button setAgentsBtn, runScenarioBtn;
    @FXML
    private ChoiceBox<String> scenariosChoiceBox, algorithmsChoiceBox;
    @FXML
    private CheckBox drawVelocitiesChkbox, drawStaticObstaclesChkbox, drawDynamicObstaclesChkbox;

    public MainWindowController(VirtualEnvironment virtualEnvironment) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainScene.fxml"));
        loader.setController(this);
        mRootStage = loader.load();
        mVirtualEnvironment = virtualEnvironment;
        mapCanvas.setWidth(mVirtualEnvironment.getMapModel().getTilesX() * mVirtualEnvironment.getMapModel().getTileSize());
        mapCanvas.setHeight(mVirtualEnvironment.getMapModel().getTilesY() * mVirtualEnvironment.getMapModel().getTileSize());
    }

    public void start() {
        mRootStage.show();
        mEnvironmentRenderer = new EnvironmentRenderer(mVirtualEnvironment, mapCanvas);

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                mEnvironmentRenderer.render();
                if (!pause)
                    mVirtualEnvironment.tickAgents();
            }
        }.start();
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void initialize() {
        mapCanvas.setOnMouseClicked(this::mouseDrawOnCanvas);
        setAgentsBtn.setOnMouseClicked(event -> {
            pause = true;
            VelocityObstacleAlgorithm algorithm;
            switch (algorithmsChoiceBox.getSelectionModel().getSelectedItem()) {
                case "VO":
                    algorithm = VelocityObstacleAlgorithm.VELOCITY_OBSTACLE;
                    break;
                case "RVO":
                    algorithm = VelocityObstacleAlgorithm.RECIPROCAL_VELOCITY_OBSTACLE;
                    break;
                case "HRVO":
                    algorithm = VelocityObstacleAlgorithm.HYBRID_RECIPROCAL_VELOCITY_OBSTACLE;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + algorithmsChoiceBox.getSelectionModel().getSelectedItem());
            }
            mVirtualEnvironment.setAlgorithm(algorithm);
            switch (scenariosChoiceBox.getSelectionModel().selectedIndexProperty().getValue())
            {
                case 0:
                    Scenario.scenario1(mVirtualEnvironment);
                    break;
                case 1:
                    Scenario.scenario2(mVirtualEnvironment);
                    break;
                case 2:
                    Scenario.scenario3(mVirtualEnvironment);
                    break;
                case 3:
                    Scenario.scenario4(mVirtualEnvironment);
                    break;
                default:
                    break;
            }
        });
        runScenarioBtn.setOnMouseClicked(event -> pause = !pause);
        drawVelocitiesChkbox.setOnMouseClicked(event -> mEnvironmentRenderer.drawVelocities = drawVelocitiesChkbox.isSelected());
        drawStaticObstaclesChkbox.setOnMouseClicked(event -> mEnvironmentRenderer.drawStaticObstacles = drawStaticObstaclesChkbox.isSelected());
        drawDynamicObstaclesChkbox.setOnMouseClicked(event -> mEnvironmentRenderer.drawDynamicObstacles = drawDynamicObstaclesChkbox.isSelected());
    }

    private void mouseDrawOnCanvas(MouseEvent mouseEvent) {
        Vector2D mapTile = mVirtualEnvironment.fromScreenToMapCoordinate2D(mouseEvent.getX(), mouseEvent.getY());
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            mVirtualEnvironment.getMap().updateTileInfo((int) mapTile.getX(), (int) mapTile.getY(), Color.BLACK, -1);
        }
        else if (mouseEvent.getButton() == MouseButton.SECONDARY)
        {
            mVirtualEnvironment.getMap().updateTileInfo((int) mapTile.getX(), (int) mapTile.getY(), Color.LIGHTGRAY, 0);
        }
    }
}