package metadata;

import controller.GoLController;
import controller.PatternEditorController;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.PatternFormatException;
import logic.PatternFormatExceptionDialog;
import logic.StaticBoard;
import logic.PatternLoader;

/**
 * This class contains all the graphical and logical methods for the 
 * Pattern Editor functionality. Giving the user the appertunity to 
 * edit patterns and watch the future generations at the editors generation
 * strip(genPane). The patterns can be edited using eighter loaded patterns, or 
 * my mouse interaction with the canvas.
 * It has a save pattern and save GIF feature, that lets the 
 * user save the pattern to eighter a RLE file or a GIF file.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public class PatternEditor implements PatternLoader{
    
    GoLController ctrl;
    StaticBoard sb;
    PatternEditorController peCtrl;
    
    private FXMLLoader      loader;
    private Stage           editorStage;
    private BorderPane      editorGrandParent;
    private GraphicsContext gc;
    
    private double offsetY;
    private double offsetX;
    private int width;
    private int height;
    private int stripFrames;
    private int pattMovesY;
    private int pattMovesX;
    private byte[][] initPattern;
    private byte[][] stripPattern;
    private byte[][] loadedPattern;
    
    /**
     * Contructor PatternEditor.
     * Contains a graphicsContext, the singelton Controller object, the 
     * singelton PatternEditor Controller object, and a clone of the StaticBoard
     * object from the current run.
     * @param gc GraphicsContext to the main canvas.
     */
    public PatternEditor(GraphicsContext gc){
       this.gc = gc;
       this.ctrl = GoLController.instance;
       this.peCtrl = PatternEditorController.peInstance;
       this.sb = new StaticBoard(gc);
    }
    
    /**
     * Initializer for the PatternEditor stage.
     * Loads the PatternEditorFXML, pauses the GoL stage. 
     * Setting title and logo.
     * @throws IOException is thrown if the FXMLLoader fails.
     */
    public void initGUI() throws IOException{
        editorStage = new Stage();
        loader = new FXMLLoader(getClass().getResource("/view/PatternEditor.fxml"));
        editorGrandParent = loader.load();
        //Pauses the main window:
        ctrl.getInitSimulation().pause();
        
        editorStage.initModality(Modality.WINDOW_MODAL);
        editorStage.initOwner(ctrl.getGrandParent().getScene().getWindow());
        
        Scene scene = new Scene(editorGrandParent);
        scene.getRoot().requestFocus();
        
        editorStage.setTitle("Pattern Editor: Game Of Life - created by "
                             + "Øyvind Mjelstad and Olav Sørlie");
        editorStage.getIcons().add(new Image("file:icon.jpg"));
        editorStage.setScene(scene);
        editorStage.show();
    }
    
    /**
     * Initiates the baseLogic.
     * Initiates a dynamic convertion, to static. 
     * Setting primitives and objects to the StaticBoard object.
     * @see metadata.PatternEditor#fetchDynamicValues() 
     * @see logic.GenerationConcurrHash#convertToStatic()
     * @see metadata.PatternEditor#createInitPattern() 
     */
    public void initLogic(){
        fetchDynamicValues();
        defaultCellSize();
        createInitPattern();
        
        if(initPattern == null){
            return;
        }
        stripPattern = initPattern;
        stripPattern = deadBorderControl(stripPattern);
        draw();
    }
    
    /**
     * Setting essential values from dynamicBoard.
     * Putting them in to the StaticBoard object.
     * CurrentGen(see link), CellSize, and Living cell color is set.
     * Calling setDefaultPaintingColors.
     * @see logic.GenerationConcurrHash#convertToStatic() 
     */
    public void fetchDynamicValues(){
        sb.setCurrentGen(ctrl.getDynamicBoard().getGeneration().convertToStatic());
        sb.getNextGeneration().setBorn(ctrl.getDynamicBoard().getBorn());
        sb.getNextGeneration().setSurvives(ctrl.getDynamicBoard().getSurvives());
        sb.setLiving(ctrl.getDynamicBoard().getLiving());
        setUnusedColorsDynamicBoard();
    }
    
    /**
     * Engages a initPattern creation at initiation.
     * If a generation with values exists in the StaticBoard's
     * current generation(currentGen) it will create a initPattern based on that
     * if it dosnt exist a pattern size dialog(initPatternSizeDialog) will show.
     * Allowing the user to pick the desiered sizes of the initPattern.
     * @see metadata.PatternEditor#createDeadBorder(byte[][]) 
     * @see metadata.PatternEditor#initPatternSizeDialog() 
     */
    public void createInitPattern(){
        initPattern = createDeadBorder(sb.getCurrentGen());
  
        if(initPattern == null)
            initPatternSizeDialog();
    }
    
    /**
     * PatternSizeDialog allows the user to set the width and height variables of 
     * this instance. Letting us create a new initPattern with the choosen sizes.
     * @see metadata.PatternSizeDialog
     */
    public void initPatternSizeDialog(){
        width = 0;
        height = 0;
        
        new PatternSizeDialog();
         
        if(width == 0 || height == 0){
            if(initPattern != null){
                width = initPattern.length;
                height = initPattern[0].length;
            }
        }else{
            initPattern = new byte[width][height];
        }
    }
    
    /**
     * Drawing on the main canvas. 
     * Updating drawOffset, makeing the initPattern appear in the middle of the 
     * canvas. 
     * Using the user choosen colors, from the living(JavaFX ColorPicker) color.
     * And initiates engageDrawStrip.
     * @see metadata.PatternEditor#drawOffset()
     * @see metadata.PatternEditor#engageDrawStrip(boolean) 
     */
    public void draw(){
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, peCtrl.getEditorCanvas().widthProperty().doubleValue(),
                    peCtrl.getEditorCanvas().heightProperty().doubleValue()); 
        drawOffset();
        
        for(int i = 0; i<initPattern.length; i++){
            for(int j = 0; j<initPattern[0].length; j++){
              
                if(initPattern[i][j] == 1){ 
                    gc.setFill(sb.getLiving());
                }
                else if(initPattern[i][j] == 0){
                    gc.setFill(sb.getDead());
                }
                gc.fillRect(offsetX + (sb.getCellSize()*i),
                            offsetY + (sb.getCellSize()*j),
                            sb.getCellSize()*0.9,
                            sb.getCellSize()*0.9);
            }
        }
        stripPattern = initPattern;
        engageDrawStrip(patternNotEmpty(initPattern));
    }
    
    /**
     * Drawoffset makes sure that the initPattern is always sentered in the 
     * middle of the main canvas(editorCanvas).
     */
    public void drawOffset(){
        offsetY = (peCtrl.getEditorCanvas().heightProperty().doubleValue()
                   -initPattern[0].length*sb.getCellSize())/2;
        offsetX = (peCtrl.getEditorCanvas().widthProperty().doubleValue()
                   -initPattern.length*sb.getCellSize())/2;
        
    }
    
    /**
     * Initiates drawStrip() if the boolean parameter entered is true.
     * @param engage if engage is true, drawStrip() will engage.
     * @see metadata.PatternEditor#drawStrip() 
     */
    public void engageDrawStrip(boolean engage){
        if(engage)
            drawStrip();
    }
    
    /**
     * Drawing a strip on the genCanvas, using a JavaFX GraphicsContext instance(strip).
     * User can choose the color to be drawn with, by a JavaFX Colorpicker in the 
     * GUI. It starts its sequence by calculating and setting the canvas width, 
     * based on the future generation. It then starts the draw sequence, by first 
     * to check if the stripPattern is not empty, if it is it will return, and end 
     * the cycle. Then a dead border will be attached to the stripPattern, if nessesary.
     * And a calculation for the stripCellSize will happend, and the stripCellSize will be set.
     * A next generation calculation through calcNextGen in NextGeneration class,
     * in package logic is runned, the pattern will be checked if it is equal 
     * to the initPattern, cycle will end if it is. 
     * And that continues for stripFrames-1 generations, if the patternEqualityCheck
     * dosnt trigger.
     * @see metadata.PatternEditor#calcCanvasWidth(double) 
     * @see metadata.PatternEditor#patternNotEmpty(byte[][]) 
     * @see metadata.PatternEditor#deadBorderControl(byte[][]) 
     * @see metadata.PatternEditor#calcStripCellSize(byte[][]) 
     * @see logic.NextGeneration#calcNextGen(byte[][]) 
     * @see metadata.PatternEditor#patternEqualityCheck(byte[][]) 
     */
    public void drawStrip(){
        Affine form = new Affine();
        stripFrames = 15;
        
        double canvasWidth  = peCtrl.getGenCanvas().widthProperty().doubleValue();
        double canvasHeight = peCtrl.getGenCanvas().heightProperty().doubleValue();
        double stripCellSize;
        double padding      = 50;
        double tx           = 0;
        
        setGenCanvasWidth(calcCanvasWidth(padding));
        GraphicsContext strip = peCtrl.getGenCanvas().getGraphicsContext2D();
        
        for(int counter = 0; counter < stripFrames; counter++){
            strip.setFill(Color.GRAY);
            strip.fillRect(0, 0, canvasWidth, canvasHeight);
            
            if(!patternNotEmpty(stripPattern)){
                return;
            }
            stripPattern = deadBorderControl(stripPattern);
            stripCellSize = calcStripCellSize(stripPattern);
            
            for(int i = 0; i < stripPattern.length; i++){
                for(int j = 0; j < stripPattern[0].length; j++){
                    if(stripPattern[i][j] == 1){
                        strip.setFill(sb.getLiving());
                    }
                    else if(stripPattern[i][j] == 0){
                        strip.setFill(sb.getDead());
                    }
                    strip.fillRect(stripCellSize*i, stripCellSize*j, 
                                   stripCellSize*0.9, stripCellSize*0.9);
                }
            }
            if(counter>0){
                tx += (stripPattern.length*stripCellSize)+padding;
            }
            form.setTx(tx);
            strip.setTransform(form);
            stripPattern = sb.getNextGeneration().calcNextGen(stripPattern);
                        
            if(patternEqualityCheck(stripPattern)){
                counter = stripFrames;
            }
        }
    }
    
    /**
     * Testing if a pattern is equal to the initPattern.
     * @param input input being tested at the initPattern
     * @return true if the parameter is identical to initPattern, 
     *         returns false if it is not.
     */
    public boolean patternEqualityCheck(byte[][] input){
        int sum = 0;
        byte[][] temp = createDeadBorder(peCtrl.getPatternEditor().getInitPattern());
        
        if(temp.length == input.length && temp[0].length == input[0].length){
            
            for(int i = 0; i < input.length; i++){
                for(int j = 0; j < input[0].length; j++){
                
                    if(input[i][j] == temp[i][j])
                        sum++;
                }
            }
        }
        return sum == temp.length*temp[0].length;
    }
    
    /**
     * Makes sure that there is always a singel field dead border around the 
     * input pattern.
     * @param input to be tested and modified.
     * @return modified with a dead border if modification was nessesary.
     * @see metadata.PatternEditor#expandPattern(byte[][]) 
     * @see metadata.PatternEditor#shrinkPattern(byte[][]) 
     */
    public byte[][] deadBorderControl(byte[][] input){
        input = expandPattern(input);
        
        if(input.length > 1 || input[0].length > 1){
            input = shrinkPattern(input);                
        }
        return input;
    }
    
    /**
     * Expands GIF/strip pattern.
     * Expands the GIF/strip pattern if nessesary. To make sure that it is 
     * always a dead cell border around the pattern. Testing if the pattern
     * needs to be expanded to obtain a dead border around it
     * @param input is the pattern to be checked and dead bordered.
     * @return returns the dead bordered pattern.
     * @see metadata.PatternEditor#createDeadBorder(byte[][]) 
     */
    public byte[][] expandPattern(byte[][] input){
        int north = 0,
             east = 0,
             west = 0,
            south = 0;
        //Expand north and south:
        for(int i = 0; i < input.length; i++){
            north += input[i][0];
            south += input[i][input[0].length-1];
        }
        //Expand east and west:
        for(int i = 0; i < input[0].length; i++){ 
            east += input[input.length-1][i];
            west += input[0][i];
        }
        if(north != 0 || south != 0 || east != 0 || west != 0){
            input = createDeadBorder(input);
        }
        return input;
    }
    
    /**
     * Shrinks the GIF/strip pattern.
     * To make sure that it is only one dead cell border around the GIF/strip 
     * pattern. Testing if the pattern needs to be shrinked, to obtain a singel
     * dead border field around it.
     * @param input is the pattern to be checked and dead bordered.
     * @return returns the dead bordered pattern.
     * @see metadata.PatternEditor#createDeadBorder(byte[][]) 
     */
    public byte[][] shrinkPattern(byte[][] input){         
        if(!patternNotEmpty(input)){
            return null;
        }
        int north = 0,
             east = 0,
             west = 0,
            south = 0;
        //Shrink north and south:
        for(int i = 1; i < input.length; i++){
            north += input[i][1];
            south += input[i][input[0].length-2];
        }
        //Shrink east and west:
        for(int i = 1; i < input[0].length; i++){
            east += input[input.length-2][i];
            west += input[1][i];
        }
        if(north == 0 || south == 0 || east == 0 || west == 0){
            input = createDeadBorder(input);
        }
        return input;
    }
    
    
    /**
     * Trimming initiation pattern, and creating a dead border around the 
     * pattern. If no pattern was detected, it will display pattern size dialog
     * to the user, letting the user create his/her own empty pattern.
     * Uses the boundingBox method of the staticBoard class, shrinking the input 
     * pattern.
     * @param input is a byte[][] pattern that is going to be trim.
     * @return the trimmed byte[][] pattern
     */
    public byte[][] createDeadBorder(byte[][] input){
        byte[][] temp = null;
        int[] moves = sb.boundingBox(input);
                
        if(moves[1]-moves[0]+3 > 0 && moves[3]-moves[2]+3 > 0){ 
            temp = new byte[moves[1]-moves[0]+3][moves[3]-moves[2]+3];
        
            for(int i = 0; i<temp.length-2; i++){
                for(int j = 0; j<temp[0].length-2; j++){ 
                    if(input[moves[0]+i][moves[2]+j] == 1)
                        temp[i+1][j+1] = 1;
                }
            }
        }
        return temp;
    }
    
    /**
     * Checks if the pattern just contains zeros by iterating through it and 
     * adding all the 1 values to a sum.
     * @param input is the input byte[][] pattern to be checked.
     * @return true if the pattern is not empty,
     *         false if the pattern is empty.
     */
    public boolean patternNotEmpty(byte[][] input){
        int sum = 0;
        for(int i = 0; i < input.length; i++){
            for(int j = 0; j < input[0].length; j++){
                if(input[i][j] == 1)
                    sum++;
            }
        }
        if(sum > 0)
            return true;
        return false;
    }
    
    /**
     * Changing cell status by mouse clicks on canvas.
     * Using the offsetX and offsetY variables to calculate where to change 
     * status. Returns if the mouse click happend outside the initPattern. 
     * If the cell was live it will die and wise versa.
     * @param e MouseEvent on PatternEditor canvas.
     */
    public void changeCellStatusClicked(MouseEvent e){
        int i = (int) ((e.getX()-offsetX)/sb.getCellSize());
        int j = (int) ((e.getY()-offsetY)/sb.getCellSize()); 
        //To avoid ArrayIndexOutOfBoundsException when drawing outside the pattern
        if(e.getX() < offsetX ||
           e.getY() < offsetY || 
           j > initPattern[0].length-1 ||
           i > initPattern.length-1){
            return;
        }
        if(initPattern[i][j] == 0){
            initPattern[i][j] = 1;
        }    
        else{    
            initPattern[i][j] = 0;
        }
        stripPattern = initPattern;
        draw();
        
        if(loadedPattern != null){
            drawLoaded();
        }
    }
    
    /**
     * Changing cell status by mouse click and drag on canvas.
     * Using the offsetX and offsetY variables to calculate where to change 
     * status. Returns if the mouse drag happend outside the initPattern. 
     * If primary button press, cells will be live. If secondary button press
     * cells will die.
     * @param e MouseEvent on PatternEditor canvas.
     */
    public void changeCellStatusDragged(MouseEvent e){
        int i = (int) ((e.getX()-offsetX)/sb.getCellSize());
        int j = (int) ((e.getY()-offsetY)/sb.getCellSize());
        //To avoid ArrayIndexOutOfBoundsException when drawing outside the pattern
        if(e.getX() < offsetX || e.getY() < offsetY ||
           j > initPattern[0].length-1 || i > initPattern.length-1){
            return;
        }
        if(e.isPrimaryButtonDown()){
            initPattern[i][j] = 1;
        }
        else if(e.isSecondaryButtonDown()){   
            initPattern[i][j] = 0;
        }
        stripPattern = initPattern;
        draw();
        
        if(loadedPattern != null){
            drawLoaded();
        }
    }
    
    /**
    * Initiates pattern placement for loaded patterns.
    * @param input input is the pattern represented as a byte[][].
    * @see logic.PatternLoader#updateBoardWithPattern(byte[][]) 
    */
    @Override
    public void updateBoardWithPattern(byte[][] input){        
       
        try{ 
            if(input.length<initPattern.length && input[0].length<initPattern[0].length){
                loadedPattern = input;
                setStartIndexLoaded();
                drawLoaded();
            }
            else{   
                throw new PatternFormatException();
            }
        }
        catch(PatternFormatException e){   
            new PatternFormatExceptionDialog("Pattern is to big, increase "
                                            + "dimensions and retry");
        }
    }
    
    /**
    * Setter for start index to loaded pattern.
    * This is where the pattern placement sequence starts. It is set to start at 
    * the middle of the current generation (currentGen) pattern.
    * @see logic.PatternLoader#setStartIndexLoaded() 
    */   
    @Override
    public void setStartIndexLoaded(){
        
        pattMovesX = (initPattern.length/2)-(loadedPattern.length/2);
        pattMovesY = (initPattern[0].length/2)-(loadedPattern[0].length/2);
    }
    
    
    /**
     * Allows the user to move the loaded pattern, and place it desierably.
     * key S: moves down
     * key W: moves up
     * key D: moves right
     * key A: moves left
     * key E: rotates 
     * key CONTROL: glue pattern
     * key ESCAPE: escapes pattern placement.
     * @param e e KeyEvent allowing user input from keyboard.
     * @see logic.PatternLoader#moveLoaded(javafx.scene.input.KeyEvent) 
     */
    @Override
    public void moveLoaded(KeyEvent e){
        if(loadedPattern != null){
            switch (e.getCode()) {
                case S:
                    pattMovesY = pattMovesY + 1;
                    drawLoaded();
                    break;
                case W:
                    pattMovesY = pattMovesY - 1;
                    drawLoaded();
                    break;
                case D:
                    pattMovesX = pattMovesX + 1;
                    drawLoaded();
                    break;
                case A:
                    pattMovesX = pattMovesX - 1;
                    drawLoaded();
                    break;
                case E:
                    rotateLoaded();
                    drawLoaded();
                    break;
                case CONTROL:
                    glueLoaded();
                    break;
                case ESCAPE:
                    loadedPattern = null;
                    draw();
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * Draws the pattern loaded in by user, either through URL or as a RLE file.
     * Uses 2D representation to load pattern. Every living cell is set to grey
     * and every dead cell is transparent.
     * @see logic.PatternLoader#drawLoaded() 
     */ 
    @Override
    public void drawLoaded(){
        double startX = pattMovesX * sb.getCellSize()+(int)offsetX; 
        double startY = pattMovesY * sb.getCellSize()+(int)offsetY;
        draw();

        for(int i = 0; i < loadedPattern.length; i++){
            for(int j = 0; j < loadedPattern[0].length; j++){
        
            if(loadedPattern[i][j] == 1){
                gc.setFill(Color.rgb(192, 192, 192, 0.8));
            }
            else if(loadedPattern[i][j] == 0){
                gc.setFill(Color.TRANSPARENT);
            }
            gc.fillRect(startX + (sb.getCellSize()*(i-1)),startY + 
                       (sb.getCellSize()*(j-1)), sb.getCellSize()*0.9, 
                        sb.getCellSize()*0.9);
            }
        }
    }
    
    /**
     * Attaches the loaded pattern to the current generation.
     * @see logic.PatternLoader#glueLoaded() 
     */
    @Override
    public void glueLoaded(){
        for(int i = 0; i < loadedPattern.length; i++){
            for(int j = 0; j < loadedPattern[0].length; j++){

                if(pattMovesX < 0 || 
                   pattMovesY < 0 || 
                   pattMovesX+i > initPattern.length || 
                   pattMovesY+j > initPattern[0].length){
                   break;
                }
                if(loadedPattern[i][j] == 1){
                    initPattern[pattMovesX+i-1][pattMovesY+j-1] = loadedPattern[i][j];
                }
            }
        }
    }
    
    /**
    * Rotates the loaded pattern, before it is glued/attached to the initPattern.
    * @see logic.PatternLoader#rotateLoaded() 
    */
    @Override
    public void rotateLoaded(){
        byte[][] rotated = new byte[loadedPattern[0].length][loadedPattern.length];
        
        for(int i = 0; i < loadedPattern.length; i++){
            for(int j = 0; j < loadedPattern[0].length; j++){
                rotated[j][loadedPattern.length-i-1] = loadedPattern[i][j];
            }
        }
        loadedPattern = rotated;
    }
    
    /**
     * Calculates canvas width.
     * Based on a simple stripPattern generation and stripCellSize calculation.
     * @param padding padding is the padding between strip generations.
     * @return sum sum is the calculated sum to the genCanvas width.
     */
    public double calcCanvasWidth(double padding){
        
        double add = 0,
               sum = 0;
        
        if(patternNotEmpty(stripPattern)){
            byte[][] temp = deadBorderControl(stripPattern);

            for(int i = 0; i < stripFrames; i++){
                if(temp != null){
                    add = temp.length * calcStripCellSize(temp);
                    sum += add+padding;
                    temp = sb.getNextGeneration().calcNextGen(temp);
                    temp = deadBorderControl(temp);
                }
            }
        }
        if(peCtrl.getGenCanvas().widthProperty().doubleValue() > sum)
            return peCtrl.getGenCanvas().widthProperty().doubleValue();
        else
            return sum;
    }
    
    /**
     * The stripCellSize is pending on the user input and the stripPattern width.
     * @param input  the height of a byte[][] is used to calculate the width.
     * @return the strip cell size.
     */
    public double calcStripCellSize(byte[][] input){
        double size = peCtrl.getGenCanvas().heightProperty().doubleValue()/
                      (input[0].length*1.1);
        return size;
    }
    
    /**
     * This method is running by the livingColorListener in the PatternEditor.
     * Changing the color based on the users choice via a JavaFX Colorpicker.
     * @param c is the input JavaFX Color, changing the living cell color in 
     *        the Gameboard class.
     */
    public void changeLivingCellColor(Color c) {
        sb.setLiving(c);
        draw();
    }
    
    /**
     * Default cell size value. Because this most likely is different 
     * from what the zoom slider in the game sequence is set to.
     */
    public void defaultCellSize(){
        sb.setCellSize(peCtrl.getZoomSlider().getValue());
    }
    
    /**
     * @return the height of the initPattern.
     */
    public int getHeight() {
        return height;
    }    
    
    /**
     * @return the height of the initPattern.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * @return main/init pattern.
     */
    public byte[][] getInitPattern() {
        return initPattern;
    }
    
    /**
     * @return is the strip frames.
     */
    public int getStripFrames(){
        return stripFrames;
    }
    
    /**
     * @return is this instance of a staticBoard clone, containing 
     * deepcloned values from the game sequences StaticBoard/DynamicBoard
     * instance.
     */
    public StaticBoard getStaticBoard(){
        return sb;
    }
    
    /**
     * Unused colors in DynamicBoard.
     * From staticBoard to DynamicBoard to white.
     */
    public void setUnusedColorsDynamicBoard(){
        sb.setDead(Color.WHITE);
        sb.setBackground(Color.WHITE);
    }
    
    /**
     * @param width is the initPattern width.
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * @param height the initPattern height.
     */
    public void setHeight(int height){
        this.height = height;
    }
    
    /**
     * @param width input put directly into the canvas's setWidth property. 
     */
    public void setGenCanvasWidth(double width){  
        peCtrl.getGenCanvas().setWidth(width);
    }
    
    /**
     * @param pattern is the byte[][] pattern to be set.
     */
    public void setInitPattern(byte[][] pattern) {
        this.initPattern = pattern;
    }
    
    /**
     * @param sb is the staticBoard object to be set.
     */
    public void setStaticBoard(StaticBoard sb){
        this.sb = sb;
    }
    
    /**
     * @param frames is the desired frames of the GIF file.
     */
    public void setFrames(int frames){
        this.stripFrames = frames;
    }
}
