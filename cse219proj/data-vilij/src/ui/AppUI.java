package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import static vilij.settings.PropertyTypes.*;
import settings.AppPropertyTypes;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;


    @SuppressWarnings("FieldCanBeLocal")
    private VBox                         vb;
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private Button                       editButton;
    private Button                       runButton;
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private TextArea                     textArea;       // text area for new data input
 //   private boolean                      hasNewText;     // whether or not the text area has any new data since last display
//    private String                       prevText;       //previous text in the box
    private HBox                         textDetail;
    private TextArea                     textA2;
    private VBox                         algPane;
    private VBox                         algNumBox;
    private ToggleGroup                  algButtons;
    private Text                         metaData;
    private List<Button>                 typeButtons;
    private List<Button>                 configButtons;
    private List<RadioButton>            radioButts;
    private StackPane                    holdAlgTypeAndSelection;
    private boolean                      running = false;
    private BooleanProperty isRunning = new SimpleBooleanProperty(running);
    private boolean                      isFirstClick = true;
    private String                       runIconPath;

    public List<Button> getConfigButtons(){ return configButtons; }

    public LineChart<Number, Number> getChart() { return chart; }

    public void setRunning(boolean b) { running = b;}

    public TextArea getTextArea() {
        return textArea;
    }

    public List<Button> getTypeButtons(){ return typeButtons; }

    public TextArea getTA2(){
        return textA2;
    }

    public VBox getVbox(){
        return vb;
    }

    public VBox getAlgPane(){ return algPane; }

    public Button getRunButton(){ return runButton; }

    public Text getMetaData(){
        return metaData;
    }

  /*  public Button getSaveButton(){
        return saveButton;
    } */

    public StackPane getHoldAlgTypeAndSelection(){ return holdAlgTypeAndSelection; }

    public void setScrnshotButtonDisable(boolean b){
        scrnshotButton.setDisable(b);
    }

    public Button getEditButton(){ return editButton; }

    public void disableSaveButton(boolean b){
        saveButton.setDisable(b);
    }

    public boolean isFirstClick(){
        return isFirstClick;
    }

    public void setIsFirstClick(boolean b){
        isFirstClick = b;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        // TODO for homework 1
        super.setToolBar(applicationTemplate);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        PropertyManager manager = applicationTemplate.manager;

        newButton.setOnAction(e -> {
            applicationTemplate.getActionComponent().handleNewRequest();

        });
        saveButton.setOnAction(event -> {
            applicationTemplate.getActionComponent().handleSaveRequest();
            saveButton.setDisable(true);
        });
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());

        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshotPath = String.join(SEPARATOR, iconsPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));

        scrnshotButton = setToolbarButton(scrnshotPath, manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()), true);
        toolBar.getItems().add(scrnshotButton);
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            }
            catch (IOException ex){
                ErrorDialog empty = ErrorDialog.getDialog();
                empty.show(manager.getPropertyValue(AppPropertyTypes.ERROR_TITLE.name()),
                            manager.getPropertyValue(AppPropertyTypes.ERROR_MSG.name()));
            }
        });

    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();

    }

    @Override
    public void clear() {
        // TODO for homework 1
        chart.getData().clear();
        textA2.clear();
        textArea.clear();
        scrnshotButton.setDisable(true);
        initialState();
    }


    public String getText(){
        return textArea.getText();
    }

    public void initialState(){
        PropertyManager manager = applicationTemplate.manager;
        newButton.setDisable(false);
        saveButton.setDisable(true);
        editButton.setVisible(true);
        editButton.setText(manager.getPropertyValue(AppPropertyTypes.EDIT_BUTTON_LABEL2.name()));
        editButton.setDisable(false);
        metaData.setText("");
        holdAlgTypeAndSelection.setVisible(false);
        radioButts.clear();
        algButtons.selectToggle(null);
        algNumBox.getChildren().clear();
        algNumBox.setVisible(false);
        textA2.setVisible(false);
        runButton.setVisible(false);
        running = false;
    }

    private void layout() {
        // TODO for homework 1
        PropertyManager manager = applicationTemplate.manager;
        primaryScene.getStylesheets().add(getClass().getResource(manager.getPropertyValue(AppPropertyTypes.STYLESHEET_PATH.name())).toExternalForm());

        HBox hBox = new HBox();
        appPane.getChildren().add(hBox);
        hBox.setPadding(new Insets(20));
        hBox.setSpacing(20);

        vb = new VBox();
        hBox.getChildren().add(vb);
        vb.setPadding(new Insets(30, 0,0,0));
        vb.setSpacing(20);

        Label newLabel = new Label(manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name()));
        vb.getChildren().add(newLabel);

        textArea = new TextArea();
        textArea.setDisable(true);
        textArea.setId(manager.getPropertyValue(AppPropertyTypes.TEXTAREA_ID.name())); //for CSS
        textArea.setMaxSize(300, 175);
        vb.getChildren().add(textArea);

        //chart things
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new LineChart<>(xAxis,yAxis);
        chart.setId(manager.getPropertyValue(AppPropertyTypes.CHART_ID.name())); //for CSS
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_LABEL.name()));
        chart.setMaxSize(600, 400);
        chart.setAnimated(false);
        hBox.getChildren().add(chart);
        chart.visibleProperty().bind(vb.visibleProperty());

        //Edit Button things
        editButton = new Button(manager.getPropertyValue(AppPropertyTypes.EDIT_BUTTON_LABEL1.name()));
        vb.getChildren().add(editButton);
        editButton.managedProperty().bind(editButton.visibleProperty());

        //metadata things
        metaData = new Text();
        metaData.setWrappingWidth(200);
        textDetail = new HBox(metaData);
        vb.getChildren().add(textDetail);

        holdAlgTypeAndSelection = new StackPane();
        vb.getChildren().add(holdAlgTypeAndSelection);
        holdAlgTypeAndSelection.managedProperty().bind(holdAlgTypeAndSelection.visibleProperty());

        algPane = new VBox();
        algNumBox = new VBox();
        holdAlgTypeAndSelection.getChildren().add(algPane);
        holdAlgTypeAndSelection.getChildren().add(algNumBox);
        algPane.managedProperty().bind(algPane.visibleProperty());

        typeButtons = Arrays.asList(new Button(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_LABEL.name())),
                                    new Button(manager.getPropertyValue(AppPropertyTypes.CLUSTERING_LABEL.name())));

        typeButtons.forEach(button -> algPane.getChildren().add(button));



        radioButts = new ArrayList<>();

        algButtons = new ToggleGroup();
        configButtons = new ArrayList<>();

        String iconPath= SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
     //   String scrnshotPath = String.join(SEPARATOR, iconPath,
     //           manager.getPropertyValue(AppPropertyTypes.CONFIG_ICON.name()));
    /*    configButtons = Arrays.asList(new Button("1",new ImageView(new Image(getClass().getResourceAsStream(scrnshotPath)))),
                                        new Button("2",new ImageView(new Image(getClass().getResourceAsStream(scrnshotPath)))),
                                        new Button("3",new ImageView(new Image(getClass().getResourceAsStream(scrnshotPath))))); */



   /*     List<HBox> buttAndConfig = Arrays.asList(new HBox(), new HBox(), new HBox());
        AtomicInteger i = new AtomicInteger(0);
        buttAndConfig.forEach(hB -> {
            algNumBox.getChildren().add(hB);
            hB.setSpacing(5);
            hB.getChildren().addAll(radioButts.get(i.intValue()), configButtons.get(i.intValue()));
            i.getAndIncrement();
        }); */


        textA2 = new TextArea();
        vb.getChildren().add(textA2);
        textA2.setMaxSize(300, 200);
        textA2.setId(manager.getPropertyValue(AppPropertyTypes.TEXTAREA2_ID.name()));
        textA2.managedProperty().bind(textA2.visibleProperty());


        runIconPath = String.join(SEPARATOR, iconPath,
                manager.getPropertyValue(AppPropertyTypes.RUN_ICON.name()));
        runButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream(runIconPath))));
        vb.getChildren().add(runButton);
        runButton.managedProperty().bind(runButton.visibleProperty());

        vb.setVisible(false);
        editButton.setText("Done");

        initialState();
    }

    private void setWorkspaceActions() {

        PropertyManager manager = applicationTemplate.manager;

        // TODO for homework 1
     //   prevText = "";
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!textArea.getText().isEmpty()) {
                saveButton.setDisable(false);
            } else {
                saveButton.setDisable(true);
            }

            textArea.setText(textArea.getText().replace("\\n\n", "\n"));
            String[] temp = textArea.getText().split("\n");

            if(temp.length < 10){
                if(!textA2.getText().isEmpty() && !textArea.getText().isEmpty()){
                    String s = textArea.getText() + textA2.getText();
                    String[] args = s.split("\n");
                    textA2.clear();
                    textArea.clear();
                    if(args.length > 10) {
                        for (int i = 0; i < 10; i++) {
                            textArea.appendText(args[i] + "\n");
                        }
                        if (args.length > 10) {
                            for (int i = 10; i < args.length; i++)
                                textA2.appendText(args[i] + "\n");
                        }
                    }
                }
            }

        });

        editButton.setOnAction(event -> {

            if (editButton.getText().equals(manager.getPropertyValue(AppPropertyTypes.EDIT_BUTTON_LABEL1.name()))) {
                editButton.setText(manager.getPropertyValue(AppPropertyTypes.EDIT_BUTTON_LABEL2.name()));
                textArea.setDisable(false);
                holdAlgTypeAndSelection.setVisible(false);
                runButton.setVisible(false);
            } else { //when the button says Done
                try {
                    ((AppActions) applicationTemplate.getActionComponent()).handleEditRequest();
                } catch (IOException e){
                    return;
                }
                editButton.setText(manager.getPropertyValue(AppPropertyTypes.EDIT_BUTTON_LABEL1.name()));
                textArea.setDisable(true);
                getHoldAlgTypeAndSelection().setVisible(true);
                algNumBox.setVisible(false);
            }
        });

        runButton.setOnAction(event -> {
            AppData appData = ((AppData)applicationTemplate.getDataComponent());
            appData.setPressed(true);
            if(!running){
                //runButton.setDisable(true);
                isFirstClick = true;
             //   configButtons.forEach(button -> button.setDisable(true));
                ((AppActions) applicationTemplate.getActionComponent()).handleRunRequest();
                appData.setPressed(false);
            } else {
            //    isFirstClick = false;
                ((AppData)applicationTemplate.getDataComponent()).runData();
            }

/*            if(!running){
                runButton.setDisable(false);
            } */

        });

        isRunning.addListener((observable, oldValue, newValue) -> {
            if(running){
                runButton.setDisable(true);
            } else {
                runButton.setDisable(false);
            }
        });

        typeButtons.forEach(button -> button.setOnAction(event -> {
            String alg = ((Button) event.getSource()).getText();
            if (alg.equals(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_LABEL.name()))) {
                ((AppActions) applicationTemplate.getActionComponent()).handleClassifAlgSelectionRequest();
                makeRadioButtons("data-vilij/src/Classifiers");
            } else if (alg.equals(manager.getPropertyValue(AppPropertyTypes.CLUSTERING_LABEL.name()))) {
                ((AppActions) applicationTemplate.getActionComponent()).handleClustAlgSelectionRequest();
                makeRadioButtons("data-vilij/src/Clustering");
            }

            algPane.setVisible(false);
            algNumBox.setVisible(true);
        }));

    }

    private void makeRadioButtons(String fileName){
        PropertyManager manager = applicationTemplate.manager;
        File alg = new File(fileName);
        File[] listOfAlgs = alg.listFiles();
   //     System.out.println(Arrays.toString(listOfAlgs));
        List<RadioButton> radioButtList = new ArrayList<>();
        radioButtList.clear();
        if(listOfAlgs!=null) {
            for(File f: listOfAlgs){
                radioButtList.add(new RadioButton(f.getName().replace(".java", "")));
            }
        }

        String iconPath= SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshotPath = String.join(SEPARATOR, iconPath,
                manager.getPropertyValue(AppPropertyTypes.CONFIG_ICON.name()));

        List<HBox> hboxList = new ArrayList<>();
        List<Button> configButtList = new ArrayList<>();
        radioButtList.forEach(b -> {
            hboxList.add(new HBox());
            configButtList.add(new Button("Config", new ImageView(new Image(getClass().getResourceAsStream(scrnshotPath)))));
        });
        radioButts = radioButtList;
        radioButts.forEach(button -> button.setToggleGroup(algButtons));
        configButtons = configButtList;
        AtomicInteger i = new AtomicInteger(0);
        hboxList.forEach(h -> {
            algNumBox.getChildren().add(h);
            h.setSpacing(5);
            h.getChildren().addAll(radioButts.get(i.intValue()), configButtons.get(i.intValue()));
            i.getAndIncrement();
        });

//
        radioButts.forEach(button -> button.setOnAction(event -> {
            String algName = button.getText();
            System.out.println(algName);
            ((AppActions)applicationTemplate.getActionComponent()).handleAlgNumSelectionRequest(algName);
            running = false;
        }));

        configButtons.forEach(button -> button.setOnAction(e -> {
            ((AppActions) applicationTemplate.getActionComponent()).handleConfigRequest();
        }));

        algNumBox.managedProperty().bind(algNumBox.visibleProperty());
    }

}