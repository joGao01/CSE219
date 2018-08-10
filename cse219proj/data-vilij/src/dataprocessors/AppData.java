package dataprocessors;

import algorithms.*;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import settings.AppPropertyTypes;
import ui.AppUI;
import ui.ClusteringConfigDialog;
import ui.ConfigurationDialog;
import vilij.components.DataComponent;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    private boolean             hasError = false;
    private String              chosenAlgType;
    private Algorithms          chosenAlg;
    private String              algName;
    private Configuration       config;
    private int currentIteration = 1;
    private HashMap<String, Configuration> ClassificationConfigData = new HashMap<>();
    private HashMap<String, Configuration> ClusteringConfigData = new HashMap<>();
    private Drop drop = new Drop(true);
    private boolean pressed;

    public void setPressed(boolean b) { pressed = b; }


    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

   /* public List<Integer> getDrop(){
        return drop;
    }*/

    public boolean thingHasError(){ return hasError;}

    private Scanner getInput(String fileName){
        Scanner thing = null;
        PropertyManager manager = applicationTemplate.manager;

        try{
            File newFile = new File(fileName);
            thing = new Scanner(newFile) ;
        } catch(Exception e){
            ErrorDialog unableToLoad = ErrorDialog.getDialog();
            unableToLoad.show(manager.getPropertyValue(AppPropertyTypes.ERROR_TITLE.name()),
                                manager.getPropertyValue(AppPropertyTypes.ERROR_MSG.name()));
        }
        return thing;
    }


    @Override
    public void loadData(Path dataFilePath) {
        hasError = false;//clears error boolean
        Scanner input = getInput(dataFilePath.toString());
        StringBuilder data = new StringBuilder();

        while (input.hasNext()) {
            String line = input.nextLine();
            data.append(line);
            data.append("\n");
        }

        String dataStr = data.toString();

        loadData(dataStr);


        if(!hasError) {
        //    processor.clear();
            ((AppUI)applicationTemplate.getUIComponent()).initialState();
      //      ((AppUI)applicationTemplate.getUIComponent()).disableSaveButton(true);
            String[] dataArray = dataStr.split("\n");
            int instances = dataArray.length;
            if (instances > 10) {
                for (int i = 0; i < 10; i++) {
                    ((AppUI) (applicationTemplate.getUIComponent())).getTextArea().appendText(dataArray[i] + "\n");
                }
                for (int i = 10; i < dataArray.length; i++) {
                    ((AppUI) (applicationTemplate.getUIComponent())).getTA2().appendText(dataArray[i] + "\n");
                }

            } else {
                for (String s : dataArray) {
                    ((AppUI) applicationTemplate.getUIComponent()).getTextArea().appendText(s + "\n");
                }
            }
            //update the metadata
            HashMap<String, String> labelNames = (HashMap<String, String>)(processor.getDataLabels());
            generateMetadata(instances, labelNames, dataFilePath);
            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(true);
            ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
            ((AppUI)applicationTemplate.getUIComponent()).disableSaveButton(true);
        }
    }

    public void loadData(String dataString) {
        // TODO for homework 1
        hasError = false;
        PropertyManager manager = applicationTemplate.manager;
        String [] dataArray = dataString.split("\n");
        processor.clear();
        int instances = 0;
        for(int i = 0; i < dataArray.length; i++){
            int lineNum = i+1;
            try{
                processor.processString(dataArray[i]);
                instances = lineNum;
            } catch (Exception e){
                if(processor.hasDupeError()){
                    ErrorDialog dupError = ErrorDialog.getDialog();
                    dupError.show(manager.getPropertyValue(AppPropertyTypes.DUPLICATE_ERROR_TITLE.name()),
                            manager.getPropertyValue(AppPropertyTypes.DUPLICATE_ERROR_MSG.name())
                                    + lineNum);
                    hasError = true;
                    return;
                } else if(processor.hasFormatError()) {
                    ErrorDialog thing = ErrorDialog.getDialog();
                    thing.show(manager.getPropertyValue(AppPropertyTypes.FORMAT_ERROR_TITLE.name()),
                            manager.getPropertyValue(AppPropertyTypes.FORMAT_ERROR_MSG.name())
                                    + lineNum);
                    hasError = true;
                    return;
                }
            }
        }
        ((AppUI)applicationTemplate.getUIComponent()).getAlgPane().setVisible(true);

        HashMap<String, String> labelNames = (HashMap<String, String>)(processor.getDataLabels());
        generateMetadata(instances, labelNames);
        System.out.println("Number Of Labels: " + labelNames.size());
        Set<String> names = new HashSet<>();
        labelNames.keySet().forEach(key -> names.add(labelNames.get(key)));

        if(names.size() != 2){
            ((AppUI)applicationTemplate.getUIComponent()).getTypeButtons().get(0).setDisable(true);
        } else {
            ((AppUI)applicationTemplate.getUIComponent()).getTypeButtons().get(0).setDisable(false);
        }
    }

    private void generateMetadata(int instances, HashMap<String, String> labelNames, Path source){
        PropertyManager manager = applicationTemplate.manager;
        StringBuilder mData = new StringBuilder();
        mData.append(manager.getPropertyValue(AppPropertyTypes.THERE_ARE.name())).append(instances).append(" instances.\n");

        List<String> keys = new ArrayList<>(labelNames.keySet());
        Set<String> names = new HashSet<>();
        keys.forEach(key -> names.add(labelNames.get(key)));
        mData.append(manager.getPropertyValue(AppPropertyTypes.THERE_ARE.name())).append(names.size()).append(" labels: \n");
        names.forEach(name -> mData.append(name).append("\n"));
        mData.append(source.toString());

        ((AppUI)applicationTemplate.getUIComponent()).getMetaData().setText(mData.toString());
    }

    private void generateMetadata(int instances, HashMap<String, String> labelNames){
        StringBuilder mData = new StringBuilder();
        mData.append("There are ").append(instances).append(" instances.\n");
        List<String> keys = new ArrayList<>(labelNames.keySet());
        Set<String> names = new HashSet<>();
        keys.forEach(key -> names.add(labelNames.get(key)));
        mData.append("There are ").append(names.size()).append(" labels: \n");
        names.forEach(name -> mData.append(name).append("\n"));
        ((AppUI)applicationTemplate.getUIComponent()).getMetaData().setText(mData.toString());
    }

    public void editData(String tsd) throws IOException{
        loadData(tsd);
        if(hasError)
            throw new IOException();
    }

    @Override
    public void saveData(Path dataFilePath) {
        // TODO: NOT A PART OF HW 1
        String thing = ((AppUI) applicationTemplate.getUIComponent()).getText();
        loadData(thing);

        if(!hasError) {
            try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
                writer.write(thing);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

    }

    public void setAlgorithmType(String algType){
        chosenAlgType = algType;
    }

    public void algorithmName(String name){
        PropertyManager manager = applicationTemplate.manager;
        algName = name;
        ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setVisible(true);
        if(chosenAlgType.equals(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_LABEL.name()))){
            if(ClassificationConfigData.get(name)==null)
                ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
            else
                ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
        } else {
            if(ClusteringConfigData.get(name)==null)
                ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
            else
                ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
        }


    }

    //there is currently no connection between the algorithm and the configs
    public void configData(){
        PropertyManager manager = applicationTemplate.manager;

        if(chosenAlgType.equals(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_LABEL.name()))){
            ConfigurationDialog configWindow = ConfigurationDialog.getDialog();
            if(ClassificationConfigData.get(algName) == null){
                config = configWindow.show("Configure Algorithm");
            } else {
                config = configWindow.show("Configure Algorithm", ClassificationConfigData.get(algName)); //alters config accordingly
            }
            ClassificationConfigData.put(algName, config);
        } else {

            ClusteringConfigDialog clusterConfigWindow = ClusteringConfigDialog.getDialog();
            if(ClusteringConfigData.get(algName)==null){
                config = clusterConfigWindow.show("Configure Algorithm");
            } else {
                config = clusterConfigWindow.show("Configure Algorithm", ClusteringConfigData.get(algName));
            }
            ClusteringConfigData.put(algName, config);
        }

        if(chosenAlgType.equals(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_LABEL.name()))){
            if(ClassificationConfigData.get(algName) != null){
                ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
            }
        } else if(chosenAlgType.equals(manager.getPropertyValue(AppPropertyTypes.CLUSTERING_LABEL.name()))){
            if(ClusteringConfigData.get(algName) != null){
                ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
            }
        }


    }

    public void algorithmCreation(Path dataFilePath) {
        //       drop.setPressed(true);
        ((AppUI) applicationTemplate.getUIComponent()).setRunning(true);
        PropertyManager manager = applicationTemplate.manager;

        DataSet points;
        if (dataFilePath != null) {
            try {
                points = DataSet.fromTSDFile(dataFilePath);
            } catch (IOException e) {
                return;
            }
        } else {
            points = new DataSet();
        }

        if (chosenAlgType.equals(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_LABEL.name()))) {
            try {
                Class classifierClass = Class.forName("Classifiers." + algName);
                Constructor classCon = classifierClass.getConstructors()[0];
                Object alg = classCon.newInstance(points, ClassificationConfigData.get(algName), applicationTemplate, drop);
                if(alg instanceof Classifier)
                    chosenAlg = (Classifier)alg; //running class, then clust -> eternal classification alg
            } catch (ClassNotFoundException | IllegalAccessException |InstantiationException |InvocationTargetException e){
                ErrorDialog error = ErrorDialog.getDialog();
                error.show();
            }
        } else {
            try {
                Class clusterClass = Class.forName("Clustering." + algName);
                System.out.println(algName);
                Constructor classCon = clusterClass.getConstructors()[0];
                Object alg = classCon.newInstance(points, ClusteringConfigData.get(algName), applicationTemplate, drop);
                if(alg instanceof Clusterer)
                    chosenAlg = (Clusterer)alg;
            } catch (ClassNotFoundException | IllegalAccessException |InstantiationException |InvocationTargetException e){
                ErrorDialog error = ErrorDialog.getDialog();
            }
        }

        System.out.println("finished algorithm creation: " + algName);
    }



    public void runData(){
        int iterations;

        if (chosenAlg.getMaxIterations() % chosenAlg.getUpdateInterval() == 0) {
            iterations = chosenAlg.getMaxIterations() / chosenAlg.getUpdateInterval();
        } else {
            iterations = chosenAlg.getMaxIterations() / chosenAlg.getUpdateInterval() + 1;
        }

        if (chosenAlg.tocontinue()) {
            ((AppUI)applicationTemplate.getUIComponent()).setRunning(true);
            new Thread(chosenAlg).start();
            ((AppUI)applicationTemplate.getUIComponent()).setRunning(false);
          //  ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
            ((AppUI)applicationTemplate.getUIComponent()).setScrnshotButtonDisable(true);
            ((AppUI) applicationTemplate.getUIComponent()).getConfigButtons().forEach(button -> button.setDisable(false));
        } else {//nonContinuous
            if ((((AppUI) applicationTemplate.getUIComponent()).isFirstClick())) {//starting alg
                drop.setPressed(true);
                new Thread(chosenAlg).start();
                ((AppUI)applicationTemplate.getUIComponent()).setRunning(true);
             //   ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
                ((AppUI) applicationTemplate.getUIComponent()).setIsFirstClick(false);
                currentIteration++;
            } else { //continuing alg
                drop.put();
                currentIteration++;
                if(currentIteration > iterations){//end of iterations
                    currentIteration = 1;
                  //  new Thread(chosenAlg).start();
                    ((AppUI) applicationTemplate.getUIComponent()).setIsFirstClick(true);
                 //   ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getConfigButtons().forEach(button -> button.setDisable(false));
                } else {//middle of iterations
                    ((AppUI) applicationTemplate.getUIComponent()).setRunning(true);
                  //  ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
                }
            }

     /*       currentIteration = 1;
           // List<List<Integer>> outputs = new ArrayList<>();
            if(chosenAlg instanceof RandomClassifier){
                outputs = ((RandomClassifier) chosenAlg).outputList();
                if(currentIteration < iterations){
                    displayData(outputs.get(currentIteration));
                    currentIteration++;
                } else {
                    currentIteration = 1;
                    System.out.println("end");
                }
            } */
        }

    }

    @Override
    public void clear() {
        processor.clear();
    }

    public synchronized void displayData(List<Integer> line) {

        System.out.println("displaying");
        if (!processor.hasDupeError() && !processor.hasFormatError()) {
            ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
            processor.makeLine(((AppUI) applicationTemplate.getUIComponent()).getChart(), line);
            processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
        //    notifyAll();
            formatChart();
        //    ((AppUI) (applicationTemplate.getUIComponent())).setScrnshotButtonDisable();
        }

    }

    public synchronized void displayClusterData(DataSet points) {
    //    System.out.println("Displaying Data");
        processor.processDataSet(points);
        if (!processor.hasDupeError() && !processor.hasFormatError()) {
            ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
            processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
            formatChart();
        //    ((AppUI) (applicationTemplate.getUIComponent())).setScrnshotButton(true);

        }


    }

    private void formatChart(){
        PropertyManager manager = applicationTemplate.manager;
        XYChart.Series<Number, Number> series = processor.getFirstLine();
        Node chartCSS = series.getNode().lookup(manager.getPropertyValue(AppPropertyTypes.CHART_CSS.name()));
        Node chartFirstLine = series.getNode().lookup(manager.getPropertyValue(AppPropertyTypes.CHART_FIRST_LINE.name()));
        if(chosenAlg instanceof Classifier){
            chartFirstLine.setStyle("-fx-stroke: blue;");
        } else {
            chartCSS.setStyle("-fx-stroke: transparent;");
        }
    }

/*    private class Producer implements Runnable{
        Drop drop;

        public Producer(Drop drop){
            this.drop = drop;
        }

        public void run(){
            drop.put();

            try{
                Thread.sleep(1000);
            } catch (InterruptedException  e){
                //something
            }
        }
    } */
}
