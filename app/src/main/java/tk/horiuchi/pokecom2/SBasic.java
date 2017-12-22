package tk.horiuchi.pokecom2;

import android.nfc.FormatException;
import android.util.Log;

import java.util.*;

import static tk.horiuchi.pokecom2.MainActivity.bank;
import static tk.horiuchi.pokecom2.MainActivity.cpuClockEmulateEnable;
import static tk.horiuchi.pokecom2.MainActivity.inkey;
import static tk.horiuchi.pokecom2.MainActivity.pb;
import static tk.horiuchi.pokecom2.MainActivity.progLength;
import static tk.horiuchi.pokecom2.MainActivity.source;

/**
 *
 */
public class SBasic {
    final int PROG_SIZE = 10_000; //

    //
    final int NONE = 0;
    final int DELIMITER = 1;
    final int VARIABLE = 2;
    final int NUMBER = 3;
    final int COMMAND = 4;
    final int QUTEDSTR = 5;
    final int FUNCTION = 6;
    final int SVARIABLE = 7;
    final int BANKNUM = 8;

    final int ERR_SYSTEM = 0;
    final int ERR_MEMORYOVER = 1;
    final int ERR_SYNTAX = 2;
    final int ERR_MATH = 3;
    final int ERR_UNDEFLINE = 4;
    final int ERR_ARGUMENT = 5;
    final int ERR_VARIABLE = 6;
    final int ERR_NESTING = 7;
    final int ERR_OPTIN = 9;

    //
    final int UNKNCOM = 0;
    final int PRINT = 1;
    final int INPUT = 2;
    final int IF = 3;
    final int THEN = 4;
    final int FOR = 5;
    final int NEXT = 6;
    final int TO = 7;
    final int GOTO = 8;
    final int GOSUB = 9;
    final int RETURN = 10;
    final int END = 11;
    final int EOL = 12;
    final int RUN = 13;
    final int LIST = 14;
    final int CSR = 15;
    final int VAC = 16;
    final int STOP = 17;

    final int FUNC_DUMMY = 30;
    final int SET  = 31;
    final int LEN  = 32;
    final int MID  = 33;
    final int VAL  = 34;
    final int SIN  = 35;
    final int COS  = 36;
    final int TAN  = 37;
    final int ASN  = 38;
    final int ACS  = 39;
    final int ATN  = 40;
    final int LOG  = 41;
    final int LN   = 42;
    final int EXP  = 43;
    final int SQR  = 44;
    final int ABS  = 45;
    final int SGN  = 46;
    final int INT  = 47;
    final int FRAC = 48;
    final int RND  = 49;
    final int RAN  = 50;
    final int KEY  = 51;

    //
    //final int EOL = '\n';
    final String EOP = "\0";

    //
    final char LE = 0xf3;
    final char GE = 0xf4;
    final char NE = 0xf1;

    //
    private double[] vars;
    private String[] svars;
    private boolean forcedExit = false;
    private String resultStr;
    private StringBuilder sb = null;
    private int currentLine;
    private int currentBank;

    //
    class Keyword {
        String keyword;
        int keywordTok;

        Keyword(String str, int t) {
            keyword = str;
            keywordTok = t;
        }
    }

    Keyword kwTable[] = {
            new Keyword("print", PRINT),
            new Keyword("input", INPUT),
            new Keyword("if", IF),
            new Keyword("then", THEN),
            new Keyword("goto", GOTO),
            new Keyword("for", FOR),
            new Keyword("next", NEXT),
            new Keyword("to", TO),
            new Keyword("gosub", GOSUB),
            new Keyword("return", RETURN),
            new Keyword("csr", CSR),
            new Keyword("vac", VAC),
            new Keyword("key", KEY),
            new Keyword("int", INT),
            new Keyword("ran#", RAN),
            new Keyword("set", SET),
            new Keyword("len", LEN),
            new Keyword("mid", MID),
            new Keyword("val", VAL),
            new Keyword("sin", SIN),
            new Keyword("cos", COS),
            new Keyword("tan", TAN),
            new Keyword("asn", ASN),
            new Keyword("acs", ACS),
            new Keyword("atn", ATN),
            new Keyword("log", LOG),
            new Keyword("ln", LN),
            new Keyword("exp", EXP),
            new Keyword("sqr", SQR),
            new Keyword("abs", ABS),
            new Keyword("sgn", SGN),
            new Keyword("frac", FRAC),
            new Keyword("rnd", RND),
            new Keyword("end", END),
            new Keyword("run", RUN),
            new Keyword("list", LIST)
    };

    private char[] prog;
    private int pc;
    private int idxEnd;
    private String token;
    private int tokType;
    private int kwToken;

    private boolean nextLine = true;
    private Double lastAns;
    private int lastBank;
    private int listIdx;

    private Lcd lcd;

    //
    class ForInfo {
        int var;
        double target;
        int loc;
    }

    //
    private  Stack fStack;

    //
    //class Label {
    //    String name;
    //    int loc;

    //    public Label(String str, int i) {
    //        name = str;
    //        loc = i;
    //    }
    //}

    //
    //private TreeMap labelTable;
    private Map<String, Integer> labelTable;

    //
    class GosubInfo {
        int bank;
        int pc;

        public GosubInfo(int b, int p) {
            bank = b;
            pc = p;
        }
    }
    private Stack gStack;

    //
    char rops[] = {
            GE, NE, LE, '<', '>', '=', 0
    };

    String relops = new String(rops);


    public SBasic(Lcd lcd) throws InterpreterException {
        this.lcd = lcd;
        vars = new double[26];
        svars = new String[27];
        for (int i = 0; i < svars.length; i++) {
            svars[i] = "";
        }
        fStack = new Stack();
        //labelTable = new TreeMap();
        //Map<String, Integer> labelTable = new TreeMap<String, Integer>();
        labelTable = new TreeMap<String, Integer>();
        gStack = new Stack();
        lastAns = new Double(0);
        prog = new char[progLength];
        //initListMap();

        //source = new SourceFile();
    }

    private void nop20ms() {
        if (cpuClockEmulateEnable) {
            Log.w("NOP", "--- WAIT ---");
            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                ;
            }
        }
    }

    private void lcdPrintAndPause(String s) {
        lcd.bprint(s);
        System.out.print(s);
        pb.progStop();
    }
    private void lcdPrint(String s) {
        Log.w("lcdPrint", s);
        lcd.bprint(s);
        System.out.print(s);
    }
    private void lcdPrint(int pos, String s) {
        lcd.bprint(s);
    }
    private void lcdPrint(char c) {
        lcd.putchar(c);
        System.out.print(c);
    }
    private void lcdPrintln() {
        System.out.println();
    }
    private void lcdPrint(Double d) {
        if (d == null) return;
        Log.w("lcdPrint", String.format("%d", d.intValue()));

        int i = d.intValue();
        if (d < 10000000000d) {
            if (d == (double)i) {
                lcd.print(String.format("%d", i));
                //System.out.printf("%d", i);
            } else {
                lcd.print(String.format("%f", d));
                //System.out.printf("%f", d);
            }
        } else {
            lcd.print(String.format("%e", d));
            //System.out.printf("%e", d);
        }
    }

    private String double2string(Double d) {
        if (d == null) return null;
        //Log.w("lcdPrint", String.format("%d", d.intValue()));

        int i = d.intValue();
        String ret;
        if (d < 10000000000d) {
            if (d == (double)i) {
                ret = String.format("%d", i);
                //System.out.printf("%d", i);
            } else {
                ret = String.format("%f", d);
                //System.out.printf("%f", d);
            }
        } else {
            ret = String.format("%e", d);
            //System.out.printf("%e", d);
        }
        return ret;
    }

    public void lastAns() {
        //lcd.cls();
        lcdPrint(lastAns);
    }

    private void printTrace() {
        String s = "";
        //Log.w("TR", String.format("pc=%d idxEnd=%d", pc, idxEnd));
        for (int id = pc; id <= idxEnd && prog[id] != '\n'; id++) {
            //Log.w("TR", String.format("%c", prog[id][bank]));
            //s += String.format("%c", prog[id][bank]);
            s += (char)(prog[id]&0xff);
        }
        if (s != null) {
            Log.w("TR", String.format("(%d):'%s'", bank, s));
        }
    }

    public void loadProg() {
        String[] temp = source.getSourceAll(bank);
        if (temp == null) return;
        int p = 0;
        for (int i = 0; i < temp.length; i++) {
            for (int j = 0; j < temp[i].length(); j++) {
                prog[p++] = temp[i].charAt(j);
            }
            prog[p++] = '\n';
        }
        prog[p] = '\0';
        idxEnd = p - 1;
    }

    public void run() throws InterpreterException {
        loadProg();
        pc = 0;
        labelTable.clear();
        scanLabels();
        Log.w("RUN", String.format("-------- %s", labelTable));
        forcedExit = false;
        sb = null;
        prtStr = "";
        sbInterp();
    }

    private void bankChange(int b) throws InterpreterException {
        bankChange(b, 0);
    }

    private void bankChange(int b, int l) throws InterpreterException {
        bank = b;
        loadProg();
        pc = 0;
        labelTable.clear();
        scanLabels();
        pc = l;
        Log.w("bank change", String.format("-------- %s", labelTable));
        getToken();
        putBack();
    }

    public void cont() throws InterpreterException {
        sbInterp();
    }

    public void calc(String s) throws InterpreterException {
        if (s == "") return;

        //Log.w("calc", "exe");
        lcd.cls();
        //bank = 10;
        pc = 0;
        nextLine = true;
        int size = s.length();
        if (size != -1) {
            //prog = new char[size];

            for (int i = 0; i < size; i++) {
                prog[i] = s.charAt(i);
            }
            idxEnd = size - 1;
        }
        sbCmd();
    }

    private void sbCmd() throws InterpreterException {

        boolean strOpe = false;

        if (nextLine) {
            nextLine = false;
            printTrace();
        }
        getToken();
        if (tokType == VARIABLE || tokType == SVARIABLE) {
            if (tokType == SVARIABLE) strOpe = true;

            putBack();
            assignment();
            /*
            getToken();
            if (!token.equals(EOL) && tokType == DELIMITER && token.equals("=")) {
                putBack();
                putBack();
                assignment();
            } else {
                //Log.w("sbCmd1", "id="+pc);
                putBack();
                putBack();
                //Log.w("sbCmd1", "id="+pc);
                lastAns = evaluate();
                //Log.w("sbCmd1", String.format("%d", lastAns.intValue()));
            }
            */
        } else if (tokType == COMMAND) {
            switch (kwToken) {
                case RUN:
                    //lcd.cls();
                    //bank = 0;
                    Log.w("SBasic", String.format("--- RUN(%d) ---", bank));
                    pb.progStart();
                    return;
                    //break;
                case LIST:
                    Log.w("SBasic", String.format("--- LIST(%d) ---", bank));
                    getToken();
                    String s;
                    if (tokType == NUMBER) {
                        s = source.getSource(bank, Integer.parseInt(token));
                    } else {
                        s = source.getSourceTop(bank);
                    }
                    lcd.print(s, 0);
                    return;
                    //break;
                default:
                    break;
            }

        } else {
            //Log.w("sbCmd2", String.format("pc=%d", pc));
            putBack();
            //Log.w("sbCmd2", String.format("pc=%d", pc));
            lastAns = evaluate();
            //Log.w("sbCmd2", String.format("%d", lastAns.intValue()));
        }

        if (strOpe) {
            Log.w("sbCmd", String.format("ans=%s", resultStr));
            lcdPrint(resultStr);

        } else {
            Log.w("sbCmd", String.format("ans=%d", lastAns.intValue()));
            lcdPrint(lastAns);
            //lcdPrintln();
        }
    }

    public void sbExit() {
        forcedExit = true;
    }

    public boolean isForcedExitReq() {
        return forcedExit;
    }

    private void sbInterp() throws InterpreterException {
        long oldTime = 0, newTime;
        int loopCnt = 0;

        do {
            if (cpuClockEmulateEnable) {
                if (loopCnt == 0) {
                    oldTime = System.currentTimeMillis();
                }
            }
            if (pb.isProgStop()) {
                try{
                    Thread.sleep(200);
                }catch(InterruptedException e){}

                //Log.w("SBasic", "prog stop...");
                continue;
            }
            if (forcedExit) {
                break;
            }
            if (nextLine) {
                nextLine = false;
                printTrace();
            }
            getToken();
            if (tokType == VARIABLE || tokType == SVARIABLE) {
                putBack();
                assignment();
            } else if (tokType == COMMAND) {
                switch (kwToken) {
                    case VAC:
                        vac();
                        break;
                    case PRINT:
                        print();
                        break;
                    case GOTO:
                        execGoto();
                        break;
                    case IF:
                        execIf();
                        break;
                    case FOR:
                        execFor();
                        break;
                    case NEXT:
                        next();
                        break;
                    case INPUT:
                        input();
                        break;
                    case GOSUB:
                        gosub();
                        break;
                    case RETURN:
                        greturn();
                        break;
                    case END:
                        return;
                }
            } else if (tokType == DELIMITER) {
                //pc++;
                //Log.w("sbInterp", "DELIMITER");
            } else {
                //Log.w("sbInterp", "???");
            }

            // 処理速度の調整
            if (cpuClockEmulateEnable) {
                if (++loopCnt > 5) {
                    loopCnt = 0;
                    newTime = System.currentTimeMillis();
                    long sleepTime = 30 - (newTime - oldTime);

                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            ;
                        }
                    }

                }
            }

        } while (!token.equals(EOP));

    }

    private void scanLabels() throws InterpreterException {
        int i;
        Object result;

        int id = pc;
        getToken();
        Log.w("scanLabels", token);
        if (tokType == NUMBER) {
            labelTable.put(token, new Integer(id));
        }
        findEOL();
        do {
            id = pc;
            getToken();
            Log.w("scanLabels", token);
            if (tokType == NUMBER) {
                result = labelTable.put(token, new Integer(id));

                if (result != null) handleErr(ERR_SYSTEM);
            }
            if (kwToken != EOL) findEOL();
        } while (!token.equals(EOP));
        pc = 0;
    }

    private void findEOL() {
        while (pc < idxEnd + 1 && prog[pc] != '\n')
            pc++;
    }

    private void assignment() throws InterpreterException {
        int var;
        double value;
        char vname;

        getToken();
        Log.w("assignment", String.format("%s", token));
        if (isSval(token)) {
            // 文字列変数の処理
            vname = token.charAt(0);
            if (vname == '$') {
                var = 26;
            } else {
                var = (int) Character.toUpperCase(vname) - 'A';
            }

            String lastToken = token;
            getToken();
            if (token.equals("(")) {
                // 配列
                getToken();
                double result = evalExp2();
                var += result;
                if (!token.equals(")")) {
                    handleErr(ERR_SYNTAX);
                }
                getToken();
            } else if (!token.equals("=")) {
                putBack();
                token = lastToken;
                putBack();
                strOpe();
                return;
            }

            //getToken();
            strOpe();
            svars[var] = resultStr;

        } else {
            // 数値変数の処理
            vname = token.charAt(0);
            Log.w("assign", String.format("%c(%02x)", vname, (int)vname));

            if (!Character.isLetter(vname)) {
                handleErr(ERR_SYSTEM);
                Log.w("assign", "NOTVAR");
                return;
            }

            var = (int) Character.toUpperCase(vname) - 'A';

            String lastToken = token;
            getToken();
            if (token.equals("(")) {
                // 配列
                getToken();
                double result = evalExp2();
                var += result;
                if (!token.equals(")")) {
                    handleErr(ERR_SYNTAX);
                }
                getToken();
            } else if (!token.equals("=")) {
                //handleErr(EQUALEXPECTED);
                //return;
                putBack();
                token = lastToken;
                putBack();
                lastAns = evaluate();
                return;
            }

            value = evaluate();
            lastAns = value;
            //Log.w("assig", String.format("%d", lastAns.intValue()));

            vars[var] = value;
        }
    }

    private void vac() {
        Log.w("VAC", "variable cleared.");
        for (int i = 0; i < 26; i++) {
            vars[i] = 0;
            svars[i] = "";
        }
        svars[26] = "";
        lastAns = 0.0;
    }

    String prtStr = "";
    private void print() throws InterpreterException {
        double result;
        int len = 0, spaces;
        String lastDelim = "";
        int pos = 0;
        //String prtStr = "";
        //StringBuilder sb = null;

        //Log.w("PRT", "Exec PRINT !!!!");

        boolean paramExist = false;
        do {
            //Log.w("PRT", "Exec PRINT do !!!!");
            getToken();
            //Log.w("PRT", String.format("token=%s", token));
            /*
            if (!paramExist && (kwToken == EOL || token.equals(":"))) {
                prtStr = "";
                sb = null;
                break;
            }
            if (token.equals(EOP)) break;
            //if (tokType == DELIMITER && token.equals(":")) {
            if (token.equals(":")) {
                //Log.w("PRT", String.format("DELIMITER(:)"));
                break;
            }
            */

            if (kwToken == EOL || token.equals(EOP) || token.equals(":")) {
                if (!paramExist) {
                    prtStr = "";
                    sb = null;
                }
                break;
            }

            paramExist = true;
            if (tokType == QUTEDSTR) {
                //Log.w("PRT", String.format("tokType=QUTEDSTR(%d)", tokType));
                //System.out.print(token);
                //lcdPrint(token);
                if (sb == null) {
                    prtStr += token;
                } else {
                    sb.replace(pos, pos + token.length(), token);
                    pos += token.length();
                    Log.w("print", String.format("sb='%s'", sb.toString()));
                }
                //Log.w("PRT", String.format("%s", token));
                len += token.length();
                getToken();
            } else if (tokType == VARIABLE || tokType == NUMBER) {
                //Log.w("PRT", String.format("tokType!=QUTEDSTR(%d)", tokType));
                putBack();
                result = evaluate();
                getToken();
                //Log.w("PRT", String.format("AAAAA=%s", token));
                //System.out.print(result);
                //lcdPrint(result);
                //String s = String.valueOf(result);
                //int l = s.length();
                //prtStr += s.substring(0, l < 12 ? l : 12);
                //prtStr += double2string(result);
                //Log.w("PRT", String.format("%d", result));

                String s = double2string(result);
                if (sb == null) {
                    prtStr += s;
                } else {
                    sb.replace(pos, pos + s.length(), s);
                    pos += s.length();
                    Log.w("print", String.format("sb='%s'", sb.toString()));
                }

                Double t = new Double(result);
                len += t.toString().length();
            } else if (tokType == SVARIABLE || tokType == FUNCTION && kwToken == MID) {
                putBack();
                strOpe();
                //String s = findSVer(token);
                if (!resultStr.isEmpty()) {
                    if (sb == null) {
                        prtStr += resultStr;
                    } else {
                        sb.replace(pos, pos + resultStr.length(), resultStr);
                        pos += resultStr.length();
                        Log.w("print", String.format("sb='%s'", sb.toString()));
                    }

                }
                /*
                if (s != null) {
                    //lcdPrint(s);
                    //prtStr += s;
                    if (sb == null) {
                        prtStr += s;
                    } else {
                        sb.replace(pos, pos + s.length(), s);
                        Log.w("print", String.format("sb='%s'", sb.toString()));
                    }

                }
                */
                len += resultStr.length();
                getToken();
            } else if (tokType == COMMAND) {
                if (kwToken == CSR) {
                    //lcdPrint(prtStr);
                    result = evaluate() + 0.01; // イマイチ！！
                    Log.w("CSR", "pos="+result);
                    if (result < 12) {
                        pos = (int) result;
                        String s = "";
                        if (prtStr.length() < 11) {
                            for (int i = 0; i < 11 - prtStr.length(); i++) {
                                s += " ";
                            }
                        }
                        if (sb == null) {
                            Log.w("print", String.format("sb=new '%s'", prtStr+s));
                            sb = new StringBuilder(prtStr + s);
                        }
                        Log.w("print", String.format("sb='%s'", sb.toString()));
                        //lcdPrint(prtStr);
                    }
                }
            }
            if (sb != null) prtStr = sb.toString();
            lcdPrint(prtStr);

            if (kwToken == EOL || token.equals(EOP) || token.equals(":")) {
                break;
            }

            lastDelim = token;
            Log.w("PRT", String.format("lastDelim='%s'", lastDelim));

            if (lastDelim.equals(":")) {
                ;
            } else if (lastDelim.equals(",")) {
                spaces = 8 - (len % 8);
                len += spaces;
                while (spaces != 0) {
                    //System.out.print(" ");
                    //lcdPrint(" ");
                    prtStr += " ";
                    spaces--;
                }
            } else if (token.equals(";")) {
                //System.out.print(" ");
                //lcdPrint(" ");
                //len++;
            } else if (kwToken != EOL && !token.equals(EOP)) {
                handleErr(ERR_SYNTAX);
            }
        } while (lastDelim.equals(";") || lastDelim.equals(","));
        if (sb != null) prtStr = sb.toString();
        if (kwToken == EOL || token.equals(EOP) || token.equals(":")) {
            //if (!lastDelim.equals(";") && !lastDelim.equals(",")) {
            //System.out.println();
            //lcdPrintln();
            //}
            if (prtStr.isEmpty() || lastDelim.equals(";")) {
                Log.w("print", "lcdPrint");
                lcdPrint(prtStr);
            } else {
                Log.w("print", String.format("lcdPrintAndPause str='%s'", prtStr));
                lcdPrintAndPause(prtStr);
                prtStr = "";
            }
        } else {
            handleErr(ERR_SYNTAX);
        }

    }

    private void execGoto() throws InterpreterException {
        Integer loc;
        getToken();
        Log.w("GOTO", String.format("%s", token));

        if (token.charAt(0) == '#' && '0' <= token.charAt(1) && token.charAt(1) <= '9') {
            // バンク切り替え
            int b = token.charAt(1) - '0';
            Log.w("GOTO", String.format("bank change -> #%d", b));
            bankChange(b);
        } else {
            loc = (Integer) labelTable.get(token);
            if (loc == null) {
                handleErr(ERR_UNDEFLINE);
            } else {
                pc = loc.intValue();
                nextLine = true;
                //Log.w("GOTO", String.format("goto %s(%d)", token, loc));
            }
        }
    }


    private void execIf() throws InterpreterException {
        double result;
        getToken();
        if (tokType == SVARIABLE ||
                tokType == FUNCTION && (kwToken == KEY || kwToken == MID) ) {
            putBack();
            boolean ret = strOpe();
            Log.w("IF", String.format("judge=%d ret='%s'", (ret ? 1 : 0), resultStr));
            if (ret) {
                getToken();

                if (kwToken != THEN && token.charAt(0) != ';') {
                    //Log.w("IF", "handleErr");
                    handleErr(ERR_SYNTAX);
                    return;
                }
                if (kwToken == THEN) {
                    //Log.w("IF", "THEN");
                    getToken();
                    if (token == EOP) {
                        handleErr(ERR_SYNTAX);
                    }
                    if (tokType == NUMBER || tokType == VARIABLE ||
                            token.charAt(0) == '#' && '0' <= token.charAt(1) && token.charAt(1) <= '9') {
                        putBack();
                        Log.w("IF", "THEN -> GOTO");
                        execGoto();
                    } else {
                        putBack();
                    }
                } else {
                    //Log.w("IF", "not THEN");
                }

            } else {
                findEOL();
            }
        } else {
            putBack();
            result = evaluate();

            Log.w("IF", String.format("if (%d)", (int)result));
            if (result != 0.0) {
                getToken();

                if (kwToken != THEN && token.charAt(0) != ';') {
                    //Log.w("IF", "handleErr");
                    handleErr(ERR_SYNTAX);
                    return;
                }
                if (kwToken == THEN) {
                    //Log.w("IF", "THEN");
                    getToken();
                    if (token == EOP) {
                        handleErr(ERR_SYNTAX);
                    }
                    if (tokType == NUMBER || tokType == VARIABLE ||
                            token.charAt(0) == '#' && '0' <= token.charAt(1) && token.charAt(1) <= '9') {
                        putBack();
                        Log.w("IF", "THEN -> GOTO");
                        execGoto();
                    } else {
                        putBack();
                    }
                } else {
                    //Log.w("IF", "not THEN");
                }
            } else {
                findEOL();
            }
        }
    }

    private void execFor() throws InterpreterException {
        ForInfo stckvar = new ForInfo();
        double value;
        char vname;
        getToken();
        vname = token.charAt(0);
        if (!Character.isLetter(vname)) {
            handleErr(ERR_SYSTEM);
            return;
        }

        stckvar.var = Character.toUpperCase(vname) - 'A';

        getToken();
        if (token.charAt(0) != '=') {
            handleErr(ERR_SYNTAX);
            return;
        }

        value = evaluate();
        vars[stckvar.var] = value;
        getToken();
        if (kwToken != TO) handleErr(ERR_SYNTAX);
        stckvar.target = evaluate();

        if (value >= vars[stckvar.var]) {
            stckvar.loc = pc;
            fStack.push(stckvar);
        } else {
            while (kwToken != NEXT) getToken();
        }
    }

    private void next() throws InterpreterException {
        ForInfo stckvar;
        //Log.w("NEXT", "do");
        try {
            stckvar = (ForInfo) fStack.pop();
            vars[stckvar.var]++;

            if (vars[stckvar.var] > stckvar.target) {
                //Log.w("NEXT", "loop end.");
                return;
            }
            fStack.push(stckvar);
            pc = stckvar.loc;
        } catch (EmptyStackException e) {
            //Log.w("NEXT", "exception occered.");
            handleErr(ERR_SYNTAX);
        }
        //Log.w("NEXT", "continue.");
    }

    private void input() throws InterpreterException {
        int var;
        //String vname;
        double val = 0.0;
        String str = "";
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        getToken();
        if (tokType == QUTEDSTR) {
            //System.out.print(token);
            //lcdPrint(token);
            str = token;
            getToken();
            if (!token.equals(",")) handleErr(ERR_SYNTAX);
            getToken();
        }
        lcdPrint(str+"?");
        getInputStream();
        int idx;
        if (tokType == VARIABLE) {
            Log.w("input", String.format("input=%s", inText));
            idx = Character.toUpperCase(token.charAt(0)) - 'A';
            try {
                vars[idx] = Integer.parseInt(inText);
            } catch (NumberFormatException e) {
                handleErr(ERR_ARGUMENT);
            }
            getToken();
        } else if (tokType == SVARIABLE) {
            if (token.charAt(0) == '$') {
                idx = 26;
            } else {
                idx = Character.toUpperCase(token.charAt(0)) - 'A';
            }
            svars[idx] = inText;
            getToken();
        } else {
            handleErr(ERR_SYNTAX);
        }
        //System.out.print("? ");
        //lcdPrint(str+"?");
        //getInputStream();
        //pb.progStop();
    }

    public static String inText;
    public static boolean inputWait = false;
    private void getInputStream() {
        inText = "";
        inputWait = true;
        do {
            if (pb.isProgStop()) {
                break;
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                ;
            }
        } while (inText.isEmpty());

        inputWait = false;
        return;
    }

    private void gosub() throws InterpreterException {
        Integer loc;
        getToken();

        if (tokType == BANKNUM) {
            gStack.push(new GosubInfo(bank, pc));
            // バンク切り替え
            int b = token.charAt(1) - '0';
            Log.w("GOSUB", String.format("bank change -> #%d(%d)", b, pc));
            bankChange(b);
        } else {
            loc = (Integer) labelTable.get(token);
            if (loc == null) {
                handleErr(ERR_UNDEFLINE);
            } else {
                //gStack.push(new Integer(pc));
                gStack.push(new GosubInfo(bank, pc));

                pc = loc.intValue();
                nextLine = true;
            }
        }
    }

    private void greturn() throws InterpreterException {
        //Integer t;
        GosubInfo gi;

        try {
            //t = (Integer) gStack.pop();
            //pc = t.intValue();
            gi = (GosubInfo)gStack.pop();
            Log.w("GOSUB-RETURN", String.format("return #%d(%d)", gi.bank, gi.pc));
            if (bank != gi.bank) {
                bankChange(gi.bank, gi.pc);
                nextLine = true;
            } else {
                pc = gi.pc;
                nextLine = true;
            }
        } catch (EmptyStackException e) {
            handleErr(ERR_SYNTAX);
        }
    }

    //****************************************************
    private boolean strOpe() throws InterpreterException {
        boolean result = false;
        getToken();
        if (token.equals(EOP)) {
            handleErr(ERR_SYSTEM);
        }
        result = strOpe1();
        putBack();
        return result;
    }

    private boolean strOpe1() throws InterpreterException {
        Log.w("strOpe1", String.format("exec : token='%s' tokType=%d", token, tokType));
        String l_temp, r_temp;
        boolean result = false;
        char op;

        result = strOpe2();

        if (token.equals(EOP)) {
            //Log.w("eval1", String.format("EOP!!! ret=%e", result));
            return  result;
        }

        op = token.charAt(0);
        if (isRelop(op)) {
            //Log.w("eval1", String.format("op='%c'", op));
            l_temp = resultStr;
            getToken();
            strOpe1();
            r_temp = resultStr;

            Log.w("strOpe1", String.format("compare!!! L='%s' R='%s'", l_temp, r_temp));
            switch (op) {
                case '=':
                    result = l_temp.equals(r_temp);
                    break;
                case NE:
                    result = !l_temp.equals(r_temp);
                    break;
                default:
                    break;
            }
            resultStr = l_temp;
        }
        Log.w("strOpe1-end", String.format("result=%d ret='%s'", (result ? 1 : 0), resultStr));
        return  result;
    }

    private boolean strOpe2() throws InterpreterException {
        Log.w("strOpe2", String.format("exec : token='%s' tokType=%d", token, tokType));
        char op;
        boolean result = false;
        String str1, str2;

        result = strOpe3();
        str1 = resultStr;

        while ((op = token.charAt(0)) == '+') {
            //Log.w("eval2", String.format("op=%c", op));
            getToken();
            //Log.w("eval2", String.format("next token=%s", token));
            strOpe3();
            str2 = resultStr;

            switch (op) {
                case '+':
                    str1 += str2;
                    break;
                default:
                    break;
            }
        }
        Log.w("strOpe2-end", String.format("ret='%s'", resultStr));
        resultStr = str1;
        return result;
    }

    private boolean strOpe3() throws InterpreterException {
        Log.w("strOpe3", String.format("exec : token='%s' tokType=%d", token, tokType));
        boolean result = false;

        switch (tokType) {
            case SVARIABLE:
                Log.w("strOpe3", "SVARIABLE");
                char ch = token.charAt(0);
                if (ch == '$') {
                    resultStr = findSVar(token);
                    getToken();
                } else {
                    getToken();
                    if (token.equals("(")) {
                        // 配列
                        getToken();
                        double r = evalExp2();
                        ch += r;
                        if (!token.equals(")")) {
                            handleErr(ERR_SYNTAX);
                        }
                        getToken();
                    }
                    resultStr = findSVar(String.valueOf(ch));
                }

                break;
            case QUTEDSTR:
                Log.w("strOpe3", "QUTERSTR");
                resultStr = token;
                getToken();
                break;
            case FUNCTION:
                switch (kwToken) {
                    case KEY:
                        Log.w("strOpe3", "KEY");
                        char c = (char)inkey.getPressKeyCode();
                        if (c == 0) {
                            resultStr = "";
                        } else {
                            resultStr = String.valueOf(c);
                        }
                        result = true;
                        //Log.w("atom", String.format("KEY result=%d", result));
                        getToken();
                        break;

                    case MID:
                        //Log.w("MID", "MID");
                        int m, n;
                        getToken();
                        //Log.w("MID", token);
                        if (token.equals("(")) {
                            getToken();
                            //Log.w("--- MID", token);
                            putBack();
                            m = (int)evaluate();
                            m--;
                            getToken();
                            //Log.w("---- MID", token);
                            if (token.equals(")") || token.equals(":") || kwToken == EOL) {
                                if (m < 0 || m >= svars[26].length()) {
                                    Log.w("MID", String.format("len=%d, m=%d", svars[26].length(), m));
                                    handleErr(ERR_ARGUMENT);
                                }
                                resultStr = svars[26].substring(m);
                                result = true;
                                //Log.w("strOpe3", String.format("m=%d ret='%s'", m, resultStr));
                                getToken();
                            } else if (token.equals(",")) {
                                getToken();
                                putBack();
                                n = (int)evaluate();
                                getToken();
                                if (!token.equals(")") && !token.equals(":") && kwToken != EOL) {
                                    handleErr(ERR_SYNTAX);
                                }
                                if (m < 0 || m >= svars[26].length() || n <= 0 || m + n > svars[26].length()) {
                                    Log.w("MID", String.format("len=%d, m=%d, n=%d", svars[26].length(), m, m + n));
                                    handleErr(ERR_ARGUMENT);
                                }
                                resultStr = svars[26].substring(m, m + n);
                                result = true;
                                //Log.w("strOpe3", String.format("m=%d n=%d ret='%s'", m, n, resultStr));
                                getToken();
                            }
                            //getToken();
                        } else {
                            handleErr(ERR_SYNTAX);
                        }
                        break;

                    default:
                        Log.w("strOpe3", "No function");
                        break;
                }
                break;
            default:
                //Log.w("strOpe3", "handleErr");
                handleErr(ERR_SYNTAX);
                break;
        }
        Log.w("strOpe3", String.format("ret='%s'", resultStr));
        return result;

    }

    //****************************************************
    private double evaluate() throws InterpreterException {
        //Log.w("eval", "exec");
        double result = 0.0;
        getToken();
        if (token.equals(EOP)) {
            handleErr(ERR_SYSTEM);
        }
        //Log.w("eval", String.format("token=%s", token));
        result = evalExp1();
        putBack();
        //Log.w("eval-end", String.format("ret=%e", result));
        return  result;
    }

    // <, >, =, <=, >=, <>
    private double evalExp1() throws InterpreterException {
        //Log.w("eval1", "exec");
        double l_temp, r_temp, result;
        char op;

        result = evalExp2();

        if (token.equals(EOP)) {
            //Log.w("eval1", String.format("EOP!!! ret=%e", result));
            return  result;
        }

        op = token.charAt(0);
        if (isRelop(op)) {
            //Log.w("eval1", String.format("op='%c'", op));
            l_temp = result;
            getToken();
            r_temp = evalExp1();

            //Log.w("eval1", String.format("compare!!! L=%e R=%e", l_temp, r_temp));
            switch (op) {
                case '<':
                    if (l_temp < r_temp) {
                        result = 1.0;
                    } else {
                        result = 0.0;
                    }
                    break;
                case LE:
                    if (l_temp <= r_temp) {
                        result = 1.0;
                    } else {
                        result = 0.0;
                    }
                    break;
                case '>':
                    if (l_temp > r_temp) {
                        result = 1.0;
                    } else {
                        result = 0.0;
                    }
                    break;
                case GE:
                    if (l_temp >= r_temp) {
                        result = 1.0;
                    } else {
                        result = 0.0;
                    }
                    break;
                case '=':
                    if (l_temp == r_temp) {
                        result = 1.0;
                    } else {
                        result = 0.0;
                    }
                    break;
                case NE:
                    if (l_temp != r_temp) {
                        result = 1.0;
                    } else {
                        result = 0.0;
                    }
                    break;
                default:
                    break;
            }
        }
        //Log.w("eval1-end", String.format("ret=%e", result));
        return  result;
    }

    private double evalExp2() throws InterpreterException {
        //Log.w("eval2", "exec");
        char op;
        double result;
        double partialResult;

        result = evalExp3();

        while ((op = token.charAt(0)) == '+' || op == '-') {
            Log.w("eval2", String.format("op=%c", op));
            getToken();
            Log.w("eval2", String.format("next token=%s", token));
            partialResult = evalExp3();
            switch (op) {
                case '-':
                    result = result - partialResult;
                    break;
                case '+':
                    result = result + partialResult;
                    break;
                default:
                    break;
            }
        }
        //Log.w("eval2", String.format("ret=%e", result));
        return result;
    }

    private double evalExp3() throws InterpreterException {
        //Log.w("eval3", "exec");
        char op;
        double result;
        double partialResult;

        result = evalExp4();

        while ((op = token.charAt(0)) == '*' || op == '/' || op == '%') {
            getToken();
            partialResult = evalExp4();
            switch (op) {
                case '*':
                    result = result * partialResult;
                    break;
                case '/':
                    if (partialResult == 0.0)
                        handleErr(ERR_MATH);
                    result = result / partialResult;
                    break;
                case '%':
                    if (partialResult == 0.0)
                        handleErr(ERR_MATH);
                    result = result % partialResult;
                    break;
                default:
                    break;
            }
        }
        //Log.w("eval3", String.format("ret=%e", result));
        return result;
    }

    private double evalExp4() throws InterpreterException {
        //Log.w("eval4", "exec");
        double result;
        double partialResult;
        double ex;
        int t;

        result = evalExp5();

        if (token.equals("^")) {
            getToken();
            partialResult = evalExp4();
            ex = result;
            if (partialResult == 0.0) {
                result = 1.0;
            } else {
                for (t = (int) partialResult - 1; t > 0; t--) {
                    result = result * ex;
                }
            }
        }
        //Log.w("eval4", String.format("ret=%e", result));
        return result;
    }

    private double evalExp5() throws InterpreterException {
        //Log.w("eval5", "exec");
        double result;
        String op;

        op = "";
        if ((tokType == DELIMITER) && token.equals("+") || token.equals("-")) {
            op = token;
            getToken();
            //Log.w("eval5", String.format("type=DELIMITER op='%s' nexttoken='%s'", op, token));
        }
        result = evalExp6();

        if (op.equals("-")) {
            result = -result;
        }

        //Log.w("eval5", String.format("ret=%e", result));
        return result;
    }

    private double evalExp6() throws InterpreterException {
        //Log.w("eval6", "exec");
        double result;

        if (token.equals("(")) {
            getToken();
            result = evalExp2();
            if (!token.equals(")") && !token.equals(":") && kwToken != EOL) {
                handleErr(ERR_SYNTAX);
            }
            getToken();
        } else {
            result = atom();
        }
        //Log.w("eval6", String.format("ret=%e", result));
        return result;
    }

    private double atom() throws InterpreterException {
        Log.w("atom", "exec");
        double result = 0.0;

        switch (tokType) {
            case NUMBER:
                try {
                    result = Double.parseDouble(token);
                    //Log.w("atom", String.format("result=%e", result));
                } catch (NumberFormatException e) {
                    handleErr(ERR_SYNTAX);
                }
                getToken();
                //Log.w("atom", String.format("next token='%s'", token));
                break;
            case VARIABLE:
                char ch = token.charAt(0);
                getToken();
                if (token.equals("(")) {
                    // 配列
                    getToken();
                    double r = evalExp2();
                    ch += r;
                    if (!token.equals(")")) {
                        handleErr(ERR_SYNTAX);
                    }
                    getToken();
                }

                result = findVar(String.valueOf(ch));
                //result = findVar(token);
                //Log.w("atom", String.format("var=%s result=%e", token, result));
                //getToken();
                //Log.w("atom", String.format("next token='%s'", token));
                break;
            case FUNCTION:
                String str;
                switch (kwToken) {
                    case SIN:
                        getToken();
                        if (!token.equals(EOP)) {
                            //double temp = evalExp2();
                            double temp = evalExp6();
                            try {
                                result = Math.sin(Math.toRadians(temp));
                                nop20ms();
                                //Log.w("atom", String.format("SIN result=%e", result));
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case COS:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.cos(Math.toRadians(temp));
                                nop20ms();
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case TAN:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.tan(Math.toRadians(temp));
                                nop20ms();
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case ASN:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.toDegrees(Math.asin(temp));
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case ACS:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.toDegrees(Math.acos(temp));
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case ATN:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.toDegrees(Math.atan(temp));
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case ABS:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.abs(temp);
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case LOG:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.log10(temp);
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case LN:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.log(temp);
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case SQR:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.sqrt(temp);
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case EXP:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            try {
                                result = Math.exp(temp);
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case KEY:
                        result = inkey.getPressKeyCode();
                        Log.w("atom", String.format("KEY result=%d", (int)result));
                        getToken();
                        break;

                    case RAN:
                        result = Math.random();
                        getToken();
                        break;

                    case INT:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            result = (int)temp;
                        }
                        //getToken();
                        break;

                    case FRAC:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            result = temp - (int)temp;
                        }
                        //getToken();
                        break;

                    case SGN:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6();
                            if (temp > 0) {
                                result = 1;
                            } else if (temp < 0) {
                                result = -1;
                            } else {
                                result = 0;
                            }
                        }
                        //getToken();
                        break;

                    case LEN:
                        str = "";
                        do {
                            getToken();
                            if (kwToken == EOL || token.equals(EOP) || token.equals(":")) break;

                            if (tokType == QUTEDSTR) {
                                str += token;
                                getToken();
                            } else if (tokType == SVARIABLE) {
                                putBack();
                                strOpe();
                                if (!resultStr.isEmpty()) {
                                    str += resultStr;
                                }
                                getToken();
                            } else {
                                handleErr(ERR_SYNTAX);
                            }
                        } while (kwToken != EOL && !token.equals(EOP));
                        result = str.length();
                        break;

                    case VAL:
                        str = "";
                        do {
                            getToken();
                            if (kwToken == EOL || token.equals(EOP) || token.equals(":")) break;

                            if (tokType == QUTEDSTR) {
                                str += token;
                                getToken();
                            } else if (tokType == SVARIABLE) {
                                putBack();
                                strOpe();
                                if (!resultStr.isEmpty()) {
                                    str += resultStr;
                                }
                                getToken();
                            } else {
                                handleErr(ERR_SYNTAX);
                            }
                        } while (kwToken != EOL && !token.equals(EOP));

                        try {
                            result = Integer.parseInt(str);
                        } catch (NumberFormatException e) {
                            handleErr(ERR_SYNTAX);
                        }
                        break;

                    default:
                        break;
                }
                break;

            default:
                handleErr(ERR_SYNTAX);
                break;
        }
        Log.w("atom", String.format("ret=%e", result));
        return result;
    }

    private double findVar(String vname) throws InterpreterException {
        if (!Character.isLetter(vname.charAt(0))) {
            handleErr(ERR_SYSTEM);
            return 0.0;
        }
        return vars[Character.toUpperCase(vname.charAt(0)) - 'A'];
    }

    private String findSVar(String vname) throws InterpreterException {
        if (vname.equals("$")) {
            return svars[26];
        }
        if (!Character.isLetter(vname.charAt(0))) {
            handleErr(ERR_SYSTEM);
            return null;
        }
        return svars[Character.toUpperCase(vname.charAt(0)) - 'A'];
    }

    private void putBack() {
        //if  (token == EOP) return;
        if (pc < 1) return;
        for (int i = 0; i < token.length(); i++) {
            if (pc > 0) pc--;
        }
        Log.w("putBack", "pc="+pc);
    }

    private void getToken() throws InterpreterException {
        char ch;
        tokType = NONE;
        token = "";
        kwToken = UNKNCOM;

        //Log.w("getToken", String.format("exec!"));

        if (pc == idxEnd + 1) {
            token = EOP;
            return;
        }

        while (pc < idxEnd + 1 && isSpaceorTab(prog[pc])) {
            pc++;
        }

        if (pc == idxEnd + 1) {
            token = EOP;
            tokType = DELIMITER;
            //Log.w("getToken", "return(EOP)");
            return;
        }

        if (prog[pc] == '\n') {
            //pc += 2;
            pc += 1;
            kwToken = EOL;
            //token = "\r\n";
            token = "\n";
            //Log.w("getToken", "return(EOL)");
            nextLine = true;
            return;
        }

        ch = (char)(prog[pc]&0xff);
        if (ch == '<' || ch == '>') {
            if (pc + 1 == idxEnd + 1) handleErr(ERR_SYNTAX);

            switch (ch) {
                case '<':
                    if (prog[pc + 1] == '>') {
                        pc += 2;
                        token = String.valueOf(NE);
                    } else if (prog[pc + 1] == '=') {
                        pc += 2;
                        token = String.valueOf(LE);
                    } else {
                        pc++;
                        token = "<";
                    }
                    break;
                case '>':
                    if (prog[pc + 1] == '=') {
                        pc +=2;
                        token = String.valueOf(GE);
                    } else {
                        pc++;
                        token = ">";
                    }
                    break;
            }
            tokType = DELIMITER;
            //Log.w("getToken", "return(DELIMITER <>)");
            return;
        }

        //Log.w("getToken", "!!!!!!");

        if (isDelim(prog[pc])) {
            token += (char)(prog[pc]&0xff);
            pc++;
            tokType = DELIMITER;
            Log.w("getToken", String.format("case DELIMITER token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
        } else if (Character.isLetter(prog[pc]) || prog[pc] == '$') {
            while (!isDelim(prog[pc])) {
                token += (char)(prog[pc]&0xff);
                pc++;
                if (pc >= idxEnd + 1) break;
            }
            kwToken = lookUp(token);
            if (kwToken == UNKNCOM) {
                if (isSval(token)) {
                    tokType = SVARIABLE;
                } else {
                    tokType = VARIABLE;
                }
            } else if (kwToken > FUNC_DUMMY) {
                tokType = FUNCTION;
            } else {
                tokType = COMMAND;
            }

            if (tokType == VARIABLE) {
                Log.w("getToken", String.format("case VARIABLE token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
            } else if (tokType == SVARIABLE) {
                    Log.w("getToken", String.format("case SVARIABLE token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
            } else if (tokType == FUNCTION) {
                Log.w("getToken", String.format("case FUNCTION token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
            } else {
                Log.w("getToken", String.format("case COMMAND token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
            }
        } else if (Character.isDigit(prog[pc])) {
            while (!isDelim(prog[pc])) {
                token += (char)(prog[pc]&0xff);
                pc++;
                if (pc >= idxEnd + 1) break;
            }
            tokType = NUMBER;
            Log.w("getToken", String.format("case NUMBER token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
        } else  if (prog[pc] == '\"') {
            //Log.w("getToken", "DQ!!!");
            tokType = QUTEDSTR;
            pc++;
            ch = (char) (prog[pc] & 0xff);
            while (ch != '\"' && ch != '\n') {
                token += ch;
                pc++;
                //tokType = QUTEDSTR;
                ch = (char) (prog[pc] & 0xff);
                //Log.w("while", String.format("%c", ch));
            }
            pc++;
            Log.w("getToken", String.format("case QUTESTR token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
        } else if (prog[pc] == '#') {
            tokType = BANKNUM;
            pc++;
            ch = (char) (prog[pc] & 0xff);
            token = "#"+ch;
            pc++;
            Log.w("getToken", String.format("case BANKNUM token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
        } else {
            token = EOP;
            Log.w("getToken", "return(EOP)");
            return;
        }
        //Log.w("getToken", "func-end");
    }

    private boolean isDelim(int c) {
        char ne = 0xf1;
        char le = 0xf3;
        char ge = 0xf4;
        if ((" \n,;<>+-/*%^=():"+ne+le+ge).indexOf(c) != -1) {
            return true;
        }
        return false;
    }

    private boolean isSpaceorTab(int c) {
        if (c == ' ' || c == '\t') {
            return true;
        }
        return false;
    }

    private boolean isRelop(int c) {
        if (relops.indexOf(c) != -1) {
            return true;
        }
        return false;
    }

    private int lookUp(String s) {
        s = s.toLowerCase();

        for (int i = 0; i < kwTable.length; i++) {
            if (kwTable[i].keyword.equals(s)) {
                return kwTable[i].keywordTok;
            }
        }
        return UNKNCOM;
    }

    private boolean isSval(String s) {
        if (s == null) return false;

        char c = Character.toUpperCase(s.charAt(0));
        if (c == '$') return true;
        if (s.length() > 1 && 'A' <= c && c < 'Z' && s.charAt(1) == '$') {
            return true;
        } else {
            return false;
        }
    }

    private void handleErr(int error) throws InterpreterException {
        String[] err = {
                "System error",
                "Memory overflow",
                "Syntax error",
                "Mathematical error",
                "Undefines line number",
                "Argument error",
                "Variable error",
                "Nesting error",
                "dummy",
                "Option error"
        };


        int idx = 0;
        String key = "";
        boolean findValue = false;
        for (Map.Entry<String, Integer>map : labelTable.entrySet()) {
            if (pc >= map.getValue()) {
                if (idx < map.getValue()) {
                    idx = map.getValue();
                    key = map.getKey();
                    findValue = true;
                }
            }
        }
        if (!findValue) {
            Log.w("handleErr", String.format("%s(%d)", err[error], error));
            lcdPrintAndPause(String.format("ERR%d", error));
        } else {
            Log.w("handleErr", String.format("%s(%d) P%d-%s", err[error], error, bank, key));
            lcdPrintAndPause(String.format("ERR%d P%d-%s", error, bank, key));
        }
        throw  new InterpreterException(err[error]);
    }

}

