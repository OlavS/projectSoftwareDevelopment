package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Parent root = FXMLLoader.load(getClass().getResource("GoL.fxml"));
        
        Scene scene = new Scene(root);

        stage.setTitle("GameOfLife - created by Øyvind Mjelstad and Olav Sørlie");
        stage.getIcons().add(new Image("file:icon.jpg"));
        
        stage.setScene(scene);  
        
        scene.getRoot().requestFocus();
        stage.show();
    }

    public static void main(String[] args) {
        
        launch(args);
    } 
}
