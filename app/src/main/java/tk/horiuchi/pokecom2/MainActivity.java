package tk.horiuchi.pokecom2;

import android.graphics.Point;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import static tk.horiuchi.pokecom2.Common.type10inch;
import static tk.horiuchi.pokecom2.Common.type7inch;
import static tk.horiuchi.pokecom2.Common.typePhone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static float dpdx, dpdx_org;
    public static int deviceType;
    public final static String src_path = Environment.getExternalStorageDirectory().getPath()+"/Basic1";
    private Common define;
    private PbMain pb = null;
    private Keyboard inkey = null;
    public static Lcd lcd = null;
    private static SBasic basic = null;
    public static boolean shift = false;
    public static boolean ext = false;
    public static boolean mode = false;
    public static boolean function = false;
    private Vibrator vib;

    public static boolean initial; // 仮

    private int[] mBtnResIds = {
            R.id.buttonDA, R.id.buttonUA, R.id.buttonMODE, R.id.buttonLA, R.id.buttonRA,
            R.id.buttonSHIFT, R.id.buttonFANCTION,

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 共通定義
        define = new Common();

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

        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Buttonインスタンスの取得
        // ButtonインスタンスのリスナーをこのActivityクラスそのものにする
        for (int i = 0; i < mBtnResIds.length; i++) {
            findViewById(mBtnResIds[i]).setOnClickListener(this);
        }
        // ボタンの枠表示を切り替える
        changeButtonFrame(mBtnResIds, false);

        // 仮
        initial = true;
        lcd.print("READY P0");

        try {
            basic = new SBasic(lcd);
            //basic.load(src_path + "/sample1.bas");
            //basic.run();

            //basic.calc("A=1+2*3");
            //basic.calc("A");
            //Log.w("Main", "----- prog completed!!! -----");
        } catch (InterpreterException e) {
            Log.w("Main", String.format("error='%s'", e.toString()));
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

    public void onClick(View v) {
        int c = v.getId();

        Log.w("Click", String.format("key=%d", c));

        if (initial) {
            initial = false;
            lcd.cls();
        }

        switch (c) {
            case R.id.buttonLA:
                lcd.moveLeft();
                break;
            case R.id.buttonRA:
                lcd.moveRight();
                break;
            case R.id.buttonDEL:
                if (shift) {
                    lcd.insert();
                } else {
                    lcd.delete();
                }
                shift = false;
                function = false;
                break;
            case R.id.buttonAC:
                lcd.cls();
                shift = false;
                function = false;
                mode = false;
                break;
            case R.id.buttonSHIFT:
                if (!function) {
                    shift = !shift;
                    lcd.refresh();
                }
                break;
            case R.id.buttonFANCTION:
                if (!mode) {
                    function = !function;
                    shift = false;
                    lcd.refresh();
                }
                break;
            case R.id.buttonMODE:
                mode = true;
                function = false;
                break;
            case R.id.buttonEXE:
                String s = lcd.getCmdBuf();
                if (s == null) break;
                try {
                    Log.w("EXE", String.format("%s", s));
                    basic.calc(lcd.getCmdBuf());
                } catch (InterpreterException e) {
                    Log.w("Main", String.format("error='%s'", e.toString()));
                }
                initial = true;
                break;
            case R.id.buttonANS:
                basic.lastAns();
                break;
            case R.id.buttonDOT:
                if (mode) {
                    mode = false;
                    ext = !ext;
                    lcd.refresh();
                    break;
                }
                // no break!!
            default:
                int code = inkey.getKeyCode(c);
                if (code != 0) lcd.putchar(code);
                shift = false;
                function = false;
                break;
        }

        vib.vibrate(10);
    }

}
