package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.LoadPattern;
import logic.PatternFormatException;
import metadata.PatternEditor;
import metadata.SaveGIF;
import metadata.SavePattern;

/**
 * Controls the PatternEditor/Metadata addon. 
 * Containing all the FXML elements of the pattern editor. The pattern editors
 * purpos is to let the user have the option to save pattern/RLE files, and GIF
 * files. The user can also manipulate the pattern he/she wants to save. 
 * Pattern editor also contains a "strip" (future generation graphic) where
 * the user can see how the created pattern will evolve.  
 * @author Olav Sørlie And Øyvind Mjelstad
 */
public class PatternEditorController implements Initializable{
       
    @FXML private BorderPane editorGrandParent;
    @FXML private ScrollPane genPane;
    @FXML private Canvas editorCanvas;
    @FXML private Canvas genCanvas;
    
    @FXML private TextField survives;
    @FXML private TextField born;
    @FXML private TextField creator;
    @FXML private TextField name;
    @FXML private TextField description;
    
    @FXML private ColorPicker liveCellColor;
    @FXML private Slider zoomSlider;
    
    @FXML private Button closeBtn;
    @FXML private Button loadFileBtn;
    @FXML private Button loadURLBtn;

    private Dialog saveDialog;
    private Slider gifSpeed;
    private Slider gifCellSize;
    private TextField frames;
    
    public static PatternEditorController instance;
    
    private GoLController ctrl;
    private PatternEditor pe;
    private GraphicsContext gc;
    public static PatternEditorController peInstance;
    
    /**
    * Initialize for the PatternEditor.
    * Creates a singelton object of this instance of PatternEditorController, 
    * and the canvas's graphicsContext2d is attached to a gc referance of the 
    * same type. A PatternEditor object is created, with the gc object of the 
    * graphicsContext2d.
    * AutoAdjust methods is initiated to make sure that the canvas objects, both 
    * gen/strip canvas is always follows the size of their designated position
    * in the BorderPane(editorGrandParent) height and width property.
    * @param location The location used to resolve relative paths for the root 
    * object, or <tt>null</tt> if the location is not known..
    * @param resources The resources used to localize the root object, or 
    * <tt>null</tt> if the root object was not localized.
    */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        peInstance = this;
        ctrl = GoLController.instance;
        gc = editorCanvas.getGraphicsContext2D();
        pe = new PatternEditor(gc);
        autoAdjustScrollPane();
        autoAdjustCanvas();
        pe.initLogic();
        setToolTip();
    }
    
    /**
     * Collection of ToolTip setters.
     * Initiates tooltip setters to help the user to understand the 
     * GUI functionalities.
     * Initiating (@link #setRulesTip() setRulesTip) method, and
     * (@link #setLoadBtnsTip() setLoadBtnsTip).
     */
    public void setToolTip(){
        setRulesetTip();
        setLoadBtnsTip();
    }
    
    /**
     * Creates a tooltip for the born and survives textfields. 
     * Making the GUI easier to understand for the user. Giving information
     * about what the textfields is for.
     */
    public void setRulesetTip(){
        final Tooltip bornTip = new Tooltip();
        bornTip.setText("Born - how\n"
                      + "many neighbours\n"
                      + "a cell must have\n"
                      + "to get born.");
 
        final Tooltip survivesTip = new Tooltip();
        survivesTip.setText("Survives - how\n"
                          + "many neighbours\n"
                          + "a cell must\n"
                          + "have to survive.");
        
        born.setTooltip(bornTip);
        survives.setTooltip(survivesTip);
    }
         
    /**
     * Creates a tooltip for the load buttons.
     * Describing how to place a pattern to the board, 
     * when hovering over the button for a couple of secounds.
     */
    public void setLoadBtnsTip(){
        final Tooltip loadTip = new Tooltip();
        loadTip.setText("Controls after load:\n"
                        + "W    - move up\n"
                        + "A    - move left\n"
                        + "S    - move down\n"
                        + "D    - move right\n"
                        + "E    - rotate\n"
                        + "CTRL - place pattern\n"
                        + "ESC  - end placement.");

        loadFileBtn.setTooltip(loadTip);
        loadURLBtn.setTooltip(loadTip);
    }
    
    /**
     * Closing the PatternEditor by a button click.
     * Contains stage.close(), makeing sure that the editor is closed properly.
     */
    public void closeBtnClicked(){
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Setting the ruleset to the current object of the StaticBoard instance.
     * The user can type in the ruleset he/she wants to use, and use the 
     * confirm button to attach it to the pattern editor sequence/StaticBoard
     * clone.
     */
    public void setRulesBtnClicked(){
        pe.getStaticBoard().getNextGeneration().
           decodeRuleset(born.getText(), survives.getText());
        
        pe.draw();
    }
    
    /**
     * Initiates a save dialog.
     * Allows the user to save the pattern either to RLE or to a GIF file, with
     * the options to change the speed, the number of frames, and the
     * size of the GIF. Creating instances of SavePattern and SaveGIF accordingly.
     * With two buttons, one is for saving the pattern to a RLE file, and the  
     * other one is to save the pattern to a GIF file.
     */
    public void saveBtnClicked(){
        saveDialog = new Dialog(); 
        saveDialog.setTitle("Save pattern");
        saveDialog.setResizable(false);
        
        VBox grandParent = new VBox();
        HBox first       = new HBox();
        HBox second      = new HBox();
        HBox third       = new HBox();
        HBox fourth      = new HBox();
        
        grandParent.getChildren().addAll(first, second, third, fourth);
        
        Label GIFMSLab = new Label();
        GIFMSLab.setText("GIF speed: ");
        
        gifSpeed = new Slider();
        gifSpeed.setMin(75);
        gifSpeed.setMax(1000);
        gifSpeed.adjustValue(120);
        
        first.getChildren().addAll(GIFMSLab, gifSpeed);
        
        Label framesLab = new Label();
        framesLab.setText("GIF frames: ");
        
        frames = new TextField();
        frames.setPromptText("Number of frames(50)");
        
        second.getChildren().addAll(framesLab, frames);
        
        Label pixlPrCellLab = new Label();
        pixlPrCellLab.setText("GIF size:");
        
        gifCellSize = new Slider();
        gifCellSize.setMin(1);
        
        SaveGIF sg = new SaveGIF();
        sg.gifCellSizeLimiter(pe.getInitPattern());
        
        third.getChildren().addAll(pixlPrCellLab, gifCellSize);

        Button saveToRLE = new Button();
        saveToRLE.setText("Save to .rle");
        saveToRLE.setOnMouseClicked(e ->{
            SavePattern sp = new SavePattern();
            sp.initSavePattern();
            saveDialog.close();
        });
        Button saveToGif = new Button();
        saveToGif.setText("Save to GIF");
        saveToGif.setOnMouseClicked(e ->{
            sg.initGIFSave();
            saveDialog.close();
        });
        saveDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        fourth.getChildren().addAll(saveToRLE, saveToGif);
        saveDialog.getDialogPane().setContent(grandParent);
        Stage diaStage = (Stage) saveDialog.getDialogPane().getScene().getWindow();
        diaStage.getIcons().add(new Image("file:icon.jpg"));
        saveDialog.showAndWait();
    }
    
    /**
     * Allows the user to load a RLE pattern from a file on
     * "Load file" button clicked.
     * @throws IOException IOException is thrown  if something went wrong during
     *         file reading. A dialog is shown to the user.
     * @throws logic.PatternFormatException is thrown if there is something
     *         wrong with the file content. A dialog is shown to the user.
     */
    public void loadFileBtnClicked() throws IOException, PatternFormatException{
        LoadPattern lp = new LoadPattern();
        byte[][] pattern = lp.readGameboardFromFile();
        
        if(pattern != null){
            pe.updateBoardWithPattern(pattern);
            pe.getStaticBoard().getNextGeneration().setBorn
                                        (ctrl.getDynamicBoard().getBorn());
            pe.getStaticBoard().getNextGeneration().setSurvives
                                        (ctrl.getDynamicBoard().getSurvives());
        }
    }
    
    /**
     * Allows the user to load a RLE pattern from a URL on
     * "Load url" button clicked.
     * @throws IOException IOException is thrown  if something went wrong during
     *         URL reading. A dialog is shown to the user.
     * @throws logic.PatternFormatException is thrown if there is something
     *         wrong with the URL content. A dialog is shown to the user.
     */
    public void loadURLBtnClicked() throws IOException, PatternFormatException{
        LoadPattern lp = new LoadPattern();
        byte[][] pattern = lp.readGameBoardFromURL();
        
        if(pattern != null){
            pe.updateBoardWithPattern(pattern);
            pe.getStaticBoard().getNextGeneration().setBorn
                                        (ctrl.getDynamicBoard().getBorn());
            pe.getStaticBoard().getNextGeneration().setSurvives
                                        (ctrl.getDynamicBoard().getSurvives());
        }
    }
    
    /**
     * Key listener that allows the user to move a loaded RLE/URL pattern 
     * around the canvas. And place it where he/she desiers. It is also possible
     * to rotate the loaded pattern. 
     * @param e e KeyEvent, listening to user input on the canvas.
     * @see metadata.PatternEditor#moveLoaded(KeyEvent e)
     */
    public void moveLoaded(KeyEvent e){
       pe.moveLoaded(e);
    }
    
    /**
     * Resets the board and the ruleset.
     * Letting the user type inn the desiered board size, via a dialog.
     * @see metadata.PatternSizeDialog#PatternSizeDialog() 
     */
    public void resetBoardBtnClicked(){
        pe.initPatternSizeDialog();
        pe.getStaticBoard().getNextGeneration().decodeRuleset("3", "23");
        pe.draw();
    }
    
    /**
     * Automaticaly adjusts the canvas with the scene size. By using a Pane 
     * wrapper that is set to contain the canvas and binding the canvas height 
     * and width property to the wrapper's width and height property. 
     * And by adding a listener to draw(), it will redraw on size adjustments of 
     * the scene.
     */ 
    public void autoAdjustCanvas(){
        Pane wrapper = new Pane(); 
        
        editorGrandParent.setCenter(wrapper);
        wrapper.getChildren().add(editorCanvas);
         
        editorCanvas.widthProperty().bind(wrapper.widthProperty());
        editorCanvas.heightProperty().bind(wrapper.heightProperty());
        
        editorCanvas.widthProperty().addListener(e -> pe.draw());
        editorCanvas.heightProperty().addListener(e -> pe.draw());
    }
    
    /**
     * AutoAdjusts strip canvas, to fit the stage. 
     * By setting the genPane to fit the bottom spot in the editorGrandParent's 
     * width and height properties.
     */
    public void autoAdjustScrollPane(){
        editorGrandParent.setBottom(genPane);
        genPane.setFitToWidth(true);
        genPane.setFitToHeight(true);
        genPane.setContent(genCanvas);
    }
    
    /**
     * Zoom slider listener.
     * Enables the user to zoom in and out on the graphical animation.
     */
    public void listenerZoom(){
        pe.getStaticBoard().setCellSize(zoomSlider.getValue());
        pe.draw();
    }
    
    /**
     * User input on canvas.
     * Allows user to click on the canvas to change the cell state.
     * @param e MouseEvent.
     * @see metadata.PatternEditor#changeCellStatusClicked
     * (javafx.scene.input.MouseEvent) 
     */
    public void mouseClicked(MouseEvent e){
        pe.changeCellStatusClicked(e);
    }
    
    /**
     * User input on canvas.
     * Allows user to mouse drag the canvas to change the cell state.
     * @param e MouseEvent.
     * @see metadata.PatternEditor#changeCellStatusDragged
     * (javafx.scene.input.MouseEvent) 
     */
    public void mouseDragged(MouseEvent e){
        pe.changeCellStatusDragged(e);
    }
    
    /**
     * Living cell color listener.
     * Allows the user to change the living cell color. Using a JavaFX 
     * ColorPicker.
     */
    public void changeLiveCellColor() {
        pe.changeLivingCellColor(liveCellColor.getValue());
    }
    
    /**
     * @return the desired name of the RLE file.
     */
    public TextField getName(){
        return name;
    }
    
    /**
     * @return the desired description of the RLE file. 
     */
    public TextField getDescription(){
        return description;
    }
    
    /**
     * @return the desired creator name of the RLE file.
     */
    public TextField getCreator(){
        return creator;
    }
    
    /**
     * @return the desired survives rule of the GIF or RLE file.
     */
    public TextField getSurvives(){
        return survives;
    }
    
    /**
     * @return the desired born rule of the GIF or RLE file.
     */
    public TextField getBorn(){
        return born;
    }
    
    /**
     * @return the slider adjusting the zoom/cellSize on the editorCanvas.
     */
    public Slider getZoomSlider(){
        return zoomSlider;
    }
    
    /**
     * @return the editable/main canvas of the patternEditor.
     */
    public Canvas getEditorCanvas(){
        return editorCanvas;
    }
    
    /**
     * @return the generation/strip canvas.
     */
    public Canvas getGenCanvas(){
        return genCanvas;
    }
    
    /**
     * @return a slider that allows the user to change speed of the GIF.
     */
    public Slider getGifSpeed(){
        return gifSpeed;
    }
    
    /**
     * @return the slider that adjusts the GIF's cell size.
     */
    public Slider getGifCellSize(){
        return gifCellSize;
    }
    
    /**
     * @return the amount of frames the user chooses to have in the GIF file.
     */
    public TextField getFrames(){
        return frames;
    }
    
    /**
     * @return instance of PatternEditor.
     */
    public PatternEditor getPatternEditor(){
        return pe;
    }
}

