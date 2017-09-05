package logic;

/**
 * Contains the two methods needed in order to represent a dynamic implementation 
 * of the game.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
interface DynamicWorld{
    boolean needExpansion();  
    void expandWorld();
}
