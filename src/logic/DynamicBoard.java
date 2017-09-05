package logic;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A representation of the Game of Life with dynamic implementation allowing the
 * board to expand. The Dynamic Board uses Generation-objects to represent the
 * game.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public class DynamicBoard extends Gameboard implements Ruleset{
             
    private int moveX = 0;
    private int moveY = 0;
    private final int areaInc = 1;
    private double offsetX;
    private double offsetY;
    private boolean grid = true;
    
    private boolean[] survives = new boolean[9];
    private boolean[]     born = new boolean[9];
        
    private Generation currentGen;
    
   
    //The default staring area.
    public static int area = 40;
    
    /**
     * Constructor
     * Class constructor with GrahpicsContexts as argument to enable 
     * drawing-calls on canvas.
     * Sets the current Generation to the prefered type.
     * Creates a Generation Manager object to manage the generation-objects.
     * Sets default Game of Life rules.
     * @param gc GraphicsContext
     */
    public DynamicBoard(GraphicsContext gc) {
        super(gc);
        currentGen = new GenerationConcurrHash(area, areaInc);
        this.born[3]     = true;
        this.survives[3] = true;
        this.survives[2] = true;
    }
    
    /**
     * Method for resetting the board to default status.
     * clears population so that there are no living cells
     * draws an empty population
     * resets the counter for living cells and sets it.
     * resets the generation counter
     * sets default rules for the game.
     * sets the movement done by user equal zero so that the drawing-area is 
     * centered.
     * sets the drawing-area and the currentGen-area back to default(25).
     */
    @Override
    public void resetBoard() {
        currentGen.clearPopulation();
        setLivingCells();
        resetGenCount();
        setConwayRules();
        moveX=0;
        moveY=0;
        area = 40;
        currentGen.setArea(40);
        draw();  
    }
    
    /**
     * Creates new boolean array with default conway rules
     */
    public void setConwayRules(){
        born        = new boolean[9];
        survives    = new boolean[9];
        born[3]     = true;
        survives[2] = true;
        survives[3] = true;
    }
    
    /**
     * Sets the offsets in x- and y-direction. Everything that uses offsets are
     * centered in the middle.
     */
    public void setOffset(){
        offsetY = (ctrl.getCanvas().heightProperty().doubleValue()-area*getCellSize())/2;
        offsetX = (ctrl.getCanvas().widthProperty().doubleValue()-area*getCellSize())/2;
    }
    
    /**
     * Uses clear rect to remove overlay
     * Iterates through the keys of the living cells hashmap and draws
     * Uses cellsize*0.9 in order to make some whitespace in-between cells
     * Saves the generation in the generation-pool.
     * Checks to see the status of the grid (on/off).
     */
    @Override
    public void draw(){
        gc.clearRect(0, 0, ctrl.getCanvas().widthProperty().doubleValue(), 
                           ctrl.getCanvas().heightProperty().doubleValue());
        setOffset();
        drawLoaded();
        
        for(int location : currentGen) {
            int[] ind = currentGen.intToInd(location);
            gc.setFill(getLiving());
            gc.fillRect(offsetX+moveX+(getCellSize()*ind[1]), 
                        offsetY+moveY+(getCellSize()*ind[0]), 
                        getCellSize()*0.9, getCellSize()*0.9);
        }
        setArea();
        if(grid)    
            drawGrid();
    }
    
    
    /**
     * Draws the pattern loaded in by user, either through URL or as a RLE file.
     * Uses 2D representation to load pattern. Every living cell is set to grey 
     * with opacity-level 0.8, and every dead cell is transparent.
     */
    @Override
    public void drawLoaded(){
        if(getLoadedPattern() != null){
            gc.setFill(Color.rgb(192, 192, 192, 0.8));
            for(int i = 0; i<getLoadedPattern().length; i++) {
                for(int j = 0; j<getLoadedPattern()[0].length; j++) {
                    if(getLoadedPattern()[i][j]==1) {
                         gc.fillRect(offsetX+getPattMovesX()*getCellSize()+moveX+(getCellSize()*j), 
                                     offsetY+getPattMovesY()*getCellSize()+moveY+(getCellSize()*i), 
                                     getCellSize()*0.9, getCellSize()*0.9); 
                    }
                }
            }
        }
    }
    
   /**
    * Calls the calcNextGen method and sets the current Generation-object equal 
    * to the next generation-object.
    * Increases the generation counter by 1 and sets living cells
    * Calls the draw method to show user the new generation.
    */
    @Override
    public void displayNextGen(){
        currentGen = currentGen.calcNextGen();
        setLivingCells();
        incGenCount();
        draw();
    }
   
    /**
     * Takes a mouse event as argument
     * Gets the coordinate of the mouse event. Casts it to int and diviveds
     * by the cell size in order to get the corresponding cell to become alive/die.
     * An if-test checks to see if the mouse event is within the canvas and area-size.
     * An if-test to see whether it is right or left mouse dragged. <br>
     * Right mousedragged = create living cell<br>
     * Left mousedragged  = remove living cell
     * @param e MouseEvent
     */
    @Override
    public void changeCellStatus(MouseEvent e) {
        int i = (int)((e.getY()-offsetY-moveY)/getCellSize()); 
        int j = (int)((e.getX()-offsetX-moveX)/getCellSize());
        
        //Avoid drawing outside canvas and board
        if(e.getX() < 0 || e.getY() < 0 || i > area-1 || j > area-1){
            return;
        }
        if(e.isPrimaryButtonDown()) {
            int temp = currentGen.indToInt(i, j);
            
            if(temp<0){
                return;
            }
            currentGen.addLivingCell(temp);
            setLivingCells();
            draw();
        }
        if(e.isSecondaryButtonDown()){           
            currentGen.removeLivingCell(currentGen.indToInt(i, j));
            draw();
        }    
    }

    /**
     * Draws a grid using horizontal and vertical lines. The constant -1.8 makes
     * sure that the living cells matches the grid-lines. The offsets and moveX
     * are used to make the grids position correct relative to userinputs.
     * The grid also makes a visualisation of the drawable-area. 
     */
    public void drawGrid() {  
        gc.setLineWidth(0.1*getCellSize());
            for (double i = 1; i<(area*getCellSize())+getCellSize(); i+=getCellSize()) {
                gc.strokeLine((offsetX+moveX+i-1.8), 
                              (offsetY+moveY-1.8), 
                              (offsetX+moveX+i-1.8), 
                              ((area*getCellSize())+(offsetY+moveY)));

                gc.strokeLine((offsetX+moveX-1.8), 
                              (moveY+offsetY+i-1.8), 
                              ((area*getCellSize())+(offsetX+moveX)), 
                              (moveY+offsetY+i-1.8));
            }

    }
    
    /**
     * Decodes rulesets from userinput and File/URL IO
     * This ruleset is being used in the calcNextGen method. 
     * It allows a diffrent variation of the game.
     * The input is checked with a matcher, and throws PatternFormatException 
     * if the ruleset was invalid.
     * @param ruleB String that contains number chars. Representing 
     *        when a cell should be born.
     * @param ruleS String that contains number chars. Representing 
     *        when a cell should survive.
     */
    @Override
    public void decodeRuleset(String ruleB, String ruleS){
        String[] b;
        String[] s;
        try{
        if(ruleB.matches("([0-8]{1,9})") && ruleS.matches("([0-8]{1,9})")){
            b = ruleB.trim().split("");
            s = ruleS.trim().split("");
        
            boolean[] birth = new boolean[9];
            boolean[] surviving = new boolean[9];
        
            for(int i = 0; i<b.length; i++){
                birth[Integer.parseInt(b[i])] = true;
            }
            for(int i = 0; i<s.length; i++){
                surviving[Integer.parseInt(s[i])] = true;
            }
            setBorn(birth);
            setSurvives(surviving);
        }
        else{
            throw new PatternFormatException(); 
        }
        }
        catch(PatternFormatException e){
            new PatternFormatExceptionDialog("Ruleset contains non-digit values");
        }
    }
    
    /** 
     * Sets the loaded pattern equal to the pattern instance-variable.
     * Allows user to manipulate the loaded pattern.
     * @param input, 2D-array representation
     */
    @Override
    public void updateBoardWithPattern(byte[][] input){        
        setLoadedPattern(input); 
        setStartIndexLoaded();
        draw();
    }
    
    /**
     * Sets the start-position for the loaded patterns relative to the board size.
     * Makes the loaded pattern appear in the center of the draw-able area (grid).
     */
    @Override
    public void setStartIndexLoaded(){
        setPattMovesX((area/2)-(getLoadedPattern().length/2));
        setPattMovesY((area/2)-(getLoadedPattern()[0].length/2));
    }
    
    /**
     * Glues the loaded pattern to the board by converting the indecies of the 
     * living cells in the 2D-array and adding their integer-value to the population map. 
     * Their placement in the map is determined by the movement done by user.
     * Sets living cells and draws the loaded pattern with "living" color.
     */
    @Override
    public void glueLoaded() { 
        if(getLoadedPattern()!=null) {
        for(int i = 0; i<getLoadedPattern().length; i++){
            for(int j = 0; j<getLoadedPattern()[0].length; j++){
                
                if(getLoadedPattern()[i][j] == 1){   
                    if(i+getPattMovesY()<0 || i+getPattMovesY()>=area || j+getPattMovesX()<0 || j+getPattMovesX() >= area){
                        continue;
                    }
                    int temp = currentGen.indToInt(i+getPattMovesY(), j+getPattMovesX());
                    currentGen.addLivingCell(temp);
                }
            }
        }
        setLivingCells();
        draw();
        }
    }
    
    /**
     * Allows the user to move the board around, by using the keyboard.<br>
     * key T: move the board upwards.<br>
     * key G: move the board downwards.<br>
     * key F: move the board to the left.<br>
     * key H: move the board to the right.
     * @param e e KeyEvent allowing user input from keyboard.
     */
    public void navigateBoard(KeyEvent e){
        switch(e.getCode()){
            case G:
                moveY  -= getCellSize();
                break;
            case T:
                moveY  += getCellSize();
                break;
            case H:
                moveX  -= getCellSize();
                break;
            case F:
                moveX  += getCellSize();
                break;         
            default:
                break;
        }
        draw();
    }
    
    /**
     * Rotates the loaded pattern in the clockwise direction by transposing the
     * 2D-array and reversing each row.
    */
    @Override
    public void rotateLoaded(){
        byte[][] rotated = new byte[getLoadedPattern()[0].length][getLoadedPattern().length];
        
        for(int i = 0; i < getLoadedPattern().length; i++){
            for(int j = 0; j < getLoadedPattern()[0].length; j++){
                rotated[j][getLoadedPattern().length-i-1] = getLoadedPattern()[i][j];
            }
        }
        setLoadedPattern(rotated);
    }

    /**
     * Sets the rules for how many living cells a dead cell needs in order
     * to become alive in the next generation. Every cell can have 0-8 neighbours.
     * @param birth is a boolean[9].
     */
    @Override
    public void setBorn(boolean[] birth){
        this.born = birth;
    }
    
    /**
     * Sets the rules for how many living cells a living cell needs in order
     * to survive through to the next generation. Every cell can have 0-8 neighbours.
     * @param surviving is a boolean[9].
     */
    @Override
    public void setSurvives(boolean[] surviving){
        this.survives = surviving;
    }
    
    /**
     * Displays the current number of living cells to the user.
     */
    public void setLivingCells() {
        ctrl.setLivingCellLabelText(getLivingCellCount());
    }
    
    /**
     * Sets grid-status true/false.
     */
    public void setGridStatus() {
        grid = !grid;
    }
    
    /**
     * @return a boolean[9].
     */
    @Override
    public boolean[] getSurvives(){
        return survives;
    }
    
    /**
     * @return a boolean[9].
     */
    @Override
    public boolean[] getBorn(){
        return born;
    }
    
    /**
     * Gets the grid status.
     * @return true if the grid-button is clicked.
     */
    public boolean getGridStatus() {
        return grid; 
    }
    
    /**
     * Displays the current area to the user.
     */
    public void setArea() {
        ctrl.setAreaLabelText(getArea());

    }
    
    /**
     * 
     * @return a String representation of the area
     */
    public String getArea() {
        return ("" + area);
    }
    
    /**
     * Gets the current generation
     * @return The current generation-object
     */
    public Generation getGeneration(){
        return currentGen;
    }

    
    /**
     * @return String representation of the size of the population map.
     */
    public String getLivingCellCount() {
        return ("Population: " + currentGen.getPopulation());
    } 
    
    /**
     * Checks if the input is valid. If invalid, an alertbox will show.
     * We set the cap of our board to 25000x25000.
     * The input also has to be an even number since the board expands +1 in both
     * x- and y-direction.
     * If the board is expanded, the living cells maintain their relative position.
     * If the board-size is set less then its current size, every living cell 
     * is removed and then the board-size is reset.
     */
    @Override
    public void changeBoardSize(){
        try {
            int input = Integer.parseInt(ctrl.getWidthTextfield().getText());
            if(input>25000) {
                input = 25000;
            }
            if(input%2!=0){
                input+=1;
            }
            if(input>area){ 
                while(input>area) {
                    currentGen.expandWorld();
                    input = input--;
                }
            draw();
            }
            else if(input == area) {
                draw();
            } 
            else {
                resetBoard();
                area = input;
                currentGen.changeBoardSize(input);
                draw();
            }   
        }catch(NumberFormatException e){
            System.err.println("User input invalid.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Number Format Error");
            alert.setHeaderText("Please insert non-negative whole even numbers into the size-field.");
            Stage diaStage = (Stage) alert.getDialogPane().getScene().getWindow();
            diaStage.getIcons().add(new Image("file:icon.jpg"));
            alert.showAndWait();       
        }
    }
}

  
