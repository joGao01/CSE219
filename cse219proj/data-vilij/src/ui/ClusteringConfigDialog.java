package ui;

import dataprocessors.Configuration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vilij.components.Dialog;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClusteringConfigDialog extends Stage implements Dialog{
    private static ClusteringConfigDialog dialog;

    private Label configurationMessage = new Label();
    private TextField iterations, intervals, numberOfClusters;
    private CheckBox cRun;

    private ClusteringConfigDialog() {/* empty constructor */ }

    public static ClusteringConfigDialog getDialog() {
        if (dialog == null)
            dialog = new ClusteringConfigDialog();
        return dialog;
    }

    public void init(Stage owner){
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);

        Label maxIterations = new Label("Max Iterations: ");
        iterations = new TextField();
        HBox iterationsBox = new HBox(maxIterations, iterations);

        Label updateIntervals = new Label("Update Intervals: ");
        intervals = new TextField();
        HBox intervalsBox = new HBox(updateIntervals, intervals);

        Label continuousRun = new Label("Continuous Run ");
        cRun = new CheckBox();
        HBox continRunBox = new HBox(continuousRun, cRun);

        Label nClusters = new Label("Number of Clusters: ");
        numberOfClusters = new TextField();
        HBox clusterBox = new HBox(nClusters, numberOfClusters);

        VBox content = new VBox(iterationsBox, intervalsBox, continRunBox, clusterBox);

        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10, 20, 20, 20));
        content.setSpacing(10);

        this.setScene(new Scene(content));
    }

    private void setConfirmationMessage(String message) {
        this.configurationMessage.setText(message);
    }

    public void show(String dialogTitle, String message) {
        setTitle(dialogTitle);           // set the title of the dialog
        setConfirmationMessage(message); // set the main error message
        showAndWait();                   // open the dialog and wait for the user to click the close button
    }

    public Configuration show(String dialogTitle){
        setTitle(dialogTitle);
        this.iterations.clear();
        this.intervals.clear();
        this.cRun.setSelected(false);
        this.numberOfClusters.clear();
        showAndWait();
        int interval, iter , nClusters;
       // interval = iter = nClusters = 0;
        int[] vals = new int[3];
        boolean cVal;

        List<String> values = Arrays.asList( intervals.getText(),
                                    iterations.getText(),
                                    numberOfClusters.getText());
        AtomicInteger count = new AtomicInteger(0);
        values.forEach(str -> {
            try {
                vals[count.intValue()] = Integer.parseInt(str);
                if(vals[count.intValue()] <= 0)
                    throw new Exception();
                count.getAndIncrement();
            } catch (Exception e){
                vals[count.intValue()] = 1;
                count.getAndIncrement();
            }
        });

        if(vals[2] < 2)
            vals[2] = 2;
        if(vals[2]>4)
            vals[2] = 4;

        intervals.setText(""+vals[0]);
        interval = vals[0];
        iterations.setText(vals[1] +"");
        iter = vals[1];
        numberOfClusters.setText(vals[2] + "");
        nClusters  = vals[2];
        cVal = cRun.isSelected();


        return new Configuration(iter, interval, cVal, nClusters);
    }

    public Configuration show(String dialogTitle, Configuration config){
        setTitle(dialogTitle);
        this.iterations.setText("" + config.getMaxIterations());
        this.intervals.setText("" + config.getUpdateIntervals());
        this.cRun.setSelected(config.getContinuousRun());
        this.numberOfClusters.setText(""+config.getNumberOfClusters());
        showAndWait();

        int interval, iter, numcluster ;
        boolean cVal;
        int[] vals = new int[3];
        List<String> values = Arrays.asList( intervals.getText(),
                iterations.getText(), numberOfClusters.getText());
        AtomicInteger count = new AtomicInteger(0);
        values.forEach(str -> {
            try {
                vals[count.intValue()] = Integer.parseInt(str);
                if(vals[count.intValue()] <= 0)
                    throw new Exception();
                count.getAndIncrement();
            } catch (Exception e){
                vals[count.intValue()] = 1;
                count.getAndIncrement();
            }
        });

        if(vals[2] < 2)
            vals[2] = 2;
        if(vals[2]>4)
             vals[2] = 4;


        intervals.setText(""+vals[0]);
        interval = vals[0];
        iterations.setText(vals[1] +"");
        iter = vals[1];
        cVal = cRun.isSelected();
        numberOfClusters.setText(vals[2] +"");
        numcluster = vals[2];

        config.setUpdateIntervals(interval);
        config.setMaxIterations(iter);
        config.setContinuousRun(cVal);
        config.setNumberOfClusters(numcluster);
        return config;
    }

}
