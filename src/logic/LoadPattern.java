package logic;

import controller.GoLController;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
* Reading and decoding RLE patterns from either files or URL.
* Game of Life patterns is commonly saved as RLE files/URL's.
* This class reads these files/URLs and decodes them to a byte[][].
* Sending them to the Gameboard, and there the user is granted the oppertunity
* to place the loaded pattern(byte[][]) desierably.
* @author Olav Sørlie and Øyvind Mjelstad.
*/
public class LoadPattern {
    
    private File file;
    private BufferedReader bReader;
    private StringBuilder builder;

    private int width;
    private int height;
    private byte[][] pattern;
    private String name;
    private String ruleB;
    private String ruleS;
    private String rawPattern;
    
    GoLController ctrl;
    
    /**
    * Loads patterns from the web. 
    * Initiating a urlDialog, sending the URL String created as a URL object to 
    * readGameboard.
    * @return the decoded data represented in a byte[][].
    * @throws IOException is thrown and a dialog is shown to the user.
    * @see logic.LoadPattern#urlDialog() 
    * @see logic.LoadPattern#readGameboard(java.io.Reader) 
    */
    public byte[][] readGameBoardFromURL() throws IOException{
        String temp = urlDialog();
        
        if(temp == null){
            return null;
        }
        URL destination = new URL(temp);
        URLConnection conn = destination.openConnection();
        pattern = readGameboard(new InputStreamReader(conn.getInputStream()));
        
        return pattern;
    }
    
    /**
     * Initiates a JavaFX TextInputDialog object that allows the user to type 
     * in a URL direction.
     * This metode is initiated by the readGameboardFromURL metode.
     * @return url.get() url.get() is the URL represented as a String.
     * @see logic.LoadPattern#readGameBoardFromURL() 
     */
    public String urlDialog(){
        TextInputDialog dialog = new TextInputDialog("Type here");
        dialog.setTitle("URL");
        dialog.setHeaderText("URL direction");
        dialog.setContentText("Type in your desiered direction:");
        Stage urlStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        urlStage.getIcons().add(new Image("file:icon.jpg"));
        Optional<String> url = null;
        
        try{  
            url = dialog.showAndWait();
            if(url.equals(Optional.empty())){
                return null;
            }
            if(!url.get().matches("^(https?|ftp|file)://[-a-zA-Z0-9@&/#%|;,+.:!?_~=]"
                                + "+[-a-zA-Z0-9@&/#%|+!?_~=]\\.rle")){
                throw new IOException();
            }
        }
        catch (IOException i){
                new IOExceptionDialog("The URL you entered is invalid, "
                                    + "it has to end with .rle");
                return null;
        }
        return url.get();
    }
    
    /**
     * Loads patterns from a local file.
     * Initiating a fileDialog, letting the user choose the file to be read and 
     * decoded. 
     * @return the decoded data represented in a byte[][].
     * @throws IOException IOException is thrown and a dialog is shown to the user.
     * @see logic.LoadPattern#fileDialog() fileDialog.
     */
    public byte[][] readGameboardFromFile() throws IOException{
        fileDialog();
        
        if(file == null){
            return null;
        }
        pattern = readGameboard(new FileReader(file)); 
        return pattern;
    }
    
    /**
     * Initiates a JavaFX FileChooser object that allows the user 
     * choose a file from his/her local disc.
     * This metode is initiated by the readGameboardFromFile metode.
     * @return contains the location data for the file on the disc.
     * @see logic.LoadPattern#readGameboardFromFile() 
     */
    public File fileDialog(){
        File dir = new File("./Patterns");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose pattern:");
        ExtensionFilter filter = new ExtensionFilter("RLE", "*.rle");
        chooser.getExtensionFilters().addAll(filter);
                 
        if(dir.exists()){
            chooser.setInitialDirectory(dir);
        }
        file = chooser.showOpenDialog(new Stage());

        if(file == null){
            return null;
        }
        return file;
    }
    
    /**
     * Reads source data from URL/File.
     * Reads through the source by using a Reader referance, and creates a 
     * StringBuilder object based on the characters in the source. 
     * Sending the StringBuilder to the decodeHeader(StringBuilder builder).
     * @param r is a Reader reference.
     * @return is the decoded data represented in a 2D byte array.
     * @see logic.LoadPattern#decodeHeader(java.lang.StringBuilder) 
     */
    public byte[][] readGameboard(Reader r){
        //Testing Reader referance:
        try{
            if(r == null){
                throw new IOException();
            }
        }
        catch(IOException i){
                new IOExceptionDialog("Reader reference doesnt have "
                                    + "any object related to it");
        }
        //Reades through the source and creates a StringBuilder object:
        try{
            bReader = new BufferedReader(r);
            builder = new StringBuilder();
            String line = bReader.readLine();
            
            while (line != null){ 
                builder.append(line);
                builder.append(System.lineSeparator());
                line = bReader.readLine();
            }   
        }
        catch(IOException e){
            new IOExceptionDialog("Content in source not supported "
                                + "for StringBuilder.");
        }
        finally{
            try{
                bReader.close();
            }
            catch(IOException e){
                new IOExceptionDialog("Failed to close BufferReader.");                        
            }
        }
        pattern = decodeHeader(builder);
        return pattern;
    }
    
    /**
     * Collects header information based on source data.
     * This method uses the Pattern and Matcher classes to retrieve x and y information
     * to the byte[][] that will represent the pattern when it has been decoded.
     * It also retrieves a ruleset, that represents the rules that goes along with the pattern.
     * @param builder is a StringBuilder object that contains the String data of the source.
     * @return the decoded data represented in a byte[][].
     */                              
    public byte[][] decodeHeader(StringBuilder builder){
        String nameRe = "(#N) ?(.*)",
                  xRe = "x ?= ?(\\d+),? ?",
                  yRe = "y ?= ?(\\d+)",
               ruleRe = "(rule ?= ?)(B)?([0-8]+)\\/(S)?([0-8]+)";
                
        ctrl = GoLController.instance; //comment out when running LoadPatternTest (1/4)
        Pattern namePattern = Pattern.compile(nameRe);
        Matcher nameMatcher = namePattern.matcher(builder);
            
        if(nameMatcher.find()){
            name = nameMatcher.group(2);
            ctrl.setPatternName(name); //comment out when running LoadPatternTest (2/4)
        }
        else{
            name = "noname";
            ctrl.setPatternName(name); //comment out when running LoadPatternTest (3/4)
            }
        Pattern sizePattern = Pattern.compile(xRe + yRe, Pattern.CASE_INSENSITIVE);
        Matcher sizeMatcher = sizePattern.matcher(builder);
            
        try{
            if(sizeMatcher.find()){
                width = Integer.parseInt(sizeMatcher.group(1));
                height = Integer.parseInt(sizeMatcher.group(2));
            }
            else{
                throw new PatternFormatException(); 
            }
        }
        catch(PatternFormatException o){
            new PatternFormatExceptionDialog("Pattern height and width formated wrong or not present in source");
        }
        Pattern rulePattern = Pattern.compile(ruleRe, Pattern.CASE_INSENSITIVE);
        Matcher ruleMatcher = rulePattern.matcher(builder);
                
        if(ruleMatcher.find()){
            ruleB = ruleMatcher.group(3).trim();
            ruleS = ruleMatcher.group(5).trim();   
        }
        else{  
            ruleB = "3";
            ruleS = "23";
        }  
    ctrl.setPatternRules(ruleB, ruleS); //comment out when running LoadPatternTest (4/4)
    pattern = decodePattern(builder); 
    
    return pattern;
    }
    
    /**
     * Decodes the pattern logic.
     * This metode uses the Pattern and Matcher classes of Java, to decode the 
     * board representation logic from the source. Returning null if 
     * patternLogic equals null.
     * <br>
     * Decoder description:<br>
     *   o = 1<br>
     *   b = 0<br>
     *  2o = 11<br>
     *  3b = 000<br>
     *  3$ = y += 3<br>
     *   ! = end
     * 
     * @param builder is a StringBuilder object that contains the 
     * String data from the source.
     * @return the decoded data represented in a byte[][]. 
     */
    public byte[][] decodePattern(StringBuilder builder){
        int yIndex  = 0;
        int yPattLog = 0;
        final String cells = "([1-9]\\d*)*?([bo$!])";
        
        pattern = new byte[height][width];
        Pattern cellPattern = Pattern.compile(cells);
        String[] patternLogic = getPatternLogic(builder);

        if(patternLogic == null){
            return null;
        }
        while(yIndex<pattern.length){

            String line = patternLogic[yPattLog++];
            line = line.trim();
            
            Matcher cellMatcher = cellPattern.matcher(line);
            
            int number = 0;
            int xIndex = 0;
            
            while(cellMatcher.find()){
                //Looking for numbers:
                if(cellMatcher.group(1) != null){
                    number = Integer.parseInt(cellMatcher.group(1));
                }
                try{
                    switch (cellMatcher.group(2)){
                        //Looking for live notation:
                        case "o": 
                            xIndex++;
                            assignValues(number, yIndex, xIndex,(byte)1);
                            xIndex = addToRealX(number, xIndex);
                            number = 0;
                            
                            break;
                        //Looking for dead notation:
                        case "b":
                            xIndex++;
                            assignValues(number, yIndex, xIndex,(byte)0);
                            xIndex = addToRealX(number, xIndex);
                            number = 0;
                                
                            break;
                        case "$":
                            yIndex++;
                                
                            if(number>0){
                                yIndex += number-1;
                            }
                            number = 0;   
                            cellMatcher.find();
                                
                            break;
                        //Trigging "end of line", and returns pattern:
                        case "!":
                            return pattern; 
                    
                        default:
                            throw new PatternFormatException();
                    }
                }
                catch(PatternFormatException i){  
                    new PatternFormatExceptionDialog("Failed to parse logic in source.");
                }
            }
        }
        return null;
    }
    
    /**
     * Returnes the pattern logic section from the source.
     * This metode separates the pattern logic section from the rest of the 
     * String data in the source. And splits that data to a String[], 
     * splitted on pattern lines. 
     * The String[] is used in the decodePattern.
     * @param builder is a StringBuilder object that contains the 
     * String data of the source.
     * @return is a String[] containing splitted pattern logic.
     * @see logic.LoadPattern#decodePattern(java.lang.StringBuilder) 
     */
    public String[] getPatternLogic(StringBuilder builder){
        final String splitAt = "(?<=\\$)",
                   findLogic = "(^[ \\d*]?[ ]?[\\r\\n$[\\d*(o|b)+|\\d*(o|b)+]\\n\\r]*[!])";
        
        Pattern findPattern = Pattern.compile(findLogic, Pattern.DOTALL | Pattern.MULTILINE);
        Matcher patternMatcher = findPattern.matcher(builder);
        rawPattern = null;
        try{ 
            if(patternMatcher.find()){        
                rawPattern = patternMatcher.group(0);
            }
            else{  
                throw new PatternFormatException();
            }
        }
        catch(PatternFormatException i){
            new PatternFormatExceptionDialog("No raw pattern found or "
                                           + "unsupported raw pattern"); 
        }
        //Creating String[]:
        String[] splitted = null;
        
        try{
            if(rawPattern != null){
                splitted = rawPattern.split(splitAt);
            }
            else{
                throw new PatternFormatException();
            }
        }
        catch(PatternFormatException i){   
            new PatternFormatExceptionDialog("Error splitting pattern logic to String[], missing line separators($)");
        }
           
    return splitted;
    }
    
    /**
     * Adding number to realX.
     * Adding the number to the realX param, subtracting 1.
     * @param number the numbers from the pattern logic
     * @param xIndex the pattern array index.
     * @return the pattern array index.
     */
    public int addToRealX(int number, int xIndex){
        if(number>0){
            xIndex += number-1;
        }
        return xIndex;
    }
    
    /**
     * Assigns values to the byte[][].
     * This byte[][] is the decoded pattern.
     * @param number is numbers from the pattern logic.
     * @param yIndex is the y index we are at in the decoding cycle.
     * @param xIndex is the pattern array index.
     * @param value represents dead or live cells, being either 1 or 0.
     */
    public void assignValues(int number, int yIndex, int xIndex, byte value){
        int numbTimes = 0;
        try{
            do{
                pattern[yIndex][xIndex+numbTimes-1] = value;
                numbTimes++;
            }
            while(numbTimes<number);             
        }
        catch(ArrayIndexOutOfBoundsException e){ 
            new PatternFormatExceptionDialog("ArrayIndexOutOfBounds reading "
                                            + "RLE logic: Pattern logic "
                                            + "contains errors.");
        }
    }

    /**
     * Creates a toString containing, name, size, rules, 
     * rawpattern and the decoded pattern for this class.
     * @return name, sizes, ruleset, rawpattern and the byte[][] represented
     * in a String.
     */
    @Override
    public String toString(){
        String result  = "Name: " + name + ". Size x = " + width + ", y = " 
                         + height + ". Rules B" + ruleB.trim() + "/S" 
                         + ruleS.trim() + ". Rawpattern: " + rawPattern.trim() 
                         + " Returned:";
        
        for(int i = 0; i<pattern.length; i++){
            for(int j = 0; j<pattern[0].length; j++){
                result += pattern[i][j];
            }
        }
        return result;
    }
}