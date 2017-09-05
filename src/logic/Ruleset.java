package logic;

/**
 * Contains every method that is nessesary to implement a changeable ruleset.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
interface Ruleset{
    
    /**
     * Decodes rulesets from userinput and RLE file/url.
     * This ruleset is being used in the calcNextGen method. 
     * It allows a diffrent variation of the game.
     * The input is checked with a matcher, and throws PatternFormatException 
     * if the ruleset was invalid.
     * @param ruleB String that contains number chars. 
     * Representing when a cell should be born.
     * @param ruleS String that contains number chars. 
     * Representing when a cell should survive.
     */
    void decodeRuleset(String ruleB, String ruleS);
    
    /**
     * Born rules gives restrictions for when a cell should be born.
     * It is tested on dead cells only.
     * @param birth is a boolean[9].
     */
    void setBorn(boolean[] birth);
    
    /**
     * Survives rules gives restrictions for when a cell should survive.
     * It is tested on live cells only.
     * @param surviving is a boolean[9].
     */
    void setSurvives(boolean[] surviving);
    
    /**
     * @return is a boolean[9].
     * @see logic.NextGeneration#setBorn(boolean[]) 
     */
    boolean[] getBorn();
    
    /**
     * @return is a boolean[9].
     * @see logic.NextGeneration#setSurvives(boolean[]) 
     */
    boolean[] getSurvives();
}