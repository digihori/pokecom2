package tk.horiuchi.pokecom2;

/**
 *
 */
public class InterpreterException extends Exception {
    String errStr;

    public InterpreterException(String str) {
        errStr = str;
    }

    public String toString() {
        return  errStr;
    }
}
