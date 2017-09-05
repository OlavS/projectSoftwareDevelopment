package logic;

/**
 * PatternFormatException is related to the LoadPattern class, and the Ruleset
 * interface.
 * This is a checked exception, that is thrown when a pattern format exception
 * occures. Being when a pattern contains errors or when a ruleset contains 
 * errors, input from user or from file.
 * @author Olav Sørlie and Øyvind Mjelstad
 */
public class PatternFormatException extends Exception{

    public PatternFormatException(){}
    
    /**
     * Constructor, with message.
     * PatternFormatException constructs a object with the capabilities 
     * granted by the Exception class.
     * @param message message is the message printed to the console.
     */
    public PatternFormatException(String message){
        super(message);
    }
    
    /**
     * Constructor, allows throwable.
     * Gives PatternFormatException the possibility to be throwable.
     * @param cause cause the message printed to the console.
     */
    public PatternFormatException(Throwable cause){
        super(cause);
    }
}