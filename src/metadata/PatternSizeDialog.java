package metadata;

import controller.PatternEditorController;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Pattern size dialog allows the user to set the sizes of the initPattern in 
 * the PatternEditor class.
 * It sets the width and height values to the PatternEditor instance trough the 
 * PatternEditorController's singelton object.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public final class PatternSizeDialog {
    
    PatternEditorController peCtrl;
    
    private Dialog sizeDialog;
    private VBox grandParent;
    private HBox topParent;
    private HBox midParent;
    private HBox botParent;
    private TextField widthTextField;
    private TextField heightTextField;
    private Label widthLab;
    private Label heightLab;
    private Button sizeBtn;
    
    /**
     * Constructor for the Dialog instance.
     * Setting the peCtrl object to its referance, creates a new Dialog.
     * Initiates createAndInitPatternSizeDialog.
     * @see metadata.PatternSizeDialog#createAndInitPatternSizeDialog() 
     */
    public PatternSizeDialog(){
        peCtrl = PatternEditorController.peInstance;
        sizeDialog = new Dialog();
        createAndInitPatternSizeDialog();
    }
    /**
     * Initiates a dialog that lets the user insert the pattern sizes for a new 
     * board. The dialog contains two fields, width and height, and a trigger
     * button. Triggering chechAndSetSizes(widthTextField, heightTextField).
     * @see metadata.PatternSizeDialog#checkAndSetSizes
     * (javafx.scene.control.TextField, javafx.scene.control.TextField) 
     */
    public void createAndInitPatternSizeDialog(){
        sizeDialog.setTitle("Size dialog");
        sizeDialog.setResizable(false);
        grandParent = new VBox();
        topParent = new HBox();
        midParent = new HBox();
        botParent = new HBox();

        grandParent.getChildren().addAll(topParent, midParent, botParent);
        widthLab = new Label();
        widthLab.setText("Width:  ");
        widthTextField = new TextField();
        widthTextField.setPromptText("Type here..");
        topParent.getChildren().addAll(widthLab, widthTextField);
        heightLab = new Label();
        heightLab.setText("Height: ");
        heightTextField = new TextField();
        heightTextField.setPromptText("Type here..");
        midParent.getChildren().addAll(heightLab, heightTextField);
       
        sizeBtn = new Button();
        sizeBtn.setText("Choose size");
        sizeBtn.setOnMouseClicked(e -> {
            checkAndSetSizes(widthTextField, heightTextField);
            sizeDialog.close();
        });
        botParent.getChildren().addAll(sizeBtn);
        sizeDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        sizeDialog.getDialogPane().setContent(grandParent);
        Stage diaStage = (Stage) sizeDialog.getDialogPane().getScene().getWindow();
        diaStage.getIcons().add(new Image("file:icon.jpg"));

        sizeDialog.showAndWait();
    }
    
    /**
     * Checks if the input sizes is set to legal values. Compared in a simple 
     * matcher. Throwing a NumberFormatException and displaying a alert
     * box to the user if the values were invalid(of non digit characters), 
     * calling patternSizeDialog if catch was initiated.
     * If it was not the sizes will follow the PatternEditorController instance, 
     * and will be as PatternEditor's width and height.
     * @param widthTextField widthTextField a JavaFX Textfield.
     * @param heightTextField heightTextField a JavaFX Textfield.
     */
    public void checkAndSetSizes(TextField widthTextField, TextField heightTextField){
        String widthText = widthTextField.getText();
        String heightText = heightTextField.getText();
        try{
            if(widthText.matches("(\\d+)") && heightText.matches("(\\d+)")){
                peCtrl.getPatternEditor().setWidth(Integer.parseInt(widthText));
                peCtrl.getPatternEditor().setHeight(Integer.parseInt(heightText));
            }else{
                throw new NumberFormatException();
            }
        }catch(NumberFormatException e){
            Alert numbFormExcep = new Alert(Alert.AlertType.ERROR);
            numbFormExcep.setTitle("NumberFormatException");
            numbFormExcep.setHeaderText("Non-digit values found");
            numbFormExcep.setContentText("Please try again");
            
            numbFormExcep.showAndWait();
            
            new PatternSizeDialog();
        }
    }
}
