package tk.horiuchi.pokecom2;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static tk.horiuchi.pokecom2.Common.MODE_PRO;
import static tk.horiuchi.pokecom2.Common.MODE_RUN;
import static tk.horiuchi.pokecom2.Common.MODE_SAVE;
import static tk.horiuchi.pokecom2.Common._EM;
import static tk.horiuchi.pokecom2.Common._EX;
import static tk.horiuchi.pokecom2.Common._GE;
import static tk.horiuchi.pokecom2.Common._LE;
import static tk.horiuchi.pokecom2.Common._NE;
import static tk.horiuchi.pokecom2.Common._PI;
import static tk.horiuchi.pokecom2.Common.cmdTable;
import static tk.horiuchi.pokecom2.Common.reservCodePB100;
import static tk.horiuchi.pokecom2.MainActivity.angleUnit;
import static tk.horiuchi.pokecom2.MainActivity.bank;
import static tk.horiuchi.pokecom2.MainActivity.cpuClockEmulateEnable;
import static tk.horiuchi.pokecom2.MainActivity.debugText;
import static tk.horiuchi.pokecom2.MainActivity.initial;
import static tk.horiuchi.pokecom2.MainActivity.inkey;
import static tk.horiuchi.pokecom2.MainActivity.last_mode;
import static tk.horiuchi.pokecom2.MainActivity.listDisp;
import static tk.horiuchi.pokecom2.MainActivity.memoryExtension;
import static tk.horiuchi.pokecom2.MainActivity.mode;
import static tk.horiuchi.pokecom2.MainActivity.pb;
import static tk.horiuchi.pokecom2.MainActivity.progLength;
import static tk.horiuchi.pokecom2.MainActivity.source;
import static tk.horiuchi.pokecom2.MainActivity.waveout;

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
    final int STEP = 8;
    final int GOTO = 9;
    final int GOSUB = 10;
    final int RETURN = 11;
    final int END = 12;
    final int EOL = 13;
    final int RUN = 14;
    final int LIST = 15;
    final int CSR = 16;
    final int VAC = 17;
    final int STOP = 18;
    final int DEFM = 19;
    final int CLEAR = 20;
    final int SAVE = 21;
    final int LOAD = 22;
    final int BEEP = 23;
    final int ON   = 24;
    final int LET  = 25;
    final int REM  = 26;
    final int READ = 27;
    final int DATA = 28;
    final int RESTORE = 29;
    final int SET  = 30;

    final int FUNC_DUMMY = 50;
    final int LEN  = 52;
    final int MID  = 53;
    final int VAL  = 54;
    final int SIN  = 55;
    final int COS  = 56;
    final int TAN  = 57;
    final int ASN  = 58;
    final int ACS  = 59;
    final int ATN  = 60;
    final int LOG  = 61;
    final int LN   = 62;
    final int EXP  = 63;
    final int SQR  = 64;
    final int ABS  = 65;
    final int SGN  = 66;
    final int INT  = 67;
    final int FRAC = 68;
    final int RND  = 69;
    final int RAN  = 70;
    final int KEY  = 71;
    final int PI   = 72;
    final int STR  = 73;

    //
    //final int EOL = '\n';
    final String EOP = "\0";


    //
    private BigDecimal[] vars;
    private BigDecimal[] exvars;
    private String[] svars;
    private String[] exsvars;

    private boolean forcedExit = false;
    private String resultStr;
    private StringBuilder sb = null;

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
            new Keyword("step", STEP),
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
            new Keyword("pi", PI),
            new Keyword("end", END),
            new Keyword("run", RUN),
            new Keyword("defm", DEFM),
            new Keyword("list", LIST),
            new Keyword("clear", CLEAR),
            new Keyword("stop", STOP),
            new Keyword("save", SAVE),
            new Keyword("load", LOAD),

            new Keyword("let", LET),
            new Keyword("rem", REM),
            new Keyword("key$", KEY),
            new Keyword("on", ON),
            new Keyword("read", READ),
            new Keyword("data", DATA),
            new Keyword("restore", RESTORE),
            new Keyword("mid$", MID),
            new Keyword("str$", STR),
            new Keyword("beep", BEEP)
    };

    private char[] prog;
    private int pc;
    private int idxEnd;
    private String token;
    private int tokType;
    private int kwToken;
    private String cmdLine = "";

    private boolean nextLine = true;
    private BigDecimal lastAns;
    private Lcd lcd;

    //
    class ForInfo {
        int var;
        BigDecimal target;
        BigDecimal step;
        int loc;
    }

    //
    private  Stack fStack;

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
            _GE, _NE, _LE, '<', '>', '=', 0
    };

    String relops = new String(rops);


    public SBasic(Lcd lcd) throws InterpreterException {
        this.lcd = lcd;
        vars = new BigDecimal[26];
        svars = new String[26];
        for (int i = 0; i < svars.length; i++) {
            svars[i] = "";
            vars[i] = BigDecimal.ZERO;
        }
        ssvar = "";
        fStack = new Stack();
        labelTable = new TreeMap<String, Integer>();
        gStack = new Stack();
        lastAns = new BigDecimal(0);
        prog = new char[progLength];
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

    public void printSaveStatus(int n) {
        switch (13-n) {
            case 1:  lcdPrint("*"); break;
            case 2:  lcdPrint("**"); break;
            case 3:  lcdPrint("***"); break;
            case 4:  lcdPrint("****"); break;
            case 5:  lcdPrint("*****"); break;
            case 6:  lcdPrint("******"); break;
            case 7:  lcdPrint("*******"); break;
            case 8:  lcdPrint("********"); break;
            case 9:  lcdPrint("*********"); break;
            case 10: lcdPrint("**********"); break;
            case 11: lcdPrint("***********"); break;
            case 12: lcdPrint("************"); break;
            default: lcd.cls(); break;
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
        String str = double2string(d);
        lcd.print12(str);
    }
    private void lcdPrint(BigDecimal bd) {
        if (bd == null) return;
        String str = numberFormat(bd);
        lcd.print12(str);
    }

    private int setEn = -1;
    private int setFn = -1;
    private String numberFormat(BigDecimal bd) {
        if (bd == null) return null;
        String str;
        String fmt;
        if (0 <= setFn && setFn <=9) {
            bd = bd.setScale(setFn, BigDecimal.ROUND_HALF_UP);
            Log.w("numberFormat", String.format("setScale=%d", setFn));
            double d = bd.doubleValue();
            if (d >= 0) {
                str = String.format(Locale.US, "%.11g", d);
            } else {
                str = String.format(Locale.US, "%.10g", d);
            }
        } else if (0 <= setEn && setEn <= 8) {
            //fmt = "%."+String.valueOf(setEn)+"g";
            //Log.w("numberFormat", fmt);
            fmt = "0.";
            int d = setEn;
            if (d == 0) d = 8;
            if (bd.compareTo(BigDecimal.ZERO) < 0 && d == 8) d = 7;
            while (d-- > 1) {
                fmt += "0";
            }
            fmt += "E00";
            //Log.w("numberFormat", fmt);
            Locale.setDefault(Locale.US);
            DecimalFormat df = new DecimalFormat(fmt);
            str = df.format(bd.doubleValue());
            Log.w("numberFormat", String.format("fmt=%s out='%s'", fmt, str));
        } else {
            double d = bd.doubleValue();
            if (d == 0) {
                str = String.format(Locale.US, "%.11g", 0d);
            } else if (d > 0) {
                //str = String.format(Locale.US, "%.8g", d);
                str = String.format(Locale.US, "%.11g", d);
            } else {
                str = String.format(Locale.US, "%.10g", d);
            }
        }

        // 一旦指数部を切り離す
        String[] temp = str.split("(?=[Ee][\\+\\-]{0,1})", 2);
        // 小数点以下の末尾の0を削除する
        if (setEn == -1) {
            temp[0] = temp[0].replaceAll("[0]+$", "").replaceAll("(\\.)(?!.*?[1-9]+)", "");
            if (temp.length > 1 && temp[0].length() > 9) {
                temp[0] = temp[0].substring(0, 9);
            }
        }
        // 文字列を作り直す
        str = temp[0];
        if (temp.length > 1) str += temp[1];
        // exponential記号を特殊記号に書き換える
        str = str.replaceAll("[Ee]\\-", String.valueOf(_EM));
        str = str.replaceAll("[Ee]\\+{0,1}", String.valueOf(_EX));

        Log.w("numberFormat", str);
        return str;
    }

    private String double2string(Double d) {
        if (d == null) return null;
        //Log.w("lcdPrint", String.format("%d", d.intValue()));
        //Log.w("double2string0", String.format("%d", d.intValue()));

        String str;
        if (d == 0) {
            str = String.format(Locale.US, "%.8g", 0d);
        } else if (d > 0) {
            str = String.format(Locale.US, "%.8g", d);
            //Log.w("double2string01-1", String.format("%s", str));
        } else {
            str = String.format(Locale.US, "%.7g", d);
            //Log.w("double2string01-2", String.format("%s", str));
        }
        //Log.w("double2string1", String.format("%s", str));
        // 一旦指数部を切り離す
        String[] temp = str.split("(?=[Ee][\\+\\-])", 2);
        // 小数点以下の末尾の0を削除する
        temp[0] = temp[0].replaceAll("[0]+$", "").replaceAll("(\\.)(?!.*?[1-9]+)", "");
        // 文字列を作り直す
        str = temp[0];
        //Log.w("double2string2", String.format("%s", str));
        if (temp.length > 1) str += temp[1];
        //Log.w("double2string3", String.format("%s", str));
        //Log.w("lcdPrint", String.format("temp[0]='%s' temp[1]='%s' str='%s'", temp[0], temp[1], str));
        Log.w("double2string", String.format("%s", str));
        // exponential記号を特殊記号に書き換える
        str = str.replaceAll("[Ee]\\+", String.valueOf((char)0xf0));
        str = str.replaceAll("[Ee]\\-", String.valueOf((char)0xf1));

        return str;
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
            debugText = String.format("(%d):'%s'", bank, s);
            Log.w("TR", debugText);
        }
    }

    public void loadProg() {
        String[] temp = source.getSourceAll(bank);
        if (temp == null) {
            prog[0] = '\0';
            return;
        }
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
        run(null);
    }
    public void run(String s) throws InterpreterException {
        loadProg();
        pc = 0;
        labelTable.clear();
        scanLabels();
        Log.w("RUN", String.format("-------- %s", labelTable));
        forcedExit = false;
        sb = null;
        prtStr = "";
        if (s != null) {
            //String num = Integer.toString((int)evalExp2());
            Integer loc = (Integer) labelTable.get(s);
            if (loc == null) {
                //putBack();
                pc = -1;
                handleErr(ERR_UNDEFLINE);
            } else {
                pc = loc.intValue();
                //nextLine = true;
                Log.w("GOTO", String.format("goto %s(%d)", s, loc));
            }

        }
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

    public int calc(String s) throws InterpreterException {
        if (s == "") return 0;

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
        cmdLine = s;
        int ret = sbCmd();
        return ret;
    }

    private int sbCmd() throws InterpreterException {

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
        } else if (tokType == QUTEDSTR) {
            putBack();
            //Log.w("sbCmd", "xxxx");
            strOpe = strOpe();
        } else if (tokType == COMMAND) {
            switch (kwToken) {
                case RUN:
                    getToken();
                    if (!token.equals(EOP)) {
                        //String num = Integer.toString((int)evalExp2());
                        String num = evalExp2().toPlainString();
                        Log.w("SBasic", String.format("--- RUN(%d) start=%s---", bank, num));
                        pb.progStart(num);
                    } else {
                        Log.w("SBasic", String.format("--- RUN(%d) ---", bank));
                        pb.progStart();
                    }
                    return 1;
                //break;
                case LIST:
                    if (mode == MODE_RUN) {
                        lcd.cls();
                        lcd.print(String.format("READY P%d", bank));
                        initial = true;
                    } else if (mode == MODE_PRO) {
                        Log.w("SBasic", String.format("--- LIST(%d) ---", bank));
                        getToken();
                        String s = "";
                        if (tokType == NUMBER) {
                            try {
                                s = source.getSource1(bank, Integer.parseInt(token));
                            } catch (NumberFormatException e) {
                                handleErr(ERR_SYNTAX);
                            }
                        } else {
                            s = source.getSourceTop(bank);
                        }
                        listDisp = true;
                        if (s != null) {
                            lcd.printSourceList(s);
                        } else {
                            lcd.printBankStatus();
                            initial = true;
                            listDisp = false;
                        }
                        //lcd.print(s, 0);
                    }
                    return 1;

                case VAC:
                    Log.w("SBasic", String.format("--- VAC ---"));
                    vac();
                    return 1;
                case CLEAR:
                    if (mode == MODE_PRO) {
                        getToken();
                        if (token.equals("A")) {
                            Log.w("SBasic", String.format("--- CLEAR ALL---"));
                            source.clearSourceAll();
                        } else {
                            Log.w("SBasic", String.format("--- CLEAR(%d) ---", bank));
                            source.clearSource(bank);
                        }
                    } else {
                        handleErr(ERR_SYNTAX);
                    }
                    return 1;
                case DEFM:
                    getToken();
                    if (token.equals(EOP)) {
                        putBack();
                        int m = defm();
                        lcdPrint(String.format("***VAR:%d", m));
                    } else if (tokType == NUMBER) {
                        int n = 0;
                        try {
                            n = Integer.parseInt(token);
                        } catch (NumberFormatException e) {
                            handleErr(ERR_SYNTAX);
                        }
                        int m = defm(n);
                        lcdPrint(String.format("***VAR:%d", m));
                    } else if (tokType == VARIABLE) {
                        putBack();
                        int n = evalExp2().intValue();
                        int m = defm(n);
                        lcdPrint(String.format("***VAR:%d", m));
                    } else {
                        handleErr(ERR_SYNTAX);
                    }
                    return 1;
                case SAVE:
                    getToken();
                    if (token.equals("A")) {
                        Log.w("SBasic", String.format("--- SAVE ALL---"));
                        // SAVE A の処理
                    } else {
                        Log.w("SBasic", String.format("--- SAVE(%d) ---", bank));
                        // SAVE の処理
                    }
                    //printSaveStatus();
                    mode = MODE_SAVE;
                    waveout.savea();
                    return 1;
                case LOAD:
                    return 0;
                case BEEP:
                    beep();
                    return 0;
                case SET:
                    execSet();
                    return 0;
                default:
                    Log.w("sbCmd", "Syntax error.");
                    handleErr(ERR_SYNTAX);
                    break;
            }

        } else if (mode == MODE_PRO && tokType == NUMBER && !cmdLine.isEmpty()) {
            Log.w("sbCmd", String.format("cmd='%s'", cmdLine));

            int ret = source.addSource(bank, cmdLine);
            if (ret == 0) {
                // 新規の行の追加の場合はそのまま内容を表示する
                String s = source.getCurrentSource(bank);
                Log.w("sbCmd", String.format("add new line='%s'", s));
                //initial = true;
                lcd.printSourceList(s);
                initial = true;
            } else if (ret == 1) {
                // 行の更新の場合は次の行を表示する
                String s = source.getCurrentSource(bank);
                Log.w("sbCmd", String.format("replace line='%s'", s));
                s = source.getSourceNext(bank);
                if (s != null) {
                    listDisp = true;
                    lcd.printSourceList(s);
                } else {
                    // 次の行がない場合は初期画面に戻る
                    lcd.printBankStatus();
                    initial = true;
                    listDisp = false;
                }
            } else if (ret == -1) {
                // 行の削除の場合は表示クリア
                Log.w("sbCmd", String.format("delete line='%s'", cmdLine));
                lcd.cls();
            }
            return 1;

        } else {
            if (tokType == FUNCTION && (kwToken == MID || kwToken == STR)) {
                // MIDとSTRは文字列操作
                putBack();
                //Log.w("sbCmd", "bbbbb");
                strOpe = strOpe();
            } else {
                //Log.w("sbCmd2", String.format("pc=%d", pc));
                putBack();
                //Log.w("sbCmd2", String.format("pc=%d", pc));
                //Log.w("sbCmd", "aaaa");
                lastAns = evaluate();
                //Log.w("sbCmd2", String.format("%d", lastAns.intValue()));
            }
        }

        if (strOpe) {
            Log.w("sbCmd", String.format("string ans=%s", resultStr));
            lcdPrint(resultStr);

        } else {
            Log.w("sbCmd", String.format("value ans=%s", lastAns.toPlainString()));
            lcdPrint(lastAns);
            //lcdPrintln();
        }
        return 1;
    }

    public void saveend() {
        mode = last_mode;
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

            if (printPause) {
                printPause = false;
                if (token.equals(",")) {
                    print();
                    continue;
                }
            }

            if (tokType == VARIABLE || tokType == SVARIABLE) {
                putBack();
                assignment();
            } else if (tokType == COMMAND) {
                switch (kwToken) {
                    case VAC:
                    case CLEAR: // プログラム動作時のCLEARはVACと同じ処理とする
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
                    case STOP:
                        pb.progStop();
                        sb = null;
                        prtStr = "";
                        Log.w("---", "STOP!!!");
                        break;
                    case END:
                        return;
                    case ON:
                        execOn();
                        break;
                    case READ:
                        read();
                        break;
                    case RESTORE:
                        restore();
                        break;
                    case REM:
                    case DATA:
                        rem();
                        break;
                    case LET:
                        // 何もしない？
                        break;
                    case BEEP:
                        beep();
                        break;
                    case SET:
                        execSet();
                        break;
                }
            } else if (tokType == DELIMITER) {
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

    private String[] rdata;
    private int rdataIdx;
    private final int rdataMax = 512;    // 仮

    private void scanLabels() throws InterpreterException {
        Object result;
        int id;
        rdata = null;
        rdataIdx = 0;
        String[] tmp = new String[rdataMax];
        int idx = 0;
        String linenum = "";
        do {
            id = pc;
            getToken();
            //Log.w("scanLabels", String.format("--->'%s'", token));
            if (tokType == NUMBER) {
                linenum = token;
                result = labelTable.put(token, new Integer(id));

                if (result != null) handleErr(ERR_SYSTEM);
            }

            getToken();
            if (tokType == COMMAND && kwToken == DATA) {
                //tmp = getData();

                do {
                    getToken();
                    if (tokType == NUMBER || tokType == QUTEDSTR) {
                        if (idx + 1 > rdataMax) {
                            Log.w("scanLabels", "too many parameters");
                            handleErr(ERR_SYSTEM);
                            return;
                        }
                        tmp[idx++] = linenum+":"+token;
                    } else {
                        Log.w("scanLabels", String.format("param error ->%s", token));
                        handleErr(ERR_SYNTAX);
                    }
                    getToken();
                    if (kwToken != EOL && !token.equals(",")) {
                        Log.w("scanLabels", String.format("param error ->%s", token));
                        handleErr(ERR_SYNTAX);
                    }
                } while (kwToken != EOL);
            }

            if (kwToken != EOL) {
                findEOL();
                if (pc < idxEnd) pc++;
            }
        } while (!token.equals(EOP));
        pc = 0;

        if (idx > 0) {
            rdata = new String[idx];
            for (int i = 0; i < idx; i++) {
                rdata[i] = tmp[i];
                Log.w("DATA", String.format("%02d : '%s'", i, rdata[i]));
            }
        }
    }

    private void findEOL() {
        while (pc < idxEnd + 1 && prog[pc] != '\n') {
            pc++;
        }
    }

    private void findEOS() {
        while (pc < idxEnd + 1 && (prog[pc] != ':' && prog[pc] != '\n')) {
            // ダブルコートの中に':'が出てくるとうまく動かない気がするが、、
            pc++;
        }
    }

    private String ssvar;
    private void assignment() throws InterpreterException {
        int var;
        BigDecimal value;
        char vname;
        int offset = 0;
        int pc_temp;

        pc_temp = pc;
        getToken();
        Log.w("assignment", String.format("%s", token));
        if (isSval(token)) {
            // 文字列変数の処理
            vname = token.charAt(0);
            if (vname == '$') {
                //String lastToken = token;
                getToken();
                if (!token.equals("=")) {
                    //putBack();
                    //token = lastToken;
                    //putBack();
                    // 変数参照なのでポインタを戻す
                    putBack(pc_temp);
                    strOpe();
                    return;
                }
                strOpe();
                if (resultStr.length() > 30) {
                    handleErr(ERR_VARIABLE);
                }
                ssvar = resultStr;
            } else {
                var = (int) Character.toUpperCase(vname) - 'A';

                //String lastToken = token;
                getToken();
                if (token.equals("(")) {
                    // 配列
                    //lastToken += token;
                    getToken();
                    //lastToken += token;
                    offset = evalExp2().intValue();
                    //var += result;
                    if (!token.equals(")")) {
                        handleErr(ERR_SYNTAX);
                    }
                    //lastToken += token;
                    //Log.w("assig", String.format("lastToken='%s'", lastToken));
                    getToken();
                }
                if (!token.equals("=")) {
                    //putBack();
                    //token = lastToken;
                    //putBack();
                    // 変数参照なのでポインタを戻す
                    putBack(pc_temp);
                    strOpe();
                    return;
                }

                //getToken();
                strOpe();
                if (resultStr.length() > 7) {
                    handleErr(ERR_VARIABLE);
                }
                if (var + offset < 26) {
                    svars[var + offset] = resultStr;
                    vars[var + offset] = BigDecimal.ZERO;
                } else {
                    if (exsvars != null && (var + offset - 26) < exsvars.length) {
                        exsvars[var + offset - 26] = resultStr;
                        exvars[var + offset - 26] = BigDecimal.ZERO;
                    } else {
                        handleErr(ERR_VARIABLE);
                    }
                }
                //if (var < 26 && !svars[var].isEmpty()) vars[var] = 0;
            }

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

            //String lastToken = token;
            getToken();
            if (token.equals("(")) {
                // 配列
                //lastToken += token;
                getToken();
                //lastToken += token;
                offset = evalExp2().intValue();
                //var += result;
                if (!token.equals(")")) {
                    handleErr(ERR_SYNTAX);
                }
                //lastToken += token;
                //Log.w("assig", String.format("lastToken='%s'", lastToken));
                getToken();
            }
            if (!token.equals("=")) {
                //handleErr(EQUALEXPECTED);
                //return;
                //putBack();
                //token = lastToken;
                //putBack();
                // 変数参照だけなのでポインタを最初に戻す
                putBack(pc_temp);
                lastAns = evaluate();
                return;
            }

            value = evaluate();
            lastAns = value;
            //Log.w("assig", String.format("%d", lastAns.intValue()));

            Log.w("assig", String.format("var=%d offset=%d value=%g", var, offset, value));
            if (var + offset < 26) {
                vars[var + offset] = value;
                svars[var + offset] = "";
            } else {
                if (exvars != null && (var + offset - 26) < exvars.length) {
                    exvars[var + offset - 26] = value;
                    exsvars[var + offset - 26] = "";
                } else {
                    handleErr(ERR_VARIABLE);
                }
            }

            //vars[var] = value;
            //svars[var] = "";
        }
    }

    public void vac() {
        Log.w("VAC", "variable cleared.");
        for (int i = 0; i < 26; i++) {
            vars[i] = BigDecimal.ZERO;
            svars[i] = "";
        }
        if (exvars != null) {
            for (int i = 0; i < exvars.length; i++) {
                exvars[i] = BigDecimal.ZERO;
                exsvars[i] = "";
            }
        }
        ssvar = "";
        lastAns = BigDecimal.ZERO;
    }

    private String prtStr = "";
    private boolean printPause = false;
    private void print() throws InterpreterException {
        //double result;
        //int len = 0, spaces;
        String lastDelim = "";
        int pos = 0;

        //Log.w("PRT", "Exec PRINT !!!!");

        boolean paramExist = false;
        do {
            //Log.w("PRT", "Exec PRINT do !!!!");
            getToken();
            //Log.w("PRT", String.format("token=%s", token));
            if (kwToken == EOL || token.equals(EOP) || tokType != QUTEDSTR && token.equals(":")) {
                if (!paramExist) {
                    prtStr = "";
                    sb = null;
                }
                break;
            }

            paramExist = true;
            if (tokType == QUTEDSTR) {
                lastDelim = "";
                //Log.w("PRT", String.format("tokType=QUTEDSTR(%d)", tokType));
                //System.out.print(token);
                //lcdPrint(token);
                putBack();
                strOpe();
                if (sb == null) {
                    //prtStr += token;
                    prtStr += resultStr;
                } else {
                    //sb.replace(pos, pos + token.length(), token);
                    //pos += token.length();
                    sb.replace(pos, pos + resultStr.length(), resultStr);
                    pos += resultStr.length();
                    Log.w("print", String.format("sb='%s'", sb.toString()));
                }
                //Log.w("PRT", String.format("%s", token));
                //len += token.length();
                getToken();
            } else if (tokType == VARIABLE || tokType == NUMBER ||
                    tokType == DELIMITER && (token.equals("+") || token.equals("-"))) {
                lastDelim = "";
                //Log.w("PRT", String.format("tokType!=QUTEDSTR(%d)", tokType));
                putBack();
                //result = evaluate();
                BigDecimal tmp = evaluate();
                getToken();

                //String s = double2string(result);
                //String s = tmp.toPlainString();
                String s = numberFormat(tmp);
                if (tmp.compareTo(BigDecimal.ZERO) >= 0) s = " " + s;
                if (sb == null) {
                    prtStr += s;
                } else {
                    sb.replace(pos, pos + s.length(), s);
                    pos += s.length();
                    Log.w("print", String.format("sb='%s'", sb.toString()));
                }

                //Double t = new Double(result);
                //len += t.toString().length();
            } else if (tokType == SVARIABLE || tokType == FUNCTION && kwToken == MID) {
                lastDelim = "";
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
                //len += resultStr.length();
                //getToken();
            } else if (tokType == COMMAND) {
                if (kwToken == CSR) {
                    lastDelim = "";
                    //lcdPrint(prtStr);
                    //result = evaluate() + 0.01; // イマイチ！！
                    int x = evaluate().intValue();
                    Log.w("CSR", "pos="+x);
                    if (0 <= x && x < 12) {
                        pos = x;
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
                    } else {
                        Log.w("print", "invalid param");
                        handleErr(ERR_ARGUMENT);
                    }
                }
            }

            if (sb != null) {
                prtStr = sb.toString();
                lcdPrint(prtStr);
            }

            if (kwToken == EOL || token.equals(EOP) || tokType != QUTEDSTR && token.equals(":")) {
                break;
            }

            lastDelim = token;
            Log.w("PRT", String.format("lastDelim='%s'", lastDelim));

            if (lastDelim.equals(":")) {
                ;
            } else if (lastDelim.equals(",")) {
                //spaces = 8 - (len % 8);
                //len += spaces;
                //while (spaces != 0) {
                    //System.out.print(" ");
                    //lcdPrint(" ");
                //    prtStr += " ";
                //    spaces--;
                //}
            } else if (token.equals(";")) {
                //System.out.print(" ");
                //lcdPrint(" ");
                //len++;
            } else if (kwToken != EOL && !token.equals(EOP)) {
                handleErr(ERR_SYNTAX);
            }
        } while (lastDelim.equals(";") /*|| lastDelim.equals(",")*/);
        if (sb != null) prtStr = sb.toString();
        if (kwToken == EOL || token.equals(EOP) || token.equals(":") || token.equals(",")) {
            if (prtStr.isEmpty() || lastDelim.equals(";")) {
                Log.w("print", "lcdPrint");
                lcdPrint(prtStr);
            } else {
                Log.w("print", String.format("lcdPrintAndPause str='%s'", prtStr));
                lcdPrintAndPause(prtStr);
                prtStr = "";
                if (token.equals(",")) {
                    //Log.w("pre putBack", String.format("%d %s", pc, token));
                    putBack();
                    //Log.w("after putBack", String.format("%d %s", pc, token));
                    printPause = true;
                }
            }
        } else {
            handleErr(ERR_SYNTAX);
        }

    }

    private void execGoto() throws InterpreterException {
        Integer loc;
        getToken();
        //Log.w("GOTO", String.format("%s", token));

        if (tokType == BANKNUM) {
            getToken();
            int b = evalExp2().intValue();
            //Log.w("GOTO", String.format("bank(int)---> %d", b));
            if (b < 10) {
                Log.w("GOTO", String.format("bank change -> #%d", b));
                bankChange(b);
            } else {
                handleErr(ERR_SYNTAX);
            }
        } else {
            //String num = Integer.toString((int)evalExp2());
            String num = evalExp2().setScale(0, BigDecimal.ROUND_DOWN).toPlainString();
            loc = (Integer) labelTable.get(num);
            if (loc == null) {
                putBack();
                handleErr(ERR_UNDEFLINE);
            } else {
                pc = loc.intValue();
                nextLine = true;
                Log.w("GOTO", String.format("goto %s(%d)", num, loc));
            }
        }
    }

    private void execOn() throws InterpreterException {
        Log.w("ON", "execON");
        //getToken();
        //Log.w("ON", String.format("%s", token));

        getToken();
        if (tokType != VARIABLE && tokType != NUMBER) {
            // ONの次が数値変数もしくは数値でなければエラー
            Log.w("ON", String.format("invalid param"));
            handleErr(ERR_SYNTAX);
        }
        putBack();

        int var = evaluate().intValue();
        if (var == 0) {
            Log.w("ON", String.format("invalid param0"));
            findEOS();  // ステートメントをスキップする
            return;
        }
        getToken();
        int kw = kwToken;
        if (tokType == COMMAND && (kw == GOTO || kw == GOSUB)) {
            int pc_temp = pc;
            getToken();
            int i = 1;
            while (i < var) {
                i++;
                getToken();
                if (token == EOP || token.equals(":")) {
                    Log.w("ON", String.format("invalid param1"));
                    return;
                }
                if (!token.equals(",")) {
                    Log.w("ON", String.format("needs delimiter:','"));
                    handleErr(ERR_SYNTAX);
                }
                pc_temp = pc;
                getToken();
                if (tokType == BANKNUM) {
                    getToken();
                }
                if (token == EOP || token.equals(":")) {
                    Log.w("ON", String.format("invalid param2"));
                    return;
                }
            }
            putBack(pc_temp);
            switch (kw) {
                case GOTO:
                    execGoto();
                    break;
                case GOSUB:
                    gosub();
                    break;
                default:
                    Log.w("ON", String.format("err1 'ON' needs 'GOTO' or 'GOSUB'"));
                    handleErr(ERR_SYNTAX);
                    break;
            }
        } else {
            Log.w("ON", String.format("err2 'ON' needs 'GOTO' or 'GOSUB'"));
            handleErr(ERR_SYNTAX);
        }
    }

    private void execIf() throws InterpreterException {
        BigDecimal result;
        getToken();
        if (tokType == SVARIABLE ||
                tokType == FUNCTION && (kwToken == KEY || kwToken == MID || kwToken == STR) ) {
            //Log.w("execIf", "1");
            putBack();
            //Log.w("execIf", "2");
            boolean ret = strOpe();
            //Log.w("execIf", "3");
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
                    if (tokType == NUMBER || tokType == VARIABLE || tokType == BANKNUM) {
                        putBack();
                        //Log.w("IF", "THEN -> GOTO");
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

            Log.w("IF", String.format("if (%d)", result.intValue()));
            if (result.compareTo(BigDecimal.ZERO) != 0) {
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
                    if (tokType == NUMBER || tokType == VARIABLE || tokType == BANKNUM) {
                        putBack();
                        //Log.w("IF", "THEN -> GOTO");
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
        BigDecimal value;
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

        stckvar.step = BigDecimal.valueOf(1);
        getToken();
        if (kwToken == STEP) {
            stckvar.step = evaluate();
            if (stckvar.step.equals(0)) handleErr(ERR_ARGUMENT);
        } else {
            putBack();
        }

        Log.w("execFor", String.format("var='%c'=%f target=%f step=%f", 'A'+stckvar.var, vars[stckvar.var], stckvar.target, stckvar.step));
        if (stckvar.step.compareTo(BigDecimal.ZERO) >= 0 && value.compareTo(vars[stckvar.var]) >= 0 ||
                stckvar.step.compareTo(BigDecimal.ZERO) < 0 && value.compareTo(vars[stckvar.var]) <= 0) {
            stckvar.loc = pc;
            fStack.push(stckvar);
        } else {
            while (kwToken != NEXT) getToken();
        }
    }

    private void next() throws InterpreterException {
        ForInfo stckvar;
        getToken();
        char vname = token.charAt(0);
        if (!Character.isLetter(vname)) {
            handleErr(ERR_SYSTEM);
            return;
        }
        int var = Character.toUpperCase(vname) - 'A';
        Log.w("NEXT", "do");
        try {
            // FORループをNEXTしないで抜けるパターンもあるので対応しているNEXTを探す
            do {
                stckvar = (ForInfo) fStack.pop();
            } while (var != stckvar.var);

            vars[stckvar.var] = vars[stckvar.var].add(stckvar.step);

            Log.w("next", String.format("var='%c'=%f target=%f step=%f", 'A'+stckvar.var, vars[stckvar.var], stckvar.target, stckvar.step));
            // ループの終了条件
            if (stckvar.step.compareTo(BigDecimal.ZERO) >= 0 && vars[stckvar.var].compareTo(stckvar.target) > 0 ||
                    stckvar.step.compareTo(BigDecimal.ZERO) < 0 && vars[stckvar.var].compareTo(stckvar.target) < 0) {
                Log.w("NEXT", "loop end.");
                return;
            }

            fStack.push(stckvar);
            pc = stckvar.loc;
        } catch (EmptyStackException e) {
            Log.w("NEXT", "stack error.");
            handleErr(ERR_SYNTAX);
        }
        //Log.w("NEXT", "continue.");
    }

    private void input() throws InterpreterException {
        int var;
        //String vname;
        //double val = 0.0;
        String str = "";
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        getToken();
        while (kwToken != EOL) {
            if (tokType == QUTEDSTR) {
                //System.out.print(token);
                //lcdPrint(token);
                str = token;
                getToken();
                if (!token.equals(",")) handleErr(ERR_SYNTAX);
                getToken();
            }
            lcdPrint(str + "?");
            getInputStream();
            if (forcedExit) {
                // 入力中に強制終了された場合はループを抜けてメインループに戻す
                break;
            }
            //int idx;
            if (tokType == VARIABLE) {
                Log.w("input", String.format("input=%s", inText));

                char vname = token.charAt(0);
                if (!Character.isLetter(vname)) {
                    handleErr(ERR_SYSTEM);
                    return;
                }
                var = (int) Character.toUpperCase(vname) - 'A';
                getToken();
                int offset = 0;
                if (token.equals("(")) {
                    // 配列
                    getToken();
                    offset = evalExp2().intValue();
                    if (!token.equals(")")) {
                        putBack();
                        handleErr(ERR_SYNTAX);
                    }
                    //getToken();
                }
                BigDecimal value = new BigDecimal(0);
                try {
                    BigDecimal tmp = new BigDecimal(inText);
                    value = tmp;
                } catch (NumberFormatException e) {
                    // 数値にパースできなかった（＝文字列だった）場合はシンタックスエラー
                    putBack();
                    handleErr(ERR_SYNTAX);
                }

                if (var + offset < 26) {
                    vars[var + offset] = value;
                    svars[var + offset] = "";
                } else {
                    if (exvars != null && (var + offset - 26) < exvars.length) {
                        exvars[var + offset - 26] = value;
                        exsvars[var + offset - 26] = "";
                    } else {
                        // 配列のオーバーラン
                        putBack();
                        handleErr(ERR_VARIABLE);
                    }
                }
                //getToken();
            } else if (tokType == SVARIABLE) {

                char vname = token.charAt(0);
                if (vname == '$') {
                    if (inText.length() > 30) {
                        // 文字数が30を超えた
                        handleErr(ERR_VARIABLE);
                    }
                    ssvar = inText;
                    getToken();
                } else {
                    var = (int) Character.toUpperCase(vname) - 'A';

                    getToken();
                    int offset = 0;
                    if (token.equals("(")) {
                        // 配列
                        getToken();
                        offset = evalExp2().intValue();
                        //var += result;
                        if (!token.equals(")")) {
                            handleErr(ERR_SYNTAX);
                        }
                    }
                    if (inText.length() > 7) {
                        // 文字数が7文字を超えた
                        putBack();
                        handleErr(ERR_VARIABLE);
                    }
                    if (var + offset < 26) {
                        svars[var + offset] = inText;
                        vars[var + offset] = BigDecimal.ZERO;
                    } else {
                        if (exsvars != null && (var + offset - 26) < exsvars.length) {
                            exsvars[var + offset - 26] = inText;
                            exvars[var + offset - 26] = BigDecimal.ZERO;
                        } else {
                            // 配列のオーバーラン
                            putBack();
                            handleErr(ERR_VARIABLE);
                        }
                    }
                    //getToken();
                    //if (var < 26 && !svars[var].isEmpty()) vars[var] = 0;
                }
            } else {
                handleErr(ERR_SYNTAX);
            }
            //getToken();
            Log.w("input", String.format("token='%s' tokType=%d kwToken=%d", token, tokType, kwToken));
            if (tokType == DELIMITER && token.equals(":")) {
                // ステートメントの区切りが来たらループを抜ける
                putBack();
                break;
            }
            if (token.equals(",")) {
                getToken();
            }
        }
    }

    public static String inText;
    public static boolean inputWait = false;
    private void getInputStream() {
        inText = "";
        inputWait = true;
        do {
            if (forcedExit || pb.isProgStop()) {
                break;
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                ;
            }
        } while (inText.isEmpty());

        inputWait = false;
        lcd.cls();;
        return;
    }

    private void gosub() throws InterpreterException {
        Integer loc;
        getToken();

        if (tokType == BANKNUM) {
            gStack.push(new GosubInfo(bank, pc));
            // バンク切り替え
            getToken();
            int b = evalExp2().intValue();
            if (b < 10) {
                Log.w("GOSUB", String.format("bank change -> #%d(%d)", b, pc));
                bankChange(b);
            } else {
                handleErr(ERR_SYNTAX);
            }
        } else {
            //String num = Integer.toString((int)evalExp2());
            String num = evalExp2().setScale(0, BigDecimal.ROUND_DOWN).toPlainString();
            loc = (Integer) labelTable.get(num);
            if (loc == null) {
                putBack();
                handleErr(ERR_UNDEFLINE);
            } else {
                //gStack.push(new Integer(pc));
                gStack.push(new GosubInfo(bank, pc));

                pc = loc.intValue();
                nextLine = true;
                Log.w("GOSUB", String.format("gosub %s(%d)", num, loc));
            }
        }

    }

    private void greturn() throws InterpreterException {
        //Integer t;
        GosubInfo gi;

        try {
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
            Log.w("GOSUB-RETURN", "Stack error.");
            handleErr(ERR_SYNTAX);
        }
    }

    private void read() throws InterpreterException {
        do {
            getToken();
            //Log.w("read", "--->"+token);
            if (tokType == VARIABLE || tokType == SVARIABLE) {
                int var;
                int offset = 0;
                char c;

                if (rdataIdx >= rdata.length) {
                    // エラー
                    Log.w("read", "error : rdata is out of index.");
                    handleErr(ERR_VARIABLE);
                }
                c = token.charAt(0);
                String[] str = rdata[rdataIdx].split(":", 2);
                if (isSval(token)) {
                    // 文字列変数の処理
                    if (c == '$') {
                        if (rdata[rdataIdx].length() > 30) {
                            Log.w("read", String.format("error : strlength is over(%d chars).", rdata[rdataIdx].length()));
                            handleErr(ERR_VARIABLE);
                        }
                        Log.w("read", String.format("data=%s", rdata[rdataIdx]));
                        //ssvar = rdata[rdataIdx++];
                        ssvar = str[1];
                        rdataIdx++;
                        getToken();
                    } else {
                        var = (int) Character.toUpperCase(c) - 'A';
                        getToken();
                        if (token.equals("(")) {
                            // 配列
                            getToken();
                            offset = evalExp2().intValue();
                            if (!token.equals(")")) {
                                handleErr(ERR_SYNTAX);
                            }
                            getToken();
                        }

                        if (str[1].length() > 7) {
                            Log.w("read", String.format("error : strlength is over(%d chars).", str[1].length()));
                            handleErr(ERR_VARIABLE);
                        }

                        Log.w("read", String.format("var=%d offset=%d data=%s", var, offset, str[1]));
                        if (var + offset < 26) {
                            svars[var + offset] = str[1];
                            vars[var + offset] = BigDecimal.ZERO;
                        } else {
                            if (exsvars != null && (var + offset - 26) < exsvars.length) {
                                exsvars[var + offset - 26] = str[1];
                                exvars[var + offset - 26] = BigDecimal.ZERO;
                            } else {
                                handleErr(ERR_VARIABLE);
                            }
                        }
                        rdataIdx++;
                    }

                } else {
                    // 数値変数の処理
                    var = (int) Character.toUpperCase(c) - 'A';

                    getToken();
                    if (token.equals("(")) {
                        // 配列
                        getToken();
                        offset = evalExp2().intValue();
                        if (!token.equals(")")) {
                            handleErr(ERR_SYNTAX);
                        }
                        getToken();
                    }

                    BigDecimal value = new BigDecimal(0);
                    //Log.w("read", String.format("id=%d ('%s').", rdataIdx, rdata[rdataIdx]));
                    Log.w("read", String.format("id=%d", rdataIdx));
                    try {
                        //value = Double.parseDouble(str[1]);
                        BigDecimal tmp = new BigDecimal(str[1]);
                        value = tmp;
                    } catch (NumberFormatException e) {
                        // シンタックスエラー
                        Log.w("read", String.format("error : not number('%s').", str[1]));
                        handleErr(ERR_SYNTAX);
                    }

                    Log.w("read", String.format("var=%d offset=%d data=%d", var, offset, value.intValue()));
                    if (var + offset < 26) {
                        vars[var + offset] = value;
                        svars[var + offset] = "";
                    } else {
                        if (exvars != null && (var + offset - 26) < exvars.length) {
                            exvars[var + offset - 26] = value;
                            exsvars[var + offset - 26] = "";
                        } else {
                            handleErr(ERR_VARIABLE);
                        }
                    }
                    rdataIdx++;
                }

            } else {
                Log.w("read", String.format("param error0 ->%s", token));
                handleErr(ERR_SYNTAX);
            }

            //getToken();
            if (kwToken != EOL && !token.equals(":") && !token.equals(",")) {
                Log.w("read", String.format("param error1 ->%s", token));
                handleErr(ERR_SYNTAX);
            }

        } while (kwToken != EOL);
    }

    private void restore() throws InterpreterException {
        getToken();
        Log.w("restore", String.format("exec:tokType=%d", tokType));
        if (kwToken == EOL || token.equals(":")) {
            Log.w("restore", "index was cleared.");
            rdataIdx = 0;
        } else if (tokType == NUMBER) {
            // 行番号指定
            boolean success = false;
            for (int i = 0; i < rdata.length; i++) {
                String[] str = rdata[i].split(":", 2);
                if (token.equals(str[0])) {
                    rdataIdx = i;
                    Log.w("restore", String.format("index was updated(index=%d, line=%s).",rdataIdx, str[0]));
                    success = true;
                    break;
                }
            }
            if (!success) {
                Log.w("restore", "Not found the linenumber.");
                handleErr(ERR_UNDEFLINE);
            }
        } else {
            Log.w("restore", "illegal parameters.");
            handleErr(ERR_SYNTAX);
        }

        //findEOS();
    }

    private void rem() throws InterpreterException {
        // 行の最後まで読み捨てる
        findEOL();
    }

    private void beep() throws InterpreterException {
        getToken();
        if (kwToken == EOL || token.equals(":")) {
            waveout.beep0();
        } else if (tokType == NUMBER || tokType == VARIABLE) {
            int x = evalExp2().intValue();
            if (x == 0) {
                waveout.beep0();
            } else if (x == 1) {
                waveout.beep1();
            } else {
                // パラメータエラー
                Log.w("beep", "invalid parameter1");
                handleErr(ERR_VARIABLE);
            }
        } else {
            // パラメータエラー
            Log.w("beep", "invalid parameter2");
            handleErr(ERR_VARIABLE);
        }
    }


    private void execSet() throws InterpreterException {
        Log.w("execSet", "called.");
        getToken();
        if (token.equals(EOP) || kwToken == EOL || token.equals(":")) {
            // パラメータエラー
            Log.w("execSet", "invalid parameter1");
            handleErr(ERR_SYNTAX);
        } else if (1 <= token.length() && token.length() <= 2) {
            char c1 = token.charAt(0);
            char c2 = token.length() > 1 ? token.charAt(1) : '\0';
            if (c1 == 'E' && '0' <= c2 && c2 <= '8') {
                //
                setEn = c2 - '0';
                setFn = -1;
                Log.w("execSet", String.format("SET %c%c", c1, c2));
            } else if (c1 == 'F' && '0' <= c2 && c2 <= '9') {
                //
                setFn = c2 - '0';
                setEn = -1;
                Log.w("execSet", String.format("SET %c%c", c1, c2));
            } else if (c1 == 'N') {
                //
                setEn = -1;
                setFn = -1;
                Log.w("execSet", String.format("SET %c", c1));
            } else {
                // パラメータエラー
                Log.w("execSet", "invalid parameter2");
                handleErr(ERR_SYNTAX);
            }
        } else {
            // パラメータエラー
            Log.w("execSet", "invalid parameter3");
            handleErr(ERR_SYNTAX);
        }
    }

    //****************************************************
    private boolean strOpe() throws InterpreterException {
        //Log.w("strOpe", String.format("exec : token='%s' tokType=%d", token, tokType));
        boolean result;
        getToken();
        if (token.equals(EOP)) {
            handleErr(ERR_SYSTEM);
        }
        result = strOpe1();
        putBack();
        return result;
    }

    private int getReservCodePB100(String s) {
        if (s == null || s.isEmpty()) return -1;
        String ss = s.substring(0, 1);
        for (int i = 0; i < 0x80; i++) {
            if (reservCodePB100[i].equals(ss)) return i;
        }
        return -1;
    }

    private boolean strOpe1() throws InterpreterException {
        //Log.w("strOpe1", String.format("exec : token='%s' tokType=%d", token, tokType));
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

            int l_value = getReservCodePB100(l_temp);
            int r_value = getReservCodePB100(r_temp);
            Log.w("strOpe1", String.format("compare!!! L='%s'(%02x) R='%s'(%02x)", l_temp, l_value, r_temp, r_value));
            switch (op) {
                case '=':
                    result = l_temp.equals(r_temp);
                    Log.w("strOpe", String.format("op='=' result=%d", result ? 1 : 0));
                    break;
                case _NE:
                    result = !l_temp.equals(r_temp);
                    Log.w("strOpe", String.format("op='!=' result=%d", result ? 1 : 0));
                    break;
                case '>':
                    //result = l_value > r_value ? true : false;
                    result = l_value > r_value;
                    Log.w("strOpe", String.format("op='>' result=%d", result ? 1 : 0));
                    break;
                case _GE:
                    //result = l_value >= r_value ? true : false;
                    result = l_value >= r_value;
                    Log.w("strOpe", String.format("op='>=' result=%d", result ? 1 : 0));
                    break;
                case '<':
                    //result = l_value < r_value ? true : false;
                    result = l_value < r_value;
                    Log.w("strOpe", String.format("op='<' result=%d", result ? 1 : 0));
                    break;
                case _LE:
                    //result = l_value <= r_value ? true : false;
                    result = l_value <= r_value;
                    Log.w("strOpe", String.format("op='<=' result=%d", result ? 1 : 0));
                    break;
                default:
                    break;
            }
            resultStr = l_temp;
        }
        //Log.w("strOpe1-end", String.format("result=%d ret='%s'", (result ? 1 : 0), resultStr));
        return  result;
    }

    private boolean strOpe2() throws InterpreterException {
        //Log.w("strOpe2", String.format("exec : token='%s' tokType=%d", token, tokType));
        char op;
        boolean result = false;
        String str1, str2;

        result = strOpe3();
        str1 = resultStr;

        while ((op = token.charAt(0)) == '+') {
            Log.w("eval2", String.format("op=%c", op));
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
        //Log.w("strOpe2-end", String.format("ret='%s'", resultStr));
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
                    //resultStr = findSVar(token);
                    resultStr = ssvar;
                    getToken();
                } else {
                    getToken();
                    int r = 0;
                    if (token.equals("(")) {
                        // 配列
                        getToken();
                        r = evalExp2().intValue();
                        //ch += r;
                        if (!token.equals(")")) {
                            handleErr(ERR_SYNTAX);
                        }
                        getToken();
                    }
                    resultStr = findSVar(String.valueOf(ch), r);
                }

                break;
            case QUTEDSTR:
                Log.w("strOpe3", "QUTERSTR");
                resultStr = token;
                result = true;
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
                            m = evaluate().intValue();
                            m--;
                            getToken();
                            Log.w("---- MID", token);
                            if (token.equals(")") || token.equals(":") || kwToken == EOL) {
                                if (m < 0 || m >= ssvar.length()) {
                                    Log.w("MID", String.format("len=%d, m=%d", ssvar.length(), m));
                                    handleErr(ERR_ARGUMENT);
                                }
                                resultStr = ssvar.substring(m);
                                result = true;
                                //Log.w("strOpe3", String.format("m=%d ret='%s'", m, resultStr));
                                getToken();
                                Log.w("---- MID2", token);
                            } else if (token.equals(",")) {
                                getToken();
                                putBack();
                                n = evaluate().intValue();
                                getToken();
                                if (!token.equals(")") && !token.equals(":") && kwToken != EOL) {
                                    handleErr(ERR_SYNTAX);
                                }
                                Log.w("MID", String.format("len=%d, m=%d, n=%d", ssvar.length(), m, m + n));
                                if (m < 0 || m >= ssvar.length() || n <= 0 || m + n > ssvar.length()) {
                                    Log.w("MID", String.format("len=%d, m=%d, n=%d", ssvar.length(), m, m + n));
                                    handleErr(ERR_ARGUMENT);
                                }
                                resultStr = ssvar.substring(m, m + n);
                                result = true;
                                //Log.w("strOpe3", String.format("m=%d n=%d ret='%s'", m, n, resultStr));
                                getToken();
                            }
                            //getToken();
                        } else {
                            handleErr(ERR_SYNTAX);
                        }
                        break;

                    case STR:
                        //Log.w("strOpe3", "STR");
                        getToken();
                        //Log.w("---STR1", token);
                        //Log.w("---STR1", String.format("%d %s", pc, token));
                        if (token.equals("(")) {
                            getToken();
                            //Log.w("---STR2", String.format("%d %s", pc, token));
                            int x = 0;
                            if (tokType == NUMBER || tokType == VARIABLE) {
                                putBack();
                                //Log.w("---STR31", String.format("%d %s", pc, token));
                                x = evaluate().intValue();
                                getToken();
                                //Log.w("---STR32", String.format("%d %s", pc, token));
                            } else {
                                // エラー
                                Log.w("strOpe", String.format("STR:illegal parameters('%s').", token));
                                handleErr(ERR_SYNTAX);
                            }
                            //Log.w("---STR2", ""+x);
                            //Log.w("---STR4", String.format("%d %s", pc, token));
                            if (token.equals(")") || token.equals(":") || kwToken == EOL) {
                                resultStr = String.valueOf(x);
                                result = true;
                                Log.w("strOpe5", String.format("STR:ret='%s'", resultStr));
                                getToken();
                                //Log.w("---STR6", String.format("%d %s", pc, token));
                            }
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
                Log.w("strOpe3", "handleErr");
                handleErr(ERR_SYNTAX);
                break;
        }
        Log.w("strOpe3", String.format("ret='%s'", resultStr));
        return result;

    }

    //****************************************************
    private BigDecimal evaluate() throws InterpreterException {
        //Log.w("eval", "exec");
        BigDecimal result;
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
    private BigDecimal evalExp1() throws InterpreterException {
        //Log.w("eval1", "exec");
        BigDecimal l_temp, r_temp, result;
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
                    if (l_temp.compareTo(r_temp) < 0) {
                        result = BigDecimal.ONE;
                    } else {
                        result = BigDecimal.ZERO;
                    }
                    break;
                case _LE:
                    if (l_temp.compareTo(r_temp) <= 0) {
                        result = BigDecimal.ONE;
                    } else {
                        result = BigDecimal.ZERO;
                    }
                    break;
                case '>':
                    if (l_temp.compareTo(r_temp) > 0) {
                        result = BigDecimal.ONE;
                    } else {
                        result = BigDecimal.ZERO;
                    }
                    break;
                case _GE:
                    if (l_temp.compareTo(r_temp) >= 0) {
                        result = BigDecimal.ONE;
                    } else {
                        result = BigDecimal.ZERO;
                    }
                    break;
                case '=':
                    if (l_temp.compareTo(r_temp) == 0) {
                        result = BigDecimal.ONE;
                    } else {
                        result = BigDecimal.ZERO;
                    }
                    break;
                case _NE:
                    if (l_temp.compareTo(r_temp) != 0) {
                        result = BigDecimal.ONE;
                    } else {
                        result = BigDecimal.ZERO;
                    }
                    break;
                default:
                    break;
            }
        }
        //Log.w("eval1-end", String.format("ret=%e", result));
        return  result;
    }

    private BigDecimal evalExp2() throws InterpreterException {
        //Log.w("eval2", "exec");
        char op;
        BigDecimal result;
        BigDecimal partialResult;

        result = evalExp3();

        while ((op = token.charAt(0)) == '+' || op == '-') {
            Log.w("eval2", String.format("op=%c", op));
            getToken();
            Log.w("eval2", String.format("next token=%s", token));
            partialResult = evalExp3();
            //BigDecimal a = new BigDecimal(result.toString());
            //BigDecimal b = new BigDecimal(partialResult.toString());
            switch (op) {
                case '-':
                    //result = result - partialResult;
                    //a = a.subtract(b);
                    result = result.subtract(partialResult);
                    break;
                case '+':
                    //result = result + partialResult;
                    //a = a.add(b);
                    result = result.add(partialResult);
                    break;
                default:
                    break;
            }
            //result = a.doubleValue();
        }
        //Log.w("eval2", String.format("ret=%e", result));
        return result;
    }

    private BigDecimal evalExp3() throws InterpreterException {
        //Log.w("eval3", "exec");
        char op;
        BigDecimal result;
        BigDecimal partialResult;

        result = evalExp4();

        while ((op = token.charAt(0)) == '*' || op == '/' || op == '%') {
            getToken();
            partialResult = evalExp4();
            //BigDecimal a = new BigDecimal(result.toString());
            //BigDecimal b = new BigDecimal(partialResult.toString());
            switch (op) {
                case '*':
                    //result = result * partialResult;
                    //a = a.multiply(b);
                    result = result.multiply(partialResult);
                    break;
                case '/':
                    if (partialResult.compareTo(BigDecimal.ZERO) == 0) handleErr(ERR_MATH);
                    //result = result / partialResult;
                    //int scale = result.signum()*partialResult.signum() >= 0 ? 10 : 9;
                    int scale = 99;
                    //a = a.divide(b, scale, BigDecimal.ROUND_HALF_UP);
                    result = result.divide(partialResult, scale, RoundingMode.HALF_UP);
                    break;
                case '%':   // PBには%の演算子はないけど、、
                    if (partialResult.compareTo(BigDecimal.ZERO) == 0) handleErr(ERR_MATH);
                    //result = result % partialResult;
                    //a = a.remainder(b);
                    result = result.remainder(partialResult);
                    break;
                default:
                    break;
            }
            //result = a.doubleValue();
        }
        //Log.w("eval3", String.format("ret=%e", result));
        return result;
    }

    private BigDecimal evalExp4() throws InterpreterException {
        //Log.w("eval4", "exec");
        BigDecimal result;
        //double partialResult;
        //double ex;
        //int t;
        int pc_temp;

        result = evalExp5();

        if (token.equals("^")) {
            // PB-100 は a^b^c の計算は (a^b)^c の順番で計算するためループで回す。
            // BigDecimalは小数のべき乗ができないのでdoubleで計算してからBigDecimalに戻す（誤差が出る！）
            boolean loop;
            double tmp = result.doubleValue();
            while (token != EOP) {
                loop = false;
                pc_temp = pc;
                getToken();
                getToken();
                if (token.equals("^")) loop = true;
                putBack(pc_temp);
                getToken();
                //result = Math.pow(result, evalExp5());
                tmp = Math.pow(tmp, evalExp5().doubleValue());
                result = result.pow(1);
                if (!loop) {
                    break;
                }
            }
            BigDecimal tmp2 = new BigDecimal(tmp);
            result = tmp2;
        }
        //Log.w("eval4", String.format("ret=%e", result));
        return result;
    }

    private BigDecimal evalExp5() throws InterpreterException {
        //Log.w("eval5", "exec");
        BigDecimal result;
        String op;

        op = "";
        if ((tokType == DELIMITER) && token.equals("+") || token.equals("-")) {
            op = token;
            getToken();
            //Log.w("eval5", String.format("type=DELIMITER op='%s' nexttoken='%s'", op, token));
        }
        result = evalExp6();

        if (op.equals("-")) {
            //result = -result;
            result = result.negate();
        }

        //Log.w("eval5", String.format("ret=%e", result));
        return result;
    }

    private BigDecimal evalExp6() throws InterpreterException {
        //Log.w("eval6", "exec");
        BigDecimal result;

        if (token.equals("(")) {
            getToken();
            result = evalExp2();
            if (token.equals(")") || token.equals(":") || token.equals(";")) {
                // デリミターが')'と':',';'の時はトークンを進める
                getToken();
            } else if (token == EOP) {
                ;   // おしまいだったら閉じカッコとみなす
            } else if (!token.equals(",") &&
                    !token.equals("=") &&
                    !token.equals(String.valueOf(_NE)) &&
                    !token.equals(String.valueOf(_GE)) &&
                    !token.equals(String.valueOf(_LE)) &&
                    kwToken != EOL) {
                Log.w("evalExp6", String.format("->'%s'", token));
                handleErr(ERR_SYNTAX);
            } else {
                ;   // それ以外の時はトークンを進めない
            }
        } else {
            result = atom();
        }
        //Log.w("eval6", String.format("ret=%e", result));
        return result;
    }

    private BigDecimal atom() throws InterpreterException {
        //Log.w("atom", "exec");
        BigDecimal result = new BigDecimal(0);

        switch (tokType) {
            case NUMBER:
                try {
                    //result = Double.parseDouble(token);
                    BigDecimal tmp = new BigDecimal(token);
                    result = tmp;
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
                int r = 0;
                if (token.equals("(")) {
                    // 配列
                    getToken();
                    //r = (int)evalExp2();
                    r = evalExp2().intValue();
                    //ch += r;
                    if (!token.equals(")")) {
                        handleErr(ERR_SYNTAX);
                    }
                    getToken();
                }

                result = findVar(String.valueOf(ch), r);
                //result = findVar(token);
                //Log.w("atom", String.format("var=%s result=%e", token, result));
                //getToken();
                //Log.w("atom", String.format("next token='%s'", token));
                break;
            case FUNCTION:
                // 関数の計算はBigDecimalでできないのでdoubleで計算してからBigDecimalに戻す
                String str;
                switch (kwToken) {
                    case SIN:
                        getToken();
                        if (!token.equals(EOP)) {
                            //double temp = evalExp2();
                            double temp = evalExp6().doubleValue();
                            double ans;
                            try {
                                //result = Math.sin(Math.toRadians(temp));
                                switch (angleUnit) {
                                    default:
                                    case 0:
                                        ans = Math.sin(Math.toRadians(temp));
                                        break;
                                    case 1:
                                        ans = Math.sin(temp);
                                        break;
                                    case 2:
                                        ans = Math.sin(Math.toRadians(temp*360/400));
                                        break;
                                }
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
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
                            double temp = evalExp6().doubleValue();
                            double ans;
                            try {
                                //result = Math.cos(Math.toRadians(temp));
                                switch (angleUnit) {
                                    default:
                                    case 0:
                                        ans = Math.cos(Math.toRadians(temp));
                                        break;
                                    case 1:
                                        ans = Math.cos(temp);
                                        break;
                                    case 2:
                                        ans = Math.cos(Math.toRadians(temp*360/400));
                                        break;
                                }
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
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
                            double temp = evalExp6().doubleValue();
                            double ans;
                            try {
                                //result = Math.tan(Math.toRadians(temp));
                                switch (angleUnit) {
                                    default:
                                    case 0:
                                        ans = Math.tan(Math.toRadians(temp));
                                        break;
                                    case 1:
                                        ans = Math.tan(temp);
                                        break;
                                    case 2:
                                        ans = Math.tan(Math.toRadians(temp*360/400));
                                        break;
                                }
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
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
                            double temp = evalExp6().doubleValue();
                            double ans;
                            try {
                                //result = Math.toDegrees(Math.asin(temp));
                                switch (angleUnit) {
                                    default:
                                    case 0:
                                        ans = Math.toDegrees(Math.asin(temp));
                                        break;
                                    case 1:
                                        ans = Math.asin(temp);
                                        break;
                                    case 2:
                                        ans = Math.toDegrees(Math.asin(temp))*400/360;
                                        break;
                                }
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
                                nop20ms();
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
                            double temp = evalExp6().doubleValue();
                            double ans;
                            try {
                                //result = Math.toDegrees(Math.acos(temp));
                                switch (angleUnit) {
                                    default:
                                    case 0:
                                        ans = Math.toDegrees(Math.acos(temp));
                                        break;
                                    case 1:
                                        ans = Math.acos(temp);
                                        break;
                                    case 2:
                                        ans = Math.toDegrees(Math.acos(temp))*400/360;
                                        break;
                                }
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
                                nop20ms();
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
                            double temp = evalExp6().doubleValue();
                            double ans;
                            try {
                                //result = Math.toDegrees(Math.atan(temp));
                                switch (angleUnit) {
                                    default:
                                    case 0:
                                        ans = Math.toDegrees(Math.atan(temp));
                                        break;
                                    case 1:
                                        ans = Math.atan(temp);
                                        break;
                                    case 2:
                                        ans = Math.toDegrees(Math.atan(temp))*400/360;
                                        break;
                                }
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
                                nop20ms();
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
                            BigDecimal temp = evalExp6();
                            try {
                                //result = Math.abs(temp);
                                result = temp.abs();
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("---ABS   next token='%s'", token));
                        break;

                    case LOG:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp6().doubleValue();
                            try {
                                double ans = Math.log10(temp);
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
                                nop20ms();
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
                            double temp = evalExp6().doubleValue();
                            try {
                                double ans = Math.log(temp);
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
                                nop20ms();
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
                            double temp = evalExp6().doubleValue();
                            try {
                                double ans = Math.sqrt(temp);
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
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
                            double temp = evalExp6().doubleValue();
                            try {
                                double ans = Math.exp(temp);
                                BigDecimal tmp = new BigDecimal(ans);
                                result = tmp;
                                nop20ms();
                            } catch (NumberFormatException e) {
                                handleErr(ERR_MATH);
                            }
                        }
                        //getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;

                    case KEY:
                        {
                            BigDecimal tmp = new BigDecimal(inkey.getPressKeyCode());
                            result = tmp;
                            Log.w("atom", String.format("KEY result=%d", result.intValue()));
                            getToken();
                        }
                        break;

                    case RAN:
                        {
                            BigDecimal tmp = new BigDecimal(Math.random());
                            result = tmp;
                            getToken();
                        }
                        break;

                    case INT:
                        getToken();
                        if (!token.equals(EOP)) {
                            //double temp = evalExp6();
                            result = evalExp6().setScale(0, BigDecimal.ROUND_DOWN);
                        }
                        //getToken();
                        break;

                    case FRAC:
                        getToken();
                        if (!token.equals(EOP)) {
                            //double temp = evalExp6();
                            result = evalExp6().remainder(BigDecimal.ONE);
                        }
                        //getToken();
                        break;

                    case SGN:
                        getToken();
                        if (!token.equals(EOP)) {
                            //int temp = evalExp6().compareTo(BigDecimal.ZERO);
                            int temp = evalExp6().signum();
                            if (temp > 0) {
                                result = BigDecimal.ONE;
                            } else if (temp < 0) {
                                result = BigDecimal.ONE.negate();
                            } else {
                                result = BigDecimal.ZERO;
                            }
                        }
                        //getToken();
                        break;

                    case PI:
                        {
                            BigDecimal tmp = new BigDecimal(Math.PI);
                            result = tmp;
                            getToken();
                        }
                        break;

                    case LEN:
                        {
                            str = "";
                            getToken();
                            if (token.equals("(")) {
                                getToken();
                                if (tokType == QUTEDSTR) {
                                    str = token;
                                } else if (tokType == SVARIABLE) {
                                    putBack();
                                    strOpe();
                                    if (!resultStr.isEmpty()) {
                                        str = resultStr;
                                    }
                                } else {
                                    // エラー
                                    Log.w("atom:LEN", "invalid param.");
                                    handleErr(ERR_SYNTAX);
                                }
                                getToken();
                                if (!token.equals(")")) {
                                    Log.w("atom:LEN", "')'is not found.");
                                    handleErr(ERR_SYNTAX);
                                }
                            } else {
                                // エラー
                                Log.w("atom:LEN", "'('is not found.");
                                handleErr(ERR_SYNTAX);
                            }
                            BigDecimal tmp = new BigDecimal(str.length());
                            result = tmp;
                        }

                        /*
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
                        */
                        break;

                    case VAL:
                        str = "";
                        getToken();
                        if (token.equals("(")) {
                            getToken();
                            if (tokType == QUTEDSTR) {
                                str = token;
                            } else if (tokType == SVARIABLE) {
                                putBack();
                                strOpe();
                                if (!resultStr.isEmpty()) {
                                    str = resultStr;
                                }
                            } else {
                                // エラー
                                Log.w("atom:VAL", "invalid param.");
                                handleErr(ERR_SYNTAX);
                            }
                            getToken();
                            if (!token.equals(")")) {
                                Log.w("atom:VAL", "')'is not found.");
                                handleErr(ERR_SYNTAX);
                            }
                        } else {
                            // エラー
                            Log.w("atom:VAL", "'('is not found.");
                            handleErr(ERR_SYNTAX);
                        }
                        /*
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
                        */

                        try {
                            //result = Integer.parseInt(str);
                            BigDecimal tmp = new BigDecimal(str);
                            result = tmp;
                        } catch (NumberFormatException e) {
                            Log.w("atom:VAL", "Number format exception occured.");
                            handleErr(ERR_SYNTAX);
                        }
                        break;

                    default:
                        break;
                }
                break;

            default:
                Log.w("atom-default", "ERR_SYNTAX");
                handleErr(ERR_SYNTAX);
                break;
        }
        Log.w("atom", String.format("ret=%s", result.toPlainString()));
        return result;
    }

    private BigDecimal findVar(String vname) throws InterpreterException {
        if (!Character.isLetter(vname.charAt(0))) {
            handleErr(ERR_SYSTEM);
            return BigDecimal.ZERO;
        }
        int var = Character.toUpperCase(vname.charAt(0)) - 'A';
        if (var >= 26) {
            Log.w("findVar", String.format("'%s'", vname));
            handleErr(ERR_SYNTAX);
        }
        if (!svars[var].isEmpty()) {
            handleErr(ERR_VARIABLE);
        }
        return vars[Character.toUpperCase(vname.charAt(0)) - 'A'];
    }

    private BigDecimal findVar(String vname, int offset) throws InterpreterException {
        if (!Character.isLetter(vname.charAt(0))) {
            handleErr(ERR_SYSTEM);
            return BigDecimal.ZERO;
        }
        int var = Character.toUpperCase(vname.charAt(0)) - 'A';
        if (var >= 26) {
            Log.w("findVar", String.format("'%s'", vname));
            handleErr(ERR_SYNTAX);
        }
        var += offset;
        //Log.w("findVar", "var="+var);
        int ex = exvars == null ? 0 : exvars.length;
        //Log.w("findVar", String.format("exvars.length=%d exsvars.length=%d", exvars.length, exsvars.length));
        //Log.w("findVar", String.format("var=%d ex=%d", var, ex));
        if (var >= 26 + ex) handleErr(ERR_VARIABLE);

        if (var < 26 && !svars[var].isEmpty() || var >= 26 && !exsvars[var - 26].isEmpty()) {
            handleErr(ERR_VARIABLE);
        }

        if (var < 26) {
            return vars[var];
        } else {
            return exvars[var - 26];
        }
        //return vars[Character.toUpperCase(vname.charAt(0)) - 'A'];
    }

    private String findSVar(String vname) throws InterpreterException {
        if (vname.equals("$")) {
            return ssvar;
        }
        if (!Character.isLetter(vname.charAt(0))) {
            handleErr(ERR_SYSTEM);
            return null;
        }
        int var = Character.toUpperCase(vname.charAt(0)) - 'A';
        if (var > 26) {
            handleErr(ERR_SYNTAX);
        }
        return svars[Character.toUpperCase(vname.charAt(0)) - 'A'];
    }

    private String findSVar(String vname, int offset) throws InterpreterException {
        if (vname.equals("$")) {
            return ssvar;
        }
        if (!Character.isLetter(vname.charAt(0))) {
            handleErr(ERR_SYSTEM);
            return null;
        }
        int var = Character.toUpperCase(vname.charAt(0)) - 'A';
        if (var > 26) {
            handleErr(ERR_SYNTAX);
        }

        var += offset;
        int ex = exvars == null ? 0 : exvars.length;
        if (var > 26 + ex) handleErr(ERR_VARIABLE);

        if (var < 26) {
            return svars[var];
        } else {
            return exsvars[var - 26];
        }
        //return svars[Character.toUpperCase(vname.charAt(0)) - 'A'];
    }

    final int defaultVar = 26;
    private int defm() {
        if (exvars == null) return defaultVar;
        return (defaultVar + exvars.length);
    }

    public int defm(int n) throws InterpreterException {
        if (n < 0 || memoryExtension && n > 222 || !memoryExtension && n > 94) {
            handleErr(ERR_MEMORYOVER);
        }
        if (n == 0) {
            exvars = null;
            exsvars = null;
        } else if (exvars == null || exvars.length != n) {
            exvars = new BigDecimal[n];
            exsvars = new String[n];
            for (int i = 0; i < n; i++) {
                exsvars[i] = "";
            }
        }
        return (defaultVar + n);
    }

    public int getDefmSize() {
        return exvars == null ? 0 : exvars.length * 8;
    }

    private void putBack() {
        //if  (token == EOP) return;
        if (pc < 1) return;
        int length = token.length() + (tokType == QUTEDSTR ? 2 : 0);
        if (tokType == QUTEDSTR) Log.w("putBack", "!!!!");
        for (int i = 0; i < length; i++) {
            if (pc > 0) pc--;
        }
        //Log.w("putBack", "pc="+pc);
    }

    private void putBack(int p) {
        if (pc < 1) return;
        pc = p;
    }

    private boolean isLetter(char c) {
        return ('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z') ? true : false;
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
        if (ch == '<' || ch == '>' || ch == '!' || ch == '=') {
            if (pc + 1 == idxEnd + 1) handleErr(ERR_SYNTAX);

            switch (ch) {
                case '<':
                    if (prog[pc + 1] == '>') {
                        pc += 2;
                        token = String.valueOf(_NE);
                    } else if (prog[pc + 1] == '=') {
                        pc += 2;
                        token = String.valueOf(_LE);
                    } else {
                        pc++;
                        token = "<";
                    }
                    break;
                case '>':
                    if (prog[pc + 1] == '=') {
                        pc +=2;
                        token = String.valueOf(_GE);
                    } else {
                        pc++;
                        token = ">";
                    }
                    break;
                case '!':
                    if (prog[pc + 1] == '=') {
                        pc +=2;
                        token = String.valueOf(_NE);
                    } else {
                        Log.w("getToken", "illegal charactors");
                        handleErr(ERR_SYNTAX);
                        //pc++;
                        //token = "!";
                    }
                    break;
                case '=':
                    if (prog[pc + 1] == '=') {
                        pc +=2;
                        token = "=";
                    } else {
                        pc++;
                        token = "=";
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
            //Log.w("getToken", String.format("case DELIMITER token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
        } else if (isLetter(prog[pc]) || prog[pc] == '$' || prog[pc] == _PI/*PI*/) {
            while (!isDelim(prog[pc])) {
                if (prog[pc] == _PI) {
                    token += "PI";
                } else {
                    token += (char) (prog[pc] & 0xff);
                }
                pc++;
                if (isReserveWord(token)) {
                    if ((token.equals("KEY") || token.equals("MID") /*|| token.equals("STR")*/) &&
                            prog[pc] == '$') {
                        Log.w("getToken", "charactor'$' is skipped.");
                        token+="$";
                        pc++;
                    }
                    break;
                }
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
            Log.w("getToken", String.format("kwToken=%d tokType=%d", kwToken, tokType));

            if (tokType == VARIABLE) {
                //Log.w("getToken", String.format("case VARIABLE token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
            } else if (tokType == SVARIABLE) {
                //Log.w("getToken", String.format("case SVARIABLE token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
            } else if (tokType == FUNCTION) {
                //Log.w("getToken", String.format("case FUNCTION token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
            } else {
                //Log.w("getToken", String.format("case COMMAND token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
            }
        } else if (prog[pc] == '.' || Character.isDigit(prog[pc])) {
            while (!isDelim(prog[pc])) {
                if (prog[pc] == _EX) {
                    token += "E+";
                } else if (prog[pc] == _EM) {
                    token += "E-";
                } else {
                    token += (char) (prog[pc] & 0xff);
                }
                pc++;
                if (pc >= idxEnd + 1) break;
            }
            tokType = NUMBER;
            //Log.w("getToken", String.format("case NUMBER token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
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
            //pc++;
            //ch = (char) (prog[pc] & 0xff);
            //token = "#"+ch;
            token = "#";
            pc++;
            //Log.w("getToken", String.format("case BANKNUM token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
        } else {
            token = EOP;
            //Log.w("getToken", "return(EOP)");
            return;
        }
        //Log.w("getToken", "func-end");
    }

    public int getProgSteps(String str) {
        char ch;
        int size = 0;
        int i = 0;

        do {
            ch = str.charAt(i);

            // スペースはカウントしない
            if (isSpaceorTab(ch)) {
                i++;
                //Log.w("step", String.format("space %d", size));
                continue;
            }

            // 予約語は1ステップ、それ以外はそのままカウントする
            if (isLetter(ch)) {
                String s = "";
                boolean f = false;
                while (i < str.length() && !isDelim(ch)) {
                    s += (ch = str.charAt(i++));
                    if (isReserveWord(s)) {
                        size++;
                        //Log.w("step", String.format("ReserveWord! %d", size));
                        f = true;
                        if (s.equals("LEN") || s.equals("VAL") || s.equals("MID")) {
                            // LEN, VAL, MIDは'('も含めて予約語なので、カウントを-1する
                            size--;
                        }
                        break;
                    }
                }
                if (!f) {
                    size += s.length();
                    //Log.w("step", String.format("not ReserveWord! %d", size));
                }
            } else  if (ch == '\"') {
                String s = "";
                s += ch;
                while (++i < str.length()) {
                    s += (ch = str.charAt(i));
                    if (ch == '\"') {
                        i++;
                        break;
                    }
                }
                size += s.length();
                //Log.w("step", String.format("quote strings %d", size));
            } else {
                i++;
                size++;
                //Log.w("step", String.format("other %d", size));
            }
        } while (i < str.length());

        return size;
    }

    private boolean isDelim(int c) {
        if ((" \n,;<>+-/*%^=():"+_NE+_LE+_GE).indexOf(c) != -1) {
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
        if (s.length() > 1 && 'A' <= c && c <= 'Z' && s.charAt(1) == '$') {
            return true;
        } else {
            return false;
        }
    }

    private boolean isReserveWord(String s) {
        for (int i = 0x90; i < 0xe0; i++) {
            if (cmdTable[i].equals("\0")) continue;
            if (cmdTable[i].equals(s)) return true;
        }
        return false;
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
        String msg;


        int idx = 0;
        String key = "";
        boolean findValue = false;
        for (Map.Entry<String, Integer>map : labelTable.entrySet()) {
            //Log.w("search", String.format("pc=%d idx=%d value=%d", pc, idx, map.getValue()));
            if (pc >= map.getValue()) {
                if (idx <= map.getValue()) {
                    idx = map.getValue();
                    key = map.getKey();
                    findValue = true;
                    //Log.w("search", String.format("---> pc=%d idx=%d key=%s", pc, idx, key));
                }
            }
        }
        Log.w("handleErr", String.format("---> pc=%d idx=%d key=%s", pc, idx, key));
        if (!pb.isProgExist() || !findValue) {
            //Log.w("handleErr", String.format("%s(%d)", err[error], error));
            msg = String.format("pc=%d %s(%d)", pc, err[error], error);
            lcdPrintAndPause(String.format("ERR%d", error));
        } else {
            //Log.w("handleErr", String.format("%s(%d) P%d-%s", err[error], error, bank, key));
            msg = String.format("pc=%d %s(%d) P%d-%s", pc, err[error], error, bank, key);
            lcdPrintAndPause(String.format("ERR%d P%d-%s", error, bank, key));
        }
        Log.w("handleErr", msg);
        debugText = msg;
        throw  new InterpreterException(err[error]);
    }

}

