package logic;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public class InstructionDialog {
    
    Stage diaStage;
    private Dialog<?> instruction;
    Button closeBtn = new Button("Close");
    
    public InstructionDialog(){
        instruction = new Dialog<>(); 
        showDialog();
    }
    
    public void showDialog(){
        VBox grandParent = new VBox();
        HBox first       = new HBox();
        HBox second      = new HBox();
        HBox third       = new HBox();
        HBox fourth      = new HBox();
        HBox fifth       = new HBox();
        Text header      = new Text();
        Text moveLoaded  = new Text();
        Text moveBoard   = new Text();
        Text bornRule    = new Text();
        Text surviveRule = new Text();
        Text margin      = new Text();
        instruction.setTitle("GameOfLife - Instructions");
        header.setText("Instructions:");
        header.setFont(Font.font("Verdana",FontWeight.BOLD, 20));
        moveLoaded.setText("Controls after load:\n"
                         + "W    - move up\n"
                         + "A    - move left\n"
                         + "S    - move down\n"
                         + "D    - move right\n"
                         + "E    - rotate\n"
                         + "CTRL - place pattern\n"
                         + "ESC  - end placement.");
        
        moveBoard.setText("\tMove whole board:\n"
                         + "\tU    - move up\n"
                         + "\tH    - move left\n"
                         + "\tJ    - move down\n"
                         + "\tK    - move right.");
        
        bornRule.setText("\nBorn - how\n"
                       + "many neighbours\n"
                       + "a cell must have\n"
                       + "to become alive.");    
        
        surviveRule.setText("\n\t\tSurvives - how\n"
                           + "\t\tmany neighbours\n"
                           + "\t\ta cell must\n"
                           + "\t\thave to survive.");
        first.getChildren().add(header);
        second.getChildren().addAll(moveLoaded, moveBoard);
        third.getChildren().addAll(bornRule, surviveRule);
        fourth.getChildren().add(margin);
        fifth.getChildren().add(closeBtn);
        grandParent.getChildren().addAll(first, second, third, fourth, fifth);
        instruction.getDialogPane().setContent(grandParent);
        diaStage = (Stage) instruction.getDialogPane().getScene().getWindow();
        diaStage.getIcons().add(new Image("file:icon.jpg"));
        diaStage.setOnCloseRequest(e -> closeDialog());
        closeBtn.setOnAction(e -> closeDialog());
        instruction.show();
    }
    
    private void closeDialog(){
        diaStage.close();
    }
}