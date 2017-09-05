
package logic;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
/**
 * Initiates a static representation of gameboard. 
 * Static means that the board itself is not expanding 
 * with the living cells movement.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public class StaticBoard extends Gameboard{
  
    private NextGeneration ng;
    
    private int width = 70, //default size
                height = 50; //default size

    private byte[][] currentGen = new byte[width][height];
    
    private Color background = Color.BLACK,
                        dead = Color.RED;
             
    
    
    /**
     * Constructs a static instance.
     * This object is created with a instance of NextGeneration. 
     * This NextGeneration object contains the standard ruleset of Game of Life(B3/S23).
     * @param gc gc is the Canvas's GraphicsContext displaying the graphics to the user. 
     */  
    public StaticBoard(GraphicsContext gc){
        super(gc);
        this.ng = new NextGeneration();
    }
    
    
    /**
     * Empty StaticBoard constructor.
     * Convinient class to use for testing primitives and array objects, 
     * not requiering GraphicsContext.
     */
    public StaticBoard() {
        
    }
    
    
    /**
     * Deepclone method for StaticBoard instance.
     * @return StaticBoard StaticBoard clone, of the current instance.
     */   
    public StaticBoard getStaticBoardClone(){
        StaticBoard clone = new StaticBoard();
        clone.setCellSize(this.getCellSize());
        clone.width = this.width;
        clone.height = this.height;
        clone.currentGen = new byte[this.width][this.height];
        clone.gc = this.gc;
        
        for(int i = 0; i <this.currentGen.length; i++){
            for(int j = 0; j < this.currentGen[0].length; j++){
                
                clone.currentGen[i][j] = this.currentGen[i][j];
            }
        }   
        clone.ng = this.ng.getNextGenerationClone();
        
        return clone;
    }
    
    
    /**
     * Draws the current generation.
     * Visualisation of the gameboard to the user, 
     * using Canvas's GraphicsContext support
     */
    @Override
    public void draw(){ 
        gc.setFill(background);
        gc.fillRect(0, 0, ctrl.getCanvas().widthProperty().doubleValue(), 
                    ctrl.getCanvas().heightProperty().doubleValue()); //Removes overlay 
      
        double startY = 0,//((ctrl.getCanvas().heightProperty().doubleValue())/(cellSize)), 
               startX = 0;//((ctrl.getCanvas().widthProperty().doubleValue())/(cellSize));
        for(int i = 0; i<currentGen.length; i++){
            for(int j = 0; j<currentGen[0].length; j++){
              
                if(currentGen[i][j] == 1) {
                    gc.setFill(getLiving());
              
                }else if(currentGen[i][j] == 0)
                    gc.setFill(dead);
                    gc.fillRect(startX + (getCellSize()*i), startY + (getCellSize()*j), getCellSize()*0.9, getCellSize()*0.9);
            }
        }
        drawLoaded();
    }
    
    
    /**
     * Looping through calcNextGen and draw().
     * Animating the gameboard visualy for the user.
     */
    @Override
    public void displayNextGen(){
        currentGen = ng.calcNextGen(currentGen);
        incGenCount();
        draw();
    }
    
    
    /**
     * User is able to change the cell state with mouse impact.
     * Right-clicks or drags creates living cells.
     * Left-click or drags eliminates living cells.
     * @param e e MouseEvent parameter given by the controller.
     */
    @Override
    public void changeCellStatus(MouseEvent e){        
        int i = (int) (e.getX()/getCellSize());
        int j = (int) (e.getY()/getCellSize()); 
        
        //To avoid ArrayIndexOutOfBoundsException when drawing outside the grid
        if(e.getX() < 0 || e.getY() < 0 || i > width-1 || j > height-1)
            return;
        
        if(e.isPrimaryButtonDown()) 
            currentGen[i][j] = 1;
        
        else if(e.isSecondaryButtonDown())
            currentGen[i][j] = 0;
        draw();
    }
    
    
    /**
     * Resets the current generation, and the Canvas.
     * Eliminating all living cells.
     */
    @Override
    public void resetBoard(){
        for(int i = 0; i<currentGen.length; i++){
            for(int j = 0; j<currentGen[0].length; j++){
                
                currentGen[i][j] = 0;   
            }
        }    
        
        resetGenCount();
        ng = new NextGeneration();
        draw();
    }  
    
    
    /**
     * Changes background color based on ColorPicker/user choice.
     * @param c c Color input
     */
    public void changeBackgroundColor(Color c) {
        background = c;
        draw();
    }
    
    
    /**
     * Changes dead cell color based on ColorPicker/user choice.
     * @param c c Color input
     */
    public void changeDeadCellColor(Color c) {
        dead = c;
        draw();
    }
    
    
    /**
    * Setter for gameboard size.
    * @param x x cells in x-direction
    * @param y y cells in y-direction
    */
    public void setBoardSize(int x, int y) {
        byte board[][] = new byte[x][y];
        currentGen = board;
        draw();
        width = x;
        height = y;
    }
    
    /**
    * Initiates pattern placement for loaded patterns.
    * @param input input is the pattern represented as a byte[][].
    */
    @Override
    public void updateBoardWithPattern(byte[][] input){        
        
        try{
            
            if(input.length<currentGen.length && input[0].length<currentGen[0].length){
                
                setLoadedPattern(input);
                setStartIndexLoaded();
                draw();
            }else{
                
                throw new PatternFormatException();
            }
        }catch(PatternFormatException e){

            new PatternFormatExceptionDialog("Pattern is to big, increase dimensions and retry");
        }
    }
    
    
    /**
    * Setter for start index to loaded pattern.
    * This is where the pattern placement sequence starts. It is set to start at 
    * the middle of the current generation (currentGen) pattern.
    */   
    @Override
    public void setStartIndexLoaded(){
        setPattMovesX((currentGen.length/2)-(getLoadedPattern().length/2));
        setPattMovesY((currentGen[0].length/2)-(getLoadedPattern()[0].length/2));
    }
    
    
    /**
     * Attaches the loaded pattern to the current generation.
     */
    @Override
    public void glueLoaded(){
        
        for(int i = 0; i < getLoadedPattern().length; i++){
            for(int j = 0; j < getLoadedPattern()[0].length; j++){
                
                if( getPattMovesX()-1 < 0 || 
                    getPattMovesY()-1 < 0 ||
                    getPattMovesX()+i-1 > width-1 ||
                    getPattMovesY()+j-1 > height-1){
                    continue;
                }
                if(getLoadedPattern()[i][j] == 1){
                    this.currentGen[getPattMovesX()+i-1]
                                   [getPattMovesY()+j-1] = getLoadedPattern()[i][j];  
                }
            }
        }
    }
     
    /**
     * Gives the border limits of the current living cells.
     * @param input input the byte[][] being checked
     * @return int[] int[] containing min width, max width, min height, max height.
     */
    public int[] boundingBox(byte[][] input){
        int[] boundingBox = new int[4];
        boundingBox[0] = input.length;
        boundingBox[1] = 0;
        boundingBox[2] = input[0].length;
        boundingBox[3] = 0;
        for(int i = 0; i<input.length; i++){
            for(int j = 0; j<input[0].length; j++){
                if(input[i][j] != 1) continue;
                if(i < boundingBox[0])
                    boundingBox[0] = i;
                if(i > boundingBox[1])
                    boundingBox[1] = i;
                if(j < boundingBox[2])
                    boundingBox[2] = j;
                if(j > boundingBox[3])
                    boundingBox[3] = j;
            }
        }
        return boundingBox;
    }
    
    /**
     * Allows the user to choose a StaticBoard size, this method will also reset
     * the current generation, leaving a completely dead generation.
     * If the user inputs a non-digit value, a NumberFormatException is thrown.
     * 
     */
    @Override
    public void changeBoardSize(){
        try {
            int widthInput = Integer.parseInt(ctrl.getHeightTextfield().getText());
            int heightInput = Integer.parseInt(ctrl.getWidthTextfield().getText());
            setBoardSize(widthInput, heightInput);

            if(widthInput < 0 || heightInput < 0 ){
                return;
            }
        }catch(NumberFormatException e) {
            System.err.println("User input invalid.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Number Format Error");
            alert.setHeaderText("Please insert non-negative whole numbers "
                              + "into the dimension-fields.");
            Stage diaStage = (Stage) alert.getDialogPane().getScene().getWindow();
            diaStage.getIcons().add(new Image("file:icon.jpg"));
            alert.showAndWait();
        }
    }
    
    /**
     * @return containing current generation, cell size, array size x and y.
     */
    @Override
    public String toString() {
        String result;
        result = "Current generation: ";
        for(int i = 0; i<currentGen.length; i++) {
            for (int j = 0; j < currentGen[0].length; j++) {
                result += currentGen[i][j];
            }
        }
        result += "\n cell size: " + getCellSize() + ", array size X: " + width 
               + ", array size Y: " + height;
        
        return result;
    }
    
    /**
     * @return the current generation.
     */
    public byte[][] getCurrentGen(){
        return currentGen;
    }
    
    /**
     * @return number of cells in X-direction.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * @return number of cells in Y-direction.
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * @return background color.
     */
    public Color getBackground(){
        return background;
    }
    
    /**
     * @return dead cell color.
     */
    public Color getDead(){
        return dead;
    }
    
    /**
     * @return this NextGeneration instance.
    */
    public NextGeneration getNextGeneration(){
        return ng;
    }
    
    /**
    * @param currentGen new current generation.
    */
    public void setCurrentGen(byte[][] currentGen) {
        this.currentGen = currentGen;
    }
    
    /**
     * @param width integer with the desired size in X-direction.
     */
    public void setWidth(int width){
        this.width = width;
    }
    
    /**
     * @param height height integer with the desired size in Y-direction.
     */
    public void setHeight(int height){
        this.height = height;
    }
    
    /** 
     * @param dead dead cell color.
     */
    public void setDead(Color dead){
        this.dead = dead;
    }
    
    /**
     * @param background background color.
     */
    public void setBackground(Color background){
        this.background = background;
    }
    
    /**
     * Draws the pattern loaded in by user, either through URL or as a RLE file.
     * Uses 2D representation to load pattern. Every living cell is set to grey
     * and every dead cell is transparent.
     */ 
    @Override
    public void drawLoaded(){
        double startX = getPattMovesY() * getCellSize(); 
        double startY = getPattMovesX() * getCellSize();

        if(getLoadedPattern() != null){
            for(int i = 0; i < getLoadedPattern().length; i++){
                for(int j = 0; j < getLoadedPattern()[0].length; j++){

                    if(getLoadedPattern()[i][j] == 1){
                        gc.setFill(Color.rgb(192, 192, 192, 0.8));
                    }
                    else if(getLoadedPattern()[i][j] == 0){
                        gc.setFill(Color.TRANSPARENT);
                    }
                    gc.fillRect(startY + (getCellSize()*(i-1)),
                                startX + (getCellSize()*(j-1)), 
                                getCellSize()*0.9, 
                                getCellSize()*0.9);
                }
            }
        }
    }
    @Override
    public void rotateLoaded(){
        byte[][] rotated = new byte[getLoadedPattern()[0].length][getLoadedPattern().length];
        
        for(int i = 0; i < getLoadedPattern().length; i++){
            for(int j = 0; j < getLoadedPattern()[0].length; j++){
                rotated[j][getLoadedPattern().length-i-1] = 
                getLoadedPattern()[i][j];
            }
        }
        setLoadedPattern(rotated);
    }   
}
