package tk.horiuchi.pokecom2;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tk.horiuchi.pokecom2.Common.cmdTable;
import static tk.horiuchi.pokecom2.Common.reservCodePB100;
import static tk.horiuchi.pokecom2.MainActivity.bankMax;
import static tk.horiuchi.pokecom2.MainActivity.source;

/**
 * Created by yoshimine on 2018/05/03.
 */

public class WaveOut {
    private byte[] mByteArray = null;
    private AudioTrack mAudioTrack = null;
    private ICallBack reference = null;
    private int linenum_top = 0;
    private int linenum_top0 = 0;
    private int linenum_top1 = 0;
    //private byte[] buf;


    private byte[] d1 = {
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
    };
    private byte[] d0 = {
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
    };

/*
    private byte[] d1 = {
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
    };
    private byte[] d0 = {
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
    };
*/
    private byte[] wdata;
    private int p = 0;
    private int progressCount;

    public WaveOut() {
        Log.w("WaveOut", "created");
    }

    // BASIC プログラムの保存
    public void savea() {
        byte[] buf = encode();
        int dl = 0x400*72 + 11*12*72 + 0x400*72 + buf.length*12*72;
        //int dl = 0x400*160 + 11*12*160 + 0x400*160 + buf.length*12*160;
        wdata = new byte[dl];
        p = 0;

        // header
        // lead in
        for (int i = 0; i < 0x400; i++ ){
            for (int j = 0; j < d1.length; j++) wdata[p++] = d1[j];
        }

        write1byte((byte)0xf7); // kind of data

        write1byte((byte)0x2f);    // filename(8 charactors)
        write1byte((byte)0x4e);
        write1byte((byte)0x4a);
        write1byte((byte)0x44);
        write1byte((byte)0x42);
        write1byte((byte)0x4e);
        write1byte((byte)0x4c);
        write1byte((byte)0);

        write1byte((byte)linenum_top0); // top of linenum
        write1byte((byte)linenum_top1); // top of linenum


        // body
        // lead in
        for (int i = 0; i < 0x400; i++ ){
            for (int j = 0; j < d1.length; j++) wdata[p++] = d1[j];
        }
        // program data
        for (int i = 0; i < buf.length; i++) {
            write1byte(buf[i]);
        }

        makeAudioTrack(wdata);
    }

    public byte[] encode() {
        final int BUFMAX = 3000;    // サイズは仮
        byte buf[] = new byte[BUFMAX];
        int l = 0;

        buf[l++] = 0x02;    // プログラム開始
        for (int i = 0; i < bankMax; i++) {
            String[] temp = source.getSourceAll(i);
            if (temp == null) {
                buf[l++] = (byte)0xe0;  // バンク区切りだけ書き込む
                Log.w("savea", "--- separate ---");
            } else {
                for (int j = 0; j < temp.length; j++) {
                    int linenum = 0;
                    String regex = "(?:^[0-9]+:?)|(?:\"[^\"]*\")|(?:[^ \"]+)";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(temp[j]);
                    ArrayList<String> data = new ArrayList<String>();
                    if (m.find()) {
                        do {
                            data.add(m.group());
                        } while (m.find());
                    }
                    for (String s : data) {
                        //Log.w("savea", String.format("this line ='%s'\n", s));

                        regex = "^\"";
                        p = Pattern.compile(regex);
                        m = p.matcher(s);
                        if (m.find()) {    // ダブルコートの文字列処理
                            if (linenum == 0) {
                                Log.w("savea", "--- skipped ---");
                                break;
                            }
                            Log.w("savea", String.format("'%s'\n", s));
                            for (int k = 0; k < s.length(); k++) {
                                char c = s.charAt(k);
                                if (isSpecialChar((byte)c)) {
                                    buf[l++] = getInternalCode(cmdTable[c]);
                                    //Log.w("---", String.format("%s\n", cmdTable[c]));
                                } else {
                                    buf[l++] = getInternalCode(String.valueOf(c));
                                }
                                //Log.w("-----", String.format("%02x\n", (int)c));
                                Log.w("savea", String.format("%02x\n", buf[l-1]));
                            }
                        } else {    // それ以外の処理
                            String[] token = s.split("[\\s]+|(?<=\\\\GE|\\\\LE|\\\\NE|\\\\UA)|(?=\\\\GE|\\\\LE|\\\\NE|\\\\UA)|(?<=[\\*\\/\\+\\-><=:;,\\(\\)])|(?=[\\*\\/\\+\\-><=:;,\\)])", 0);
                            for (int k = 0; k < token.length; k++) {
                                Log.w("savea", String.format("'%s'\n", token[k]));
                                if (linenum == 0) {
                                    if (k == 0) {
                                        try {
                                            int num = Integer.parseInt(token[0]);
                                            if (1 <= num && num <= 9999) {
                                                linenum = num;
                                                Log.w("savea", String.format("linenum=%d\n", linenum));

                                                if (k + 1 < token.length && token[1].equals(":")) {
                                                    k++;    // 行番号の次が:（コロン）の場合は読み捨て
                                                }
                                                int l0 = linenum % 100;
                                                l0 = l0 / 10 * 0x10 + l0 % 10;
                                                buf[l++] = (byte) l0;
                                                int l1 = linenum / 100;
                                                l1 = l1 / 10 * 0x10 + l1 % 10;
                                                buf[l++] = (byte) l1;
                                                if (linenum_top == 0) {
                                                    linenum_top = linenum;    // 先頭の行番号を覚えておく
                                                    linenum_top0 = l0;
                                                    linenum_top1 = l1;
                                                }
                                                Log.w("savea", String.format("%02x %02x\n", l0, l1));
                                            } else {
                                                Log.w("savea", "--- skipped ---");
                                                break;
                                            }
                                        } catch (NumberFormatException e) {
                                            Log.w("savea", "--- skipped ---");
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                } else {
                                    // 行番号以外の処理
                                    // 先頭に予約語がある場合は分離し、残りを内部コードに変換する
                                    String[] code = splitEx(token[k]);
                                    String tmp;
                                    if (code.length > 1) {
                                        buf[l++] = getReservFunction(code[0]);
                                        //Log.w("-----", String.format("%s\n", code[0]));
                                        Log.w("savea", String.format("%02x\n", buf[l-1]));
                                        //if (code[0].equals(""))
                                        tmp = code[1];
                                    } else {
                                        tmp = code[0];
                                    }
                                    for (int n = 0; n < tmp.length(); n++) {
                                        char c = tmp.charAt(n);
                                        if (isSpecialChar((byte)c)) {
                                            buf[l++] = getInternalCode(cmdTable[c]);
                                            //Log.w("---", String.format("%s\n", cmdTable[c]));
                                        } else {
                                            if (c == ':') {
                                                buf[l++] = (byte)0xfe;		// statement delimiter
                                            } else {
                                                buf[l++] = getInternalCode(String.valueOf(c));
                                            }
                                        }
                                        Log.w("savea", String.format("%02x\n", buf[l-1]));
                                    }

                                }
                            }
                        }
                        //Log.w("savea", String.format("'%s'\n", s));

                    }
                    buf[l++] = (byte)0xff;  // 行区切り
                    Log.w("savea", "--- EOL ---");
                }
                buf[l++] = (byte)0xe0;  // バンク区切り
                Log.w("savea", "--- separate ---");
            }
        }
        buf[l++] = (byte)0xf0;    // プログラム終了
        buf[l++] = (byte)0x00;    // エンドマーク

        /*
        Log.w("savea", "--- dump ---");
        for (int i = 0; i < l; i++) {
            Log.w("savea", String.format("%02x\n", buf[i]));
        }
        */

        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = buf[i];
        }
        return (ret);
    }

    private byte getInternalCode(String s) {
        for (int i = 0x00; i < 0x7f; i++) {
            if (reservCodePB100[i].equals(s)) {
                return((byte)i);
            }
        }
        return 0;
    }
    private byte getReservFunction(String s) {
        for (int i = 0x80; i < 0xcf; i++) {
            if (reservCodePB100[i].equals(s)) {
                return((byte)i);
            }
        }
        return 0;
    }

    private String[] splitEx(String s) {
        // 文字列の先頭に予約語があったら分離する
        for (int i = 0x80; i < 0xcf; i++) {
            Pattern p = Pattern.compile("^"+reservCodePB100[i]);
            Matcher m = p.matcher(s);
            if (m.find()) {
                String[] ret = new String[2];
                ret[0] = reservCodePB100[i];
                ret[1] = s.replace(m.group(), "");
                //Log.w("-----", String.format("'%s'\n", ret[0]));
                //Log.w("-----", String.format("'%s'\n", ret[1]));
                return(ret);
            }
        }
        String[] ret = new String[1];
        ret[0] = s;
        return(ret);
    }

    private boolean isSpecialChar(byte c) {
        //Log.w("isSpecialChar", ""+c);
        int cc = c & 0xff;
        return (0xe0 <= cc && cc <= 0xff ? true : false);
    }

    private void makeAudioTrack(byte [] in) {
        // byte配列を生成し、音データを読み込む
        mByteArray = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            mByteArray[i] = in[i];
        }

        // AudioTrackを生成する
        // (22kHz、モノラル、8bitの音声データ)
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                22050,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT,
                in.length,
                AudioTrack.MODE_STATIC);

        progressCount = 13;
        mAudioTrack.setPositionNotificationPeriod(in.length / progressCount);
        mAudioTrack.setPlaybackPositionUpdateListener(
                new AudioTrack.OnPlaybackPositionUpdateListener() {
                    public void onPeriodicNotification(AudioTrack track) {
                        complete(--progressCount);
                        //Log.w("------", String.format("%d\n", progressCount));
                    }
                    // 再生完了時のコールバック
                    public void onMarkerReached(AudioTrack track) {
                        if(track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
                            //Log.w("------", String.format("onMarkerReached()\n"));
                            track.stop();   // 再生完了したので再生停止
                            track.release();
                            //mAudioTrack.release();  // インスタンス解放
                            complete(0); // コールバック関数を呼ぶ
                        }
                    }
                }
        );

        // 音声データを書き込む
        mAudioTrack.write(mByteArray, 0, mByteArray.length);
        mAudioTrack.setNotificationMarkerPosition(mByteArray.length);
        // 再生開始
        mAudioTrack.play();

    }

    public void stop() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
        }
    }

    interface ICallBack {
        public void playComplete(int n);
    }

    public void setReference(ICallBack arg) {
        reference = arg;
    }

    public void complete(int n) {
        if (reference != null) {
            reference.playComplete(n);
        }
    }

    private void write1byte(byte c) {
        Log.w("w", String.format("%02x\n", c));
        write1bit(0);   // start bit
        write1bit((c & 0x01) != 0 ? 1 : 0);
        write1bit((c & 0x02) != 0 ? 1 : 0);
        write1bit((c & 0x04) != 0 ? 1 : 0);
        write1bit((c & 0x08) != 0 ? 1 : 0);
        write1bit((c & 0x10) != 0 ? 1 : 0);
        write1bit((c & 0x20) != 0 ? 1 : 0);
        write1bit((c & 0x40) != 0 ? 1 : 0);
        write1bit((c & 0x80) != 0 ? 1 : 0);
        write1bit(parity(c));   // parity
        write1bit(1);write1bit(1);   // stop bit
    }

    private void write1bit(int b) {
        if (b != 0) {
            for (int i = 0; i < d1.length; i++) wdata[p++] = d1[i];
        } else {
            for (int i = 0; i < d0.length; i++) wdata[p++] = d0[i];
        }
    }

    private byte parity(byte c) {
        int p = 0;
        int val = c & 0xff;
        for (int i = 0; i < 8; i++) {
            if ((val & 0x01) != 0) {
                p++;
            }
            val >>= 1;
        }
        return ((byte)(p & 0x01));

    }
}
