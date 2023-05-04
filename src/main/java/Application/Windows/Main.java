package Application.Windows;

import Application.Controllers.MainWindowController;
import Navigation.Agent;
import Navigation.World;
import Patterns.Observer.IMouseEventReceiver;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
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
        for (int i = 10; i < 16; i++)
            for (int j = 10; j < 16; j++)
            {
                world.getMap().UpdateTile(i,j, Color.BLACK, -1);
            }
        for (int i = 23; i < 29; i++)
            for (int j = 10; j < 16; j++)
            {
                world.getMap().UpdateTile(i,j, Color.BLACK, -1);
            }
        for (int i = 10; i < 16; i++)
            for (int j = 23; j < 29; j++)
            {
                world.getMap().UpdateTile(i,j, Color.BLACK, -1);
            }
        for (int i = 23; i < 29; i++)
            for (int j = 23; j < 29; j++)
            {
                world.getMap().UpdateTile(i,j, Color.BLACK, -1);
            }
        List<Agent> yellowTeam = new ArrayList<>();
        for (int i = 30; i <= 180; i+=50)
        {
            for (int j = 30; j <= 180; j+=50)
            {
                Agent agent = new Agent(i, j, 10, Color.YELLOW, world, false);
                yellowTeam.add(agent);
            }
        }
        List<Agent> redTeam = new ArrayList<>();
        for (int i = 620; i <= 770; i+=50)
        {
            for (int j = 30; j <= 180; j+=50)
            {
                Agent agent = new Agent(i, j, 10, Color.RED, world, false);
                redTeam.add(agent);
            }
        }
        List<Agent> blueTeam = new ArrayList<>();
        for (int i = 620; i <= 770; i+=50)
        {
            for (int j = 620; j <= 770; j+=50)
            {
                Agent agent = new Agent(i, j, 10, Color.BLUE, world, false);
                blueTeam.add(agent);
            }
        }
        List<Agent> greenTeam = new ArrayList<>();
        for (int i = 30; i <= 180; i+=50)
        {
            for (int j = 620; j <= 770; j+=50)
            {
                Agent agent = new Agent(i, j, 10, Color.GREEN, world, false);
                greenTeam.add(agent);
            }
        }
        world.agents().addAll(
                Stream.of(yellowTeam.stream(),
                    redTeam.stream(),
                    blueTeam.stream(),
                    greenTeam.stream())
                .flatMap(i -> i).collect(Collectors.toList())
        );
        for (Agent a: world.agents()) {
            a.MoveTo(Math.abs(world.getMapModel().sizeX() - a.getPosition().getX()), Math.abs(world.getMapModel().sizeY() - a.getPosition().getY()));
        }
    }

    public void Scenario2()
    {
        int numObjects = 80;
        double r = 1.0, g = 0, b = 0;
        for (int i = 0; i < numObjects; i++) {
            if (i <= 27) {
                r = 1 - i/27d;
                g = i/27d;
                b = 0;
            }
            else if(i <= 54) {
                r = 0;
                g = 1 - (i-27)/27d;
                b = (i - 27)/27d;
            }
            else {
                g = 0;
                b = 1 - (i-54)/27d;
                r = (i - 54)/27d;
            }

            double angle = Math.toRadians(360.0 / numObjects * i); // угол в радианах
            int x = (int) (world.getMapModel().sizeX() * 0.5 + (world.getMapModel().sizeX() * 0.47d) * Math.cos(angle)); // координата X объекта
            int y = (int) (world.getMapModel().sizeY() * 0.5 + (world.getMapModel().sizeY() * 0.47d) * Math.sin(angle)); // координата Y объекта
            // создание и расстановка объекта с координатами (x, y)
            world.agents().add(new Agent(x, y, 10, new Color(r, g,b,1), world, false));
        }
        for (Agent a: world.agents()) {
            a.MoveTo(Math.abs(world.getMapModel().sizeX() - a.getPosition().getX()), Math.abs(world.getMapModel().sizeY() - a.getPosition().getY()));
        }
    }

    public void Scenario3()
    {
        world.agents().add(new Agent(200,200,20,Color.RED, world, false));
        world.agents().add(new Agent(200,400,20,Color.RED, world, false));
        world.agents().get(0).MoveTo(200,400);
        world.agents().get(1).MoveTo(200,200);
    }
}