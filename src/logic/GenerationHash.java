/**
 *
 * @author Olav Sørlie & Øyvind Mjelstad
 */

package logic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * HashMap-implementation
 * A Generation is represented by a map of living cells and a map of
 * the neighbourhood (8 neighbour-cells) of every living cell. Every living cell
 * has a key(location) that represent their position in the matrix. If a cell is
 * on the periphery of the board (area), the next generation could have need
 * for a larger area. Even though the cell at the edge of the board might die in 
 * the next generation, the board will still expand to make sure there is room for the 
 * next generation. These two methods (expandWorld, and needExpansions) are the only 
 * parts of the interface DynamicWorld.
 * <br>
 * The GenerationHash extends Generation, which is the superclass containing indexToInteger
 * and integerToIndex conversions.
 * 
 * @author Olav Sørlie and Øyvind Mjelstad
 */

public class GenerationHash extends Generation{
    
    //Contains the key(location) for every living cell in the map.
    private HashMap<Integer, String> population = new HashMap<>();
    //contains the "neighbouring" eight cells around every living cell
    private HashMap<Integer, Integer> neighbourhood = new HashMap<>();

    public GenerationHash(int area, int areaInc){
	super(area, areaInc);
    }
    
     /**
     * Adds a key,val to the map. This method is used by
     * the change-cell-status method found in DynamicBoard.
     * @param location The location of which the cell is.
     */
    @Override
    public void addLivingCell(int location){
	population.put(location, ""); 
    }
    
    /**
    * Removes the key,val from the map. This metod is used by
    * the change-cell-status method found in DynamicBoard.
    * @param location The location of which the cell is.
    */
    @Override
    public void removeLivingCell(int location){
        population.remove(location, "");
    }

   /**
     * Enables to be the target of an enhanced for-loop.
     * Overrides the iterator in generation
     * This is utilized in the draw-method of the DynamicBoard class.
     * @return an iterator
     */    
    @Override
    public Iterator<Integer> iterator(){
	return population.keySet().iterator();
    }
	
    /**
     * Checks the boundary of the map to see if there are any living cells there.
     * @return true if there are any living cells at the boundary of the map
     */
    @Override
    public boolean needExpansion(){
	for(int cell : population.keySet()){
            int[] ind = intToInd(cell);
            //if on border
            if( ind[0] == 0 || ind[0] == getArea()-1 ||
		ind[1] == 0 || ind[1] == getArea()-1 ) 
            return true;
	}
	return false;
    }
    
    /**
     * Creates a new HashMap and adds the living cells
     * of the current population into the new map. The new living cells
     * are positioned relative to the area-increase. If the area of the new
     * world is greater than the cap (25000), the board is reset.
     */
    @Override
    public void expandWorld(){
	HashMap<Integer, String> newCells = new HashMap<>();
        int newArea = getArea()+2*getAreaInc();
        
        if(newArea > 25000) {
            ctrl.resetBtnClicked();
        }
	
        int[] ind_i = new int[population.size()];
	int[] ind_j = new int[population.size()];
        int i = 0;
	for(int cell : population.keySet()){
            int[] ind = intToInd(cell);
            ind_i[i] = ind[0]+getAreaInc(); 
            ind_j[i] = ind[1]+getAreaInc();
            i++;
	}
	for(i = 0; i<ind_i.length; i++){
            newCells.put(indToInt(ind_i[i], ind_j[i],newArea), "");
        }
        DynamicBoard.area = newArea;
        population        = newCells;
        setArea(newArea);
	}
    
    /**
     * Creates a new map that will contain the living cells of the next generation.
     * Goes through the population-map and checks how many living neigthbours each cells
     * have. Puts the cells that will survive into the nextGen map.
     * Calls the countNeighbours-method which fills the neighbourhood-map with potentially 
     * living cells.
     * <br>
     * Goes through the neighbourhood-map and adds those who have been
     * "ticked" the correct number of times(3) to the population-map.
     * 
     * @return next Generations living cells 
     */
    @Override
    public GenerationHash calcNextGen(){
        long start = startTimer();
        if(needExpansion())
            expandWorld();
		
        GenerationHash nextGen = new GenerationHash(getArea(), getAreaInc());
        neighbourhood.clear();
        for(int livingCell : population.keySet()){
            int neighbours = countNeighbours(livingCell);
	
            if(ctrl.getDynamicBoard().getSurvives()[neighbours])
                nextGen.addLivingCell(livingCell);	
        }	
        for(int deadCell : neighbourhood.keySet()) {
          
            if(ctrl.getDynamicBoard().getBorn()[neighbourhood.get(deadCell)])
                nextGen.addLivingCell(deadCell);
        }
        nextGenerationConcurrentPrintPerformance(start);
        return nextGen;
    }
    
    /**
     * Starts the timer
     * @return the start time when this method was called
     */
    public long startTimer(){
        long start = System.currentTimeMillis();
        return start;
    }
    
    /**
     * Prints the time it took from calculating the next generation to returning
     * the next generation.
     * @param start the start value
     */
    public void nextGenerationConcurrentPrintPerformance(long start){
	long elapsed = System.currentTimeMillis() - start;
	System.out.println("Counting time (ms): " + elapsed);
    }
    
    /**
     * @return the class name
     */
    @Override
    public String toString(){
        return "HashMapGeneration";
    }
	
    
    /**
     * Counts neighbours for every living cell. 
     * @param livingCell
     * @return number of neighbours living(!) for the current cell
     */
    private int countNeighbours(int livingCell){
	int cnt = 0;
        
        //Converts the livingcells position to an index. 
	int[] ind = intToInd(livingCell);
	int i = ind[0], j = ind[1]; 

        //Nortwest,west,southwest - northeast, east, southeast
	for(int x = i-1; x<i+2; x++){
            cnt+= neighbourAt(x, j-1);
            cnt+= neighbourAt(x, j+1);
	}
        //Sole north and sole south
        cnt += neighbourAt(i-1, j);
	cnt += neighbourAt(i+1, j);

	return cnt;
	
    }
    

    /**
     * Three scenarios for this method:<br>
     *      1: The cell is already alive (a key in the population). We return 1
     *         to give the living cell 1 living neighbour.
     *          <br>
     *      2: The cell is not alive and it is the first time we've encounter it.
     *         In this case we put the cell-position into the neighbourhood-map
     *         and gives it a value of 1. The value represents number of times the
     *         cell has been "marked".
     *         <br>
     *      3: The cell is not alive and it has already been "marked".
     *         We replace the existing value of that cell with +1 more than
     *         previous value.
     *      
     * @param i index_i, representing the row
     * @param j index_j, represents the position in the row.
     * @return returns 1 if the neighbour is alive, and 0 if the neighbour is not alive.
     */
    private int neighbourAt(int i, int j){
	int cell;
	try{
            cell = indToInt(i, j);
	
        }catch(NoSuchElementException e){
            return 0;
	}
	
        //Hvis den gjeldene cellen ligger i bunken med levende celler returnerer vi 1 slik
        // at counteren (cnt) i countNeighbours øker med 1.
	if(population.containsKey(cell))
            return 1;
        
	if(!neighbourhood.containsKey(cell)) 
            neighbourhood.put(cell, 1);
        
        else{
            int tmp = neighbourhood.get(cell); 
            neighbourhood.remove(cell);
            neighbourhood.put(cell,tmp+1);
	}
	return 0;
    }
    
    /**
    * Removes all of the mappings from population.
    */
    @Override
    public void clearPopulation() {
        population.clear();
    }

    /**
    * This method makes it possible to constantly track number of living cells.
    * @return number of key-value mappings in population.
    */
    @Override
    public int getPopulation() {
        return population.size();
    }
    
    /**
     * Converts the population-map to a static 2D-array.
     * This method is used when entering the pattern editor.
     * @return 2D-array representation of the population.
     */
    @Override
    public byte[][] convertToStatic() {
        byte tmp[][] = new byte[getArea()][getArea()];
        for(int i = 0; i<getArea(); i++)
            for(int j = 0; j<getArea(); j++) {
                if(population.containsKey(indToInt(i,j))) {
                    tmp[i][j] = 1;
                }
                else
                    tmp[i][j] = 0;     
        }
        return tmp;
    }
}
 