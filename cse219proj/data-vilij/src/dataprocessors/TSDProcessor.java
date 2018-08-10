package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }


    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    private XYChart.Series<Number, Number> firstLine;
    private boolean dupeError;
    private boolean formatError;

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        dupeError= false;
        formatError = false;
    }

    public Map<String, String> getDataLabels() { return dataLabels; }
    public boolean hasDupeError() { return dupeError; }
    public boolean hasFormatError() { return formatError; }
    public XYChart.Series<Number, Number> getFirstLine(){ return firstLine; }
    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try {
                      String   name  = checkedname(list.get(0));
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      if(dataLabels.containsKey(name)){
                          dupeError = true;
                          throw new Exception();
                      }
                      if(!(label.equals("null"))) {
                          dataLabels.put(name, label);
                      }
                      dataPoints.put(name, point);
                  } catch (Exception e) {
                      errorMessage.setLength(0);
                        if(!dupeError){
                            formatError = true;
                        }
                      errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                      hadAnError.set(true);
                  }
              });
        if (errorMessage.length() > 0) {
            System.out.println(errorMessage.toString());
            throw new Exception(errorMessage.toString());
        }

    }

    public void processDataSet(DataSet dataset){
        formatError = false;
        dupeError = false;
        this.dataPoints = dataset.getLocations();
        this.dataLabels = dataset.getLabels();
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
            //    System.out.println(entry.getKey());
                XYChart.Data<Number, Number> aPoint = new XYChart.Data<>(point.getX(), point.getY());
                aPoint.setNode(new HoverNode(entry.getKey())); //added
                series.getData().add(aPoint);
            });
          //  chart.getData().clear();
            if(firstLine == null){
                firstLine = series;
            }
            chart.getData().add(series);
        }
    }


    void makeLine(XYChart<Number, Number> chart, List<Integer> line){
  //     chart.getData().clear();
        Set<Point2D> points = new HashSet<>(dataPoints.values());
        double currMaxX, currMinX;
        currMaxX = 0;
        currMinX = Integer.MAX_VALUE;
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Algorithm");
        for(Point2D p: points){
            if(p.getX() > currMaxX)
                currMaxX = p.getX();
            if(p.getX() < currMinX)
                currMinX = p.getX();
        }

        int a = line.get(0);
        int b = line.get(1);
        int c = line.get(2);
        double y1;
        double y2;

        if(b == 0){
            y1 = y2 = 1;
        } else {
            y1 = -(c-(a*currMinX))/b;
            y2 = -(c-(a*currMaxX))/b;
        }

        series.getData().add(new XYChart.Data<>(currMinX, y1));
        series.getData().add(new XYChart.Data<>(currMaxX, y2));
        firstLine = series;
        chart.getData().add(series);
        for (XYChart.Data<Number,Number> data : chart.getData().get(chart.getData().size()-1).getData()) {
            StackPane node = (StackPane) data.getNode();
            node.setVisible(false);
        }

        /*
        double avg = sum/count;
        series.getData().add(new XYChart.Data<>(currMinX, avg));
        series.getData().add(new XYChart.Data<>(currMaxX, avg));

        }
        */
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
        dupeError = false;
        formatError = false;
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }

    private class HoverNode extends StackPane {
        private String name;
        private Label nameLabel;

        private HoverNode(String n){
            name = n;

            setOnMouseEntered(event -> {
                nameLabel = new Label();
                nameLabel.setText(name);
                nameLabel.setMinSize(50, 30);

                getChildren().add(nameLabel);
                setCursor(Cursor.CROSSHAIR);
                toFront();
                System.out.println(name);
            });

            setOnMouseExited(event -> {
                getChildren().clear();
                setCursor(Cursor.DEFAULT);
            });
        }

    }


}
