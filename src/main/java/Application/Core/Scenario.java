package Application.Core;

import Navigation.Agent;
import Navigation.VirtualEnvironment;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scenario {
    public static void scenario1(VirtualEnvironment virtualEnvironment) {
        virtualEnvironment.agents().clear();
        virtualEnvironment.getMap().clearMapTilesInfo();
        for (int i = 10; i < 16; i++)
            for (int j = 10; j < 16; j++) {
                virtualEnvironment.getMap().updateTileInfo(i, j, Color.BLACK, -1);
            }
        for (int i = 23; i < 29; i++)
            for (int j = 10; j < 16; j++) {
                virtualEnvironment.getMap().updateTileInfo(i, j, Color.BLACK, -1);
            }
        for (int i = 10; i < 16; i++)
            for (int j = 23; j < 29; j++) {
                virtualEnvironment.getMap().updateTileInfo(i, j, Color.BLACK, -1);
            }
        for (int i = 23; i < 29; i++)
            for (int j = 23; j < 29; j++) {
                virtualEnvironment.getMap().updateTileInfo(i, j, Color.BLACK, -1);
            }
        List<Agent> yellowTeam = new ArrayList<>();
        for (int i = 30; i <= 190; i += 40) {
            for (int j = 30; j <= 190; j += 40) {
                Agent agent = new Agent(i, j, 10, Color.YELLOW, virtualEnvironment);
                yellowTeam.add(agent);
            }
        }
        List<Agent> redTeam = new ArrayList<>();
        for (int i = 610; i <= 770; i += 40) {
            for (int j = 30; j <= 190; j += 40) {
                Agent agent = new Agent(i, j, 10, Color.RED, virtualEnvironment);
                redTeam.add(agent);
            }
        }
        List<Agent> blueTeam = new ArrayList<>();
        for (int i = 610; i <= 770; i += 40) {
            for (int j = 610; j <= 770; j += 40) {
                Agent agent = new Agent(i, j, 10, Color.BLUE, virtualEnvironment);
                blueTeam.add(agent);
            }
        }
        List<Agent> greenTeam = new ArrayList<>();
        for (int i = 30; i <= 190; i += 40) {
            for (int j = 610; j <= 770; j += 40) {
                Agent agent = new Agent(i, j, 10, Color.GREEN, virtualEnvironment);
                greenTeam.add(agent);
            }
        }
        virtualEnvironment.agents().addAll(
                Stream.of(yellowTeam.stream(),
                        redTeam.stream(),
                        blueTeam.stream(),
                        greenTeam.stream())
                        .flatMap(i -> i).collect(Collectors.toList())
        );
        for (Agent a : virtualEnvironment.agents()) {
            a.moveTo(Math.abs(virtualEnvironment.getMapModel().sizeX() - a.getPosition().getX()), Math.abs(virtualEnvironment.getMapModel().sizeY() - a.getPosition().getY()));
        }
    }

    public static void scenario2(VirtualEnvironment virtualEnvironment) {
        virtualEnvironment.agents().clear();
        virtualEnvironment.getMap().clearMapTilesInfo();
        int numObjects = 80;
        double r, g, b;
        for (int i = 0; i < numObjects; i++) {
            if (i <= 27) {
                r = 1 - i / 27d;
                g = i / 27d;
                b = 0;
            } else if (i <= 54) {
                r = 0;
                g = 1 - (i - 27) / 27d;
                b = (i - 27) / 27d;
            } else {
                g = 0;
                b = 1 - (i - 54) / 27d;
                r = (i - 54) / 27d;
            }

            double angle = Math.toRadians(360.0 / numObjects * i); // угол в радианах
            int x = (int) (virtualEnvironment.getMapModel().sizeX() * 0.5 + (virtualEnvironment.getMapModel().sizeX() * 0.47d) * Math.cos(angle)); // координата X объекта
            int y = (int) (virtualEnvironment.getMapModel().sizeY() * 0.5 + (virtualEnvironment.getMapModel().sizeY() * 0.47d) * Math.sin(angle)); // координата Y объекта
            // создание и расстановка объекта с координатами (x, y)
            virtualEnvironment.agents().add(new Agent(x, y, 10, new Color(r, g, b, 1), virtualEnvironment));
        }
        for (Agent a : virtualEnvironment.agents()) {
            a.moveTo(Math.abs(virtualEnvironment.getMapModel().sizeX() - a.getPosition().getX()), Math.abs(virtualEnvironment.getMapModel().sizeY() - a.getPosition().getY()));
        }
    }

    public static void scenario(VirtualEnvironment virtualEnvironment) {
        virtualEnvironment.agents().clear();
        virtualEnvironment.getMap().clearMapTilesInfo();
        virtualEnvironment.agents().add(new Agent(200, 200, 20, Color.RED, virtualEnvironment));
        virtualEnvironment.agents().add(new Agent(200, 400, 20, Color.RED, virtualEnvironment));
        virtualEnvironment.agents().get(0).moveTo(200, 400);
        virtualEnvironment.agents().get(1).moveTo(200, 200);
    }

    public static void scenario3(VirtualEnvironment virtualEnvironment)
    {
        virtualEnvironment.agents().clear();
        virtualEnvironment.getMap().clearMapTilesInfo();
        List<Agent> firstGroup = new ArrayList<>();
        List<Agent> secondGroup = new ArrayList<>();
        for (int i = 30; i <= 780; i+=40)
        {
                Agent agent = new Agent(i, 385, 10, Color.GREEN, virtualEnvironment);
                firstGroup.add(agent);
                Agent agent1 = new Agent(i, 415, 10, Color.ORANGE, virtualEnvironment);
                secondGroup.add(agent1);
        }
        virtualEnvironment.agents().addAll(Stream.of(firstGroup.stream(), secondGroup.stream()).flatMap(i -> i).collect(Collectors.toList()));
        for (Agent a: firstGroup)
        {
            a.moveTo(a.getPosition().getX(), 500);
        }
        for (Agent a: secondGroup)
        {
            a.moveTo(a.getPosition().getX(), 300);
        }
    }
}
