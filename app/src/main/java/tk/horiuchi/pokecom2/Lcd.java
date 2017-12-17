package tk.horiuchi.pokecom2;

import android.os.Handler;
import android.util.Log;

import static tk.horiuchi.pokecom2.Common.MODE_PRO;
import static tk.horiuchi.pokecom2.Common.MODE_RUN;
import static tk.horiuchi.pokecom2.Common.cmdTable;
import static tk.horiuchi.pokecom2.MainActivity.bank;
import static tk.horiuchi.pokecom2.MainActivity.bankStatus;
import static tk.horiuchi.pokecom2.MainActivity.basic;
import static tk.horiuchi.pokecom2.MainActivity.initial;
import static tk.horiuchi.pokecom2.MainActivity.listDisp;
import static tk.horiuchi.pokecom2.MainActivity.mode;
import static tk.horiuchi.pokecom2.MainActivity.pb;
import static tk.horiuchi.pokecom2.MainActivity.source;
import static tk.horiuchi.pokecom2.PbMain.digi;


/**
 * Created by yoshimine on 2017/11/18.
 */

public class Lcd {
    private RefreshScreenInterFace listener = null;
    private static int cursor = 0;
    private int[] buf;
    private int buf_index = 0;
    private int buf_top = 0;
    private final int bufMax = 128;
    private boolean flash = false;
    private boolean flashBank = false;
    private int charBack = 0;
    private int flashingCnt = 0;
    private int bankBack = 0;
    private int flashingBankCnt = 0;

    public Lcd() {
        //
        buf = new int[bufMax];
        for (int i = 0; i < bufMax; i++) {
            buf[i] = 0;
        }

        final Handler _handler1 = new Handler();
        final int DELAY1 = 50;
        _handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                flashingCursor();
                flashingBank();
                //Log.w("LOG", "run.....");
                _handler1.postDelayed(this, DELAY1);
            }
        }, DELAY1);

    }

    public String getCmdBuf() {
        if (buf_index == 0) return null;

        char[] temp = new char[buf_index];
        for (int i = 0; i < buf_index; i++) {
            temp[i] = (char)buf[i];
        }
        String s = String.valueOf(temp);
        Log.w("getCmdBuf", String.format("%s", s));
        return String.valueOf(temp);
    }

    public void setListener(RefreshScreenInterFace listener) {
        this.listener = listener;
    }

    private void flashingReset() {
        flashingCnt = 0;
        flash = false;
    }

    public void printBankStatus() {
        bankStatus = true;
        flashingBankCnt = 0;
        printDigit(0, 'P');
        printDigit(1, ' ');
        for (int i = 0; i < 10; i++) {
            int c;
            if (!source.checkProgExist(i)) {
                c = '0' + i;
                //printDigit(2 + i, '0'+i);
            } else {
                c = '_';
                //printDigit(2 + i, '_');
            }
            printDigit(2 + i, c);
            buf[2 + i] = c;
        }
    }

    private void flashingBank() {
        if (!bankStatus) return;

        flashingBankCnt--;
        if (flashingBankCnt < 1) {
            flashingBankCnt = 10;
            if (!flashBank) {
                //bankBack = buf[2 + bank];
                printDigit(2 + bank, ' ');
            } else {
                printDigit(2 + bank, buf[2 + bank]);
            }
            flashBank = !flashBank;
            listener.refreshScreen();

            //Log.w("Lcd", String.format("flashingBank(%d)", bank));
        }
    }

    private void flashingCursor() {
        if (initial) return;    // 仮

        flashingCnt--;
        if (flashingCnt < 1) {
            flashingCnt = 10;
            if (!flash) {
                if (cursor < buf_index - buf_top) {
                    charBack = buf[buf_top + cursor];
                }
                printDigit(cursor, '_');
            } else {
                if (cursor < buf_index - buf_top) {
                    printDigit(cursor, charBack);
                } else {
                    printDigit(cursor, ' ');
                }
            }
            flash = !flash;
            listener.refreshScreen();

            //Log.w("Lcd", "flashingCursor");
        }
    }

    private void refreshPrint() {
        int i;
        for (i = 0; i <= 11 && i < buf_index; i++) {
            printDigit(i, buf[buf_top + i]);
        }
        if (i < 11) {
            for ( ; i <= 11; i++) {
                printDigit(i, ' ');
            }
        }
        listener.refreshScreen();
    }

    public void refresh() {
        listener.refreshScreen();
    }

    public void cls() {
        initial = false;
        bankStatus = false;

        cursor = 0;
        for (int i = 0; i < digi.length; i++) {
            digi[i] = 0;
        }
        buf_index = 0;
        buf_top = 0;
        for (int i = 0; i < bufMax; i++) {
            buf[i] = 0;
        }
        listener.refreshScreen();
        debug();
    }

    public void insert() {
        if (buf_index < bufMax - 2 && buf_top + cursor != buf_index) {
            int i;
            for (i = buf_index; i > buf_top + cursor; i--) {
                buf[i] = buf[i-1];
                Log.w("Lcd", String.format("buf[%d]=%x", i, buf[i]));
            }
            buf[i] = ' ';
            buf_index++;
            refreshPrint();
            flashingReset();
            debug();
        }
    }

    public void delete() {
        if (buf_top + cursor < buf_index) {
            for (int i = buf_top + cursor; i < buf_index; i++) {
                buf[i] = buf[i + 1];
            }
            buf_index--;
            refreshPrint();
            flashingReset();
            debug();
        }
    }

    public void moveLeft() {
        if (cursor > 0) {
            refreshPrint();
            if (cursor == 11 && buf_top > 0) {
                buf_top--;
                for (int i = 0; i <= 11; i++) {
                    printDigit(i, buf[buf_top + i]);
                }
            } else {
                cursor--;
            }
            flashingReset();
        }
        debug();
    }

    public void moveRight() {
        if (cursor < buf_index - buf_top) {
            refreshPrint();
            if (cursor < 11) {
                cursor++;
            } else {
                buf_top++;
                for (int i = 0; i <= 11; i++) {
                    printDigit(i, buf[buf_top + i]);
                }
            }
            flashingReset();
        }
        debug();
    }

    public void printSourceList(String s) {
        if (bankStatus) {
            bankStatus = false;
        }
        cls();
        for (int i = 0; i < s.length(); i++) {
            buf[i] = s.charAt(i);
        }
        for (int i = 0; i < 12 && i < s.length(); i++) {
            printDigit(i, buf[i]);
        }
        buf_top = 0;
        buf_index = s.length();
        cursor = buf_index < 12 ? buf_index : 11;
    }

    public void bprint(String s) {
        if (bankStatus) {
            bankStatus = false;
        }

        cls();
        initial = true;

        for (int i = 0; i < s.length() && i < 12; i++) {
            printDigit(i, s.charAt(i));
        }
        listener.refreshScreen();
        if (s.length() > 12) {
            try{
                Thread.sleep(800);
            }catch(InterruptedException e){}

            for (int i = 0; i < s.length() - 12; i++) {
                if (basic.isForcedExitReq()) break;
                try{
                    Thread.sleep(300);
                }catch(InterruptedException e){}

                for (int j = 0; j < 12; j++) {
                    printDigit(j, s.charAt(i + 1 + j));
                }
                listener.refreshScreen();
            }
        }
    }

    public void print(String s) {
        if (bankStatus) {
            bankStatus = false;
        }
        if (initial) {
            initial = false;
            cls();
        }

        for (int i = 0; i < s.length(); i++) {
            putc(s.charAt(i));
        }
    }

    public void print(String s, int idx) {
        if (idx > 12) return;
        if (bankStatus) {
            bankStatus = false;
        }
        if (initial) {
            initial = false;
            cls();
        }
        for (int i = 0; i < s.length(); i++) {
            buf[i] = s.charAt(i);
        }
        int j = 0;
        for (int i = 0; i < 12 && i < idx; i++) {
            printDigit(i, s.charAt(j++));
        }
        for (int i = idx; i < 12 && j < s.length(); i++) {
            printDigit(i, s.charAt(j++));
        }
    }

    public void putchar(int c) {
        if (bankStatus) {
            bankStatus = false;
        }
        if (initial) {
            initial = false;
            cls();
        }

        if (0x10 <= c && c <= 0x1f && !cmdTable[c].equals("\0")) {
            // カーソル移動系
            switch (c) {
                case 0x10:  // LA
                    moveLeft();
                    break;
                case 0x11:  // RA
                    moveRight();
                    break;
                case 0x12:  // DA
                case 0x13:  // UA
                case 0x19:  // ANS
                    break;
                case 0x1a:  // AC
                    cls();
                    break;
                case 0x1b:  // EXE
                    Log.w("putChar", "EXE!!!!!");

                    String s = getCmdBuf();
                    if (mode == MODE_RUN && pb.isProgStop()) {
                        pb.progRestart();
                    }
                    if (s == null) break;
                    //try {
                    //    Log.w("EXE", String.format("%s", s));
                    //    calc(getCmdBuf());
                    //} catch (InterpreterException e) {
                    //    Log.w("Main", String.format("error='%s'", e.toString()));
                    //}
                    break;
                case 0x1c:  // STOP
                    if (mode == MODE_RUN && pb.isProgExist()) {
                        //pb.progStop();
                        basic.sbExit();
                    }
                    break;
                case 0x1d:  // DEL
                    delete();
                    break;
                case 0x1e:  // INS
                    insert();
                    break;
                default:
                    break;
            }
        } else if (0x80 <= c && c <=0x8f && !cmdTable[c].equals("\0")) {
            if (mode == MODE_RUN && (c & 0x0f) < 10) {
                bank = c & 0x0f;
                pb.progStart();
/*
                try {
                    Log.w("lcd", String.format("run! P=%d", bank));
                    basic.run();
                } catch (InterpreterException e) {
                    Log.w("Main", String.format("error='%s'", e.toString()));
                }
*/
            } else if (mode == MODE_PRO && (c & 0x0f) < 10) {
                bank = c & 0x0f;
                printBankStatus();
                initial = true;
                listDisp = false;
                Log.w("putchar", String.format("bank=%d", bank));
            }
            // バンクの切り替え
        } else if (0x90 <= c && c <=0xdf && !cmdTable[c].equals("\0")) {
            // 予約語の入力
            if (buf_top + cursor != 0 && buf[buf_top + cursor - 1] != ' ') putc(' ');
            print(cmdTable[c]);
            putc(' ');
            Log.w("Lcd", String.format("putchar='%s'", cmdTable[c]));
        } else {
            putc(c);
        }
    }

    public void putc(int c) {
        if (buf_index < bufMax - 1) {
            buf[buf_top + cursor] = c;
            if (buf_index < buf_top + cursor + 1) buf_index++;

            //Log.w("Lcd", String.format("%s", buf));
            if (cursor < 11) {
                printDigit(cursor, c);
                cursor++;
            } else {
                //buf_top = buf_index - cursor;
                buf_top++;
                for (int j = 0; j < 12; j++) {
                    printDigit(j, buf[buf_top + j]);
                }
            }
            flashingReset();
            debug();
        }

    }

    private void printDigit(int x, int c) {
        int index = x * 5;
        for (int i = 0; i < charData.length; i++) {
            if (charData[i].code == c) {
                digi[index] = (byte) ((charData[i].gdata >> 32) & 0xff);
                digi[index + 1] = (byte) ((charData[i].gdata >> 24) & 0xff);
                digi[index + 2] = (byte) ((charData[i].gdata >> 16) & 0xff);
                digi[index + 3] = (byte) ((charData[i].gdata >> 8) & 0xff);
                digi[index + 4] = (byte) ((charData[i].gdata) & 0xff);
            }
        }
        listener.refreshScreen();
    }

    private void debug() {
        Log.w("Lcd", String.format("cursor=%d buf_index=%d buf_top=%d", cursor, buf_index, buf_top));
    }

    private final class CharData {
        public int code;
        public long gdata;

        public CharData(int code, long gdata) {
            this.code = code;
            this.gdata = gdata;
        }
    }

    private final CharData[] charData = {
            new CharData(' ', 0x0000000000L),
            new CharData('!', 0x00005f0000L),
            new CharData('\"', 0x0007000700L),
            new CharData('#', 0x147f147f14L),
            new CharData('$', 0x242a7f2a12L),
            new CharData('%', 0x2313086462L),
            new CharData('&', 0x3649552250L),
            new CharData('\'', 0x0005030000L),
            new CharData('(', 0x001c224100L),
            new CharData(')', 0x0041221c00L),
            new CharData('*', 0x14083e0814L),
            new CharData('+', 0x08083e0808L),
            new CharData(',', 0x0050300000L),
            new CharData('-', 0x0808080808L),
            new CharData('.', 0x0060600000L),
            new CharData('/', 0x2010080402L),
            new CharData('0', 0x3e5149453eL),
            new CharData('1', 0x00427f4000L),
            new CharData('2', 0x4261514946L),
            new CharData('3', 0x2141454b31L),
            new CharData('4', 0x1814127F10L),
            new CharData('5', 0x2745454539L),
            new CharData('6', 0x3c4a494930L),
            new CharData('7', 0x0101790503L),
            new CharData('8', 0x3649494936L),
            new CharData('9', 0x064949291eL),

            new CharData(':', 0x0036360000L),
            new CharData(';', 0x0056360000L),
            new CharData('<', 0x0814224100L),
            new CharData('=', 0x1414141414L),
            new CharData('>', 0x0041221408L),
            new CharData('?', 0x0201510906L),

            new CharData('@', 0x324979413eL),
            new CharData('A', 0x7e0909097eL),
            new CharData('B', 0x7f49494936L),
            new CharData('C', 0x3e41414122L),
            new CharData('D', 0x7f4141221cL),
            new CharData('E', 0x7f49494941L),
            new CharData('F', 0x7f09090901L),
            new CharData('G', 0x3e4149497aL),
            new CharData('H', 0x7f0808087fL),
            new CharData('I', 0x00417f4100L),
            new CharData('J', 0x2040413f01L),
            new CharData('K', 0x7f08142241L),
            new CharData('L', 0x7f40404040L),
            new CharData('M', 0x7f020c027fL),
            new CharData('N', 0x7f0408107fL),
            new CharData('O', 0x3e4141413eL),
            new CharData('P', 0x7f09090906L),
            new CharData('Q', 0x3e4151215eL),
            new CharData('R', 0x7f09192946L),
            new CharData('S', 0x4649494931L),
            new CharData('T', 0x01017f0101L),
            new CharData('U', 0x3f4040403fL),
            new CharData('V', 0x1f2040201fL),
            new CharData('W', 0x7f2018207fL),
            new CharData('X', 0x6314081463L),
            new CharData('Y', 0x0304780403L),
            new CharData('Z', 0x6151494543L),
            new CharData('[', 0x007f414100L),
            new CharData('\\', 0x15167c1615L),
            new CharData(']', 0x0041417f00L),
            new CharData('^', 0x04027f0204L),
            new CharData('_', 0x4040404040L),

            new CharData('a', 0x2054545478L),
            new CharData('b', 0x3f48444438L),
            new CharData('c', 0x3844444420L),
            new CharData('d', 0x384444487fL),
            new CharData('e', 0x3854545418L),
            new CharData('f', 0x00087e0902L),
            new CharData('g', 0x085454543cL),
            new CharData('h', 0x7f08040478L),
            new CharData('i', 0x00447d4000L),
            new CharData('j', 0x2040403d00L),
            new CharData('k', 0x007f102844L),
            new CharData('l', 0x00417f4000L),
            new CharData('m', 0x7c04780478L),
            new CharData('n', 0x7c08040478L),
            new CharData('o', 0x3844444438L),
            new CharData('p', 0x7c14141408L),
            new CharData('q', 0x081414147cL),
            new CharData('r', 0x007c080404L),
            new CharData('s', 0x4854545424L),
            new CharData('t', 0x00043e4420L),
            new CharData('u', 0x3c4040207cL),
            new CharData('v', 0x1c2040201cL),
            new CharData('w', 0x3c4030403cL),
            new CharData('x', 0x4424384844L),
            new CharData('y', 0x444830100cL),
            new CharData('z', 0x4464544c44L),

            new CharData(0xe0, 0x1c2222221cL),  // Circle
            new CharData(0xe1, 0x3e2222223eL),  // Square
            new CharData(0xe2, 0x1018141810L),  // Triangle
            new CharData(0xe3, 0x2214081422L),  // Multiple
            new CharData(0xe4, 0x08082a0808L),  // Divide
            new CharData(0xe5, 0x1c4e7f4e1cL),  // Spade
            new CharData(0xe6, 0x0c1e3c1e0cL),  // Heart
            new CharData(0xe7, 0x081c3e1c08L),  // Dia
            new CharData(0xe8, 0x0c4d7f4d0cL),  // Clover

            new CharData(0xe9, 0xffffffffffL),  // Box
            new CharData(0xea, 0x0018180000L),  // Dot
            new CharData(0xeb, 0x0609090600L),  // Degree
            new CharData(0xec, 0x6355494163L),  // Sigma
            new CharData(0xed, 0x4e7101714eL),  // Omega
            new CharData(0xee, 0x7c10100c10L),  // Mu

            new CharData(0xf0, 0x7c54545400L),  // exponent
            new CharData(0xf1, 0x14161c3414L),  // <>
            new CharData(0xf2, 0x443c047c44L),  // pi
            new CharData(0xf3, 0x5058545250L),  // <=
            new CharData(0xf4, 0x5052545850L),  // >=
            new CharData(0xf5, 0x081c2a0808L),  // left allow
            new CharData(0xf6, 0x10207f2010L),  // down allow
            new CharData(0xf7, 0x08082a1c08L),  // right allow


            new CharData(0xff, 0xffffffffffL)
    };
}
