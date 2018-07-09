package entity.exceptions;

public class ScriptException extends Exception{

    public ScriptException(String string) {
        super(string);
    }

    public ScriptException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
    
}
