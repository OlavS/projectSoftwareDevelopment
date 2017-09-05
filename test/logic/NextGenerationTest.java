/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Arbeid
 */
public class NextGenerationTest {
    
    StaticBoard sb = new StaticBoard();
    NextGeneration ng = new NextGeneration();
    
    @BeforeClass
    public static void setUpClass() {
        System.out.println("Before class");
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("After class");

    }
    
    /**
     * Test of calcNextGen method.
     */
    @Test
    public void testCalcNextGen() {
        System.out.println("calcNextGen");
        byte[][] testBoard1 = { 
            {0,0,0,0,0},
            {0,0,0,0,0},
            {0,1,1,1,0},
            {0,0,0,0,0},
            {0,0,0,0,0},
        };        
        byte[][] expResult =  { 
            {0,0,0,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,0,0,0},
        }; 
        byte[][] result = ng.calcNextGen(testBoard1);
        assertArrayEquals(expResult, result);
    }
    
    /**
    * Test of calcNextGen method (String-representation).
    */
    @Test
    public void testNextGenToString() {
            
        byte[][] testBoard2 = { 
            {0,0,0,0},
            {0,1,1,0},
            {0,1,1,0},
            {0,0,0,0}, 
        };  
        
        sb.setCurrentGen(testBoard2);
            
        String expResult = "Current generation: 0000011001100000\n cell size: 15, array size X: 4, array size Y: 4";
        
        
        //"\n cell size: " + cellSize + ", array size X: " + width + ", array size Y: " + height;
        //"Current generation: "
        
        StaticBoard gb = new StaticBoard();
        gb.setCurrentGen(testBoard2);
        testBoard2 = ng.calcNextGen(testBoard2);
    
        String result = sb.toString();
    
        assertEquals(expResult, result);
        
    }
        
    /**
    * Test of countNeighbours method.
    */
    @Test
    public void testCountNeighbours() {
        System.out.println("countNeighbours");
        int i = 1;
        int j = 1;
        
        byte[][] currentGen = { 
            {0,1,1,0,0},
            {0,0,1,0,0},
            {0,1,1,0,0},
            {0,0,0,0,0},
            {0,0,0,0,0},
        };
    
        int expResult = 5;
        int result = NextGeneration.countNeighbours(i, j, currentGen);
        assertEquals(expResult, result);
        System.out.print(NextGeneration.countNeighbours(i, j, currentGen));
        // countNeighbours seems to be working.
    }

    /**
    * Test of checkBorders method, of class NextGeneration.
    */
    @Test
    public void testCheckBorders() {
        System.out.println("checkBorders");
        int i = 6;
        int j = 6;
        byte[][] currentGen = { 
            {0,1,1,0,0},
            {1,0,1,0,0},
            {0,1,0,0,0},
            {0,0,0,0,0},
            {0,0,0,0,0},
        };        
        int expResult = 0;
        int result = NextGeneration.checkBorders(i, j, currentGen);
        assertEquals(expResult, result);
        //Checking position (6,6) which is outside the currentGen-matrix.
    }
}
