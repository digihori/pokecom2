package tk.horiuchi.pokecom2;

import android.util.Log;

import static tk.horiuchi.pokecom2.MainActivity.keyExt;
import static tk.horiuchi.pokecom2.MainActivity.keyFunc;
import static tk.horiuchi.pokecom2.MainActivity.keyShift;
import static tk.horiuchi.pokecom2.MainActivity.mBtnResIds;
import static tk.horiuchi.pokecom2.MainActivity.mBtnStatus;


/**
 * Created by yoshimine on 2017/11/18.
 */

public class Keyboard {
    public Keyboard() {
        //
    }

    public int getKeyCode(int x) {
        int index = 0;
        if (keyExt) {
            if (keyShift) {
                index = 3;
            } else {
                index = 2;
            }
        } else {
            if (keyFunc) {
                index = 4;
            } else if (keyShift) {
                index = 1;
            } else {
                index = 0;
            }
        }

        for (int i = 0; i < keyTable.length; i++) {
            if (keyTable[i].keyCode == x) {
                Log.w("Keyboard", String.format("getKeyCode=%x", keyTable[i].charCode[index]));
                return (keyTable[i].charCode[index]);
            }
        }
        return 0;
    }

    public int getPressKeyCode() {
        int btnId = 0;
        for (int idx = 0; idx < mBtnStatus.length; idx++) {
            if (mBtnStatus[idx]) {
                btnId = mBtnResIds[idx];
                break;
            }
        }
        for (int i = 0; i < keyTable.length; i++) {
            if (keyTable[i].keyCode == btnId) {
                //Log.w("Keyboard", String.format("getKeyCode=%x", keyTable[i].charCode[0]));
                return (keyTable[i].charCode[0]);
            }
        }
        return 0;
    }

    private final class KeyTable {
        private int keyCode;
        private int[] charCode;
        //private int charCode0;  // normal
        //private int charCode1;  // shift
        //private int charCode2;  // ext
        //private int charCode3;  // ext-shift
        //private int charCode4;  // function

        public KeyTable(int k, int c0, int c1, int c2, int c3, int c4) {
            keyCode = k;
            charCode = new int[5];
            charCode[0] = c0;
            charCode[1] = c1;
            charCode[2] = c2;
            charCode[3] = c3;
            charCode[4] = c4;
        }
    }

    private final KeyTable[] keyTable = {
            new KeyTable(R.id.buttonQ, 'Q', '!',  'q', '%',  0),
            new KeyTable(R.id.buttonW, 'W', '\"', 'w', '\'', 0),
            new KeyTable(R.id.buttonE, 'E', '#',  'e', '@',  0),
            new KeyTable(R.id.buttonR, 'R', '$',  'r', '\\', 0),
            new KeyTable(R.id.buttonT, 'T', '(',  't', '[',  0),
            new KeyTable(R.id.buttonY, 'Y', ')',  'y', ']',  0),
            new KeyTable(R.id.buttonU, 'U', '?',  'u', '&',  0),
            new KeyTable(R.id.buttonI, 'I', ':',  'i', 0xf5, 0),
            new KeyTable(R.id.buttonO, 'O', ';',  'o', 0xf6, 0),
            new KeyTable(R.id.buttonP, 'P', ',',  'p', 0xf7, 0),

            new KeyTable(R.id.buttonA, 'A', 0x95, 'a', 0xe0, 0xd0),
            new KeyTable(R.id.buttonS, 'S', 0x99, 's', 0xe1, 0xd1),
            new KeyTable(R.id.buttonD, 'D', 0x9a, 'd', 0xe2, 0xd2),
            new KeyTable(R.id.buttonF, 'F', 0x9b, 'f', 0xe3, 0xd3),
            new KeyTable(R.id.buttonG, 'G', 0x9c, 'g', 0xe4, 0xd4),
            new KeyTable(R.id.buttonH, 'H', 0x94, 'h', 0xe5, 0xd5),
            new KeyTable(R.id.buttonJ, 'J', 0x96, 'j', 0xe6, 0xd6),
            new KeyTable(R.id.buttonK, 'K', 0x97, 'k', 0xe7, 0xd7),
            new KeyTable(R.id.buttonL, 'L', 0x92, 'l', 0xe8, 0xd8),
            new KeyTable(R.id.buttonANS, 0x19, 0x19, 0x19, 0x19, 0),

            new KeyTable(R.id.buttonZ, 'Z', 0x98, 'z', 0xe9, 0xd9),
            new KeyTable(R.id.buttonX, 'X', 0x9d, 'x', 0xea, 0xda),
            new KeyTable(R.id.buttonC, 'C', 0x9e, 'c', 0xeb, 0xdb),
            new KeyTable(R.id.buttonV, 'V', 0xb4, 'v', '_',  0xdc),
            new KeyTable(R.id.buttonB, 'B', 0xa2, 'b', 0xec, 0xdd),
            new KeyTable(R.id.buttonN, 'N', 0xa1, 'n', 0xed, 0xb3),
            new KeyTable(R.id.buttonM, 'M', 0x90, 'm', 0xee, 0x93),
            new KeyTable(R.id.buttonSPC, ' ',  ' ',  ' ',   ' ',  0),
            new KeyTable(R.id.buttonEQ,  '=',  0xf1, '=',   0xf1, 0),
            new KeyTable(R.id.buttonEXP, 0xf0, 0xf2,  0xf0, 0xf2, 0),

            new KeyTable(R.id.button0, '0', 0x80, '0', 0x80, 0),
            new KeyTable(R.id.button1, '1', 0x81, '1', 0x81, 0),
            new KeyTable(R.id.button2, '2', 0x82, '2', 0x82, 0),
            new KeyTable(R.id.button3, '3', 0x83, '3', 0x83, 0),
            new KeyTable(R.id.button4, '4', 0x84, '4', 0x84, 0),
            new KeyTable(R.id.button5, '5', 0x85, '5', 0x85, 0),
            new KeyTable(R.id.button6, '6', 0x86, '6', 0x86, 0),
            new KeyTable(R.id.button7, '7', 0x87, '7', 0x87, 0),
            new KeyTable(R.id.button8, '8', 0x88, '8', 0x88, 0),
            new KeyTable(R.id.button9, '9', 0x89, '9', 0x89, 0),
            new KeyTable(R.id.buttonDOT,   '.', '^',  0,   0,    0),
            new KeyTable(R.id.buttonPLS,   '+', 0xf4, '+', 0xf4, 0),
            new KeyTable(R.id.buttonMINUS, '-', 0xf3, '-', 0xf3, 0),
            new KeyTable(R.id.buttonMLT,   '*', '>',  '*', '>',  0),
            new KeyTable(R.id.buttonDIV,   '/', '<',  '/', '<',  0),
            new KeyTable(R.id.buttonAC,   0x1a, 0, 0, 0, 0),
            new KeyTable(R.id.buttonEXE,  0x1b, 0x1b, 0x1b, 0x1b, 0x1b),
            new KeyTable(R.id.buttonSTOP, 0x1c, 0, 0, 0, 0),
            new KeyTable(R.id.buttonDEL,  0x1d, 0x1e, 0, 0, 0),
            new KeyTable(R.id.buttonLA,   0x10, 0x10, 0x10, 0x10, 0),
            new KeyTable(R.id.buttonRA,   0x11, 0x11, 0x11, 0x11, 0),
            new KeyTable(R.id.buttonDA,   0x12, 0x12, 0x12, 0x12, 0),
            new KeyTable(R.id.buttonUA,   0x13, 0x13, 0x13, 0x13, 0),

            new KeyTable(0, 0, 0, 0, 0, 0)
    };

}
