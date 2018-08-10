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

public class ConfigurationDialog extends Stage implements Dialog {

    private static ConfigurationDialog dialog;

    private Label configurationMessage = new Label();
    private TextField iterations, intervals;
    private CheckBox cRun;

    private ConfigurationDialog() { /* empty constructor */ }

    public static ConfigurationDialog getDialog() {
        if (dialog == null)
            dialog = new ConfigurationDialog();
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

        VBox content = new VBox(iterationsBox, intervalsBox, continRunBox);

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

    private void clear() {
        intervals.clear();
        iterations.clear();
        cRun.setSelected(false);
    }

    public Configuration show(String dialogTitle){
        setTitle(dialogTitle);
        clear();
        showAndWait();
        int interval, iter ;
        boolean cVal;
        int[] vals = new int[2];
        List<String> values = Arrays.asList( intervals.getText(),
                iterations.getText());
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

        intervals.setText(""+vals[0]);
        interval = vals[0];
        iterations.setText(vals[1] +"");
        iter = vals[1];
        cVal = cRun.isSelected();

        return new Configuration(iter, interval, cVal);
    }

    public Configuration show(String dialogTitle, Configuration config){
        setTitle(dialogTitle);
        this.iterations.setText("" + config.getMaxIterations());
        this.intervals.setText("" + config.getUpdateIntervals());
        this.cRun.setSelected(config.getContinuousRun());
        showAndWait();

        int interval, iter ;
        boolean cVal;
        int[] vals = new int[2];
        List<String> values = Arrays.asList( intervals.getText(),
                iterations.getText());
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

        intervals.setText(""+vals[0]);
        interval = vals[0];
        iterations.setText(vals[1] +"");
        iter = vals[1];
        cVal = cRun.isSelected();

        config.setUpdateIntervals(interval);
        config.setMaxIterations(iter);
        config.setContinuousRun(cVal);
        return config;
    }

}
