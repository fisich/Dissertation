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

public class Main extends Application implements IMouseEventReceiver {
    World world;
    MainWindowController controller;
    Agent agent;
    Agent agentDummy1, agentDummy2, agentDummy3, agentDummy4, agentDummy5, agentDummy6, agentDummy7;
    volatile boolean simulate = true;

    public Main() {
        //Debugs();
        world = new World(20, 30, 30);
        // Scenario 1 and 2
        /*agent = new Agent(400, 400, 20, Color.AZURE, world, false);
        world.agents.add(agent);
        agentDummy1 = new Agent(50, 200, 20, Color.BROWN, world, true);
        world.agents.add(agentDummy1);
        agentDummy2 = new Agent(200,200,20,Color.BLACK, world,false);
        world.agents.add(agentDummy2);*/
        // Scenario 3
        agent = new Agent(300, 50, 20, Color.AZURE, world, false);
        world.agents.add(agent);
        agentDummy1 = new Agent(425, 175, 20, Color.BROWN, world, false);
        world.agents.add(agentDummy1);
        agentDummy2 = new Agent(550,300,20,Color.BLACK, world,false);
        world.agents.add(agentDummy2);
        agentDummy3 = new Agent(425, 425, 20, Color.GRAY, world, false);
        world.agents.add(agentDummy3);
        agentDummy4 = new Agent(300, 550, 20, Color.SILVER, world, false);
        world.agents.add(agentDummy4);
        agentDummy5 = new Agent(175,425,20,Color.BLUE, world,false);
        world.agents.add(agentDummy5);
        agentDummy6 = new Agent(50, 300, 20, Color.LIME, world, false);
        world.agents.add(agentDummy6);
        agentDummy7 = new Agent(175,175,20,Color.ORANGE, world,false);
        world.agents.add(agentDummy7);

        // Scenario 4
        //agent = new Agent(400, 300, 20, Color.AZURE, world, true);
        //world.agents.add(agent);
        //agentDummy1 = new Agent(300, 300, 20, Color.BLACK, world, false);
        //world.agents.add(agentDummy1);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new MainWindowController(world);
        controller.AddObserver(this);
        controller.start();
        //world.SimulateAgents();
    }

    @Override
    public void stop(){
        System.out.println("Stage is closing");
        simulate = false;
    }

    @Override
    public void Update(double x, double y) {
        // Scenario 1
        //agent.MoveTo(300, 180);
        //agentDummy1.MoveTo(400,200);
        // agentDummy2 is static

        // Scenario 2
        // agent.MoveTo(200, 180);
        // agentDummy1.MoveTo(400,200);
        // agentDummy2.MoveTo(400,300);

        // Scenario 3
        agent.MoveTo(300, 550);
        agentDummy1.MoveTo(175,425);
        agentDummy2.MoveTo(50,300);
        agentDummy3.MoveTo(175,175);
        agentDummy4.MoveTo(300,50);
        agentDummy5.MoveTo(425,175);
        agentDummy6.MoveTo(550,300);
        agentDummy7.MoveTo(425,425);

        // Scenario 4
        //agent.MoveTo(100, 300);
    }
}