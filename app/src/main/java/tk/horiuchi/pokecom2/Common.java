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
    public final static int MODE_SAVE = 2;


    public final static char _EX = 0xf0;
    public final static char _EM = 0xf1;
    public final static char _NE = 0xf2;
    public final static char _PI = 0xf3;
    public final static char _LE = 0xf4;
    public final static char _GE = 0xf5;

    // カーソル移動系
    public final static char _LA   = 0x10;
    public final static char _RA   = 0x11;
    public final static char _DA   = 0x12;
    public final static char _UA   = 0x13;
    public final static char _ANS  = 0x19;
    public final static char _AC   = 0x1a;
    public final static char _EXE  = 0x1b;
    public final static char _STOP = 0x1c;
    public final static char _DEL  = 0x1d;
    public final static char _INS  = 0x1e;
    public final static char _C_START = _LA;
    public final static char _C_END   = _INS;

    // バンク切り替え系
    public final static char _P0 = 0x80;
    public final static char _P1 = 0x80;
    public final static char _P2 = 0x80;
    public final static char _P3 = 0x80;
    public final static char _P4 = 0x80;
    public final static char _P5 = 0x80;
    public final static char _P6 = 0x80;
    public final static char _P7 = 0x80;
    public final static char _P8 = 0x80;
    public final static char _P9 = 0x80;
    public final static char _B_START = _P0;
    public final static char _B_END   = _P9;

    // 予約語
    public final static char _RSV_START = 0x90;
    public final static char _RSV_END   = 0xdf;

    public final static String[] cmdTable = {
    /*00*/  "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*10*/  "\\DMY", "\\DMY", "\\DMY", "\\DMY", "\0", "\0", "\0", "\0", "\0", "\\ANS", "\\AC", "\\EXE", "\\STOP", "\\DEL", "\\INS", "\0",
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
    /*f0*/  "\\EX", "\\EM", "\\NE", "\\PI", "\\LE", "\\GE", "\\LA", "\\DA", "\\RA", "\0", "\0", "\0", "\0", "\0", "\0", "\0"
    };

    // CASIO PB-100 コード体系
    public final static String[] reservCodePB100 = {
    /*00*/  " ", "+", "-", "*", "/", "\\UA", "!", "\"", "#", "$", ">", "\\GE", "=", "\\LE", "<", "\\NE",
    /*10*/  "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "\\PI", ")", "(", "\\EM", "\\EX",
    /*20*/  "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
    /*30*/  "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "\0", "\0", "\0", "\0", "\0", "\0",
    /*40*/  "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
    /*50*/  "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "\0", "\0", "?", ",", ";", ":",
    /*60*/  "\\CI", "\\SG", "\\DG", "\\TR", "@", "\\CR", "\\DV", "\\SP", "\\LA", "\\HT", "\\DI", "\\CL", "\\MU", "\\OM", "\\DA", "\\RA",
    /*70*/  "%", "\\", "\\SQ", "[", "&", "_", "'", "\\DT", "]", "\\BX", "\0", "\0", "\0", "\0", "\0", "\0",
    /*80*/  "SIN", "COS", "TAN", "ASN", "ACS", "ATN", "LOG", "LN", "EXP", "SQR", "INT", "FRAC", "ABS", "SGN", "RND", "RAN#",
    /*90*/  "LEN\\(", "VAL\\(", "MID\\(", "KEY", "CSR", "TO", "STEP", "THEN", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*a0*/  "FOR", "NEXT", "GOTO", "GOSUB", "RETURN", "IF", "PRINT", "INPUT", "MODE", "STOP", "END", "\0", "\0", "\0", "\0", "\0",
    /*b0*/  "VAC", "SET", "PUT", "GET", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*c0*/  "DEFM", "SAVE", "LOAD", "VAR", "LIST", "RUN", "CLEAR", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*d0*/  "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*e0*/  "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
    /*f0*/  "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0"
    };
}