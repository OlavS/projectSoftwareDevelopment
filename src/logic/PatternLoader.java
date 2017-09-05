package logic;

import javafx.scene.input.KeyEvent;

/**
 * Contains everything that is nessesary to implement the load pattern 
 * functionality/RLE decoder found in class LoadPattern.
 * @author Olav Sørlie and Øyvind Mjelstad.
 */
public interface PatternLoader{
    
    /**
     * Initiates the loadPattern sequence.
     * Setting the objects loadedPattern(byte[][]) to the input pattern.
     * And setting the start indexes to the loaded pattern.
     * To initiate the draw sequence.
     * @param n the loaded pattern.
     * @see logic.PatternLoader#setStartIndexLoaded() 
     * @see logic.PatternLoader#drawLoaded() 
     */
    void updateBoardWithPattern(byte[][] n);
    
    /**
     * Setting the start indexes to the loaded pattern.
     * The startindexes is used both to draw the loaded pattern and to 
     * glue it to the current generation. It is manipulated through the 
     * moveLoaded method.
     * @see logic.PatternLoader#moveLoaded(javafx.scene.input.KeyEvent) 
     * @see logic.PatternLoader#drawLoaded() 
     * @see logic.PatternLoader#glueLoaded() 
     */
    void setStartIndexLoaded();
    
    /**
     * Draws the loaded pattern to the Gameboard.
     * Drawing the pattern on top of the current generation, without changeing 
     * the current generation's values. The attachment happends in the 
     * glueloaded sequence.
     * @see logic.PatternLoader#glueLoaded() 
     */
    void drawLoaded();
    
    /**
     * Allows the user to move loaded patterns, and attach it to the generation.
     * By using KeyEvent. 
     * @param e KeyEvent
     */
    void moveLoaded(KeyEvent e);
    
    /**
     * Allows the user to rotate the loaded pattern.
     * Transforming the pattern to rotated by creating a new pattern.
     */
    void rotateLoaded();
    
    /**
     * Attaching the loaded pattern to the current generation, by iterating
     * through the indexes the pattern is at and changing the values of the 
     * generation accordingly.
     */
    void glueLoaded();
}
