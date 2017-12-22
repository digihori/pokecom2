package tk.horiuchi.pokecom2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tk.horiuchi.pokecom2.Common.MODE_PRO;
import static tk.horiuchi.pokecom2.Common.MODE_RUN;
import static tk.horiuchi.pokecom2.Common.cmdTable;
import static tk.horiuchi.pokecom2.Common.type10inch;
import static tk.horiuchi.pokecom2.Common.type7inch;
import static tk.horiuchi.pokecom2.Common.typePhone;

public class MainActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static float dpdx, dpdx_org;
    public static int deviceType;
    public final static String src_path = Environment.getExternalStorageDirectory().getPath()+"/pokecom2";
    //private Common define;
    public static PbMain pb = null;
    public static Keyboard inkey = null;
    public static Lcd lcd = null;
    public static SBasic basic = null;
    public static SourceFile source = null;
    public static boolean keyShift = false;
    public static boolean keyExt = false;
    public static boolean keyMode = false;
    public static boolean keyFunc = false;
    public static boolean vibrate_enable, debug_info;
    public static boolean cpuClockEmulateEnable = true;
    private Vibrator vib;

    public static boolean initial; // 仮
    public static boolean bankStatus = false;
    public static int mode = MODE_RUN;
    public static boolean selectBank = false;
    public static int bank;
    public static final int progLength = 2000;  // 仮
    public static final int bankMax = 10;


    public static int[] mBtnResIds = {
            R.id.buttonDA, R.id.buttonUA, R.id.buttonMODE, R.id.buttonLA, R.id.buttonRA,
            R.id.buttonSHIFT, R.id.buttonFUNC,

            R.id.buttonQ, R.id.buttonW, R.id.buttonE, R.id.buttonR, R.id.buttonT,
            R.id.buttonY, R.id.buttonU, R.id.buttonI, R.id.buttonO, R.id.buttonP,

            R.id.buttonA, R.id.buttonS, R.id.buttonD, R.id.buttonF, R.id.buttonG,
            R.id.buttonH, R.id.buttonJ, R.id.buttonK, R.id.buttonL, R.id.buttonANS,

            R.id.buttonZ, R.id.buttonX, R.id.buttonC, R.id.buttonV, R.id.buttonB,
            R.id.buttonN, R.id.buttonM, R.id.buttonSPC, R.id.buttonEQ, R.id.buttonEXP,

            R.id.buttonAC, R.id.buttonDEL, R.id.buttonSTOP, R.id.buttonDIV,
            R.id.button7, R.id.button8, R.id.button9, R.id.buttonMLT,
            R.id.button4, R.id.button5, R.id.button6, R.id.buttonMINUS,
            R.id.button1, R.id.button2, R.id.button3, R.id.buttonPLS,
            R.id.button0, R.id.buttonDOT, R.id.buttonEXE
    };

    public static Boolean[] mBtnStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_main);

        // 共通定義
        //define = new Common();

        // ファイルIOのパーミッション関係の設定
        verifyStoragePermissions(this);

        // ディレクトリがなければ作成する
        File dir = new File(src_path);
        if (!dir.exists()) {
            dir.mkdirs();
            Log.w("Main", "mkdir");
        }


        // Buttonインスタンスの取得
        // ButtonインスタンスのリスナーをこのActivityクラスそのものにする
        for (int i = 0; i < mBtnResIds.length; i++) {
            findViewById(mBtnResIds[i]).setOnClickListener(this);
        }

        // dp->px変換のためにDisplayMetricsを取得しておく
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dpdx_org = dpdx = metrics.density;
        Log.w("Main", String.format("widthPixels=%d\n", metrics.widthPixels));
        Log.w("Main", String.format("heightPixels=%d\n", metrics.heightPixels));
        Log.w("Main", String.format("Xdpi=%f\n", metrics.xdpi));
        Log.w("Main", String.format("Ydpi=%f\n", metrics.ydpi));
        Log.w("Main", String.format("density=%f\n", metrics.density));
        Log.w("Main", String.format("densityDpi=%d\n", metrics.densityDpi));
        Log.w("Main", String.format("scaledDensity=%f\n", metrics.scaledDensity));
        // デバイスタイプとスケールの設定
        if (getResources().getBoolean(R.bool.is_7inch)) {
            deviceType = type7inch;
            if (1.3f < dpdx_org && dpdx_org< 1.4f) {
                // nexus7(2012) tvdpi の時はスケール２倍
                dpdx = 2f;
            } else {
                // それ以外（多分xhdpiしかない？）の時はスケール３倍
                dpdx = 3f;
            }
            Log.w("Main", String.format("deviceType=7inch tablet(%d) scale=%f\n", deviceType, dpdx));
        } else if (getResources().getBoolean(R.bool.is_10inch)) {
            deviceType = type10inch;
            dpdx = 4f;
            Log.w("Main", String.format("deviceType=10inch tablet(%d) scale=%f\n", deviceType, dpdx));
        } else {
            deviceType = typePhone;
            if (dpdx_org == 1.5) {
                // hdpiの時は少し小さめにする
                dpdx = 1.3f;
            }
            Log.w("Main", String.format("deviceType=phone(%d) scale=%f\n", deviceType, dpdx));
        }

        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);

        pb = new PbMain(this, sv);

        inkey = new Keyboard();
        lcd = new Lcd();
        lcd.setListener(pb);


        // デフォルトの設定
        PreferenceManager.setDefaultValues(this, R.xml.preference, true);

        // 設定値をロード
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        debug_info = sp.getBoolean("debug_checkbox_key", false);
        vibrate_enable = sp.getBoolean("vibrator_checkbox_key", true);

        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Buttonインスタンスの取得
        // ButtonインスタンスのリスナーをこのActivityクラスそのものにする
        mBtnStatus = new Boolean[mBtnResIds.length];
        for (int i = 0; i < mBtnResIds.length; i++) {
            mBtnStatus[i] = false;
            findViewById(mBtnResIds[i]).setOnClickListener(this);
            findViewById(mBtnResIds[i]).setOnTouchListener(this);
        }
        // ボタンの枠表示を切り替える
        changeButtonFrame(mBtnResIds, debug_info);

        // 仮
        lcd.print("READY P0");
        initial = true;

        calcMemoryAndDisp(false);
        listDisp = false;

        source = new SourceFile();
        try {
            basic = new SBasic(lcd);
            //Log.w("Main", "----- prog completed!!! -----");
        } catch (InterpreterException e) {
            Log.w("Main", String.format("error='%s'", e.toString()));
        }

    }

    private static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        stretchItemSize((GridLayout)findViewById(R.id.keyAreaMainkey), (ImageView)findViewById(R.id.imageViewMainkey));
        stretchItemSize((GridLayout)findViewById(R.id.keyAreaTenkey), (ImageView)findViewById(R.id.imageViewTenkey), 20);

    }

    public Point getBitmapSizeFromDynamicImageLayer(ImageView imageLayer) {
        final int actualHeight, actualWidth;
        final int imageLayerHeight = imageLayer.getHeight(), imageLayerWidth = imageLayer.getWidth();
        //Log.w("LOG", "imageLayerWidth="+imageLayerWidth+" imageLayerHeight="+imageLayerHeight);
        final int bitmapHeight = imageLayer.getDrawable().getIntrinsicHeight(), bitmapWidth = imageLayer.getDrawable().getIntrinsicWidth();
        //Log.w("LOG", "bitmapWidth="+bitmapWidth+" bitmapHeight="+bitmapHeight);

        if (imageLayerHeight * bitmapWidth <= imageLayerWidth * bitmapHeight) {
            actualWidth = bitmapWidth * imageLayerHeight / bitmapHeight;
            actualHeight = imageLayerHeight;
        } else {
            actualHeight = bitmapHeight * imageLayerWidth / bitmapWidth;
            actualWidth = imageLayerWidth;
        }
        //Log.w("LOG", "x="+actualWidth+" y="+actualHeight);
        return new Point(actualWidth, actualHeight);
    }

    public void stretchItemSize(GridLayout gl, ImageView iv) {
        Point p = getBitmapSizeFromDynamicImageLayer(iv);
        Log.w("LOG", "p.x="+p.x+" p.y="+p.y);

        int childWidth = p.x / gl.getColumnCount();
        int childHeight = p.y / gl.getRowCount();

        Log.w("LOG", "iv w="+iv.getWidth()+" h="+iv.getHeight());
        Log.w("LOG", "column="+gl.getColumnCount()+" row="+gl.getRowCount());
        Log.w("LOG", "w="+childWidth+" h="+childHeight);
        for (int i = 0; i < gl.getChildCount(); i++) {
            gl.getChildAt(i).setMinimumWidth(childWidth);
            gl.getChildAt(i).setMinimumHeight(childHeight);
        }
    }
    public void stretchItemSize(GridLayout gl, ImageView iv, int x) {
        Point p = getBitmapSizeFromDynamicImageLayer(iv);
        Log.w("LOG", "p.x="+p.x+" p.y="+p.y);

        int childWidth = p.x / gl.getColumnCount();
        int childHeight = p.y / gl.getRowCount();

        Log.w("LOG", "iv w="+iv.getWidth()+" h="+iv.getHeight());
        Log.w("LOG", "column="+gl.getColumnCount()+" row="+gl.getRowCount());
        Log.w("LOG", "w="+childWidth+" h="+childHeight);
        for (int i = 0; i < gl.getChildCount(); i++) {
            if (i == x) {
                gl.getChildAt(i).setMinimumWidth(childWidth * 2);
                gl.getChildAt(i).setMinimumHeight(childHeight);
            } else {
                gl.getChildAt(i).setMinimumWidth(childWidth);
                gl.getChildAt(i).setMinimumHeight(childHeight);
            }
        }
    }

    // ボタンのリソースファイルを更新する処理
    // 位置調整用のボタン枠表示の切り替え
    protected void changeButtonFrame(int[] ids, boolean flg) {

        for (int i = 0; i < ids.length; i++) {
            if (flg) {
                findViewById(ids[i]).setBackgroundResource(R.drawable.button_debug);
            } else {
                findViewById(ids[i]).setBackgroundResource(R.drawable.button);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ボタン表示枠の更新
        if (debug_info) {
            changeButtonFrame(mBtnResIds, true);
        } else {
            changeButtonFrame(mBtnResIds, false);
        }
    }

    public static boolean listDisp = false;

    public void onClick(View v) {
        int c = v.getId();

        Log.w("Click", String.format("key=%d", c));

        if (c != R.id.buttonSTOP && pb.isProgRunning()) return;

        // モードの切り替え
        boolean modeChange = true;
        switch (c) {
            case R.id.buttonMODE:
                keyMode = !keyMode;
                break;
            case R.id.buttonSHIFT:
                keyShift = !keyShift;
                break;
            case R.id.buttonFUNC:
                keyFunc = !keyFunc;
                break;
            case R.id.buttonDOT:
                if (keyMode) {
                    keyMode = false;
                    keyExt = !keyExt;
                } else {
                    modeChange = false;
                }
                break;
            case R.id.button0:
                if (keyMode) {
                    keyMode = false;
                    mode = MODE_RUN;
                    calcMemoryAndDisp(false);
                    lcd.cls();
                    lcd.print(String.format("READY P%d", bank));
                    initial = true;
                } else {
                    modeChange = false;
                }
                break;
            case R.id.button1:
                if (keyMode) {
                    keyMode = false;
                    mode = MODE_PRO;
                    calcMemoryAndDisp(true);
                    lcd.printBankStatus();
                    initial = true;
                    listDisp = false;
                } else {
                    modeChange = false;
                }
                break;
            default:
                modeChange = false;
                break;
        }
        if (modeChange) {
            lcd.refresh();  // 表示の更新
            return; // モード切り替えのキー入力はこれ以降の処理をしない
        }

        // 通常のキー入力処理
        if (mode == MODE_RUN) {
            switch (c) {
                case R.id.buttonEXE:
                    if (pb.isProgStop()) {
                        lcd.putchar(inkey.getKeyCode(c));
                    } else {
                        String s = lcd.getCmdBuf();
                        if (s == null) break;
                        try {
                            Log.w("EXE", String.format("%s", s));
                            basic.calc(lcd.getCmdBuf());
                        } catch (InterpreterException e) {
                            Log.w("Main", String.format("error='%s'", e.toString()));
                        }
                        initial = true;
                    }
                    break;
                case R.id.buttonANS:
                    basic.lastAns();
                    break;
                default:
                    int code = inkey.getKeyCode(c);
                    if (code != 0) lcd.putchar(code);
                    break;
            }

        } else if (mode == MODE_PRO) {
            String s;
            switch (c) {
                case R.id.buttonDA:
                    if (listDisp) {
                        s = source.getSourceNext(bank);
                        listDisp = true;
                        Log.w("Main", "listNext");
                    } else {
                        s = source.getSourceTop(bank);
                        listDisp = true;
                        Log.w("Main", "listTop");
                    }
                    if (s != null) {
                        lcd.printSourceList(s);
                        //initial = true;
                    }
                    break;
                case R.id.buttonUA:
                    if (listDisp) {
                        s = source.getSourcePrev(bank);
                        listDisp = true;
                        Log.w("Main", "listPrev");
                    } else {
                        s = source.getSourceBottom(bank);
                        listDisp = true;
                        Log.w("Main", "listBottom");
                    }
                    if (s != null) {
                        lcd.printSourceList(s);
                        //initial = true;
                    }
                    break;

                case R.id.buttonEXE:
                    s = lcd.getCmdBuf();
                    if (s == null) break;

                    String[] temp = s.split("\\s+");
                    if (temp[0].equals("LIST")) {
                        String ss;
                        if (temp.length == 1) {
                            ss = source.getSourceTop(bank);
                        } else {
                            int num = Integer.parseInt(temp[1]);
                            ss = source.getSource(bank, num);
                        }
                        listDisp = true;
                        if (ss != null) lcd.printSourceList(ss);
                    } else if (temp[0].equals("CLEAR")) {
                        if (temp.length == 1) {
                            source.clearSource(bank);
                        } else if (temp[1].equals("A")) {
                            source.clearSourceAll();
                        } else {
                            ;
                        }
                        lcd.cls();
                        calcMemoryAndDisp(true);
                    } else {
                        // プログラムの入力確定処理
                        int ret = source.addSource(bank, s);
                        if (ret == 0) {
                            // 新規の行の追加の場合はそのまま内容を表示する
                            initial = true;
                            lcd.printSourceList(s);
                            initial = true;
                        } else if (ret == 1) {
                            // 行の更新の場合は次の行を表示する
                            s = source.getSourceNext(bank);
                            listDisp = true;
                            if (s != null) {
                                lcd.printSourceList(s);
                            }
                        } else if (ret == -1) {
                            // 行の削除の場合は表示クリア
                            lcd.cls();
                        }
                        //lcd.cls();
                        calcMemoryAndDisp(true);
                    }
                    break;
                case R.id.buttonANS:
                    break;
                default:
                    int code = inkey.getKeyCode(c);
                    if (code != 0) lcd.putchar(code);
                    break;
            }

        }
        keyShift = false;
        keyFunc = false;


        if (vibrate_enable) {
            vib.vibrate(10);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionsMenu_01:
                //nosave = true;
                actLoad();
                return true;
            case R.id.optionsMenu_02:
                //nosave = true;
                actSave();
                return true;
            case R.id.optionsMenu_03:   // reset
                //modeInit();
                return true;
            case R.id.optionsMenu_04:   // settings
                //nosave = true;
                Intent intent1 = new android.content.Intent(this, MyPreferenceActivity.class);
                startActivity(intent1);
                return true;
            case R.id.optionsMenu_05:   // help
                //Toast.makeText(this, "未実装だよ！", Toast.LENGTH_LONG).show();
                // ボタンをタップした際の処理を記述
                AboutDialogFragment dialog = new AboutDialogFragment();
                dialog.show(getFragmentManager(), "");
                return true;
            case R.id.optionsMenu_06:   // exit
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // BASICプログラムの読み込み
    protected int load(String filename) {
        int ret = 0;
        int b = 0;
        int[] idx = new int[bankMax];

        char[][] ch = new char[progLength][bankMax];
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(filename);

            byte buf[] = new byte[progLength];
            int len = 0, x;
            while ((x = fis.read(buf)) != -1) {
                len += x;
            }

            int r = 0, w = 0, c = 0;
            int[] dest = new int[100];
            String str = "";

            b = 0;
            while (r < len) {
                // 1行読み込み
                str = "";
                w = 0;
                while ((c = buf[r++]) != '\n') {
                    if (c == '\r') continue;    // \rは読み捨てる
                    str += String.format("%c", c);
                    if (r >= len) break;
                }
                int llen = str.length();
                //Log.w("LOG", String.format("%s", str));

                // バンク指定のフィールドかどうかチェックする
                String regex = "\\[P[0-9]\\]";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(str);
                if (m.find()) {
                    //Log.w("AAAAAA", String.format("'%s'", m.group()));
                    //Log.w("AAAAAA", String.format("'%s'", m.group().substring(2,3)));
                    b = Integer.parseInt(m.group().substring(2, 3));
                    //idxEnd[bank] = 0;
                    w = 0;
                    idx[b] = 0;
                    continue;
                }


                if (llen != 0) {    // 空行は読み捨てる
                    for (int i = 0; i < llen; i++) {
                        c = str.charAt(i);
                        if (c == '\\') {
                            if (i + 2 < llen) {
                                String s = str.substring(i, i + 3);
                                //Log.w("LOG", String.format("s='%s'(%d) %02x %02x %02x", s, s.length(), (int)str.charAt(i), (int)str.charAt(i+1), (int)str.charAt(i+2)));
                                //Log.w("LOG", String.format("s='%s'", s));
                                for (int j = 0xe0; j <= 0xff; j++) {
                                    //Log.w("LOG", String.format("j=%02x='%s'(%d)", j, cmdTable[j], cmdTable[j].length()));
                                    if (cmdTable[j].equals(s)) {
                                        dest[w++] = j;  // エスケープ文字を内部コードに変換する
                                        //Log.w("LOG", String.format("s='%s'(%d)=%x", cmdTable[j], cmdTable[j].length(), j));
                                    }
                                }
                                i += 2;
                            } else {
                                // 無効なコードなので読み捨てる
                            }
                        } else {
                            dest[w++] = c;
                        }
                    }
                    dest[w++] = '\n';
                }
                for (int i = 0; i < w; i++, idx[b]++) {
                    ch[idx[b]][b] = (char)dest[i];
                }
            }

            Log.w("LOAD", String.format("file=%s length=%04x", filename, len));
        } catch (IOException e) {
            Log.d("LOAD", e.toString());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // ロードしたサイズを計算する
        for (int i = 0; i < bankMax; i++) {
            ret += idx[i];
        }

        // プログラムがロードされているバンクの最小値をbankにセットする
        bank = 0;
        for (int i = 0; i < bankMax; i++) {
            if (idx[i] != 0) {
                bank = i;
                break;
            }
        }

        for (int i = 0; i < bankMax; i++) {
            if (idx[i] == 0) continue;
            char[] charArray = new char[idx[i]];
            for (int j = 0; j < idx[i]; j++) {
                charArray[j] = ch[j][i];
            }
            String str1 = String.valueOf(charArray);
            String str2[] = str1.split("\\n", 0);

            source.loadSource(i, str2);
        }

        return ret;
    }

    // BASIC プログラムの保存
    protected void save(String filename) {
        FileOutputStream fos = null;

        try {
            File dir = new File(src_path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fos = new FileOutputStream(filename);
            byte buf[] = new byte[progLength];

            int l = 0;
            for (int i = 0; i < bankMax; i++) {
                String[] temp = source.getSourceAll(i);
                if (temp != null) {
                    String s = String.format("[P%d]", i);
                    for (int n = 0; n < s.length(); n++) {
                        buf[l++] = (byte)s.charAt(n);
                    }
                    buf[l++] = '\n';

                    for (int j = 0; j < temp.length; j++) {
                        for (int k = 0; k < temp[j].length(); k++) {
                            char c = temp[j].charAt(k);
                            if (0xe0 <= c && c <= 0xff) {
                                for (int n = 0; n < cmdTable[c].length(); n++) {
                                    buf[l++] = (byte) cmdTable[c].charAt(n);
                                }
                            } else {
                                buf[l++] = (byte)c;
                            }
                        }
                        buf[l++] = '\n';
                    }
                    buf[l++] = '\n';
                }
            }
            fos.write(buf, 0, l);
            fos.flush();

        } catch (IOException e) {
            Log.d("MainActivity", e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void actLoad(){
        Intent intent = new android.content.Intent(getApplication(), FileLoad.class);
        startActivityForResult(intent, 0);
    }

    public void actSave() {
        Intent intent = new android.content.Intent(getApplication(), FileSave.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.w("LOG", "onActivityResult.");

        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    String txt1 = data.getExtras().getString("filePath");
                    if (txt1 != null) {
                        Log.w("LOG", "path="+txt1);
                        int len = load(txt1);
                        Toast.makeText(this, "loaded:"+txt1+"("+len+")", Toast.LENGTH_LONG).show();
                    } else {
                        // エラー
                        Log.w("MainAct", "load error!");
                    }

                } else {
                    // 何もしない
                    Log.w("MainAct", "ファイル読み出しがキャンセルされた");
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    String txt1 = data.getExtras().getString("filePath");
                    if (txt1 != null) {
                        txt1 = src_path + "/" + txt1; // オープンするファイルのパスとファイル名
                        Log.w("LOG", "path=" + txt1);
                        save(txt1);
                        Toast.makeText(this, "saved:"+txt1, Toast.LENGTH_LONG).show();
                    } else {
                        // エラー
                        Log.w("MainAct", "save error!");
                    }
                } else {
                    Log.w("MainAct", "ファイル保存がキャンセルされた");
                }
                break;
            default:
                break;
        }

    }

    private void calcMemoryAndDisp(boolean disp) {
        if (!disp) {
            ((ImageView)findViewById(R.id.d1)).setImageResource(R.drawable.d10);
            ((ImageView)findViewById(R.id.d2)).setImageResource(R.drawable.d00);
            ((ImageView)findViewById(R.id.d3)).setImageResource(R.drawable.d00);
            ((ImageView)findViewById(R.id.d4)).setImageResource(R.drawable.d00);
            return;
        }

        int total = 0, remain = 544+1024;
        total = source.getUsedMemorySize();
        //for (int i = 0; i < 10; i++) {
        //    total += idxEnd[i];
        //}
        remain -= total;
        if (remain < 0) remain = 0;

        int digit1000 = remain / 1000;
        int digit100 = (remain - digit1000 * 1000) / 100;
        int digit10 = (remain - digit1000 * 1000 - digit100 * 100) / 10;
        int digit1 = remain - digit1000 * 1000 - digit100 * 100 - digit10 * 10;

        int res;
        ImageView iv = (ImageView)findViewById(R.id.d1);
        switch (digit1000) {
            default:
            case 0: res = R.drawable.d10; break;
            case 1: res = R.drawable.d11; break;
        }
        iv.setImageResource(res);

        iv = (ImageView)findViewById(R.id.d2);
        switch (digit100) {
            default:
            case 0:
                if (digit1000 != 0) {
                    res = R.drawable.d0;
                } else {
                    res = R.drawable.d00;
                }
                break;
            case 1: res = R.drawable.d1; break;
            case 2: res = R.drawable.d2; break;
            case 3: res = R.drawable.d3; break;
            case 4: res = R.drawable.d4; break;
            case 5: res = R.drawable.d5; break;
            case 6: res = R.drawable.d6; break;
            case 7: res = R.drawable.d7; break;
            case 8: res = R.drawable.d8; break;
            case 9: res = R.drawable.d9; break;
        }
        iv.setImageResource(res);

        iv = (ImageView)findViewById(R.id.d3);
        switch (digit10) {
            default:
            case 0:
                if (digit1000 != 0 && digit100 != 0) {
                    res = R.drawable.d0;
                } else {
                    res = R.drawable.d00;
                }
                break;
            case 1: res = R.drawable.d1; break;
            case 2: res = R.drawable.d2; break;
            case 3: res = R.drawable.d3; break;
            case 4: res = R.drawable.d4; break;
            case 5: res = R.drawable.d5; break;
            case 6: res = R.drawable.d6; break;
            case 7: res = R.drawable.d7; break;
            case 8: res = R.drawable.d8; break;
            case 9: res = R.drawable.d9; break;
        }
        iv.setImageResource(res);

        iv = (ImageView)findViewById(R.id.d4);
        switch (digit1) {
            default:
            case 0:
                if (digit1000 != 0 && digit100 != 0 && digit10 != 0) {
                    res = R.drawable.d0;
                } else {
                    res = R.drawable.d00;
                }
                break;
            case 1: res = R.drawable.d1; break;
            case 2: res = R.drawable.d2; break;
            case 3: res = R.drawable.d3; break;
            case 4: res = R.drawable.d4; break;
            case 5: res = R.drawable.d5; break;
            case 6: res = R.drawable.d6; break;
            case 7: res = R.drawable.d7; break;
            case 8: res = R.drawable.d8; break;
            case 9: res = R.drawable.d9; break;
        }
        iv.setImageResource(res);
    }

    private int getBtnIdx(int id) {
        for (int idx = 0; idx < mBtnResIds.length; idx++) {
            if (id == mBtnResIds[idx]) return idx;
        }
        return -1;
    }

    private void setBtnStatus(int id, boolean sts) {
        int idx = getBtnIdx(id);
        if (idx != -1) mBtnStatus[idx] = sts;
    }

    public int getPressBtnId() {
        for (int idx = 0; idx < mBtnResIds.length; idx++) {
            if (mBtnStatus[idx]) return mBtnResIds[idx];
        }
        return 0;
    }

    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        String str = "";

        int eventAction = event.getActionMasked();
        //int pointerIndex = event.getActionIndex();
        //int pointerId = event.getPointerId(pointerIndex);

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                setBtnStatus(id, true);
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setBtnStatus(id, false);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }


        /*
        str = String.format("mBtnStatus=[%s %s %s %s]",
                String.valueOf(mBtnStatus[0]),
                String.valueOf(mBtnStatus[1]),
                String.valueOf(mBtnStatus[2]),
                String.valueOf(mBtnStatus[3])  );

        ((TextView)findViewById(R.id.text1)).setText(str);
        Log.w("OnTouch", str);
        */
        return false;
    }

}
