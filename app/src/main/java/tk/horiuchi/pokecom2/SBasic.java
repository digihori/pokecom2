package tk.horiuchi.pokecom2;

//import com.sun.org.apache.regexp.internal.RE;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.*;
/**
 *
 */
public class SBasic {
    final int PROG_SIZE = 10_000; //

    //
    final int NONE = 0;
    final int DELIMITER = 1;
    final int VARIABLE = 2;
    final int NUBMER = 3;
    final int COMMAND = 4;
    final int QUTEDSTR = 5;

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
            new Keyword("end", END)
    };

    private char[] prog;
    private int progIdx;
    private String token;
    private int tokType;
    private int kwToken;

    private boolean nextLine = true;
    private Double lastAns;

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
    }

    public void load(String progName) throws InterpreterException {
        char tempbuf[] = new char[PROG_SIZE];
        int size;

        size = loadProgram(tempbuf, progName);
        if (size != -1) {
            prog = new char[size];

            System.arraycopy(tempbuf, 0, prog, 0, size);
            Log.w("load", String.format("file open! size=%d", size));
            //for (int i = 0; i < size; i++) {
            //    Log.w("load", String.format("prog=%s", prog[i]));
            //}
        }

    }

    private void lcdPrint(String s) {
        System.out.print(s);
    }
    private void lcdPrint(char c) {
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
        for (int id = progIdx; id < prog.length && prog[id] != '\n'; id++) {
            s += prog[id];
        }
        if (s != null) {
            Log.w("TR", String.format("%s", s));
        }
    }

    private int loadProgram(char[] p, String fname) throws InterpreterException {
        int size = 0;
        try {
            FileReader fr = new FileReader(fname);
            BufferedReader br = new BufferedReader(fr);
            size = br.read(p, 0, PROG_SIZE);
            fr.close();
        } catch (FileNotFoundException e) {
            handleErr(FILENOUFOUND);
        } catch (IOException e) {
            handleErr(FILEIOERROR);
        }

        if (p[size - 1] == (char) 26) size--;
        return size;
    }

    public void run() throws InterpreterException {
        progIdx = 0;
        scanLabels();
        Log.w("RUN", String.format("-------- %s", labelTable));
        sbInterp();
    }

    public void calc(String s) throws InterpreterException {
        if (s == "") return;

        //Log.w("calc", "exe");
        lcd.cls();
        progIdx = 0;
        nextLine = true;
        int size = s.length();
        if (size != -1) {
            prog = new char[size];

            for (int i = 0; i < size; i++) {
                prog[i] = s.charAt(i);
            }
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
                //Log.w("sbCmd1", "id="+progIdx);
                putBack();
                putBack();
                //Log.w("sbCmd1", "id="+progIdx);
                lastAns = evaluate();
                //Log.w("sbCmd1", String.format("%d", lastAns.intValue()));
            }
        } else {
            //Log.w("sbCmd2", "id="+progIdx);
            putBack();
            //Log.w("sbCmd2", "id="+progIdx);
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
                //progIdx++;
                //Log.w("sbInterp", "DELIMITER");
            } else {
                //Log.w("sbInterp", "???");
            }
        } while (!token.equals(EOP));

    }

    private void scanLabels() throws InterpreterException {
        int i;
        Object result;

        int id = progIdx;
        getToken();
        if (tokType == NUBMER) {
            labelTable.put(token, new Integer(id));
        }
        findEOL();
        do {
            id = progIdx;
            getToken();
            if (tokType == NUBMER) {
                result = labelTable.put(token, new Integer(id));

                if (result != null) handleErr(DUPLABEL);
            }
            if (kwToken != EOL) findEOL();
        } while (!token.equals(EOP));
        progIdx = 0;
    }

    private void findEOL() {
        while (progIdx < prog.length && prog[progIdx] != '\n')
            progIdx++;
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
            progIdx = loc.intValue();
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
            stckvar.loc = progIdx;
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
            progIdx = stckvar.loc;
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
            gStack.push(new Integer(progIdx));

            progIdx = loc.intValue();
            nextLine = true;
        }
    }

    private void greturn() throws InterpreterException {
        Integer t;
        try {
            t = (Integer) gStack.pop();
            progIdx = t.intValue();
            nextLine = true;
        } catch (EmptyStackException e) {
            handleErr(RETURNWITHOUTGOSUB);
        }
    }

    //****************************************************
    private double evaluate() throws InterpreterException {
        //Log.w("eval", "exec");
        double result = 0.0;
        getToken();
        if (token.equals(EOP)) {
            handleErr(NOEXP);
        }
        result = evalExp1();
        putBack();
        //Log.w("eval", String.format("ret=%e", result));
        return  result;
    }

    // <, >, =, <=, >=, <>
    private double evalExp1() throws InterpreterException {
        //Log.w("eval1", "exec");
        double l_temp, r_temp, result;
        char op;

        result = evalExp2();

        if (token.equals(EOP)) return  result;

        op = token.charAt(0);
        if (isRelop(op)) {
            l_temp = result;
            getToken();
            r_temp = evalExp1();

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
        //Log.w("eval1", String.format("ret=%e", result));
        return  result;
    }

    private double evalExp2() throws InterpreterException {
        //Log.w("eval2", "exec");
        char op;
        double result;
        double partialResult;

        result = evalExp3();

        while ((op = token.charAt(0)) == '+' || op == '-') {
            getToken();
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
            case NUBMER:
                try {
                    result = Double.parseDouble(token);
                } catch (NumberFormatException e) {
                    handleErr(SYNTAX);
                }
                getToken();
                break;
            case VARIABLE:
                result = findVar(token);
                getToken();
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
        if (progIdx < 1) return;
        for (int i = 0; i < token.length(); i++) {
            if (progIdx > 0) progIdx--;
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

        if (progIdx == prog.length) {
            token = EOP;
            return;
        }

        while (progIdx < prog.length && isSpaceorTab(prog[progIdx]))
            progIdx++;

        if (progIdx == prog.length) {
            token = EOP;
            tokType = DELIMITER;
            //Log.w("getToken", "return(EOP)");
            return;
        }

        if (prog[progIdx] == '\n') {
            //progIdx += 2;
            progIdx += 1;
            kwToken = EOL;
            //token = "\r\n";
            token = "\n";
            //Log.w("getToken", "return(EOL)");
            nextLine = true;
            return;
        }

        ch = prog[progIdx];
        if (ch == '<' || ch == '>') {
            if (progIdx + 1 == prog.length) handleErr(SYNTAX);

            switch (ch) {
                case '<':
                    if (prog[progIdx + 1] == '>') {
                        progIdx += 2;
                        token = String.valueOf(NE);
                    } else if (prog[progIdx + 1] == '=') {
                        progIdx += 2;
                        token = String.valueOf(LE);
                    } else {
                        progIdx++;
                        token = "<";
                    }
                    break;
                case '>':
                    if (prog[progIdx + 1] == '=') {
                        progIdx +=2;
                        token = String.valueOf(GE);
                    } else {
                        progIdx++;
                        token = ">";
                    }
                    break;
            }
            tokType = DELIMITER;
            //Log.w("getToken", "return(DELIMITER <>)");
            return;
        }

        //Log.w("getToken", "!!!!!!");

        if (isDelim(prog[progIdx])) {
            token += prog[progIdx];
            progIdx++;
            tokType = DELIMITER;
            Log.w("getToken", String.format("case DELIMITER token='%s' progIdx=%d tokType=%d kwToken=%d", token, progIdx, tokType, kwToken));
        } else if (Character.isLetter(prog[progIdx])) {
            while (!isDelim(prog[progIdx])) {
                token += prog[progIdx];
                progIdx++;
                if (progIdx >= prog.length) break;
            }
            kwToken = lookUp(token);
            if (kwToken == UNKNCOM) tokType = VARIABLE;
            else tokType = COMMAND;
            Log.w("getToken", String.format("case VARIABLE/COMMAND token='%s' progIdx=%d tokType=%d kwToken=%d", token, progIdx, tokType, kwToken));
        } else if (Character.isDigit(prog[progIdx])) {
            while (!isDelim(prog[progIdx])) {
                token += prog[progIdx];
                progIdx++;
                if (progIdx >= prog.length) break;
            }
            tokType = NUBMER;
            Log.w("getToken", String.format("case NUMBER token='%s' progIdx=%d tokType=%d kwToken=%d", token, progIdx, tokType, kwToken));
        } else  if (prog[progIdx] == '\"') {
            //Log.w("getToken", "DQ!!!");
            progIdx++;
            ch = prog[progIdx];
            while (ch != '\"' && ch != '\n') {
                token += ch;
                progIdx++;
                tokType = QUTEDSTR;
                ch = prog[progIdx];
                //Log.w("while", String.format("%c", ch));
            }
            progIdx++;
            Log.w("getToken", String.format("case DQ token='%s' progIdx=%d tokType=%d kwToken=%d", token, progIdx, tokType, kwToken));
        } else {
            token = EOP;
            Log.w("getToken", "return(EOP)");
            return;
        }
        //Log.w("getToken", "func-end");
    }

    private boolean isDelim(char c) {
        if (" \n,;<>+-/*%^=():".indexOf(c) != -1) {
            return true;
        }
        return false;
    }

    private boolean isSpaceorTab(char c) {
        if (c == ' ' || c == '\t') {
            return true;
        }
        return false;
    }

    private boolean isRelop(char c) {
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

