package logic;

import controller.GoLController;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;



/**
 * This class is the superclass for both the DynamicBoard- and StaticBoard representations.
 * Everything that the two subclasses share are initiated in this class. It also contains
 * abstract methods that are in both subclasses in order to be able to create an 
 * instance of both DynamicBoard and StaticBoard with reference to the Gameboard.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public abstract class Gameboard implements PatternLoader{
    
    GraphicsContext gc;   
    GoLController ctrl;

    private Color living = Color.PURPLE;   
    private double cellSize = 12; 
    private byte[][] loadedPattern;
    private int genCount = 1;
    private int pattMovesY;
    private int pattMovesX;
    
    //Constructor
    public Gameboard(GraphicsContext gc){
        this.gc = gc;
        this.ctrl = GoLController.instance;
    }
    
    // Empty constructor in order enable empty StaticBoard-constructor
    public Gameboard() {
    
    }
    
    public void resetGenCount() {
        genCount = 1;
    }  
   
    /**
     * Increases the generation counter by one.
     */
    public void incGenCount() {
        genCount = genCount+1;
    }
    
    
    /**
     * Allows the user to move loaded patterns, and attach it to the generation.
     * By using KeyEvent.<br>
     * Description:<br>
     * key S: moves down<br>
     * key W: moves up<br>
     * key D: moves right<br>
     * key A: moves left<br>
     * key E: rotates <br>
     * key CONTROL: glue pattern<br>
     * key ESCAPE: exit pattern placement. 
     */
    @Override
    public void moveLoaded(KeyEvent e){     
        switch (e.getCode()) {   
            case S:
               if(loadedPattern!=null){
                    pattMovesY += 1;
                break;
               }
            case W:
                if(loadedPattern!=null) {
                    pattMovesY -= 1;
                }
                break;
            case D:
                if(loadedPattern!=null) {
                    pattMovesX += 1;
                }
                break;
            case A:
                if(loadedPattern!=null) {
                    pattMovesX -= 1;
                }
                break;
            case E:
                rotateLoaded();
                break;
            case CONTROL:
                glueLoaded();
                break;
            case ESCAPE:
                loadedPattern = null;
                break;           
        }
        draw();

    }
    
    /**
     * Changes the cellSize and calls draw at the end, to animate a zoom effect
     * on the canvas.
     * @param cellSize cellsize is the visual pixel size of the cells.
     */
    public void changeCellSize(double cellSize){ 
        this.cellSize = cellSize;
        draw();
        }
    
    /**
     * Changes living cell color based on ColorPicker/user choice.
     * @param c Color input
     */
    public void changeLivingCellColor(Color c) { 
        living = c; 
        draw();
    }

    /**
     * @return a string constructed for the generation counter label in the 
     *         controller/FXML.
     */
    public String getGenCounter() {
        return ("Generation: " + genCount);
    }
    
    /**
     * @return the size of the cells in pixels.
     */
    public double getCellSize(){
        
        return cellSize;
    }
    
    /**
     * @return the living cell color. JavaFX Color.
     */
    public Color getLiving(){
        
        return living;
    }
    
    
    public byte[][] getLoadedPattern() {
        return loadedPattern;
    }

    public void setLoadedPattern(byte[][] loadedPattern) {
        this.loadedPattern = loadedPattern;
    }

    public int getGenCount() {
        return genCount;
    }

    public void setGenCount(int genCount) {
        this.genCount = genCount;
    }

    public int getPattMovesY() {
        return pattMovesY;
    }

    public void setPattMovesY(int pattMovesY) {
        this.pattMovesY = pattMovesY;
    }

    public int getPattMovesX() {
        return pattMovesX;
    }

    public void setPattMovesX(int pattMovesX) {
        this.pattMovesX = pattMovesX;
    }

    /**
     * @param living the living cell color. JavaFX Color.
     */
    public void setLiving(Color living){
        
        this.living = living;
    }
    
    /**
     * @param cellSize the size of the cells in pixels.
     */
    public void setCellSize(double cellSize){
        
        this.cellSize = cellSize;
    }

    public abstract void displayNextGen();
    public abstract void resetBoard();
    public abstract void changeCellStatus(MouseEvent e);
    public abstract void draw();
    public abstract void changeBoardSize();
}
