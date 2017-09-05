package metadata;

import controller.PatternEditorController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.stage.FileChooser;
import logic.PatternFormatException;
import logic.PatternFormatExceptionDialog;

/**
 * Save allows the user to save his/her pattern to a RLE file.
 * This class contains all the concrete logic that is nessesary for saving a 
 * pattern to a RLE file.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public class SavePattern{
    
    private PatternEditorController peCtrl; 
    private File file;
    private StringBuilder builder;
    private StringBuilder rawPattern;
    private String born;
    private String survives;
    private byte[][] RLEPattern;
    
    /**
     * SavePattern contructor, attaches the PatternEditorController singelthon
     * object to the peCtrl referance.
     */
    public SavePattern(){
        this.peCtrl = PatternEditorController.peInstance;
    }

    /**
     * Initiation cycle, engaging every method that is nessesary to create a RLE 
     * save file. 
     */
    public void initSavePattern(){
        encodePattern();
        encodePatternDuplicates();
        removeZeroesLineshift();
        encodeMultipleLineshift();
        retrieveRuleStrings();
        checkRuleset();
        buildRLE();
        saveToRLEDialog();
        savePatternFile();
    }
    
    /**
     * Saving the pattern to a RLE file.
     * Initiates a OutputStreamWriter that saves the file to the desired 
     * destination, using saveToRLEDialog's path.
     */
    public void savePatternFile(){
        if(file == null){
            return;
        }
        try{
            OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file));
        
            try{
                output.append(builder.substring(0));
                output.close();
            }
            catch(IOException e){
                System.err.println("No pattern found.");
            }
            if(!file.exists()){
               throw new FileNotFoundException();
            }
        }
        catch(FileNotFoundException e){
                System.err.println("No file saved.");
        }
    }
    
    /**
     * FileChooser, allowing the user to choose a destination path for the RLE file.
     * File path is set to ./Patterns as default, if the folder exists.
     * @return is given a name and has a destination path.
     */
    public File saveToRLEDialog(){
        File dir = new File("./Patterns");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save pattern:");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("RLE", "*.rle");
        chooser.getExtensionFilters().addAll(filter);
        
        if(dir.exists()){
            chooser.setInitialDirectory(dir);
        }
        file = chooser.showSaveDialog(null);
        return file;
    }
    
    /**
     * Encodes the pattern logic.
     * From byte[][] to RLE format.
     * Adding characters to a StringBuilder rawPattern.
     * Description:
     *                1 = "o"
     *                0 = "b"
     *      end of line = "$"
     *  end of byte[][] = "!"
     */
    public void encodePattern(){
        rawPattern = new StringBuilder();
        RLEPattern = trimPattern(peCtrl.getPatternEditor().getInitPattern());
        int count = 0;
        
        for(int i = 0; i < RLEPattern.length; i++){
            for(int j = 0; j < RLEPattern[0].length; j++){
                
                if(RLEPattern[i][j] == 1){
                    rawPattern.append("o");
                }
                else if(RLEPattern[i][j] == 0){
                    rawPattern.append("b");
                }
                count++;
            }            
            if(count != (RLEPattern.length*RLEPattern[0].length))
                rawPattern.append("$");
        }
        rawPattern.append("!");
    }
    
    /**
     * Shrinking the RLE file.
     * Shrinking the RLE file to contain a number and a character for every 
     * line with more than one "o" or "b" in a row. Making sure the file is as
     * small as posible.
     * Sample:
     * "bbb" = "3b"
     * "oooo" = "4o"
     */
    public void encodePatternDuplicates(){
        StringBuilder result = new StringBuilder();
        int count = 1;

        for(int i = 1; i < rawPattern.length(); i++){
            if(rawPattern.charAt(i) == rawPattern.charAt(i-1)){
                count++;
            }
            else{
                if(count > 1){
                    result.append(String.valueOf(count) + rawPattern.charAt(i-1));
                }else{ 
                    result.append(rawPattern.charAt(i-1));
                }
                count = 1;
            }
        }
        result.append(String.valueOf("!"));
        rawPattern = result;
    }
    
    /**
     * Shrinking the pattern further by removing zeroes/b's at the end of the 
     * patternlines.
     */
    public void removeZeroesLineshift(){        
        Pattern zeroPatt = Pattern.compile("([1-9]\\d*)?b\\$");
        Matcher zeroMatch = zeroPatt.matcher(rawPattern);
        
        while(zeroMatch.find()){
            rawPattern.delete(zeroMatch.start(), zeroMatch.end()-1);
            zeroMatch.reset();
        }
    }
    
    /**
     * Shrinking the RLE file, replacing successive "$"/lineshifts, with 
     * a number telling how many rows to skip.
     * Sample:
     * "$$$" = "3$"
     */
    public void encodeMultipleLineshift(){
        Pattern lineshiftPatt = Pattern.compile("(\\$+)"); 
        Matcher lineshiftMatch = lineshiftPatt.matcher(rawPattern);
        String replacement;
        int length;
        
        while(lineshiftMatch.find()){
            length = lineshiftMatch.end()-lineshiftMatch.start();

            if(length > 1){
                replacement = String.valueOf(length) + "$";
                rawPattern.replace(lineshiftMatch.start(), lineshiftMatch.end(), replacement);
            }
        }
    }
    
    /**
     * String build, containing all RLE data.
     * Initiates a StringBuilder, that contains all of the RLE data. 
     */
    public void buildRLE(){
        builder = new StringBuilder();
                
        if(!peCtrl.getName().getText().isEmpty()){
            builder.append("#N " + peCtrl.getName().getText());
            builder.append(System.lineSeparator());
        }
        if(!peCtrl.getCreator().getText().isEmpty()){
            builder.append("#O " + peCtrl.getCreator().getText());
            builder.append(System.lineSeparator());
        }
        if(!peCtrl.getDescription().getText().isEmpty()){
            builder.append("#C " + peCtrl.getDescription().getText());
            builder.append(System.lineSeparator());
        }
        builder.append("x = " + getRLEPattern()[0].length + ", ");
        builder.append("y = " + getRLEPattern().length + ", ");
        builder.append("rule = B" + getBorn() + "/S" + getSurvives());
        builder.append(System.lineSeparator());
        builder.append(rawPattern);
    }
    
    /**
     * Retrieves the ruleset written in the textfields else the current ruleset 
     * from the current Gameboard instance, if nothing is written both 
     * PatternEditor GUI TextFields.
     */
    public void retrieveRuleStrings(){
        
        if(peCtrl.getBorn().getText().isEmpty() || peCtrl.getSurvives().getText().isEmpty()){
            String b = null;
            String s = null;
            
            for(int i = 0; i < 9; i++){
                
                if(peCtrl.getPatternEditor().getStaticBoard().getNextGeneration().getBorn()[i]){
                    b += i;   
                }
                if(peCtrl.getPatternEditor().getStaticBoard().getNextGeneration().getSurvives()[i]){
                    s += i;
                }
            }
            setBorn(b.replace("null", ""));
            setSurvives(s.replace("null", ""));
        }else{
            setBorn(peCtrl.getBorn().getText());
            setSurvives(peCtrl.getSurvives().getText());
        }
    }
    
    /**
     * Testing if the user input ruleset is supported.
     * @see logic.PatternFormatException
     * @see logic.PatternFormatExceptionDialog
     */
    public void checkRuleset(){
        try{
            if(!born.matches("([0-8]{1,9})") && !survives.matches("([0-8]{1,9})")){
                born = "3";
                survives = "23";
                throw new PatternFormatException();
            }
        }catch(PatternFormatException e){
            new PatternFormatExceptionDialog("Ruleset is not supported or not set, ruleset is set to default Conways(B3/S23)");
        }   
    }
    
    /**
     * Shrinks the pattern, so that there is no rows or columns around the 
     * pattern that contains just dead cells.
     * @param trim is the pattern to be trimmed.
     * @return dosnt contain any dead cell borders.
     */
    public byte[][] trimPattern(byte[][] trim){
        byte[][] trimmed = null;
        int[] moves = peCtrl.getPatternEditor().getStaticBoard().boundingBox(trim);
                
        if(moves[1]-moves[0]+1 > 0 && moves[3]-moves[2]+1 > 0){
            trimmed = new byte[moves[1]-moves[0]+1][moves[3]-moves[2]+1];
        
            for(int i = 0; i<trimmed.length; i++){
                for(int j = 0; j<trimmed[0].length; j++){
                
                    if(trim[moves[0]+i][moves[2]+j] == 1){
                        trimmed[i][j] = 1;
                    }
                }
            }
        }
        return trimmed;
    }
    
    /**
     * @return is the unencoded initPattern, that is 
     * trimmed.
     */
    public byte[][] getRLEPattern(){
        return RLEPattern;
    }
    
    /**
     * @return is the save file.
     */
    public File getFile() {
        return file;
    }
    
    /**
     * @return contains all the file's String data.
     */
    public StringBuilder getBuilder() {
        return builder;
    }
    
    /**
     * @return contains the decoded pattern.
     */
    public StringBuilder getRawPattern() {
        return rawPattern;
    }
    
    /**
     * @return is a String containing the born rules.
     */
    public String getBorn() {
        return born;
    }
    
    /**
     * @return is a String containing the survives rules.
     */
    public String getSurvives() {
        return survives;
    }
    
    /**
     * @param rawPattern contains the decoded pattern.
     */
    public void setRawPattern(StringBuilder rawPattern) {
        this.rawPattern = rawPattern;
    }
    
    /**
     * @param builder contains all the file's String data.
     */
    public void setBuilder(StringBuilder builder) {
        this.builder = builder;
    }
    
    /**
     * @param file is the save file.
     */
    public void setFile(File file) {
        this.file = file;
    }
    
    /**
     * @param ruleS is a String containing the survives rules.
     */
    public void setSurvives(String ruleS) {
        this.survives = ruleS;
    }
    
    /**
     * @param ruleB is a String containing the born rules.
     */
    public void setBorn(String ruleB) {
        this.born = ruleB;
    }
}
