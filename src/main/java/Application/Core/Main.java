package Application.Core;

import Application.Controllers.MainWindowController;
import Navigation.VelocityObstacle.VelocityObstacleAlgorithm;
import Navigation.VirtualEnvironment;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    VirtualEnvironment virtualEnvironment;
    MainWindowController windowController;


    public Main() {
        virtualEnvironment = new VirtualEnvironment(20, 40, 40, VelocityObstacleAlgorithm.VELOCITY_OBSTACLE);
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        windowController = new MainWindowController(virtualEnvironment);
        windowController.start();
    }

    @Override
    public void stop() {
        System.out.println("Application is closing");
    }
}