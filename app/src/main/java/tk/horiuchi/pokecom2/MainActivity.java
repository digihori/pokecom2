package tk.horiuchi.pokecom2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowMetrics;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.util.DisplayMetrics.DENSITY_280;
import static android.util.DisplayMetrics.DENSITY_360;
import static android.util.DisplayMetrics.DENSITY_400;
import static android.util.DisplayMetrics.DENSITY_420;
import static android.util.DisplayMetrics.DENSITY_560;
import static android.util.DisplayMetrics.DENSITY_HIGH;
import static android.util.DisplayMetrics.DENSITY_LOW;
import static android.util.DisplayMetrics.DENSITY_MEDIUM;
import static android.util.DisplayMetrics.DENSITY_TV;
import static android.util.DisplayMetrics.DENSITY_XHIGH;
import static android.util.DisplayMetrics.DENSITY_XXHIGH;
import static android.util.DisplayMetrics.DENSITY_XXXHIGH;
import static tk.horiuchi.pokecom2.Common.MODE_PRO;
import static tk.horiuchi.pokecom2.Common.MODE_RUN;
import static tk.horiuchi.pokecom2.Common.MODE_SAVE;
import static tk.horiuchi.pokecom2.Common._GE;
import static tk.horiuchi.pokecom2.Common._LE;
import static tk.horiuchi.pokecom2.Common._NE;
import static tk.horiuchi.pokecom2.Common.cmdTable;
import static tk.horiuchi.pokecom2.Common.type10inch;
import static tk.horiuchi.pokecom2.Common.type7inch;
import static tk.horiuchi.pokecom2.Common.typePhone;
import static tk.horiuchi.pokecom2.SBasic.inputWait;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, WaveOut.ICallBack, PopupMenu.OnMenuItemClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static float dpdx, dpdx_org;
    public static int deviceType;
    public final static String src_path = Environment.getExternalStorageDirectory().getPath() + "/pokecom2";
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
    public static boolean vibrate_enable, debug_info, legacy_storage_io;
    //public static boolean cpuClockEmulateEnable;
    public static int cpuClockWait;
    public static boolean memoryExtension;
    public static int ui_design;
    private Vibrator vib;
    private String[] cmdHistory;
    private int idxHistory, idxHistoryDisp;

    public static boolean initial; // 仮
    public static boolean resultDisp = false;
    public static boolean bankStatus = false;
    public static int angleUnit = 0;    // 0:DEG 1:RAD 2:GRAD
    public static int mode = MODE_RUN;
    public static int last_mode = MODE_RUN;
    public static int bank;
    public static final int progLength = 3000;  // 仮
    public static final int bankMax = 10;
    public static TextView debugWindow;
    public static String debugText = "";
    private boolean nosave = false;
    public static int keyMaskCnt = 0;
    private int debugPrintCnt = 0;
    public static WaveOut waveout;
    public static String activityTag;
    public String debugInfo;
    private LinearLayout background;


    public static int[] mBtnResIds = {
            R.id.buttonDA, R.id.buttonUA, R.id.buttonFUNC,
            R.id.buttonMODE, R.id.buttonLA, R.id.buttonRA, R.id.buttonSHIFT,

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
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        setContentView(R.layout.activity_main);

        activityTag = (String) findViewById(R.id.activity_main).getTag();
        Log.w("Main", String.format("activity_main='%s'\n", activityTag));

        // ファイルIOのパーミッション関係の設定
        verifyStoragePermissions(this);

        // ディレクトリがなければ作成する　これはAndroid10以降は使えない！
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File dir = new File(src_path);
            if (!dir.exists()) {
                dir.mkdirs();
                Log.w("Main", "mkdir");
            }
        }

        // dp->px変換のためにDisplayMetricsを取得しておく
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);

        Point point = new Point(0, 0);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            // Android 10-
            Log.w("DispChk", String.format("Version 10-"));
            WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
            //Insets insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            int width = windowMetrics.getBounds().width();
            int height = windowMetrics.getBounds().height();
            point.set(width, height);
            //Log.w("DispChk", String.format("point.x=%d y=%d", point.x, point.y));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Android 4.2-
            Log.w("DispChk", String.format("Version 4.2-"));
            display.getRealSize(point);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            // Android 3.2-
            Log.w("DispChk", String.format("Version 3.2-"));
            try {
                Method getRawWidth = Display.class.getMethod("getRawWidth");
                Method getRawHeight = Display.class.getMethod("getRawHeight");
                int width = (Integer) getRawWidth.invoke(display);
                int height = (Integer) getRawHeight.invoke(display);
                point.set(width, height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 流石にこれより古いバージョンは見ない
        }

        //DisplayMetrics metrics = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dpdx_org = dpdx = metrics.density;
        //Log.w("Main", String.format("widthPixels=%d\n", metrics.widthPixels));
        Log.w("Main", String.format("widthPixels=%d\n", point.x));
        //Log.w("Main", String.format("heightPixels=%d\n", metrics.heightPixels));
        Log.w("Main", String.format("heightPixels=%d\n", point.y));
        Log.w("Main", String.format("Xdpi=%f\n", metrics.xdpi));
        Log.w("Main", String.format("Ydpi=%f\n", metrics.ydpi));
        Log.w("Main", String.format("density=%f\n", metrics.density));
        Log.w("Main", String.format("densityDpi=%d(%s)\n", metrics.densityDpi, getGeneralizedDensity(metrics.densityDpi)));
        Log.w("Main", String.format("scaledDensity=%f\n", metrics.scaledDensity));

        double inchX = metrics.widthPixels / metrics.xdpi;
        double inchY = metrics.heightPixels / metrics.ydpi;
        double inch = Math.sqrt(inchX * inchX + inchY * inchY);
        Log.w("Main", String.format("inch=%2.2f\n", inch));

        // デバイスタイプとスケールの設定
        if (getResources().getBoolean(R.bool.is_7inch)) {
            deviceType = type7inch;
            dpdx = dpdx_org * 1.5f; // 7inchタブレットはスケールを1.5倍する
            /*
            if (1.3f < dpdx_org && dpdx_org< 1.4f) {
                // nexus7(2012) tvdpi の時はスケール２倍
                dpdx = 2.1f;
            } else if (dpdx_org == 1.0f) {
                dpdx = 1.7f;    // mdpi の時はスケール1.5倍
            } else {
                // それ以外（多分xhdpiしかない？）の時はスケール３倍
                dpdx = 3.1f;
            }
            */
            Log.w("Main", String.format("deviceType=7inch tablet(%d) scale=%f\n", deviceType, dpdx));
        } else if (getResources().getBoolean(R.bool.is_10inch)) {
            deviceType = type10inch;
            if (getResources().getBoolean(R.bool.is_sw1024dp)) {
                dpdx = dpdx_org * 2.4f;
            } else {
                dpdx = dpdx_org * 2;    // スケールは2倍
            }
            /*
            if (dpdx_org == 1.0f) {
                dpdx = 2.1f;
            } else if (dpdx_org == 1.5f) {
                dpdx = 3.1f;
            } else {
                dpdx = dpdx_org * 2 + 0.1f;
            }

            */
            Log.w("Main", String.format("deviceType=10inch tablet(%d) scale=%f\n", deviceType, dpdx));
        } else {
            deviceType = typePhone;
            /*
            if (point.x <= 800 && dpdx_org == 1.5f) {
                // hdpiの時は少し小さめにする
                dpdx = 1.3f;
            } else if (dpdx_org == 3.0f) {
                if (point.x >= 2280) {
                    dpdx = 3.2f;
                } else if (point.x > 1920) {
                    dpdx = 3.1f;
                }
                // xxdpiで幅が1920より大きい端末は少し大きめにする
                //dpdx = 3.1f;
            } else if (dpdx_org == 3.5f) {
                // xxxhdpiの時は少し大きめにする
                dpdx = 4.0f;
            } else if (dpdx_org == 4.0f) {
                dpdx = 4.1f;
            }

             */
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
        legacy_storage_io = sp.getBoolean("storage_checkbox_key", true);
        //cpuClockEmulateEnable = sp.getBoolean("cpu_clock_key", true);
        cpuClockWait = Integer.parseInt(sp.getString("cpu_clock_wait_key", "2"));
        memoryExtension = sp.getBoolean("memory_unit_key", true);
        ui_design = Integer.parseInt(sp.getString("ui_design_key", "0"));

        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Buttonインスタンスの取得
        // ButtonインスタンスのリスナーをこのActivityクラスそのものにする
        findViewById(R.id.popMenu).setOnClickListener(this);
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

        if (source == null) {
            source = new SourceFile();
        }
        try {
            basic = new SBasic(lcd);
            //Log.w("Main", "----- prog completed!!! -----");
        } catch (InterpreterException e) {
            Log.w("Main", String.format("error='%s'", e.toString()));
        }

        debugWindow = (TextView) findViewById(R.id.debugWindow);

        final Handler _handler1 = new Handler();
        final int DELAY1 = 50;
        _handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (debug_info) {
                    debugPrintCnt++;
                    if ((debugPrintCnt & 8) != 0) debugPrint();
                }
                if (keyMaskCnt > 0) {
                    keyMaskCnt--;
                    //Log.w("Main", String.format("keyMaskCnt=%d", keyMaskCnt));
                }
                _handler1.postDelayed(this, DELAY1);
            }
        }, DELAY1);

        cmdHistory = new String[8];
        idxHistory = 0;

        waveout = new WaveOut();
        waveout.setReference(this);

        debugInfo =
                String.format("activity_main='%s'\n", activityTag) +
                        String.format("widthPixels=%d\n", point.x) +
                        String.format("heightPixels=%d\n", point.y) +
                        String.format("Xdpi=%f\n", metrics.xdpi) +
                        String.format("Ydpi=%f\n", metrics.ydpi) +
                        String.format("density=%f\n", metrics.density) +
                        String.format("densityDpi=%d(%s)\n", metrics.densityDpi, getGeneralizedDensity(metrics.densityDpi)) +
                        String.format("scaledDensity=%f\n", metrics.scaledDensity) +
                        String.format("inch=%2.2f\n", inch) +
                        String.format("deviceType=%d\n", deviceType) +
                        String.format("dpdx=%f", dpdx);
    }

    private String getGeneralizedDensity(int dpi) {
        switch (dpi) {
            default:
                return ("mdpi");
            case DENSITY_LOW:
                return ("ldpi");
            case DENSITY_MEDIUM:
                return ("mdpi");
            case DENSITY_TV:
                return ("tvdpi");
            case DENSITY_HIGH:
                return ("hdpi");
            case DENSITY_280:
                return ("DENSITY_280");
            case DENSITY_XHIGH:
                return ("xhdpi");
            case DENSITY_360:
                return ("DENSITY_360");
            case DENSITY_400:
                return ("DENSITY_400");
            case DENSITY_420:
                return ("DENSITY_420");
            case DENSITY_XXHIGH:
                return ("xxhdpi");
            case DENSITY_560:
                return ("DENSITY_560");
            case DENSITY_XXXHIGH:
                return ("xxxhdpi");
        }
    }

    private void deviceReset() {
        // プログラムが動いていたら停止する
        if (mode == MODE_RUN && pb.isProgExist()) {
            basic.sbExit();
        }
        // 変数の初期化
        basic.vac();
        try {
            basic.defm(0);
        } catch (InterpreterException e) {
        }
        // プログラムのオールクリア
        source.clearSourceAll();
        // モードの初期化
        mode = MODE_RUN;
        keyShift = false;
        keyExt = false;
        keyMode = false;
        keyFunc = false;
        lcd.print("READY P0");
        initial = true;

        calcMemoryAndDisp(false);
        listDisp = false;

        lcd.refresh();
    }

    private void debugPrint() {
        debugWindow.setText(debugText);
        //Log.w("debug", "exec!!!");
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

        // メインキーの画像を切り替える
        ImageView iv = (ImageView) findViewById(R.id.imageViewMainkey);
        if (ui_design == 0) {
            iv.setImageResource(R.drawable.pb100_mainkey);
            for (int i = 0; i < 3; i++) {
                findViewById(mBtnResIds[i]).setOnClickListener(null);
            }
        } else {
            iv.setImageResource(R.drawable.fx700p_mainkey);
            for (int i = 0; i < 3; i++) {
                findViewById(mBtnResIds[i]).setOnClickListener(this);
            }
        }

        // キーレイアウトを更新する
        stretchItemSize((GridLayout) findViewById(R.id.keyAreaMainkey), (ImageView) findViewById(R.id.imageViewMainkey));
        stretchItemSize((GridLayout) findViewById(R.id.keyAreaTenkey), (ImageView) findViewById(R.id.imageViewTenkey), 20);

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
        Log.w("LOG", "p.x=" + p.x + " p.y=" + p.y);

        int childWidth = p.x / gl.getColumnCount();
        int childHeight = p.y / gl.getRowCount();

        Log.w("LOG", "iv w=" + iv.getWidth() + " h=" + iv.getHeight());
        Log.w("LOG", "column=" + gl.getColumnCount() + " row=" + gl.getRowCount());
        Log.w("LOG", "w=" + childWidth + " h=" + childHeight);
        for (int i = 0; i < gl.getChildCount(); i++) {
            gl.getChildAt(i).setMinimumWidth(childWidth);
            ((TextView) gl.getChildAt(i)).setMinWidth(childWidth);
            gl.getChildAt(i).setMinimumHeight(childHeight);
            ((TextView) gl.getChildAt(i)).setMinHeight(childHeight);
        }
    }

    public void stretchItemSize(GridLayout gl, ImageView iv, int x) {
        Point p = getBitmapSizeFromDynamicImageLayer(iv);
        Log.w("LOG", "p.x=" + p.x + " p.y=" + p.y);

        int childWidth = p.x / gl.getColumnCount();
        int childHeight = p.y / gl.getRowCount();

        Log.w("LOG", "iv w=" + iv.getWidth() + " h=" + iv.getHeight());
        Log.w("LOG", "column=" + gl.getColumnCount() + " row=" + gl.getRowCount());
        Log.w("LOG", "w=" + childWidth + " h=" + childHeight);
        for (int i = 0; i < gl.getChildCount(); i++) {
            if (i == x) {
                gl.getChildAt(i).setMinimumWidth(childWidth * 2);
                ((TextView) gl.getChildAt(i)).setMinWidth(childWidth * 2);
                gl.getChildAt(i).setMinimumHeight(childHeight);
                ((TextView) gl.getChildAt(i)).setMinHeight(childHeight);
            } else {
                gl.getChildAt(i).setMinimumWidth(childWidth);
                ((TextView) gl.getChildAt(i)).setMinWidth(childWidth);
                gl.getChildAt(i).setMinimumHeight(childHeight);
                ((TextView) gl.getChildAt(i)).setMinHeight(childHeight);
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
                if (i < 3) {
                    findViewById(ids[i]).setBackgroundResource(R.drawable.button_hide);
                } else {
                    findViewById(ids[i]).setBackgroundResource(R.drawable.button);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nosave) {
            nosave = false;
            return;
        }
        if (source != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Gson gson = new Gson();

            for (int i = 0; i < bankMax; i++) {
                String[] src = source.getSourceAll(i);

                if (src != null) {
                    // objectをjson文字列へ変換
                    String jsonInstanceString = gson.toJson(src);
                    // 変換後の文字列をputStringで保存
                    prefs.edit().putString("PREF_P" + i, jsonInstanceString).apply();
                }
            }

            Log.w("Main", "------------- onPause()");
            if (debug_info) {
                Toast.makeText(this, String.format("Activity saved."), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        for (int i = 0; i < bankMax; i++) {
            // 保存されているjson文字列を取得
            String s = prefs.getString("PREF_P" + i, "");
            prefs.edit().putString("PREF_P" + i, "").apply();  // 読み出した部分はクリア

            if (s != null && !s.isEmpty()) {
                Gson gson = new Gson();
                String[] src = gson.fromJson(s, String[].class);

                source.setSourceAll(i, src);
            }

        }
        Log.w("Main", "------------ onResume()    source restored!!! ---------");

        // ボタン表示枠の更新
        if (debug_info) {
            changeButtonFrame(mBtnResIds, true);
        } else {
            changeButtonFrame(mBtnResIds, false);
            debugText = "";
            debugPrint();
        }
    }

    private void changeFunckeyImg(boolean f) {
        if (ui_design == 0) return;

        ImageView iv = (ImageView) findViewById(R.id.imageViewMainkey);
        if (f) {
            iv.setImageResource(R.drawable.fx700p_mainkey_f);
        } else {
            iv.setImageResource(R.drawable.fx700p_mainkey);
        }
    }

    public static boolean listDisp = false;

    public void onClick(View v) {
        int c = v.getId();
        Log.w("Click", String.format("key=%d", c));

        // 通常のキー入力処理
        inKey(c, v);
    }

    private void inKey(int c, View v) {
        if (keyMaskCnt > 0) return;
        if (mode == MODE_SAVE && c != R.id.buttonSTOP) return;
        if (c != R.id.buttonSTOP && (pb.isProgRunning() && !inputWait)) return;

        // モードの切り替え
        boolean modeChange = true;
        switch (c) {
            case R.id.popMenu:
                if (v == null) return;
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_main, popup.getMenu());
                popup.getMenu().findItem(R.id.optionsMenu_debug).setVisible(debug_info);
                popup.show();
                popup.setOnMenuItemClickListener(this);
                return;
            case R.id.buttonMODE:
                keyMode = !keyMode;
                break;
            case R.id.buttonSHIFT:
                keyShift = !keyShift;
                break;
            case R.id.buttonFUNC:
                keyFunc = !keyFunc;
                changeFunckeyImg(keyFunc);
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
            case R.id.button4:
                if (keyMode) {
                    keyMode = false;
                    angleUnit = 0;  // DEG
                } else {
                    modeChange = false;
                }
                break;
            case R.id.button5:
                if (keyMode) {
                    keyMode = false;
                    angleUnit = 1;  // RAD
                } else {
                    modeChange = false;
                }
                break;
            case R.id.button6:
                if (keyMode) {
                    keyMode = false;
                    angleUnit = 2;  // GRAD
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
                    if (pb.isProgStop() || inputWait) {
                        lcd.putchar(inkey.getKeyCode(c));
                    } else {
                        String s = lcd.getCmdBuf();
                        if (s == null) break;
                        int result = 0;
                        try {
                            Log.w("EXE", String.format("%s", s));
                            result = basic.calc(lcd.getCmdBuf());
                        } catch (InterpreterException e) {
                            Log.w("Main", String.format("error='%s'", e.toString()));
                        }
                        //initial = true;
                        if (result != 0) {
                            resultDisp = true;
                        }
                        cmdHistory[idxHistory++ & 7] = s;
                        //idxHistory++;
                        for (int i = 0; i < 8; i++) {
                            Log.w("history", String.format("[%d] '%s'", i, cmdHistory[i]));
                        }
                    }
                    break;
                case R.id.buttonANS:
                    basic.lastAns();
                    break;
                case R.id.buttonUA:
                case R.id.buttonDA:
                case R.id.buttonLA:
                    if (c == R.id.buttonLA && resultDisp &&
                            cmdHistory[(idxHistory - 1) & 7] != null &&
                            !cmdHistory[(idxHistory - 1) & 7].isEmpty()) {
                        idxHistoryDisp = idxHistory - 1;
                        lcd.print(cmdHistory[idxHistoryDisp & 7]);
                        break;
                    }
                    if (c == R.id.buttonUA &&
                            cmdHistory[(idxHistoryDisp - 1) & 7] != null &&
                            !cmdHistory[(idxHistoryDisp - 1) & 7].isEmpty()) {
                        idxHistoryDisp--;
                        lcd.cls();
                        lcd.print(cmdHistory[idxHistoryDisp & 7]);
                    }
                    if (c == R.id.buttonDA &&
                            cmdHistory[(idxHistoryDisp + 1) & 7] != null &&
                            !cmdHistory[(idxHistoryDisp + 1) & 7].isEmpty()) {
                        idxHistoryDisp++;
                        lcd.cls();
                        lcd.print(cmdHistory[idxHistoryDisp & 7]);
                    }
                    // no break!
                default:
                    int code = inkey.getKeyCode(c);
                    if (code != 0) lcd.putchar(code);
                    break;
            }

        } else if (mode == MODE_PRO) {
            String s;
            switch (c) {
                case R.id.buttonDA:
                    //Log.w("Main", String.format("listDisp=%s initial=%s", listDisp, initial));
                    if (listDisp /*|| initial*/) {
                        s = source.getSourceNext(bank);
                        listDisp = true;
                        //Log.w("Main", "listNext");
                    } else {
                        s = source.getSourceTop(bank);
                        listDisp = true;
                        //Log.w("Main", "listTop");
                    }
                    if (s != null) {
                        lcd.printSourceList(s);
                        //initial = true;
                    }
                    break;
                case R.id.buttonUA:
                    //Log.w("Main", String.format("listDisp=%s initial=%s", listDisp, initial));
                    if (listDisp /*|| initial*/) {
                        s = source.getSourcePrev(bank);
                        listDisp = true;
                        //Log.w("Main", "listPrev");
                    } else {
                        s = source.getSourceBottom(bank);
                        listDisp = true;
                        //Log.w("Main", "listBottom");
                    }
                    if (s != null) {
                        lcd.printSourceList(s);
                        //initial = true;
                    }
                    break;

                case R.id.buttonEXE:
                    s = lcd.getCmdBuf();
                    if (s == null) break;

                    try {
                        Log.w("EXE", String.format("%s", s));
                        basic.calc(s);
                    } catch (InterpreterException e) {
                        Log.w("Main", String.format("error='%s'", e.toString()));
                    }
                    calcMemoryAndDisp(true);
                    break;
                case R.id.buttonANS:
                    break;
                default:
                    int code = inkey.getKeyCode(c);
                    if (code != 0) lcd.putchar(code);
                    break;
            }

        } else if (mode == MODE_SAVE) {
            if (c == R.id.buttonSTOP) {
                // 前回のモードに戻す
                basic.printSaveStatus(0);
                mode = last_mode;
                waveout.stop();
                lcd.refresh();
            }
        }
        keyShift = false;
        if (keyFunc) {
            keyFunc = false;
            changeFunckeyImg(false);
        }


        if (vibrate_enable) {
            //vib.vibrate(10);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
                //debugText = String.format("SDK_INT=%d,%d -> vibration 1", Build.VERSION.SDK_INT, Build.VERSION_CODES.O);
                //if (vib.hasVibrator()) {
                //    debugText = String.format("Vibrate ---> YES");
                //} else {
                //    debugText = String.format("Vibrate ---> NO");
                //}
                Log.w("vib", debugText);
            } else {
                //deprecated in API 26
                vib.vibrate(10);
                //debugText = String.format("SDK_INT=%d,%d -> vibration 0", Build.VERSION.SDK_INT, Build.VERSION_CODES.O);
                Log.w("vib", debugText);
            }
        }

    }

    private boolean optionsItemSelectedSub(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionsMenu_01:
                nosave = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || !legacy_storage_io) {
                    // Android 11(Q)以降はストレージアクセス権限が厳密になったため
                    actLoadx();
                } else {
                    actLoad();
                }
                return true;
            case R.id.optionsMenu_02:
                nosave = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || !legacy_storage_io) {
                    // Android 11(Q)以降はストレージアクセス権限が厳密になったため
                    actSavex();
                } else {
                    actSave();
                }
                return true;
            case R.id.optionsMenu_03:   // reset
                deviceReset();
                return true;
            case R.id.optionsMenu_04:   // settings
                nosave = true;
                Intent intent1 = new android.content.Intent(this, MyPreferenceActivity.class);
                startActivity(intent1);
                return true;
            case R.id.optionsMenu_05:   // help
                AboutDialogFragment dialog = new AboutDialogFragment();
                dialog.show(getFragmentManager(), "");
                return true;
            case R.id.optionsMenu_06:   // exit
                finish();
                return true;
            case R.id.optionsMenu_debug:
                new AlertDialog.Builder(this)
                        .setTitle("Debug info")
                        .setMessage(debugInfo)
                        .setPositiveButton("OK", null)
                        .create()
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        //Log.w("popMenu", String.format("menu item=%d", item.getItemId()));
        return optionsItemSelectedSub(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.w("onPrepareOptionsMenu", "called");
        MenuItem item = menu.findItem(R.id.optionsMenu_debug);
        item.setVisible(debug_info);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsItemSelectedSub(item);
    }

    // BASICプログラムの読み込み
    protected int load(Uri uri) {
        int ret = 0;
        int b = 0;
        int[] idx = new int[bankMax];

        char[][] ch = new char[progLength][bankMax];
        FileInputStream fis = null;

        try {
            InputStream is = getContentResolver().openInputStream(uri);
            byte[] buf = new byte[is.available()];
            //is.read(buffer);


            //fis = new FileInputStream(filename);

            //byte buf[] = new byte[progLength];
            int len = 0, x;
            while ((x = is.read(buf)) != -1) {
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
                    String s = "";
                    try {
                        // 1byte文字以外はExceptionを吐くのでcatchで拾って読み捨てる
                        s = String.format("%c", c);
                    } catch (IllegalFormatCodePointException e) {
                        Log.w("load", "IlleagalFormatCodeException!!!");
                    } catch (IllegalFormatException e) {
                        Log.w("load", "IlleagalFormatException!!!");
                    }
                    //str += String.format("%c", c);
                    str += s;
                    if (r >= len) break;
                }
                int llen = str.length();
                //Log.w("LOG", String.format("%s", str));

                // バンク指定のフィールドかどうかチェックする
                String regex = "\\[P[0-9]\\]";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(str);
                if (m.find()) {
                    b = Integer.parseInt(m.group().substring(2, 3));
                    w = 0;
                    idx[b] = 0;
                    continue;
                }


                if (llen != 0) {    // 空行は読み捨てる
                    // 行番号で始まっていない行はコメント行とみなして読み捨てる
                    regex = "^[0-9]+";
                    p = Pattern.compile(regex);
                    m = p.matcher(str);
                    if (!m.find()) {
                        Log.w("load", String.format("This line was skipped. -> '%s'", str));
                        continue;
                    }

                    // 行番号、REM〜、ダブルコーテーションで囲まれた文字列、それ以外に分割する
                    regex = "(?:^[0-9]+:?)|(?:REM.*$)|(?:\"[^\"]*\")|(?:[^\"]+)";
                    p = Pattern.compile(regex);
                    m = p.matcher(str);
                    ArrayList<String> data = new ArrayList<String>();
                    if (m.find()) {
                        do {
                            data.add(m.group());
                        } while (m.find());
                    }

                    for (String s : data) {
                        //Log.w("load", String.format("s='%s'", s));
                        // 分割した文字列毎に処理する
                        boolean noencode = false;
                        regex = "^\"|^REM";
                        p = Pattern.compile(regex);
                        m = p.matcher(s);
                        if (m.find()) {    // ダブルコートの文字列orREM
                            noencode = true;
                        }

                        int l = s.length();
                        for (int i = 0; i < l; i++) {
                            c = s.charAt(i);
                            if (c == '\\') {
                                if (i + 1 < l && s.charAt(i + 1) == '\\') {
                                    dest[w++] = '\\';
                                    i++;
                                } else if (i + 2 < l) {
                                    String ss = s.substring(i, i + 3);
                                    //Log.w("LOG", String.format("ss='%s'(%d) %02x %02x %02x", ss, ss.length(), (int)s.charAt(i), (int)s.charAt(i+1), (int)s.charAt(i+2)));
                                    //Log.w("LOG", String.format("ss='%s'", ss));
                                    for (int jj = 0xe0; jj <= 0xff; jj++) {
                                        //Log.w("LOG", String.format("j=%02x='%s'(%d)", j, cmdTable[j], cmdTable[j].length()));
                                        if (cmdTable[jj].equals(ss)) {
                                            if (jj == 0xf9) {
                                                dest[w++] = 0x5e;
                                                // 0xf9 は上矢印で定義しているが内部的には'^'で使っているため変換する
                                            } else {
                                                dest[w++] = jj;  // エスケープ文字を内部コードに変換する
                                            }
                                            //Log.w("LOG", String.format("s='%s'(%d)=%x", cmdTable[j], cmdTable[j].length(), j));
                                        }
                                    }
                                    i += 2;
                                } else {
                                    // 無効なコードなので読み捨てる
                                }
                            } else if (!noencode && i + 1 < llen && c == '>' && s.charAt(i + 1) == '=') {
                                dest[w++] = _GE;   // >=
                                i++;
                            } else if (!noencode && i + 1 < llen && c == '<' && s.charAt(i + 1) == '=') {
                                dest[w++] = _LE;   // <=
                                i++;
                            } else if (!noencode && i + 1 < llen && c == '<' && s.charAt(i + 1) == '>' ||
                                    i + 1 < llen && c == '!' && s.charAt(i + 1) == '=') {
                                dest[w++] = _NE;   // <>, !=
                                i++;
                            } else {
                                dest[w++] = c;
                            }
                        }
                        //dest[w++] = ' ';
                    }
                    dest[w++] = '\n';


                    /*
                    for (int i = 0; i < llen; i++) {
                        c = str.charAt(i);
                        if (c == '\\') {
                            if (i + 1 < llen && str.charAt(i + 1) == '\\') {
                                dest[w++] = '\\';
                                i++;
                            } else if (i + 2 < llen) {
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
                        } else if (i + 1 < llen && c == '>' && str.charAt(i + 1) == '=') {
                            dest[w++] = _GE;   // >=
                            i++;
                        } else if (i + 1 < llen && c == '<' && str.charAt(i + 1) == '=') {
                            dest[w++] = _LE;   // <=
                            i++;
                        } else if (i + 1 < llen && c == '<' && str.charAt(i + 1) == '>' ||
                                i + 1 < llen && c == '!' && str.charAt(i + 1) == '=') {
                            dest[w++] = _NE;   // <>, !=
                            i++;
                        } else {
                            dest[w++] = c;
                        }
                    }
                    dest[w++] = '\n';
                    */
                }
                for (int i = 0; i < w; i++, idx[b]++) {
                    ch[idx[b]][b] = (char) dest[i];
                }
            }

            //Log.w("LOAD", String.format("file=%s length=%04x", filename, len));
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

            //Log.w("load", String.format("------ bank:%d", i));
            //for (int k = 0; k < str2.length; k++) {
            //    Log.w("load", str2[k]);
            //}
        }

        return ret;
    }

    // BASIC プログラムの保存
    protected void save(Uri uri) {
        Log.w("save", String.format("uri=%s", uri));

        //FileOutputStream fos = null;

        try {
            OutputStream os = getContentResolver().openOutputStream(uri);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                File dir = new File(src_path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
            //fos = new FileOutputStream(filename);
            byte buf[] = new byte[progLength];

            int l = 0;
            for (int i = 0; i < bankMax; i++) {
                String[] temp = source.getSourceAll(i);
                if (temp != null) {
                    String s = String.format("[P%d]", i);
                    for (int n = 0; n < s.length(); n++) {
                        buf[l++] = (byte) s.charAt(n);
                    }
                    buf[l++] = '\n';

                    for (int j = 0; j < temp.length; j++) {
                        for (int k = 0; k < temp[j].length(); k++) {
                            char c = temp[j].charAt(k);
                            if (0xe0 <= c && c <= 0xff) {
                                switch (c) {
                                    case 0xf2:
                                        buf[l++] = '<';
                                        buf[l++] = '>';
                                        break;
                                    case 0xf4:
                                        buf[l++] = '<';
                                        buf[l++] = '=';
                                        break;
                                    case 0xf5:
                                        buf[l++] = '>';
                                        buf[l++] = '=';
                                        break;
                                    default:
                                        for (int n = 0; n < cmdTable[c].length(); n++) {
                                            buf[l++] = (byte) cmdTable[c].charAt(n);
                                        }
                                        break;
                                }
                            } else {
                                if (c == '\\') {
                                    buf[l++] = buf[l++] = (byte) c;
                                } else {
                                    buf[l++] = (byte) c;
                                }
                            }
                        }
                        buf[l++] = '\n';
                    }
                    buf[l++] = '\n';
                }
            }
            os.write(buf, 0, l);
            os.flush();
            os.close();

        } catch (IOException e) {
            Log.d("MainActivity", e.toString());
            //} finally {
            //    if (os != null) {
            //        try {
            //            os.close();
            //        } catch (IOException e) {
            //            e.printStackTrace();
            //        }
            //    }
        }
    }

    private final ActivityResultLauncher<Intent> actLoadResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        //結果を受け取った後の処理
                        Uri uri = result.getData().getData();
                        Log.w("actLoadResultLauncher", String.format("uri=%s", uri));
                        source.clearSourceAll();
                        int len = load(uri);
                        Toast.makeText(this, "loaded:" + uri + "(" + len + " bytes)", Toast.LENGTH_LONG).show();
                    }
                }
            });

    public void actLoadx() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        actLoadResultLauncher.launch(intent);
    }

    public void actLoad() {
        Intent intent = new android.content.Intent(getApplication(), FileLoad.class);
        startActivityForResult(intent, 0);
    }

    private final ActivityResultLauncher<Intent> actSaveResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        //結果を受け取った後の処理
                        Uri uri = result.getData().getData();
                        Log.w("actSaveResultLauncher", String.format("uri=%s", uri));
                        save(uri);
                        Toast.makeText(this, "saved:" + uri, Toast.LENGTH_LONG).show();
                    }
                }
            });

    private void actSavex() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        actSaveResultLauncher.launch(intent);
    }

    public void actSave() {
        Intent intent = new android.content.Intent(getApplication(), FileSave.class);
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.w("LOG", "onActivityResult.");

        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    String txt1 = data.getExtras().getString("filePath");
                    if (txt1 != null) {
                        Log.w("LOG", "path=" + txt1);
                        source.clearSourceAll();
                        File f = new File(txt1);
                        int len = load(Uri.fromFile(f));
                        Toast.makeText(this, "loaded:" + txt1 + "(" + len + ")", Toast.LENGTH_LONG).show();
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
                        File f = new File(txt1);
                        save(Uri.fromFile(f));
                        Toast.makeText(this, "saved:" + txt1, Toast.LENGTH_LONG).show();
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
            ((ImageView) findViewById(R.id.d1)).setImageResource(R.drawable.d10);
            ((ImageView) findViewById(R.id.d2)).setImageResource(R.drawable.d00);
            ((ImageView) findViewById(R.id.d3)).setImageResource(R.drawable.d00);
            ((ImageView) findViewById(R.id.d4)).setImageResource(R.drawable.d00);
            return;
        }

        int total = 0;
        int remain = memoryExtension ? 544 + 1024 : 544;
        total = source.getUsedMemorySize();
        total += basic.getDefmSize();
        Log.w("calcMem", String.format("memory=%d use=%d remain=%d", remain, total, remain - total));
        remain -= total;
        if (remain < 0) remain = 0;

        //Log.w("Memory", "remain="+remain);

        int digit1000 = remain / 1000;
        int digit100 = (remain - digit1000 * 1000) / 100;
        int digit10 = (remain - digit1000 * 1000 - digit100 * 100) / 10;
        int digit1 = remain - digit1000 * 1000 - digit100 * 100 - digit10 * 10;

        int res;
        ImageView iv = (ImageView) findViewById(R.id.d1);
        switch (digit1000) {
            default:
            case 0:
                res = R.drawable.d10;
                break;
            case 1:
                res = R.drawable.d11;
                break;
        }
        iv.setImageResource(res);

        iv = (ImageView) findViewById(R.id.d2);
        switch (digit100) {
            default:
            case 0:
                if (digit1000 == 0) {
                    res = R.drawable.d00;
                } else {
                    res = R.drawable.d0;
                }
                break;
            case 1:
                res = R.drawable.d1;
                break;
            case 2:
                res = R.drawable.d2;
                break;
            case 3:
                res = R.drawable.d3;
                break;
            case 4:
                res = R.drawable.d4;
                break;
            case 5:
                res = R.drawable.d5;
                break;
            case 6:
                res = R.drawable.d6;
                break;
            case 7:
                res = R.drawable.d7;
                break;
            case 8:
                res = R.drawable.d8;
                break;
            case 9:
                res = R.drawable.d9;
                break;
        }
        iv.setImageResource(res);

        iv = (ImageView) findViewById(R.id.d3);
        switch (digit10) {
            default:
            case 0:
                if (digit1000 == 0 && digit100 == 0) {
                    res = R.drawable.d00;
                } else {
                    res = R.drawable.d0;
                }
                break;
            case 1:
                res = R.drawable.d1;
                break;
            case 2:
                res = R.drawable.d2;
                break;
            case 3:
                res = R.drawable.d3;
                break;
            case 4:
                res = R.drawable.d4;
                break;
            case 5:
                res = R.drawable.d5;
                break;
            case 6:
                res = R.drawable.d6;
                break;
            case 7:
                res = R.drawable.d7;
                break;
            case 8:
                res = R.drawable.d8;
                break;
            case 9:
                res = R.drawable.d9;
                break;
        }
        iv.setImageResource(res);

        iv = (ImageView) findViewById(R.id.d4);
        switch (digit1) {
            default:
            case 0:
                res = R.drawable.d0;
                break;
            case 1:
                res = R.drawable.d1;
                break;
            case 2:
                res = R.drawable.d2;
                break;
            case 3:
                res = R.drawable.d3;
                break;
            case 4:
                res = R.drawable.d4;
                break;
            case 5:
                res = R.drawable.d5;
                break;
            case 6:
                res = R.drawable.d6;
                break;
            case 7:
                res = R.drawable.d7;
                break;
            case 8:
                res = R.drawable.d8;
                break;
            case 9:
                res = R.drawable.d9;
                break;
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

        int eventAction = event.getActionMasked();

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
        return false;
    }

    public void playComplete(int n) {
        if (n == 0) {
            Log.w("Main", "Audio track is complete.");
            mode = last_mode;
            basic.printSaveStatus(0);
            lcd.refresh();
        } else {
            basic.printSaveStatus(n);
            lcd.refresh();
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        int inputDevice = event.getSource();

        Log.w("dispatchKeyEvent", String.format("device=%s",Integer.toBinaryString(inputDevice)));

        if (event.isShiftPressed()) {
            Log.w("dispatchKeyEvent", String.format("shift!!!!!!!!!!!"));
        }

        int id = 0;
        if (true || (inputDevice & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
                (inputDevice & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (inputDevice & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD ||
                (inputDevice & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {

            Log.w("dispatchKeyEvent", String.format("code=%d", keyCode));

            for (int i = 0; i < keyTbl.length; i++) {
                if (keyCode == keyTbl[i].keycode) {
                    id = keyTbl[i].btnid;
                    break;
                }
            }
            if (id != 0) {
                if (action == KeyEvent.ACTION_DOWN) {
                    //Log.w("dispatchKeyEvent", String.format("key down! code=%d id=%d", keyCode, id));
                    inKey(id, null);
                    setBtnStatus(id, true);
                } else {
                    //Log.w("dispatchKeyEvent", String.format("key down or not"));
                    setBtnStatus(id, false);
                }
            }

            // キーコードをログ出力
            String msg = "keyCode:" + keyCode;

            //background.requestFocus();
        }
        //return super.dispatchKeyEvent(event);
        return true;

    }

    private class Keytbl {
        int keycode;
        int btnid;

        Keytbl(int k, int i) {
            keycode = k;
            btnid = i;
        }
    }

    private Keytbl keyTbl[] = {
            new Keytbl(KeyEvent.KEYCODE_A, R.id.buttonA),
            new Keytbl(KeyEvent.KEYCODE_B, R.id.buttonB),
            new Keytbl(KeyEvent.KEYCODE_C, R.id.buttonC),
            new Keytbl(KeyEvent.KEYCODE_D, R.id.buttonD),
            new Keytbl(KeyEvent.KEYCODE_E, R.id.buttonE),
            new Keytbl(KeyEvent.KEYCODE_F, R.id.buttonF),
            new Keytbl(KeyEvent.KEYCODE_G, R.id.buttonG),
            new Keytbl(KeyEvent.KEYCODE_H, R.id.buttonH),
            new Keytbl(KeyEvent.KEYCODE_I, R.id.buttonI),
            new Keytbl(KeyEvent.KEYCODE_J, R.id.buttonJ),
            new Keytbl(KeyEvent.KEYCODE_K, R.id.buttonK),
            new Keytbl(KeyEvent.KEYCODE_L, R.id.buttonL),
            new Keytbl(KeyEvent.KEYCODE_M, R.id.buttonM),
            new Keytbl(KeyEvent.KEYCODE_N, R.id.buttonN),
            new Keytbl(KeyEvent.KEYCODE_O, R.id.buttonO),
            new Keytbl(KeyEvent.KEYCODE_P, R.id.buttonP),
            new Keytbl(KeyEvent.KEYCODE_Q, R.id.buttonQ),
            new Keytbl(KeyEvent.KEYCODE_R, R.id.buttonR),
            new Keytbl(KeyEvent.KEYCODE_S, R.id.buttonS),
            new Keytbl(KeyEvent.KEYCODE_T, R.id.buttonT),
            new Keytbl(KeyEvent.KEYCODE_U, R.id.buttonU),
            new Keytbl(KeyEvent.KEYCODE_V, R.id.buttonV),
            new Keytbl(KeyEvent.KEYCODE_W, R.id.buttonW),
            new Keytbl(KeyEvent.KEYCODE_X, R.id.buttonX),
            new Keytbl(KeyEvent.KEYCODE_Y, R.id.buttonY),
            new Keytbl(KeyEvent.KEYCODE_Z, R.id.buttonZ),
            new Keytbl(KeyEvent.KEYCODE_0, R.id.button0),
            new Keytbl(KeyEvent.KEYCODE_1, R.id.button1),
            new Keytbl(KeyEvent.KEYCODE_2, R.id.button2),
            new Keytbl(KeyEvent.KEYCODE_3, R.id.button3),
            new Keytbl(KeyEvent.KEYCODE_4, R.id.button4),
            new Keytbl(KeyEvent.KEYCODE_5, R.id.button5),
            new Keytbl(KeyEvent.KEYCODE_6, R.id.button6),
            new Keytbl(KeyEvent.KEYCODE_7, R.id.button7),
            new Keytbl(KeyEvent.KEYCODE_8, R.id.button8),
            new Keytbl(KeyEvent.KEYCODE_9, R.id.button9),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_0, R.id.button0),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_1, R.id.button1),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_2, R.id.button2),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_3, R.id.button3),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_4, R.id.button4),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_5, R.id.button5),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_6, R.id.button6),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_7, R.id.button7),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_8, R.id.button8),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_9, R.id.button9),
            new Keytbl(KeyEvent.KEYCODE_CTRL_LEFT, R.id.buttonFUNC),
            new Keytbl(KeyEvent.KEYCODE_ALT_LEFT, R.id.buttonMODE),
            new Keytbl(KeyEvent.KEYCODE_SHIFT_LEFT, R.id.buttonSHIFT),
            new Keytbl(KeyEvent.KEYCODE_SHIFT_RIGHT, R.id.buttonSHIFT),
            new Keytbl(KeyEvent.KEYCODE_DPAD_DOWN, R.id.buttonDA),
            new Keytbl(KeyEvent.KEYCODE_DPAD_UP, R.id.buttonUA),
            new Keytbl(KeyEvent.KEYCODE_DPAD_LEFT, R.id.buttonLA),
            new Keytbl(KeyEvent.KEYCODE_DPAD_RIGHT, R.id.buttonRA),
            new Keytbl(KeyEvent.KEYCODE_SPACE, R.id.buttonSPC),
            new Keytbl(KeyEvent.KEYCODE_ENTER, R.id.buttonEXE),
            new Keytbl(0, R.id.buttonANS),
            new Keytbl(KeyEvent.KEYCODE_EQUALS, R.id.buttonEQ),
            new Keytbl(KeyEvent.KEYCODE_CLEAR, R.id.buttonAC),
            new Keytbl(KeyEvent.KEYCODE_DEL, R.id.buttonDEL),
            new Keytbl(KeyEvent.KEYCODE_ESCAPE, R.id.buttonSTOP),
            new Keytbl(KeyEvent.KEYCODE_SLASH, R.id.buttonDIV),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_DIVIDE, R.id.buttonDIV),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_MULTIPLY, R.id.buttonMLT),
            new Keytbl(KeyEvent.KEYCODE_APOSTROPHE, R.id.buttonMLT),
            new Keytbl(KeyEvent.KEYCODE_MINUS, R.id.buttonMINUS),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_SUBTRACT, R.id.buttonMINUS),
            new Keytbl(KeyEvent.KEYCODE_PLUS, R.id.buttonPLS),
            new Keytbl(KeyEvent.KEYCODE_SEMICOLON, R.id.buttonPLS),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_ADD, R.id.buttonPLS),
            new Keytbl(KeyEvent.KEYCODE_PERIOD, R.id.buttonDOT),
            new Keytbl(KeyEvent.KEYCODE_NUMPAD_DOT, R.id.buttonDOT),
            new Keytbl(0, R.id.buttonEXP),
    };

}
