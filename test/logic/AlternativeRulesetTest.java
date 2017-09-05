
package logic;

import logic.NextGeneration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing decodeRuleset and calcNextGen with diffrent ruleset.
 * 3 diffrent ruleset tests, and one decodeRuleset test.
 * 
 * @author Olav
 */
public class AlternativeRulesetTest {
    
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
     * Test of ruleset 2x2: B36/S125.
     */
    @Test
    public void testRulesetTwoByTwo() {
        System.out.println("2x2: B36/S125");
        byte[][] twobytwo = { 
            {0,0,0,0,0,0},
            {0,1,0,0,0,0},
            {0,1,0,0,1,0},
            {0,1,0,1,0,0},
            {0,0,0,0,0,0},
            {0,0,0,1,0,0},
            {0,0,0,0,0,0},
        };   
        
        byte[][] expResult =  { 
            {0,0,0,0,0,0},
            {0,1,0,0,0,0},
            {1,1,0,0,1,0},
            {0,1,1,1,0,0},
            {0,0,1,0,0,0},
            {0,0,0,0,0,0},
            {0,0,0,0,0,0},
        };
        
        ng.decodeRuleset("36", "125");
        byte[][] result = ng.calcNextGen(twobytwo);
        assertArrayEquals(expResult, result);
    }
    
     /**
     * Test of ruleset Highlife: B36/S23.
     */
    @Test
    public void testRulesetHighlife() {
        System.out.println("Highlife: B36/S23");
        byte[][] highlife = { 
            {0,0,0,0},
            {0,0,1,0},
            {0,1,1,0},
            {0,0,0,0},
        };        
        
        byte[][] expResult =  { 
            {0,0,0,0},
            {0,1,1,0},
            {0,1,1,0},
            {0,0,0,0},
        };
        
        ng.decodeRuleset("36", "23");
        byte[][] result = ng.calcNextGen(highlife);
        assertArrayEquals(expResult, result);
    }
        
    /**
     * Test of ruleset Conway's: B3/S23.
     */
    @Test
    public void testRulesetConways() {
        System.out.println("Conways: B3/S23");
        byte[][] conways = {
            {0,0,0,0,0},
            {0,0,1,0,0},
            {0,0,0,1,0},
            {0,1,1,1,0},
            {0,0,0,0,0},
        };      
        
        byte[][] expResult =  { 
            {0,0,0,0,0},
            {0,0,0,0,0},
            {0,1,0,1,0},
            {0,0,1,1,0},
            {0,0,1,0,0},
        };
        
        ng.decodeRuleset("3", "23");
        byte[][] result = ng.calcNextGen(conways);
        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test of ruleset decoder at B36/S125
     */
    @Test
    public void testDecodeRuleset(){
        System.out.println("DecodeRulesetTest:B36/S125");
        
        String birth = "36";
        String stillAlive = "125";
        
        boolean[] born = {false, false, false, true, false, false, true, false, false};
        boolean[] survives = {false, true, true, false, false, true, false, false, false};
        
        ng.decodeRuleset(birth, stillAlive);
        assertArrayEquals(born, ng.getBorn());
        assertArrayEquals(survives, ng.getSurvives());
    }
}
