package Application.Windows;

import Application.Controllers.MainWindowController;
import Navigation.Agent;
import Navigation.World;
import Patterns.Observer.IMouseEventReceiver;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends Application implements IMouseEventReceiver {
    World world;
    MainWindowController controller;
    boolean start = true;


    public Main() {
        world = new World(20, 40, 40);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new MainWindowController(world);
        controller.AddObserver(this);
        controller.start();
        for (int i = 9; i < 16; i++)
            for (int j = 9; j < 16; j++)
            {
                world.map.UpdateTile(i,j, Color.BLACK, -1);
            }
        for (int i = 23; i < 31; i++)
            for (int j = 9; j < 16; j++)
            {
                world.map.UpdateTile(i,j, Color.BLACK, -1);
            }
        for (int i = 9; i < 16; i++)
            for (int j = 23; j < 31; j++)
            {
                world.map.UpdateTile(i,j, Color.BLACK, -1);
            }
        for (int i = 23; i < 31; i++)
            for (int j = 23; j < 31; j++)
            {
                world.map.UpdateTile(i,j, Color.BLACK, -1);
            }
        List<Agent> yellowTeam = new ArrayList<>();
        for (int i = 30; i <= 150; i+=40)
        {
            for (int j = 30; j <= 150; j+=40)
            {
                yellowTeam.add(new Agent(i, j, 10, Color.YELLOW, world, false));
            }
        }
        List<Agent> redTeam = new ArrayList<>();
        for (int i = 650; i <= 770; i+=40)
        {
            for (int j = 30; j <= 150; j+=40)
            {
                //redTeam.add(new Agent(i, j, 10, Color.RED, world, false));
            }
        }
        List<Agent> blueTeam = new ArrayList<>();
        for (int i = 650; i <= 770; i+=40)
        {
            for (int j = 650; j <= 770; j+=40)
            {
                //blueTeam.add(new Agent(i, j, 10, Color.BLUE, world, false));
            }
        }
        List<Agent> greenTeam = new ArrayList<>();
        for (int i = 30; i <= 150; i+=40)
        {
            for (int j = 650; j <= 770; j+=40)
            {
                //greenTeam.add(new Agent(i, j, 10, Color.GREEN, world, false));
            }
        }
        world.agents = Stream.of(yellowTeam.stream(),
                redTeam.stream(),
                blueTeam.stream(),
                greenTeam.stream())
                .flatMap(i -> i)
                .collect(Collectors.toList());
    }

    @Override
    public void stop(){
        System.out.println("Stage is closing");
    }

    @Override
    public void Update(double x, double y) {
        if (start) {
            Scenario1();
            start = false;
        }
    }

    public void Scenario1()
    {
        for (Agent a: world.agents) {
            a.MoveTo(Math.abs(800 - a.getPosition().getX()), Math.abs(800 - a.getPosition().getY()));
        }
    }
}