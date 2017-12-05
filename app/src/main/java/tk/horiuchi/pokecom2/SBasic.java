package tk.horiuchi.pokecom2;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.*;

import static tk.horiuchi.pokecom2.MainActivity.bank;
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

    //
    final int SYNTAX = 0;
    final int UNBALPARENS = 1;
    final int NOEXP = 2;
    final int DIVBYZERO = 3;
    final int EQUALEXPECTED = 4;
    final int NOTVAR = 5;
    final int LABELTABLEFULL = 6;
    final int DUPLABEL = 7;
    final int UNDEFLABEL = 8;
    final int THENECPECTED = 9;
    final int TOEXPECTED = 10;
    final int NEXTWITHOUTFOR = 11;
    final int RETURNWITHOUTGOSUB = 12;
    final int MISSINGQOUTE = 13;
    final int FILENOUFOUND = 14;
    final int FILEIOERROR = 15;
    final int INPUTIOERROR = 16;

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

    //
    final String EOP = "\0";

    //
    final char LE = 1;
    final char GE = 2;
    final char NE = 3;

    //
    private double vars[];

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
            new Keyword("sin", SIN),
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
    class Label {
        String name;
        int loc;

        public Label(String str, int i) {
            name = str;
            loc = i;
        }
    }

    //
    private TreeMap labelTable;

    //
    private Stack gStack;

    //
    char rops[] = {
            GE, NE, LE, '<', '>', '=', 0
    };

    String relops = new String(rops);


    public SBasic(Lcd lcd) throws InterpreterException {
        this.lcd = lcd;
        vars = new double[26];
        fStack = new Stack();
        labelTable = new TreeMap();
        gStack = new Stack();
        lastAns = new Double(0);
        prog = new char[progLength];
        //initListMap();

        //source = new SourceFile();
    }


    private void lcdPrint(String s) {
        lcd.print(s);
        System.out.print(s);
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

        if (nextLine) {
            nextLine = false;
            printTrace();
        }
        getToken();
        if (tokType == VARIABLE) {
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
        } else if (tokType == COMMAND) {
            switch (kwToken) {
                case RUN:
                    //lcd.cls();
                    bank = 0;
                    Log.w("SBasic", String.format("--- RUN(%d) ---", bank));
                    run();
                    return;
                    //break;
                case LIST:
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

        Log.w("sbCmd", String.format("ans=%d", lastAns.intValue()));
        lcdPrint(lastAns);
        lcdPrintln();
    }

    private void sbInterp() throws InterpreterException {
        do {
            if (nextLine) {
                nextLine = false;
                printTrace();
            }
            getToken();
            if (tokType == VARIABLE) {
                putBack();
                assignment();
            } else if (tokType == COMMAND) {
                switch (kwToken) {
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

                if (result != null) handleErr(DUPLABEL);
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
        vname = token.charAt(0);
        Log.w("assign", String.format("%c", vname));

        if (!Character.isLetter(vname)) {
            handleErr(NOTVAR);
            Log.w("assign", "NOTVAR");
            return;
        }

        var = (int) Character.toUpperCase(vname) - 'A';

        getToken();
        if (!token.equals("=")) {
            handleErr(EQUALEXPECTED);
            return;
        }

        value = evaluate();
        lastAns = value;
        //Log.w("assig", String.format("%d", lastAns.intValue()));

        vars[var] = value;
    }

    private void print() throws InterpreterException {
        double result;
        int len = 0, spaces;
        String lastDelim = "";

        //Log.w("PRT", "Exec PRINT !!!!");

        do {
            //Log.w("PRT", "Exec PRINT do !!!!");
            getToken();
            //Log.w("PRT", String.format("token=%s", token));
            if (kwToken == EOL || token.equals(EOP)) break;
            if (/*tokType == DELIMITER &&*/ token.equals(":")) {
                //Log.w("PRT", String.format("DELIMITER(:)"));
                break;
            }

            if (tokType == QUTEDSTR) {
                //Log.w("PRT", String.format("tokType=QUTEDSTR(%d)", tokType));
                //System.out.print(token);
                lcdPrint(token);
                //Log.w("PRT", String.format("%s", token));
                len += token.length();
                getToken();
            } else {
                //Log.w("PRT", String.format("tokType!=QUTEDSTR(%d)", tokType));
                putBack();
                result = evaluate();
                getToken();
                //Log.w("PRT", String.format("AAAAA=%s", token));
                //System.out.print(result);
                lcdPrint(result);
                //Log.w("PRT", String.format("%d", result));

                Double t = new Double(result);
                len += t.toString().length();
            }
            lastDelim = token;
            //Log.w("PRT", String.format("lastDelim='%s'", lastDelim));

            if (lastDelim.equals(":")) {
                ;
            } else if (lastDelim.equals(",")) {
                spaces = 8 - (len % 8);
                len += spaces;
                while (spaces != 0) {
                    //System.out.print(" ");
                    lcdPrint(" ");
                    spaces--;
                }
            } else if (token.equals(";")) {
                //System.out.print(" ");
                //lcdPrint(" ");
                //len++;
            } else if (kwToken != EOL && !token.equals(EOP)) {
                handleErr(SYNTAX);
            }
        } while (lastDelim.equals(";") || lastDelim.equals(","));
        if (kwToken == EOL || token.equals(EOP) || token.equals(":")) {
            if (!lastDelim.equals(";") && !lastDelim.equals(",")) {
                //System.out.println();
                lcdPrintln();
            }
        } else {
            handleErr(SYNTAX);
        }
    }

    private void execGoto() throws InterpreterException {
        Integer loc;
        getToken();

        loc = (Integer) labelTable.get(token);
        if (loc == null) {
            handleErr(UNDEFLABEL);
        } else {
            pc = loc.intValue();
            nextLine = true;
            //Log.w("GOTO", String.format("goto %s(%d)", token, loc));
        }
    }


    private void execIf() throws InterpreterException {
        double result;
        result = evaluate();

        Log.w("IF", String.format("if (%d)", result));
        if (result != 0.0) {
            getToken();
            if (kwToken != THEN) {
                handleErr(THENECPECTED);
                return;
            }
        } else
            findEOL();
    }

    private void execFor() throws InterpreterException {
        ForInfo stckvar = new ForInfo();
        double value;
        char vname;
        getToken();
        vname = token.charAt(0);
        if (!Character.isLetter(vname)) {
            handleErr(NOTVAR);
            return;
        }

        stckvar.var = Character.toUpperCase(vname) - 'A';

        getToken();
        if (token.charAt(0) != '=') {
            handleErr(EQUALEXPECTED);
            return;
        }

        value = evaluate();
        vars[stckvar.var] = value;
        getToken();
        if (kwToken != TO) handleErr(TOEXPECTED);
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
            handleErr(NEXTWITHOUTFOR);
        }
        //Log.w("NEXT", "continue.");
    }

    private void input() throws InterpreterException {
        int var;
        double val = 0.0;
        String str;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        getToken();
        if (tokType == QUTEDSTR) {
            //System.out.print(token);
            lcdPrint(token);
            getToken();
            if (!token.equals(",")) handleErr(SYNTAX);
            getToken();
        } else {
            //System.out.print("? ");
            lcdPrint("? ");
        }
    }

    private void gosub() throws InterpreterException {
        Integer loc;
        getToken();

        loc = (Integer) labelTable.get(token);
        if (loc == null)
            handleErr(UNDEFLABEL);
        else {
            gStack.push(new Integer(pc));

            pc = loc.intValue();
            nextLine = true;
        }
    }

    private void greturn() throws InterpreterException {
        Integer t;
        try {
            t = (Integer) gStack.pop();
            pc = t.intValue();
            nextLine = true;
        } catch (EmptyStackException e) {
            handleErr(RETURNWITHOUTGOSUB);
        }
    }

    /*
    private void execSin() throws InterpreterException {
        Log.w("SIN", "execSin");
        int param;
        Double ans;
        getToken();


        try {
            param = Integer.parseInt(token);
            ans = Math.sin(param);
            Log.w("SIN", String.format("sin(%d)=%e", param, ans));
        } catch (NumberFormatException e) {
            handleErr(SYNTAX);
        }

    }
    */

    //****************************************************
    private double evaluate() throws InterpreterException {
        //Log.w("eval", "exec");
        double result = 0.0;
        getToken();
        if (token.equals(EOP)) {
            handleErr(NOEXP);
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
            //Log.w("eval2", String.format("op=%c", op));
            getToken();
            //Log.w("eval2", String.format("next token=%s", token));
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
                        handleErr(DIVBYZERO);
                    result = result / partialResult;
                    break;
                case '%':
                    if (partialResult == 0.0)
                        handleErr(DIVBYZERO);
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
            if (!token.equals(")")) {
                handleErr(UNBALPARENS);
            }
            getToken();
        } else {
            result = atom();
        }
        //Log.w("eval6", String.format("ret=%e", result));
        return result;
    }

    private double atom() throws InterpreterException {
        //Log.w("atom", "exec");
        double result = 0.0;

        switch (tokType) {
            case NUMBER:
                try {
                    result = Double.parseDouble(token);
                    //Log.w("atom", String.format("result=%e", result));
                } catch (NumberFormatException e) {
                    handleErr(SYNTAX);
                }
                getToken();
                //Log.w("atom", String.format("next token='%s'", token));
                break;
            case VARIABLE:
                result = findVar(token);
                //Log.w("atom", String.format("var=%s result=%e", token, result));
                getToken();
                //Log.w("atom", String.format("next token='%s'", token));
                break;
            case FUNCTION:
                switch (kwToken) {
                    case SIN:
                        getToken();
                        if (!token.equals(EOP)) {
                            double temp = evalExp1();
                            try {
                                result = Math.sin(Math.toRadians(temp));
                                //Log.w("atom", String.format("SIN result=%e", result));
                            } catch (NumberFormatException e) {
                                handleErr(SYNTAX);
                            }
                        }
                        getToken();
                        //Log.w("atom", String.format("next token='%s'", token));
                        break;
                    default:
                        break;
                }
                break;
            default:
                handleErr(SYNTAX);
                break;
        }
        Log.w("atom", String.format("ret=%e", result));
        return result;
    }

    private double findVar(String vname) throws InterpreterException {
        if (!Character.isLetter(vname.charAt(0))) {
            handleErr(SYNTAX);
            return 0.0;
        }
        return vars[Character.toUpperCase(vname.charAt(0)) - 'A'];
    }

    private void putBack() {
        //if  (token == EOP) return;
        if (pc < 1) return;
        for (int i = 0; i < token.length(); i++) {
            if (pc > 0) pc--;
        }
    }

    private void handleErr(int error) throws InterpreterException {
        String[] err = {
                "Syntax Error",
                "Unbalanced Parentheses",
                "No Expression Present",
                "Division by Zero",
                "Equal sign expected",
                "Not a variable",
                "Label table full",
                "Duplicate label",
                "Underfined label",
                "THEN expected",
                "TO expected",
                "NEXT without FOR",
                "RETURN without GOSUB",
                "Closing quotes needed",
                "File not found",
                "I/O error while loading file",
                "I/O error on INPUT statement"
        };

        throw  new InterpreterException(err[error]);
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
            if (pc + 1 == idxEnd + 1) handleErr(SYNTAX);

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
        } else if (Character.isLetter(prog[pc])) {
            while (!isDelim(prog[pc])) {
                token += (char)(prog[pc]&0xff);
                pc++;
                if (pc >= idxEnd + 1) break;
            }
            kwToken = lookUp(token);
            if (kwToken == UNKNCOM) {
                tokType = VARIABLE;
            } else if (kwToken > FUNC_DUMMY) {
                tokType = FUNCTION;
            } else {
                tokType = COMMAND;
            }

            if (tokType == VARIABLE) {
                Log.w("getToken", String.format("case VARIABLE token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
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
            pc++;
            ch = (char)(prog[pc]&0xff);
            while (ch != '\"' && ch != '\n') {
                token += ch;
                pc++;
                tokType = QUTEDSTR;
                ch = (char)(prog[pc]&0xff);
                //Log.w("while", String.format("%c", ch));
            }
            pc++;
            Log.w("getToken", String.format("case DQ token='%s' pc=%d tokType=%d kwToken=%d", token, pc, tokType, kwToken));
        } else {
            token = EOP;
            Log.w("getToken", "return(EOP)");
            return;
        }
        //Log.w("getToken", "func-end");
    }

    private boolean isDelim(int c) {
        if (" \n,;<>+-/*%^=():".indexOf(c) != -1) {
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
}

