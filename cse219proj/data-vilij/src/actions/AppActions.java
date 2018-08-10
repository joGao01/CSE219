package actions;

import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import vilij.settings.PropertyTypes;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;


/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;
    /** Path to the data file currently active. */
    private Path dataFilePath;
    private SimpleBooleanProperty isUnsaved;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        isUnsaved = new SimpleBooleanProperty(false);
    }


    @Override
    public void handleNewRequest() {
      try {
            if(isUnsaved.get() || promptToSave()){
            //    promptToSave();
                applicationTemplate.getUIComponent().clear();
                ((AppUI)applicationTemplate.getUIComponent()).initialState();
                isUnsaved.set(false);
                dataFilePath = null;
               // ((AppData)applicationTemplate.getDataComponent()).setHasError(false);
            }
            //bring back algtype choices
            ((AppUI)applicationTemplate.getUIComponent()).getVbox().setVisible(true);
            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(false);
        } catch (IOException e){
            PropertyManager manager = applicationTemplate.manager;
            ErrorDialog saveError = ErrorDialog.getDialog();
            saveError.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                            manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name()));
        }

    }

    @Override
    public void handleSaveRequest() {
        save();
    }

    public void handleEditRequest() throws IOException{
        PropertyManager manager = applicationTemplate.manager;
        String textGiven = ((AppUI)applicationTemplate.getUIComponent()).getText();

        if(textGiven.isEmpty()){
            ErrorDialog empty = ErrorDialog.getDialog();
            empty.show(manager.getPropertyValue(AppPropertyTypes.NO_DATA_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.NO_DATA_MSG.name()));
            throw new IOException();
        } else {
            ((AppData) applicationTemplate.getDataComponent()).editData(textGiven);
        }
    }

    @Override
    public void handleLoadRequest() {
        PropertyManager manager = applicationTemplate.manager;
        Path dataFile = Paths.get(manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name()));

        FileChooser load = new FileChooser();
        FileChooser.ExtensionFilter limitExtension = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name()),
                manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()));
        try {
            load.setInitialDirectory(dataFile.toFile());
            load.getExtensionFilters().add(limitExtension);
        }catch(Exception e){
            ErrorDialog someError = ErrorDialog.getDialog();
            someError.show(manager.getPropertyValue(AppPropertyTypes.ERROR_TITLE.name()),
                            manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));
        }
        File toOpen = load.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        if(toOpen != null) {
            (applicationTemplate.getUIComponent()).clear();
            applicationTemplate.getDataComponent().loadData(toOpen.toPath());
            boolean error = ((AppData)(applicationTemplate.getDataComponent())).thingHasError();
        //    error = false;
            if(!error) {
                ((AppUI) applicationTemplate.getUIComponent()).getVbox().setVisible(true);
                ((AppUI)applicationTemplate.getUIComponent()).getHoldAlgTypeAndSelection().setVisible(true);
                ((AppUI)applicationTemplate.getUIComponent()).getEditButton().setVisible(false);
                dataFilePath = toOpen.toPath();
            }
        }

    }

    public void handleRunRequest(){
        ((AppData)applicationTemplate.getDataComponent()).algorithmCreation(dataFilePath);
        ((AppData)applicationTemplate.getDataComponent()).runData();
    }

    public void handleClassifAlgSelectionRequest(){
        PropertyManager manager = applicationTemplate.manager;
        String alg = manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_LABEL.name());
        ((AppData)applicationTemplate.getDataComponent()).setAlgorithmType(alg);

    }

    public void handleClustAlgSelectionRequest(){
        PropertyManager manager = applicationTemplate.manager;
        String alg = manager.getPropertyValue(AppPropertyTypes.CLUSTERING_LABEL.name());
        ((AppData)applicationTemplate.getDataComponent()).setAlgorithmType(alg);


    }

    public void handleAlgNumSelectionRequest(String name){
        ((AppData)applicationTemplate.getDataComponent()).algorithmName(name);
    }

    public void handleConfigRequest(){
        ((AppData)applicationTemplate.getDataComponent()).configData();
    }

    @Override
    public void handleExitRequest() {
        try {
            if(isUnsaved.get() || promptToSave()) {
                Platform.exit();
            } else if (!isUnsaved.get()){
                Platform.exit();
            } else{
                return;
            }
     } catch (IOException e){
            PropertyManager manager = applicationTemplate.manager;
            ErrorDialog saveError = ErrorDialog.getDialog();
            saveError.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                    manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name()));
        }

    }

    @Override
    public void handlePrintRequest() {
        //nothing to see here
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: FIX
        PropertyManager manager = applicationTemplate.manager;
        FileChooser scrnshot = new FileChooser();
        Path initDir = Paths.get(manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name()));
        scrnshot.setInitialDirectory(initDir.toFile());

        FileChooser.ExtensionFilter limitToImages = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.SCRNSHOT_EXT_DESC.name()),
                                                                                        manager.getPropertyValue(AppPropertyTypes.SCRNSHOT_EXT_TO_SHOW.name()));
  //      scrnshot.setSelectedExtensionFilter(limitToImages);
        scrnshot.getExtensionFilters().add(limitToImages);

        File fc = scrnshot.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        WritableImage thing = ((AppUI)applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(),
                                                                                                null);
        if(fc != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(thing, null),
                        manager.getPropertyValue(AppPropertyTypes.SCRNSHOT_EXT.name()), fc);
            } catch (IOException e) {
                ErrorDialog saveError = ErrorDialog.getDialog();
                saveError.show(manager.getPropertyValue(AppPropertyTypes.SAVE_ERROR_TITLE.name()),
                            manager.getPropertyValue(AppPropertyTypes.SAVE_ERROR_MSG.name()));
            }
        }

    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException  {
        if(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText().isEmpty()) return false;

        Dialog confirm = ConfirmationDialog.getDialog();
        PropertyManager manager = applicationTemplate.manager;
        confirm.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                     manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if(((ConfirmationDialog)confirm).getSelectedOption() == null){
            ((ConfirmationDialog)confirm).close();
            return false;
        }
        if (((ConfirmationDialog)confirm).getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            return save();
        } else if (((ConfirmationDialog)confirm).getSelectedOption().equals(ConfirmationDialog.Option.NO)) {
            applicationTemplate.getUIComponent().clear();
            return true;
        } else {
            ((ConfirmationDialog)confirm).close();
            return false;
        }


    }

    private boolean save() {

        ((AppData) (applicationTemplate.getDataComponent())).loadData(((AppUI) (applicationTemplate.getUIComponent())).getText() + "\n"
                                                                        +((AppUI)(applicationTemplate.getUIComponent())).getTA2().getText());
        boolean error = ((AppData) (applicationTemplate.getDataComponent())).thingHasError();
        if (!error) {
            if (dataFilePath == null) {
                PropertyManager manager = applicationTemplate.manager;
                FileChooser save = new FileChooser();
                FileChooser.ExtensionFilter limitExtension = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name()),
                        manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()));
                save.setSelectedExtensionFilter(limitExtension);
                save.getExtensionFilters().addAll(limitExtension);
                save.setTitle(manager.getPropertyValue(PropertyTypes.SAVE_WORK_TITLE.name()));


                Path dataFile = Paths.get(manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name()));
                save.setInitialDirectory(dataFile.toFile());

                save.setInitialFileName(manager.getPropertyValue(AppPropertyTypes.INITIAL_FILE_NAME.name()));

                File temp = save.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

                if (temp != null) {
                    dataFilePath = temp.toPath();
                } else {
                    return false;
                }
            }

          //  if (dataFilePath != null) {
            applicationTemplate.getDataComponent().saveData(dataFilePath);
            isUnsaved.set(false);
            return true;
           // }
        }
        return false;
    }

}
