package logic;

import controller.GoLController;
import java.util.Iterator; 
import java.util.NoSuchElementException;

/**
 * A Generation is a representation of living cells at a point in time.
 * This Generation superclass contains the methods for converting an index to integer, and vice versa.
 * Generation implements Iterable to enable the enhanced for-loop in the DynamicBoard's draw-method.
 * <br>
 * The abstract methods are restrictions every subclass needs to implement.<br>
 * We have created two subclasses: <br>
 *      GenerationHash          - A HashMap representation<br>
 *      GenerationConcurrHash   - A ConcurrentHashMap representation that uses threads for better performance
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public abstract class Generation implements Iterable<Integer>, DynamicWorld{
    GoLController ctrl;
    
    private int area;
    private final int areaInc;

    public Generation(int area, int areaInc){
	this.area = area;
        this.areaInc = areaInc;
        this.ctrl = GoLController.instance;
    }
    
    /**
     * Takes two integers as argument and converts them to an integer.
     * This method is used because the living-cells are represented as
     * an integers, and drawn as an indecies.
     * @param i row
     * @param j column
     * @return integer representation of the index
     */
    public int indToInt(int i, int j){
	if(i<0 || j<0 )
          return -1; 
        
	return i*area + j;
    }
	
    /**
     * Takes an integer as argument and convert it to an index
     * Creates a int-array with size 2. Uses position 0 as the
     * row, and position 1 as the column. 
     * @param i position int the map
     * @return index representation of the integer
     */
    public int[] intToInd(int i){
	int[] ind = new int[2];
	ind[0] = (int)(i/area);
	ind[1] = i - ind[0]*area ;
        return ind;
    }
    
    /**
     * Duplicate of the method above, but with an extra argument (area).
     * This method is only used in the expand-world method in the subclasses of 
     * generation to make the area of the expanded world match the area the new
     * cells use.
     * @param i position in the map
     * @param area corresponding area of the board
     * @return index representation of the integer
     */
    public int[] intToInd(int i, int area){
	int[] ind = new int[2];
	ind[0] = (int)(i/area);
	ind[1] = i - ind[0]*area ;
        return ind;
    }
    
    /**
     * Duplicate of the method indToInt but with an extra argument (area).
     * This method is only used in the expand-world method in the subclasses of
     * generation to make the area of the expanded world match the area the new
     * cells use.
     * @param i row
     * @param j colum
     * @param area corresponding area board
     * @return integer representation of the index
     */
    public int indToInt(int i, int j, int area){
	return i*area + j;
    }
    
    /**
     * Changes area of both the dynamic board and the generation-area.
     * @param input new area
     */
    public void changeBoardSize(int input){
        DynamicBoard.area = input;
        this.area = input;
    }
    
    public void setArea(int area) {
        this.area = area;
    }
    
    public int getAreaInc() {
        return areaInc;
    }
    
    public int getArea() {
        return area;
    }
    
    @Override
    public abstract Iterator<Integer> iterator();     
    
    public abstract void clearPopulation(); 
    public abstract Generation calcNextGen();
    public abstract void addLivingCell(int loc);
    public abstract void removeLivingCell(int i);
    public abstract int getPopulation();
    public abstract byte[][] convertToStatic();
}
