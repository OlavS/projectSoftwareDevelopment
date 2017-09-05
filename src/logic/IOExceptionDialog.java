package logic;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Initiates a ioException dialog object.
 * Creates a Alert dialog object, that is used to display a IOException message 
 * to the user. The alert type is of type error.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public final class IOExceptionDialog {
    
    private Alert ioAlert;
    
    /**
     * Constructor creates a instance of Alert, and takes a parameter sendt to 
     * the ioExceptionDialog(String ioExp) method.
     * @param ioExp is the error message that will be displayed to the user. 
     * @see logic.IOExceptionDialog#ioExceptionDialog(java.lang.String)
     */
    public IOExceptionDialog(String ioExp){
        ioAlert = new Alert(Alert.AlertType.ERROR);
        ioExceptionDialog(ioExp);
    }
    
    /**
     * Sets the content text of the ioAlert object.
     * It also sets the title, head line and puts a icon in the top of the 
     * dialog stage. 
     * @param ioExp the String representing the content text.
     */
    public void ioExceptionDialog(String ioExp){
        ioAlert.setTitle("IOException");
        ioAlert.setHeaderText("IOException: Loading/saving source failed");
        ioAlert.setContentText(ioExp);
        Stage diaStage = (Stage) ioAlert.getDialogPane().getScene().getWindow();
        diaStage.getIcons().add(new Image("file:icon.jpg"));
        ioAlert.showAndWait();
    }
}
