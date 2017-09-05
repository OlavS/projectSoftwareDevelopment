package logic;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Initiates a PatternFormatException dialog object.
 * Creates a Alert dialog object, that is used to display a PatternFormatException
 * message to the user. The alert type is of type error.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public final class PatternFormatExceptionDialog {
    
    private Alert pfAlert;
    
    /**
     * Constructor creates a instance of Alert, and takes a parameter sendt to 
     * the PatternFormatExceptionDialog(String pfExp) method.
     * @param pfExp is the error message that will be displayed to the user. 
     * @see logic.PatternFormatExceptionDialog
     * #patternFormatExceptionDialog(java.lang.String) 
     */
    public PatternFormatExceptionDialog(String pfExp){
        pfAlert = new Alert(Alert.AlertType.ERROR);
        patternFormatExceptionDialog(pfExp);
    }
    
    /**
     * Sets the content text of the pfAlert object.
     * It also sets the title, head line and puts a icon in the top of the 
     * dialog stage. 
     * @param pfExp the String representing the content text.
     */
    public void patternFormatExceptionDialog(String pfExp){
        pfAlert.setTitle("PatternFormatException");
        pfAlert.setHeaderText("PatternFormatException: Loading/saving source failed");
        pfAlert.setContentText(pfExp);
        Stage diaStage = (Stage) pfAlert.getDialogPane().getScene().getWindow();
        diaStage.getIcons().add(new Image("file:icon.jpg"));
        
        pfAlert.showAndWait();
    }
    
    
    
    
}
