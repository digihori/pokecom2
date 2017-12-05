package tk.horiuchi.pokecom2;

/**
 * Created by yoshimine on 2017/11/19.
 */

public class Common {
    public Common() {}

    public final static int typePhone = 0;
    public final static int type7inch = 1;
    public final static int type10inch = 2;

    public final static int MODE_RUN = 0;
    public final static int MODE_PRO = 1;

    public final static String[] cmdTable = {
    /*00*/  "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*10*/  "\\LA", "\\RA", "\\DA", "\\UA", "\0", "\0", "\0", "\0", "\0", "\\ANS", "\\AC", "\\EXE", "\\STOP", "\\DEL", "\\INS", "\0",
    /*20*/  " ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/",
    /*30*/  "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?",
    /*40*/  "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
    /*50*/  "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\", "]", "^", "_",
    /*60*/  "'", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
    /*70*/  "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "\0", "\0", "\0", "~", "\0",
    /*80*/  "\\P0", "\\P1", "\\P2", "\\P3", "\\P4", "\\P5", "\\P6", "\\P7", "\\P8", "\\P9", "\0", "\0", "\0", "\0", "\0", "\0",
    /*90*/  "INPUT", "KEY", "PRINT", "CSR", "GOTO", "GOSUB", "IF", "THEN", "RETURN", "FOR", "TO", "STEP", "NEXT", "STOP", "END", "\0",
    /*a0*/  "VAC", "LIST", "RUN", "CLEAR", "MODE", "SET", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*b0*/  "LEN", "MID", "VAL", "RAN#", "DEFM", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*c0*/  "SAVE", "LOAD", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*d0*/  "SIN", "COS", "TAN", "ASN", "ACS", "ATN", "LOG", "LN", "EXP", "SQR", "ABS", "SGN", "INT", "FRAC", "\0", "\0",
    /*e0*/  "\\CI", "\\SQ", "\\TR", "\\CR", "\\DV", "\\SP", "\\HT", "\\DI", "\\CL", "\\BX", "\\DT", "\\DG", "\\SG", "\\OM", "\\MU", "\0",
    /*f0*/  "\\EX", "\\NE", "\\PI", "\\LE", "\\GE", "\\LA︎", "\\DA︎", "\\RA︎", "\0︎", "\0", "\0", "\0", "\0", "\0", "\0", "\0"
    };

}