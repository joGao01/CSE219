package dataprocessors;

import org.junit.Assert;
import org.junit.Test;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AppDataTest {

    private TSDProcessor processor = new TSDProcessor();
    private boolean hasError = false;

    private void loadData(String dataString) {
        // TODO for homework 1
        hasError = false;
     //   PropertyManager manager = applicationTemplate.manager;
        String[] dataArray = dataString.split("\n");
        processor.clear();
        int instances = 0;
        for (int i = 0; i < dataArray.length; i++) {
            int lineNum = i + 1;
            try {
                processor.processString(dataArray[i]);
                instances = lineNum;
            } catch (Exception e) {
                if (processor.hasDupeError()) {
           /*         ErrorDialog dupError = ErrorDialog.getDialog();
                    dupError.show("Error",
                            "There is a duplicate error at: "
                                    + lineNum); */
                    hasError = true;
                } else if (processor.hasFormatError()) {
                /*    ErrorDialog thing = ErrorDialog.getDialog();
                    thing.show("Format Error",
                            "There is a format error at"
                                    + lineNum); */
                    hasError = true;
                }

            }
        }
    //    ((AppUI) applicationTemplate.getUIComponent()).getAlgPane().setVisible(true);
    }

    private void saveData(Path dataFilePath, String dataToBeSaved) {
        loadData(dataToBeSaved);

        if(!hasError) {
            try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
                writer.write(dataToBeSaved);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

    }


  /**Valid Test case for saveData()*/
   @Test
   public void validSaveData(){
       Path test = Paths.get("C:\\Users\\joyce\\Documents\\yanggao\\hw5\\data-vilij\\data-Vilij-Test\\testData\\test.tsd");
       saveData(test, "@a\tLabel\t4,6");
   }



//configDialogVal is acting as user input where the first val is the intervals and the second is the iterations
    private Configuration show(String dialogTitle, List<String> configDialogVal){
    //    setTitle(dialogTitle);
    //    this.iterations.setText("" + config.getMaxIterations());
    //    this.intervals.setText("" + config.getUpdateIntervals());
    //    this.cRun.setSelected(config.getContinuousRun());
    //    showAndWait();

        int interval, iter ;
//        boolean cVal;
        int[] vals = new int[2];
//        List<String> values = configDialogVal;
        AtomicInteger count = new AtomicInteger(0);
        configDialogVal.forEach(i -> {
            try {
                vals[count.intValue()] = Integer.parseInt(i);
                if(vals[count.intValue()] <= 0)
                    throw new Exception();
                count.getAndIncrement();
            } catch (Exception e){
                vals[count.intValue()] = 1;
                count.getAndIncrement();
            }
        });

//        intervals.setText(""+vals[0]);
        interval = vals[0];
  //      iterations.setText(vals[1] +"");
        iter = vals[1];
    //    cVal = cRun.isSelected();

  //      config.setUpdateIntervals(interval);
   //     config.setMaxIterations(iter);
   //     config.setContinuousRun(cVal);
        return new Configuration(iter, interval, true);
    }

    /**Test case where config is empty because common boundary case*/
    @Test
    public void randomClassifierEmptyConfigData() {
        Configuration test = show("Test", Arrays.asList("", ""));
        Assert.assertEquals(1, test.getMaxIterations());
        Assert.assertEquals(1, test.getUpdateIntervals());
    }

    /**Invalid Max Iterations for Random Classfier where a String is entered because common boundary case*/
    @Test
    public void invalidMaxIterations(){
        Configuration test = show("Test", Arrays.asList("2", "saddfg"));
        Assert.assertEquals(1, test.getMaxIterations());
    }

    /**Invalid Update Intervals for Random Classifier where update intervals is a negative integer because common boundary case*/
    @Test
    public void invalidUpdateIntervals(){
        Configuration test = show("Test", Arrays.asList("0","2"));
        Assert.assertEquals(1 ,test.getUpdateIntervals());
    }

    /**Valid Configuration input for comparison to the boundary cases*/
    @Test
    public void validConfigData(){
        Configuration test = show("Test", Arrays.asList("1", "2"));
        Assert.assertEquals(1, test.getUpdateIntervals());
        Assert.assertEquals(2, test.getMaxIterations());
    }


    private Configuration show2(String dialogTitle, List<String> mockInput){
   /*     setTitle(dialogTitle);
        this.iterations.clear();
        this.intervals.clear();
        this.cRun.setSelected(false);
        this.numberOfClusters.clear();
        showAndWait(); */
        int interval, iter , nClusters;
        // interval = iter = nClusters = 0;
        int[] vals = new int[3];
        boolean cVal;

     //   List<String> values = mockInput;
        AtomicInteger count = new AtomicInteger(0);
        mockInput.forEach(str -> {
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

    //    intervals.setText(""+vals[0]);
        interval = vals[0];
    //    iterations.setText(vals[1] +"");
        iter = vals[1];
    //    numberOfClusters.setText(vals[2] + "");
        nClusters  = vals[2];
    //    cVal = cRun.isSelected();


        return new Configuration(iter, interval, true, nClusters);
    }

    /**Empty config is a common boundary test case*/
    @Test
    public void emptyClusterConfig(){
        Configuration test = show2("Test", Arrays.asList("", "", ""));
        Assert.assertEquals(1, test.getMaxIterations());
        Assert.assertEquals(1, test.getUpdateIntervals());
        Assert.assertEquals(2, test.getNumberOfClusters());
    }

    /**Invalid Data to make sure it gracefully degredates to valid input*/
    @Test
    public void invalidMaxIterationCluster(){
        Configuration test = show2("Test", Arrays.asList("2", "0", "3"));
        Assert.assertEquals(1, test.getMaxIterations());
        Assert.assertEquals(2, test.getUpdateIntervals());
        Assert.assertEquals(3, test.getNumberOfClusters());
    }

    /**Test case where a String is entered and the input is gracefully degredated*/
    @Test
    public void invalidUpdateIntervalCluster(){
        Configuration test = show2("Test", Arrays.asList("SOMEBODYONCETOLDME", "2", "3"));
        Assert.assertEquals(1, test.getUpdateIntervals());
        Assert.assertEquals(2, test.getMaxIterations());
        Assert.assertEquals(3, test.getNumberOfClusters());
    }

    /**boundary Test Case where a number larger than 4 is entered for number of clusters and input is gracefully degredated*/
    @Test
    public void invalidNumberOfClusters(){
        Configuration test = show2("Test", Arrays.asList("3", "2", "5"));
        Assert.assertEquals(3, test.getUpdateIntervals());
        Assert.assertEquals(2, test.getMaxIterations());
        Assert.assertEquals(4, test.getNumberOfClusters());
    }

    /**Valid Test Case for comparison to the invalid configs*/
    @Test
    public void validCase(){
        Configuration test = show2("Test", Arrays.asList("2","3","4"));
        Assert.assertEquals(2, test.getUpdateIntervals());
        Assert.assertEquals(3, test.getMaxIterations());
        Assert.assertEquals(4, test.getNumberOfClusters());
    }
}