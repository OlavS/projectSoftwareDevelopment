
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import logic.DynamicBoard;
import logic.Gameboard;
import logic.InstructionDialog;
import logic.LoadPattern;
import logic.PatternFormatException;
import logic.StaticBoard;
import metadata.PatternEditor;



/**
 * GoLController handles all user inputs.
 * Sending userinputs to their destination. Containing all controller instances
 * from the GoL.fxml file.
 * 
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public class GoLController implements Initializable {
    
    @FXML private BorderPane grandParent;
    @FXML private Canvas canvas;
    @FXML private Slider speedSlider;
    @FXML private Slider zoomSlider;
    @FXML private Label genCountLabel;
    @FXML private Label cellCountLabel;
    @FXML private Label backgroundLabel;
    @FXML private Label deadCellLabel;
    @FXML private Label setSizeLabel;
    
    @FXML private ColorPicker liveCellColor;
    @FXML private ColorPicker deadCellColor;
    @FXML private ColorPicker backgroundColor;
    @FXML private Text lastLoaded;
    @FXML private TextField widthTextfield;
    @FXML private TextField heightTextfield;
    @FXML private TextField born;
    @FXML private TextField survives;
    @FXML private Button startBtn;
    @FXML private Button loadFileBtn;
    @FXML private Button loadURLBtn;
    @FXML private Button patternEditorBtn;
    @FXML private Button confirmSizeBtn;
    
    private Gameboard gb;
    private GraphicsContext gc;
    public static GoLController instance;
   
    private Timeline initSimulation = new Timeline(new KeyFrame
                                     (Duration.millis(120), 
                                      e -> nextGenBtnClicked()));    
    
    
    /**
     * Initialize/startup sequence.
     * Initiates required object creations and method runs to initialize 
     * Application.
     * @param url 
     * The location used to resolve relative paths for the root 
     * object, or <tt>null</tt> if the location is not known..
     * @param rb
     * The resources used to localize the root object, or <tt>null</tt> if
     * the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;    
        gc = canvas.getGraphicsContext2D();
        gb = new DynamicBoard(gc);
        /*gb = new StaticBoard(gc);*/ //Static representation of the Gameboard
        showDynamicBtns();
        setTooltips();
        autoAdjustCanvas();
    }
    
    /**
     * Sets the tooltips for various buttons where their usage is not
     * obvious.
     */
    public void setTooltips(){
        setRulesetTip();
        setLoadBtnsTip();
        setPatternEditorBtnTip();
    }
    
    /**
     * Tooltip for ruleset. Displayed when hovering over
     */
    public void setRulesetTip(){
        final Tooltip bornTip = new Tooltip();
        bornTip.setText(  "Born - how\n"
                        + "many neighbours\n"
                        + "a cell must have\n"
                        + "to become alive.");

        
        final Tooltip survivesTip = new Tooltip();
        survivesTip.setText(  "Survives - how\n"
                            + "many neighbours\n"
                            + "a cell must\n"
                            + "have to survive.");
        
        born.setTooltip(bornTip);
        survives.setTooltip(survivesTip);
    }
    
    /**
     * Describes the pattern editor functionality to the user, 
     * when hovering over the button for a couple of secounds.
     */
    public void setPatternEditorBtnTip(){
        final Tooltip editorTip = new Tooltip();
        editorTip.setText(  "Pattern editor:\n"
                          + "edit patterns\n"
                          + "and save them\n"
                          + "as pattern files,\n"
                          + "and GIF files.");
        
        patternEditorBtn.setTooltip(editorTip);
    }
    
    
    /**
     * Describes the load controls to the user, 
     * when hovering over the button for a couple of secounds.
     */
    public void setLoadBtnsTip(){
        final Tooltip loadTip = new Tooltip();
        loadTip.setText(  "Controls after load:\n"
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
     * Hiding buttons related to the StaticBoard implementation, 
     * when DynamicBoard is running.
     */
     public void showDynamicBtns(){
        if(gb.getClass().getName().equals("logic.DynamicBoard")) {
            deadCellLabel.setVisible(false);
            deadCellColor.setVisible(false);
            backgroundLabel.setVisible(false);
            backgroundColor.setVisible(false);
            setSizeLabel.setVisible(true);
            widthTextfield.setVisible(true);
            heightTextfield.setVisible(false);
            confirmSizeBtn.setVisible(true);
            widthTextfield.setPromptText("Area of square");
        }
     }
     
     
    /**
     * Setting the timeline cycle for the animation.
     * @param timeline
     * Timeline takes care of the animation loop.
     */
    public void setTimeline(Timeline timeline){
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
    }
    
    
    /**
     * StartBtnClicked handles Timeline on button click.
     * Initiates the play and pause method of the timeline cycle, and changing 
     * startBtn's .getText property between "Start" and "Pause".
     */
    public void startBtnClicked(){
        if(startBtn.getText().equals("Start")){
            setTimeline(initSimulation);
            initSimulation.play();
            startBtn.setText("Pause");
        }else{
            startBtn.setText("Start");
            initSimulation.pause();
        }
    }
    
    public void instructionBtnClicked(){
        new InstructionDialog();
    }
    
    /**
     * editorBtnClicked initiates PatternEditor on button click.
     * @throws IOException IOException is thrown if the FXMLLoader fails.  
     */
    public void editorBtnClicked() throws IOException{
        PatternEditor pe = new PatternEditor(gc);
        pe.initGUI();
    } 
    
    
    /**
     * ClearBtnClicked pauses simulation and resets board on button click.
     */
    public void resetBtnClicked() {
        initSimulation.pause();
        startBtn.setText("Start");
        gb.resetBoard();
        genCountLabel.setText(gb.getGenCounter());
    }
    
    
    /**
     * NextGenClicked jumps to the next generation on button click.
     */
    public void nextGenBtnClicked() {
        gb.displayNextGen();
        genCountLabel.setText(gb.getGenCounter());
    }

    /**
     * Displays a grid on the canvas when clicked. 
     */
    public void gridBtnClicked() {
        getDynamicBoard().setGridStatus();
        gb.draw();
    }
    
    
    /**
     * LoadFileBtnClicked() allows the user to load a RLE pattern from a file on
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
            gb.updateBoardWithPattern(pattern);
        }
    }
    
    
    /**
     * LoadURLBtnClicked() allows the user to load a RLE pattern from a URL on
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
            gb.updateBoardWithPattern(pattern);
        }
    }
    
    
   /**
    * Adjust the size of the StaticBoard.
    * The user can type in the desired board size, and click "Confirm". The
    * game board will then resize.
    * @deprecated not used in DynamicBoard.
    */
    public void confirmSizeBtnClicked() {
        gb.changeBoardSize();
    }
    
    
    /**
     * Changes the to alternative ruleset on userinput. 
     * The input must be valid, it is tested before it is set.
     */
    public void confirmRulesBtnClicked(){
        String b = born.getText();
        String s = survives.getText();
        
        getDynamicBoard().decodeRuleset(b, s);
        /*getStaticBoard().getNextGeneration().decodeRuleset(b, s);*/
        //Required for StaticBoard.
    }
    
    
    /**
     * Velocity slider listener.
     * Enables the user to change the velocity of the graphical animation.
     */
    public void listenerSpeed(){
        initSimulation.setRate(speedSlider.getValue());
    }
    
    
    /**
     * Zoom slider listener.
     * Enables the user to zoom in and out on the graphical animation.
     */
    public void listenerZoom(){
        gb.changeCellSize(zoomSlider.getValue());
    }
    
   
    /**
     * Background color listener.
     * Allows the user to change the background color of the graphical animation.
     * Using a JavaFX ColorPicker.
     * @deprecated not used in DynamicBoard.
     */
    public void changeBackgroundColor(){
        /*StaticBoard.changeBackgroundColor(backgroundColor.getValue());*/
    }
    
    
    /**
     * Living cell color listener.
     * Allows the user to change the living cell color. Using a JavaFX 
     * ColorPicker.
     */
    public void changeLiveCellColor(){
        gb.changeLivingCellColor(liveCellColor.getValue());
    }
    
    
    /**
     * Dead cell color listener.
     * Allows the user to change the dead cell color. Using a JavaFX ColorPicker.
     * @deprecated not used in DynamicBoard.
     */
    public void changeDeadCellColor(){
        /*StaticBoard.changeDeadCellColor(deadCellColor.getValue());*/
    }
    
    
    /**
     * Mouse listener that allows the user to change the cell state on mouse 
     * click and mouse dragged.
     * @param e e MouseEvent, listening to user input on the canvas.
     */
    public void mouseListner(MouseEvent e){
        gb.changeCellStatus(e);
    }
   
    
    /**
     * Key listener that allows the user to move a loaded RLE/URL pattern 
     * around the canvas. And place it where he/she desiers. It is also possible
     * to rotate the loaded pattern.
     * It also allows the user to move the generation around in zoomed mode, by 
     * using the arrow keys.
     * @param e e KeyEvent, listening to user input on the canvas.
     */
    public void keyEventScene(KeyEvent e){
        canvas.requestFocus();        
        gb.moveLoaded(e);
        getDynamicBoard().navigateBoard(e); //Comment out when running StaticBoard.
    }
    
    
    /**
    * Automaticaly adjusts the canvas with the scene size.
    * And by adding a listener to draw(), it will redraw on size adjustments of 
    * the scene. 
    */
    public void autoAdjustCanvas(){
        Pane wrapper = new Pane(); 
        
        grandParent.setCenter(wrapper);
        wrapper.getChildren().add(canvas);
         
        canvas.widthProperty().bind(wrapper.widthProperty());
        canvas.heightProperty().bind(wrapper.heightProperty());
        
        canvas.widthProperty().addListener(e -> gb.draw());
        canvas.heightProperty().addListener(e -> gb.draw());
    }
    
    
    /**
     * @return livingCellLabel is a label.
     */
    public Label getLivingCellLabel(){
        return cellCountLabel;
    }
    
    
    /**
     * @return widthTextfield where the user can choose the 
     * board width when running a StaticBoard instance.
     */
    public TextField getWidthTextfield(){
        return widthTextfield;
    }
    
    
    /**
     * @return heightTextfield where the user can choose the 
     * board height when running a StaticBoard instance.
     */
    public TextField getHeightTextfield(){
        return heightTextfield;
    }
    
    
    /**
     * Used to pause simulation when pattern editor is opened.
     * @return timeline of main window.
     */
    public Timeline getInitSimulation(){
        return initSimulation;
    }
    
    
    /**
     * Getter for the highest node in the node hierarki, the BorderPane.
     * @return grandParent is the top node in the node hierarki in 
     * the GoL Stage.
     */
    public BorderPane getGrandParent(){
        return grandParent;
    }
    
    
    /**
     * Used to calculate offsets.
     * @return the canvas containing gameboard animation.
     */
    public Canvas getCanvas(){
        return canvas;
    }
    
    
    /**
     * Getter for the current DynamicBoard object. 
     * NOTE: Be careful using this, make sure that the Gameboard object you have
     * initiated actualy is a DynamicBoard.
     * @return the current DynamicBoard object.
     */
    public DynamicBoard getDynamicBoard(){
        return (DynamicBoard) gb;
    }
    
    
    public StaticBoard getStaticBoard(){
        return (StaticBoard) gb;
    }
    
    
    /**
     * Setter for the games ruleset.
     * @param b born rule.
     * @param s survives rule.
     */
    public void setPatternRules(String b, String s){
        getDynamicBoard().decodeRuleset(b, s);
        /*getStaticBoard().getNextGeneration().decodeRuleset(b, s);*/
        //Required for StaticBoard.
    } 
    
    
    /**
     * Setter for loaded pattern name label. Last loaded will be displayed over 
     * the load pattern buttons.
     * @param name the name parameter located in the RLE file/URL 
     * with a #N tag, followed by the name. 
     */
    public void setPatternName(String name){
        lastLoaded.setText("Last loaded: " + name);
    }
    
    
    /**
     * Setter for the living cell label. 
     * This label displays how many living cells it is on the DynamicBoard. 
     * @param s the String containing "Population: " and a cell count.
     */
    public void setLivingCellLabelText(String s) {
        cellCountLabel.setText(s);
    }
    
    public void setAreaLabelText(String s) {
        setSizeLabel.setText("Area: " + s + "x"  + s);
    }
}