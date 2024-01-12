package agh.ics.oop.model;

import agh.ics.oop.presenter.SimulationPresenter;
import javafx.scene.control.SplitPane;

import java.util.Map;

public interface IMap {
    void deleteDeadAnimals();

    void moveAnimals();

    void eatGrass();

    void reproduce();

    void placeNewGrass(int grassEachDay);

    void descendantCounting();

    void animalsNextDate();

    int numberOfAnimals();

    int getWidth();

    int getHeight();
    
    Arguments getArgs();

    WorldElement objectAt(Vector2d vector2d);

    void addObserver(MapChangeListener observer);

    void removeObserver(MapChangeListener observer);

    int getDay();

    MapDirection[] getMostPopularGenom();

    int getGrassFields();

    double averageEnergyLevel();

    double averageChildrenCount();

    double averageAge();
}