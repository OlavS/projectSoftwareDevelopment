package logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * LoadPatternTest is a jUnit test class.
 * It is testing if the patterns are decoded properly through 
 * the readGameboard metode in the ReadPattern class.
 * Comment out all the marked GoLController commands from the LoadPattern 
 * class to run this test.
 * @author Olav Sørlie
 */
public class LoadPatternTest{
    
    LoadPattern lp;
    
    @BeforeClass
    public static void setUpClass(){
        System.out.println("Before class");
    }
    
    @AfterClass
    public static void tearDownClass(){
        System.out.println("After class");
    }
    /**
     * Testing ReadGameboard.
     * If it can decode a small pattern like a glider.
     * @throws IOException IOException is thrown and a dialog is shown to the user.
     * @throws PatternFormatException PatternFormatException is thrown and a dialog is shown to the user.
     */
    @Test
    public void testReadGameboardSmall() throws IOException, PatternFormatException{
        System.out.println("readGameboard");
        FileReader gliderTest = new FileReader(new File("./Patterns/tester/glider.rle/"));
        byte[][] expResult = {
            {0,1,0},
            {0,0,1},
            {1,1,1},
        };
        lp = new LoadPattern();
        byte[][] result = lp.readGameboard(gliderTest);
        assertArrayEquals(expResult, result);
    }
    /**
     * Testing ReadGameboard.
     * If it can decode a pattern that has skip line logic in it.
     * @throws IOException IOException is thrown and a dialog is shown to the user.
     * @throws PatternFormatException PatternFormatException is thrown and a dialog is shown to the user.
     */
    @Test
    public void testReadGameboardSkipLine() throws IOException, PatternFormatException{
        System.out.println("readGameboard2");
        FileReader TBTGliderTest = new FileReader(new File("./Patterns/tester/2x2linepuffer.rle/"));
        byte[][] expResult = {
            {0,0,0,0,1},
            {0,0,0,1,0},
            {0,0,1,0,0},
            {0,0,0,0,0},
            {1,0,0,0,0},
            {1,0,0,1,0},
            {1,0,1,0,0},
            {0,0,0,0,0},
            {0,0,1,0,0},
        };
        lp = new LoadPattern();
        byte[][] result = lp.readGameboard(TBTGliderTest);
        assertArrayEquals(expResult, result);
    }
    /**
     * Testing ReadGameboard.
     * If it can decode a small pattern.
     * @throws IOException IOException is thrown and a dialog is shown to the user.
     * @throws PatternFormatException PatternFormatException is thrown and a dialog is shown to the user.
     */
    @Test
    public void testReadGameboardLong() throws IOException, PatternFormatException{
        System.out.println("readGameboard3");
        FileReader TBTBlockOscillatorsTest = new FileReader(new File("./Patterns/tester/2x2blockoscillators.rle/"));
        byte[][] expResult = {
            {1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1},
        };
        lp = new LoadPattern();
        byte[][] result = lp.readGameboard(TBTBlockOscillatorsTest);
        assertArrayEquals(expResult, result);
    }
    /**
     * Testing toString.
     * Suppose to return name, size, rules, rawpattern and the decoded pattern 
     * in a singel line String
     * @throws FileNotFoundException FileNotFoundException is thrown if the choosen file is not a path.
     * @throws IOException IOException is thrown and a dialog is shown to the user.
     * @throws PatternFormatException 
     */
    @Test 
    public void testToStringReadGameboard() throws FileNotFoundException, IOException, PatternFormatException{
        System.out.println("readGameboardToString");
        FileReader glider = new FileReader(new File("./Patterns/tester/glider.rle/"));
        String expResult = "Name: noname. Size x = 3, y = 3. Rules B3/S23. Rawpattern: bo$2bo$3o! Returned:010001111";
        lp = new LoadPattern();
        lp.readGameboard(glider);
        String result = lp.toString();
        assertEquals(expResult, result);
    }
}
    
