package agh.ics.oop.presenter;

import agh.ics.oop.model.element.Animal;
import agh.ics.oop.model.map.IMap;
import agh.ics.oop.model.simulation.SimulationState;
import agh.ics.oop.model.simulation.Arguments;
import agh.ics.oop.model.simulation.Simulation;
import agh.ics.oop.model.util.ImageBox;
import agh.ics.oop.model.util.MapChangeListener;
import agh.ics.oop.model.util.MapDirection;
import agh.ics.oop.model.util.Vector2d;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import java.awt.*;
import javafx.scene.input.MouseEvent;
import java.util.Objects;
import java.util.List;
import static java.lang.Math.*;

public class SimulationPresenter implements MapChangeListener {

    public GridPane mapGrid;
    public Label dayLabel;
    public SplitPane mainSplitPane;
    public Label data1;
    public Label mapStats;
    public AnchorPane rightSide;
    public VBox legend;
    public HBox waterLegend;
    public Label stats;
    public Label trackingLabel;
    public VBox plotField;
    public BorderPane main;
    public Label info;
    public Label graph;
    public Label simulationID;
    public Button simulationButton;
    public Button genomeButton;
    private int state=0;
    private LineChart<Number,Number> chart;
    private IMap map;
    private Simulation simulation;
    private Stage primaryStage;
    private boolean firstClick=true;
    private Animal trackedAnimal = null;
    private static final Image GRASS = new Image(Objects.requireNonNull(SimulationPresenter.class.getResource("/images/grass.png")).toExternalForm());
    private static final Image ANIMAL = new Image(Objects.requireNonNull(SimulationPresenter.class.getResource("/images/paw.png")).toExternalForm());
    private static final Image WATER = new Image(Objects.requireNonNull(SimulationPresenter.class.getResource("/images/water.png")).toExternalForm());

    public void setPrimaryStage(Stage primaryStage) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        main.setMinHeight((int) (screenSize.height * 0.9));
        main.setMinWidth((int) (screenSize.width * 0.95));

        this.primaryStage = primaryStage;
        this.primaryStage.setOnCloseRequest(event -> {
            simulation.setState(SimulationState.FINISHED);
        });
        simulationButton.setCursor(Cursor.HAND);
    }

    public void setWorldMap(IMap map){
        this.map = map;
        simulationID.setText("Simulation " + map.getID());
        if (map.isWaterMap()) {
            waterLegend.setVisible(true);
            waterLegend.setManaged(true);
        } else {
            waterLegend.setVisible(false);
            waterLegend.setManaged(false);
        }
        plot();
    }

    public void drawMap(IMap worldMap){
        mapGrid.setAlignment(Pos.CENTER);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int width = (int) (screenSize.width * 0.7);
        int height = (int) (screenSize.height * 0.85);
        int CELL = min(height/ worldMap.getHeight(),width/ worldMap.getWidth());

        Arguments args = worldMap.getArgs();
        Vector2d topRight = new Vector2d(args.mapWidth(),args.mapHeight());
        Vector2d bottomLeft = new Vector2d(0,0);

        int rowSize = args.mapWidth();
        int colSize = args.mapHeight();
        for (int j = 0; j < rowSize; j++) {
            ColumnConstraints column = new ColumnConstraints(CELL);
            mapGrid.getColumnConstraints().add(column);
        }

        for (int i = 0; i < colSize; i++) {
            RowConstraints row = new RowConstraints(CELL);
            mapGrid.getRowConstraints().add(row);
        }


        for (int i = bottomLeft.getX(); i < topRight.getX(); i++) {
            for (int j = bottomLeft.getY(); j < topRight.getY(); j++) {

                int adjustedI = i - bottomLeft.getX() ;
                int adjustedJ = colSize  - (j - bottomLeft.getY()) -1;

                Pane pane = new Pane();
                pane.setStyle("-fx-background-color: #D4E7C5; -fx-border-color: black; -fx-border-width: 0.5px;");

                if(j>=map.getHeight()*2/5 && j<map.getHeight()*3/5){
                    pane.setStyle("-fx-background-color: #99BC85; -fx-border-color: black; -fx-border-width: 0.5px;");
                }
                if (worldMap.objectAt(new Vector2d(i, j)) != null) {
                    String path = worldMap.objectAt(new Vector2d(i, j)).toString();
                    ImageBox imageBox = null;
                    if (path.equals("paw")) {
                        Animal animal = worldMap.getAnimal(new Vector2d(i, j));
                        if (animal!=null) {
                            if(trackedAnimal != null && worldMap.positionContainsAnimal(new Vector2d(i, j), trackedAnimal)){
                                pane.setStyle("-fx-background-color: #e19d5c; -fx-border-color: black; -fx-border-width: 0.5px;");
                            }
                            imageBox = new ImageBox(ANIMAL,animal.getEnergy(),worldMap.getArgs().animalEnergy());
                            imageBox.setRotation(worldMap.objectAt(new Vector2d(i, j)).getOrientation().getI());
                            imageBox.setFit(CELL * 0.7);
                            setOnAnimalClicked(imageBox, animal);
                            if(simulation.getState()==SimulationState.STOPED || map.getDay()==1){
                                imageBox.setCursor(Cursor.HAND);
                            }
                        }
                    }
                    else if (path.equals("water")){
                        imageBox = new ImageBox(WATER);
                        imageBox.setFit(CELL);
                        pane.setStyle("-fx-background-color: #52aac0; -fx-border-color: black; -fx-border-width: 0.5px;");
                    } else{
                        imageBox= new ImageBox(GRASS);
                        imageBox.setFit(CELL);
                    }

                    mapGrid.add(pane,adjustedI, adjustedJ);
                    if (imageBox!=null)
                        mapGrid.add(imageBox, adjustedI, adjustedJ);

                } else{
                    mapGrid.add(pane,adjustedI, adjustedJ);
                }
            }
        }

        dayLabel.setText("Day " + worldMap.getDay());
        mapStats.setText("Number of animals: "+ worldMap.numberOfAnimals()+"\nMost popular genome: "
                + worldMap.getMostPopularGenome()
                + "\nNumber of grass fields: "+ worldMap.getGrassFields()
                + "\nAverage energy level: "
                + worldMap.averageEnergyLevel()+"\nAverage child count: "
                + worldMap.averageChildrenCount()+"\nAverage dead animal age: "
                + worldMap.averageAge()+"\nNumber of animals ever lived: " + worldMap.everAnimalCount()
                +"\nNumber of free fields: " + worldMap.getFreeFields());

        plotChange();
        if (trackedAnimal!=null) {
            trackingStats(trackedAnimal);
            if(trackedAnimal.getEnergy()<=0)
                stats.setText(stats.getText() + "\nDeath Day: " + trackedAnimal.getDeathDate());
        }
    }

    private void plotChange(){
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();

        yAxis.setUpperBound(max((int)yAxis.getUpperBound(),map.numberOfAnimals()));
        yAxis.setUpperBound(max((int)yAxis.getUpperBound(),map.getGrassFields()));
        yAxis.setUpperBound(max((int)yAxis.getUpperBound(),map.averageAge()));
        yAxis.setUpperBound(max((int)yAxis.getUpperBound(),map.averageEnergyLevel()));
        yAxis.setUpperBound(max((int)yAxis.getUpperBound(),map.averageChildrenCount()));
        yAxis.setUpperBound(min((int)yAxis.getUpperBound(),1000));
        if(map.getDay()>30) {
            xAxis.setLowerBound(map.getDay()-30);
            xAxis.setUpperBound(map.getDay());
        }

        XYChart.Series<Number, Number> series0 = chart.getData().get(0);
        XYChart.Series<Number, Number> series1 = chart.getData().get(1);
        XYChart.Series<Number, Number> series2 = chart.getData().get(2);
        XYChart.Series<Number, Number> series3 = chart.getData().get(3);
        XYChart.Series<Number, Number> series4 = chart.getData().get(4);

        series0.getData().add(new XYChart.Data<>(map.getDay(), map.numberOfAnimals()));
        series1.getData().add(new XYChart.Data<>(map.getDay(),map.getGrassFields()));
        series2.getData().add(new XYChart.Data<>(map.getDay(), map.averageEnergyLevel()));
        series3.getData().add(new XYChart.Data<>(map.getDay(), map.averageAge()));
        series4.getData().add(new XYChart.Data<>(map.getDay(), map.averageChildrenCount()));
    }

    private void plot(){
        NumberAxis xAxis = new NumberAxis(0,30,1);
        NumberAxis yAxis = new NumberAxis(0,max(map.numberOfAnimals()*2,map.getGrassFields()*2), 1);

        chart = new LineChart<>(xAxis, yAxis);
        chart.setMaxSize(450,350);

        chart.setCreateSymbols(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);

        XYChart.Series<Number, Number> animalSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> grassSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> energySeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> lifeSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> kidSeries = new XYChart.Series<>();

        animalSeries.setName("Animal count");
        grassSeries.setName("Grass count");
        energySeries.setName("Average energy");
        lifeSeries.setName("Average age");
        kidSeries.setName("Average child count");

        animalSeries.getData().add(new XYChart.Data<>(1, map.numberOfAnimals()));
        grassSeries.getData().add(new XYChart.Data<>(1, map.getGrassFields()));

        energySeries.getData().add(new XYChart.Data<>(1, map.averageEnergyLevel()));
        lifeSeries.getData().add(new XYChart.Data<>(1, map.averageAge()));
        kidSeries.getData().add(new XYChart.Data<>(1, map.averageChildrenCount()));

        chart.getData().add(animalSeries);
        chart.getData().add(grassSeries);
        chart.getData().add(energySeries);
        chart.getData().add(lifeSeries);
        chart.getData().add(kidSeries);

        plotField.getChildren().add(chart);
    }

    @Override
    public void mapChanged(IMap worldMap) {
        Platform.runLater(() -> {
                    clearGrid();
                    drawMap(worldMap);
                });
    }

    public void startSimulation() {
        if (firstClick) {
            Thread thread = new Thread((simulation));
            thread.start();
            firstClick = false;
        } else {
            simulation.setState(SimulationState.STARTED);
        }
    }


    private void clearGrid() {
        mapGrid.getChildren().retainAll(mapGrid.getChildren().get(0));
        mapGrid.getColumnConstraints().clear();
        mapGrid.getRowConstraints().clear();
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    private void setTrackedAnimal(Animal animal){
        this.trackedAnimal = animal;
    }

    private void setOnAnimalClicked(ImageBox imageBox, Animal animal) {
        imageBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(simulation.getState()==SimulationState.STOPED || map.getDay()==1) {
                    trackingLabel.setText("Tracked animal statistics: ");
                    setTrackedAnimal(animal);
                    trackingStats(animal);
                    mapChanged(map);
                }
            }
        });
    }

    private void trackingStats(Animal animal){
        stats.setText(
                  "ID: " + animal.getMyId()
                + "\nPosition: " + animal.getPosition()
                + "\nGenome: " + animal.getGenome()
                + "\nCurrent index: " + animal.getIndex()
                + "\nEnergy level: " + animal.getEnergy()
                + "\nKids count: " + animal.getChildrenCount()
                + "\nDescendantCount: " + animal.descendantCalculate()
                + "\nAge: " + animal.getAge()
                + "\nGrass eaten: " + animal.getGrassEaten()
        );
    }

    public void onSimulationButtonClicked(ActionEvent actionEvent) {
        if (state==0){
            startSimulation();
            state = 1;
            simulationButton.setText("Stop Simulation");
            genomeButton.setDisable(true);
        }
        else {
            genomeButton.setDisable(false);
            simulation.stopSimulation();
            state = 0;
            simulationButton.setText("Start Simulation");
            mapChanged(map);
        }
    }

    private void popularGenome() {
        List<MapDirection> mostPopularGenome = map.getMostPopularGenome();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int width = (int) (screenSize.width * 0.7);
        int height = (int) (screenSize.height * 0.85);
        int CELL = min(height/ map.getHeight(),width/ map.getWidth());
        Arguments args = map.getArgs();
        Vector2d topRight = new Vector2d(args.mapWidth(), args.mapHeight());
        Vector2d bottomLeft = new Vector2d(0, 0);

        for (int i = bottomLeft.getX(); i < topRight.getX(); i++) {
            for (int j = bottomLeft.getY(); j < topRight.getY(); j++) {
                Pane pane = new Pane();
                if (map.objectAt(new Vector2d(i, j)) != null) {
                    ImageBox imageBox;
                    Animal animal = map.getAnimal(new Vector2d(i, j));
                    if (animal != null) {
                        if (mostPopularGenome.equals(animal.getGenome().getMoves())) {
                            pane.setStyle("-fx-background-color: #ff7070; -fx-border-color: black; -fx-border-width: 0.5px;");
                            imageBox = new ImageBox(ANIMAL,animal.getEnergy(),map.getArgs().animalEnergy());
                            imageBox.setRotation(map.objectAt(new Vector2d(i, j)).getOrientation().getI());
                            imageBox.setFit(CELL * 0.7);
                            setOnAnimalClicked(imageBox, animal);
                            mapGrid.add(pane, i - bottomLeft.getX(), map.getArgs().mapHeight() - (j - bottomLeft.getY()) - 1);
                            mapGrid.add(imageBox, i - bottomLeft.getX(), map.getArgs().mapHeight() - (j - bottomLeft.getY()) - 1);
                        }
                    }
                }
            }
        }
    }


    public void onShowGenome(ActionEvent actionEvent) {
        Platform.runLater(this::popularGenome);
    }
}
