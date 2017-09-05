package logic;

/**
 * Next generation calculates the next-current generation.
 * This class takes care of neighbour counting and calculation of the next generation.
 * @author Olav Sørlie
 */
public class NextGeneration implements Ruleset{
    
    private boolean[] survives = new boolean[9];
    private boolean[] born = new boolean[9];
    
    LoadPattern lp = new LoadPattern();
    
    /**
     * Constructor containing the default ruleset Conway's Game of Life.
     */
    public NextGeneration(){
        this.born[3] = true;
        this.survives[2] = true;
        this.survives[3] = true;
    }
    
    /**
     * Deepclone method for Next Generation object.
     * Deepclones the current instance of NextGeneration.
     * @return a deepclone of the current NextGeneration object.
     */
    public NextGeneration getNextGenerationClone(){
        
        NextGeneration clone = new NextGeneration();
        
        clone.born = new boolean[this.born.length];
        clone.survives = new boolean[this.survives.length];
        
        for(int i = 0; i < born.length; i++){
            clone.born[i] = this.born[i];
            clone.survives[i] = this.survives[i];
        }
        return clone;
    }
    
    /**
     * Calculating next generation.
     * Based on the number of neighbours all the cells on the board has,
     * and the ruleset that is currently being used. The default ruleset is 
     * Conway's.
     * @param currGen  is the current generation to be calculated.
     * @return the new current generation, in other words the next generation.
     */
    public byte[][] calcNextGen(byte[][] currGen){
               
        byte[][] nextGen = new byte[currGen.length][currGen[0].length];
       
        for(int i = 0; i<currGen.length; i++){
            
            for(int j = 0; j<currGen[0].length; j++){
                
                int neighbours = countNeighbours(i, j, currGen);
                
                
                if(born[neighbours] && currGen[i][j] == 0){
                
                    nextGen[i][j] = 1;
                
                }else if(survives[neighbours] && currGen[i][j] == 1){
                    
                    nextGen[i][j] = currGen[i][j];
                    
                }else{      
                    
                    nextGen[i][j] = 0;
                
                }
            }
        }
        return nextGen;
    }
    
    
    /**
     * Counts all the cells neighbours.
     * @param i is the x-position in the generation to be counted.
     * @param j is the y-position in the generation to be counted.
     * @param currentGen is the generation to be counted.
     * @return is the number of neighbours the cell has.
     */    
    protected static int countNeighbours(int i, int j, byte[][] currentGen){
        int count = 0;
        
        for(int x = i-1; x<i+2; x++){
            count+=checkBorders(x, j-1, currentGen);
            count+=checkBorders(x, j+1, currentGen);
        }
        count+=checkBorders(i-1, j, currentGen);
        count+=checkBorders(i+1, j, currentGen);

        return count;
    } 
    
    /**
     * Is a support method for countNeighbours.
     * It tries to check the current cell, and throws a ArrayIndexOutOfBounds 
     * if it is outside the borders.
     * @param i x-position of the cell being checked.
     * @param j y-position of the cell being checked.
     * @param currentGen is the current generation to be checked.
     * @return 0 if the position is out of bounds, else the value of the 
     * generation being checked.
     */
    protected static int checkBorders(int i, int j, byte[][] currentGen){
        
        try{
            return currentGen[i][j];
        }
        catch(ArrayIndexOutOfBoundsException e){

        }
        return 0;
    }
    
    /**
     * Decodes rulesets from userinput and RLE file/url.
     * This ruleset is being used in the calcNextGen method. 
     * It allows a diffrent variation of the game.
     * The input is checked with a matcher, and throws PatternFormatException 
     * if the ruleset was invalid.
     * @param ruleB ruleB String that contains number chars. 
     * Representing when a cell should be born.
     * @param ruleS ruleS String that contains number chars. 
     * Representing when a cell should survive.
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
        
            for(int i = 0; i<b.length; i++)
                birth[Integer.parseInt(b[i])] = true;
                        
            for(int i = 0; i<s.length; i++)
                surviving[Integer.parseInt(s[i])] = true;
   
            setBorn(birth);
            setSurvives(surviving);
        
        }else{
        
            throw new PatternFormatException(); 
        }
        }catch(PatternFormatException e){
            
            new PatternFormatExceptionDialog("Ruleset contains non-digit values");
        }
    }
    
    /**
     * Born rules gives restrictions for when a cell should be born.
     * It is tested on dead cells only.
     * @param birth is a boolean[9].
     */
    @Override
    public void setBorn(boolean[] birth){
        this.born = birth;
    }
    
    /**
     * Survives rules gives restrictions for when a cell should survive.
     * It is tested on live cells only.
     * @param surviving is a boolean[9].
     */
    @Override
    public void setSurvives(boolean[] surviving){
        this.survives = surviving;
    }
    
    /**
     * @return is a boolean[9].
     * @see logic.NextGeneration#setSurvives(boolean[]) 
     */
    @Override
    public boolean[] getSurvives(){
        return survives;
    }
    
    /**
     * @return is a boolean[9].
     * @see logic.NextGeneration#setBorn(boolean[]) 
     */
    @Override
    public boolean[] getBorn(){
        return born;
    }
}
