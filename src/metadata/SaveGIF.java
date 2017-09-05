package metadata;

import controller.PatternEditorController;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javafx.stage.FileChooser;

/**
 * This class takes care of saving the pattern to a GIF file.
 * It is using lieng.GIFWriter package, and the class contains all the specific 
 * variables and objects nessecary to create a GIF file.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public class SaveGIF{
        
    private int frames;
    private int GIFms;
    private int width;
    private int height;
    private int gifCellSize;
    private byte[][] frame;
    private String path;
    
    private Thread writerThread;
    
    PatternEditorController peCtrl;
    
    /**
     * Constructor for SaveGIF.
     * Contains the PatternEditorController singelton object.
     */
    public SaveGIF() {
        
        this.peCtrl = PatternEditorController.peInstance;
    }
    
    /**
     * Initiates the SaveGIF essentials.
     * Setting variable values, and initiates a FileChooser from saveGIFDialog().
     * Initiates lieng.GIFWriter, and calls writeGoLSequenceToGIF(writer, getFrames()).
     * @see metadata.SaveGIF#saveGIFDialog() 
     * @see metadata.SaveGIF#fetchGifCellSize() 
     * @see metadata.SaveGIF#checkFramesPresentAndSet() 
     * @see metadata.PatternEditor#createDeadBorder(byte[][]) 
     * @see metadata.SaveGIF#threadSequence() 
     */
    public void initGIFSave(){
        path = saveGIFDialog();
        fetchGifCellSize();
        checkFramesPresentAndSet();
        checkAndSetFrameSize();
        GIFms = (int)peCtrl.getGifSpeed().getValue();
        frame = peCtrl.getPatternEditor().
        createDeadBorder(peCtrl.getPatternEditor().getInitPattern());
        threadSequence();
    }
    
    /**
     * Setting the gifCellSize, according to what the gifCellSize slider in the 
     * PatternEditorController is set to.
     */
    public void fetchGifCellSize(){
        gifCellSize = (int)peCtrl.getGifCellSize().getValue();
    }
    
    /**
     * FileChooser, allowing the user to choose a destination path for the GIF file.
     * File path is set to ./GIF as default, if the folder exists.
     * @return file file is given a name and has a destination path.
     */
    public String saveGIFDialog(){
        File dir = new File("./GIF");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save GIF:");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter
                                             ("GIF", "*.gif");
        chooser.getExtensionFilters().addAll(filter);
        
        if(dir.exists()){
            chooser.setInitialDirectory(dir);
        }
        File file = chooser.showSaveDialog(null);
        
        try{
            return file.getPath(); 
        }
        catch(NullPointerException e){ 
            return null;
        }
    }
    
    /**
     * Checks if the user have entered any data in the frames TextField.
     * Default value if user didnt enter anything is 50. This value will be
     * decreased automaticly if the gif writer detects any pattern duplicates,
     * this is detected via the patternEqualityCheck() method found in the 
     * PatternEditor class.
     */
    public void checkFramesPresentAndSet(){
        if(peCtrl.getFrames().getText() != null && 
           peCtrl.getFrames().getText().matches("(\\d+)")){
            frames = Integer.parseInt(peCtrl.getFrames().getText());
        }
        else{   
            frames = 50;
        }
    }
    
    /**
     * Looking for the biggest pattern in the writer cycle before writing starts. 
     * And sets the gif width and height accordingly. These values is then 
     * attached to the lieng.GIFWriter object, in the threadSequence() method.
     */
    public void checkAndSetFrameSize(){
        int tempWidth;
        int tempHeight;
        frame = peCtrl.getPatternEditor().
                createDeadBorder(peCtrl.getPatternEditor().getInitPattern());
        
        if(frame == null){
            return;
        }
        int counter = 0;
        
        do{
            tempWidth = (frame.length)*gifCellSize;
            tempHeight = (frame[0].length)*gifCellSize;
            
            if(tempWidth > width){
                width = tempWidth;
            }
            if(tempHeight > height){
                height = tempHeight;
            }
            frame = peCtrl.getPatternEditor().getStaticBoard()
                   .getNextGeneration().calcNextGen(frame);
            frame = peCtrl.getPatternEditor().
                    createDeadBorder(frame);
            counter++;
            
        }while(frame != null && 
              (!peCtrl.getPatternEditor().patternEqualityCheck(frame) &&
               counter<frames));   
    }
    
    /**
     * A singel thread sequence, used to write the GIF sequence. This eases the 
     * work for JavaFX Thread. And creates a better user experience(the user can
     * now use the GUI at the same time as saving the GIF.
     */
    public void threadSequence(){
        if(path==null){
            return;
        }
        writerThread = new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    while(!writerThread.isInterrupted()){
                        int counter = 0;
                        lieng.GIFWriter writer = new lieng.GIFWriter
                                                 (width, height, path, GIFms);
                        writeGoLSequenceToGIF(writer, frames, 
                                              counter, livingColor());
                        writerThread.interrupt();
                    }
                }catch(IOException e){
                    System.err.println("Failed to write GIFSequence " + 
                                       Thread.currentThread().getName()); 
                }
            }
        });
        writerThread.start();
    }
    
    
    /**
     * Writing to GIF file.
     * Writing the GIF sequence to file using lieng.GIFWriter.
     * @param writer writer is a lieng.GIFWriter.
     * @param frames frames is the number of frames input given by the user.
     * @param counter counter is counting frames.
     * @param living living is the color of the living cells.
     * @throws IOException IOException is thrown if the writer is unnable to 
     * write.
     */
    public void writeGoLSequenceToGIF(lieng.GIFWriter writer, 
                                      int frames, int counter, 
                                      Color living) throws IOException{
        boolean equivalent = false;
        
        if(frame != null){
            equivalent = peCtrl.getPatternEditor().patternEqualityCheck(frame);
        }
        if(frame == null || (counter > 0 && (counter == frames || equivalent))){
            writer.insertCurrentImage();
            writer.close();
            return;
        }
        else if (counter > 0){
            writer.insertAndProceed();
        }
        for(int i = 0; i < frame.length; i++){
            for(int j = 0; j < frame[0].length; j++){
                if(frame[i][j] == 1){
                    writer.fillRect(calcMinX(i), calcMaxX(i)-2, 
                                    calcMinY(j), calcMaxY(j)-2, living);
                }
            }
        }
        counter++;
        frame = peCtrl.getPatternEditor().getStaticBoard().
                getNextGeneration().calcNextGen(frame);
        deadBorderControlFrame();
        writeGoLSequenceToGIF(writer, frames, counter, living);
    }
    
    /**
     * Initiates a testing cycle for the stripPattern.
     * To make sure that it is always a dead cell border around the pattern.
     */
    public void deadBorderControlFrame(){
        frame = peCtrl.getPatternEditor().expandPattern(frame);
        
        if(frame.length > 1 || frame[0].length > 1){
            byte[][] temp = peCtrl.getPatternEditor().shrinkPattern(frame);
            frame = temp;
        }
    }
    
    /**
     * Limiting the gifCellSize, to avoid exception and a huge workload for the 
     * thread and the computer.
     * @param input input is the array that is messured by. Using the .length
     * method.
     */
    public void gifCellSizeLimiter(byte[][] input){
                
        if(input.length > 1000 || input[0].length > 1000){
            peCtrl.getGifCellSize().setMax(5);
            peCtrl.getGifCellSize().adjustValue(3);
        }
        else if(input.length > 500 || input[0].length > 500){
            peCtrl.getGifCellSize().setMax(10);
            peCtrl.getGifCellSize().adjustValue(5);
        }
        else if(input.length > 250 || input[0].length > 250){
            peCtrl.getGifCellSize().setMax(15);
            peCtrl.getGifCellSize().adjustValue(7);
        }
        else if(input.length > 125 || input[0].length > 125){
            peCtrl.getGifCellSize().setMax(20);
            peCtrl.getGifCellSize().adjustValue(10);
        }
        else{
            peCtrl.getGifCellSize().setMax(25);
            peCtrl.getGifCellSize().adjustValue(15);
        }  
    }
    
    /**
     * JavaFX to awt color converter, for the living color (JavaFX Colorpicker).
     * @return cell color as a awt.Color object.
     */
    public Color livingColor(){
        Color living = new Color(
        (float)peCtrl.getPatternEditor().getStaticBoard().getLiving().getRed(), 
        (float)peCtrl.getPatternEditor().getStaticBoard().getLiving().getGreen(), 
        (float)peCtrl.getPatternEditor().getStaticBoard().getLiving().getBlue());
        return living;
    }
    
    
    /**
     * @param j is the x index, being multiplied by the current cellSize.
     * @return is the start position to the GIF cell.
     */
    public int calcMinX(int j){
        
        return (int)(j*gifCellSize);
    }
    
    
    /**
     * @param j is the y index, being multiplied by the current cellSize.
     * @return is the start position to the GIF cell.
     */
    public int calcMinY(int j){
        
        return (int)(j*gifCellSize);
    }
    
    
    /**
     * @param j is the x index, being multiplied by the current cellSize + 1.
     * @return is the end position to the GIF cell.
     */
    public int calcMaxX(int j){
        
        return (int)((j+1)*gifCellSize);
    }
    
    /**
     * @param j is the y index, being multiplied by the current cellSize + 1.
     * @return is the end position to the GIF cell.
     */
    public int calcMaxY(int j){
        
        return (int)((j+1)*gifCellSize);
    }
}
