package logic;

import java.util.ArrayList; 

/**
 * This class manages Generation-objects. It stores the generations in a Generation-pool. 
 * This way the user can navigate to previous generations. The generation-pool 
 * has a default cap of 50 generations, but can be expanded.
 * 
 * @deprecated Reason: class is not fully operable and will
 * not be ready in time for the deadline. For later versions the Generation-manager 
 * could be used to gather statistics be utilising the generation-pool.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
@Deprecated
public class GenerationManager{   
    
    private final int poolSize;
    private final ArrayList<Generation> generations = new ArrayList<>();
    private final Generation current;
    
    /**
     * Constructor
     * @param current The current generation
     * @param poolSize The prefered size of the generation-pool.
     */
    public GenerationManager(Generation current, int poolSize){
        this.poolSize = poolSize;
	this.current = current;
	}

    /**
     * Stores generations inside the geneartions-arraylist.
     * @param gen The generation that is stored.
     */
    private void saveGeneration(Generation gen){ 
        generations.add(gen); 
	if(poolSize < 0) return;
        if(generations.size() > poolSize) 
            generations.remove(0);
	}
           
    /**
     * Displays the previous generation
     * @return The previous generation and removes it from the pool
     */
    public Generation prevFrame(){     
        if(generations.isEmpty()){      
            return null;    
        } 
        Generation tail = generations.remove(generations.size()-1);      
        return tail;    
        }
}