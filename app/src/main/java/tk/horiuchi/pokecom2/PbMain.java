package tk.horiuchi.pokecom2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Timer;
import java.util.TimerTask;

import static tk.horiuchi.pokecom2.Common.MODE_PRO;
import static tk.horiuchi.pokecom2.Common.MODE_RUN;
import static tk.horiuchi.pokecom2.MainActivity.angleUnit;
import static tk.horiuchi.pokecom2.MainActivity.basic;
import static tk.horiuchi.pokecom2.MainActivity.dpdx;
import static tk.horiuchi.pokecom2.MainActivity.keyExt;
import static tk.horiuchi.pokecom2.MainActivity.keyFunc;
import static tk.horiuchi.pokecom2.MainActivity.keyMaskCnt;
import static tk.horiuchi.pokecom2.MainActivity.keyShift;
import static tk.horiuchi.pokecom2.MainActivity.mode;
import static tk.horiuchi.pokecom2.MainActivity.bank;

/**
 * Created by yoshimine on 2017/11/18.
 */

public class PbMain extends SurfaceView implements RefreshScreenInterFace, SurfaceHolder.Callback {
    private PbMain thread = null;
    private SurfaceHolder holder;
    private int refresh_cnt = 0;
    private boolean disp_on = true;

    private final int column = 12;
    private final int digit = 5 * column;
    public static byte digi[];

    private boolean pause = false;


    public PbMain(Context context, SurfaceView sv) {
        super(context);
        holder = sv.getHolder();
        holder.addCallback(this);
        holder.setFixedSize(getWidth(), getHeight());

        Log.w("PbMain", "--------------SurfaceView started!----------------");

        digi = new byte[digit];
        for (int i=0; i<digit; i++) {
            digi[i] = 0;
        }
        bank = 0;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {

        Timer timer1 = new Timer();
        timer1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (refresh_cnt != 0) {
                    refresh_cnt = 0;
                    _refreshScreen();
                }
            }
        }, 0, 50);

        Log.w("PbMain", "--------------SurfaceView surfaceChanged!----------------");
        //refreshScreen();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        doDraw(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread=null;
    }

    /*
    @Override
    public void run() {
        Log.w("PbMain", "Thread start !!!");
        try {
            basic.run();
        } catch (InterpreterException e) {

        }
        thread = null;
        //pause = false;
        Log.w("PbMain", "Thread finish !!!");
    }
    */

    protected void doDraw(SurfaceHolder holder) {
        //描画処理を開始
        Canvas c = holder.lockCanvas();

        if (c != null) {
            c.scale(dpdx, dpdx);

            int x_org, y_org, stp_x, stp_y, d_row, d_col;
            int x, y;
            int i, j, k;
            Paint p = new Paint();

            x_org = 18;
            y_org = 40;
            stp_x = 5;
            stp_y = 6;
            d_row=7;
            d_col=5;
            c.drawColor(0xFFEEFFFF);

            p.setStyle(Paint.Style.FILL);

            for (k = 0, x = x_org; k < column; k++) {
                for (j = 0; j < d_col; j++, x += stp_x) {
                    for (i = 0, y = y_org; i < d_row; i++, y += stp_y) {
                        if (disp_on && (digi[k * d_col + j] & 0x01 << i) != 0) {
                            p.setColor(Color.DKGRAY);
                        } else {
                            p.setColor(0xffdcdcdc);
                        }
                        c.drawRect(x+1, y+1, x + stp_x, y + stp_y, p);
                    }
                }
                x += stp_x;
            }

            // シンボル表示
            p.setTextSize(14);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            if (keyExt) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("EXT", 18, 20, p);

            if (keyShift) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            p.setStyle(Paint.Style.STROKE);
            c.drawRect(48, 1, 60, 14, p);
            p.setStyle(Paint.Style.FILL);
            c.drawText("S", 50, 13, p);
            if (keyFunc) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            p.setStyle(Paint.Style.STROKE);
            c.drawRect(48, 15, 60, 28, p);
            p.setStyle(Paint.Style.FILL);
            c.drawText("F", 50, 27, p);

            if (mode == MODE_RUN) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("RUN", 62, 14, p);
            if (mode == MODE_PRO) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("WRT", 62, 26, p);

            if (angleUnit == 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("DEG", 96, 20, p);
            if (angleUnit == 1) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("RAD", 125, 20, p);
            if (angleUnit == 2) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("GRA", 154, 20, p);

            if (false) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("TR", 194, 20, p);

            if (false) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("PRT", 300, 20, p);
            if (pause) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("STOP", 334, 20, p);

            //描画処理を終了
            holder.unlockCanvasAndPost(c);
        }

    }


    public void refreshScreen() {
        refresh_cnt++;
        //Log.w("ref", "refreshScreen called.....");
    }

    private void _refreshScreen() {
        doDraw(holder);
    }

    public void progStart() {
        progStart(null);
    }
    public void progStart(String s) {
        if (thread == null) {
            //thread = new Thread(this);
            //thread.start();

            new Thread(new Runnable() {
                String lavel;
                public Runnable setStartLine(String s) {
                    lavel = s;
                    return this;
                }

                @Override
                public void run() {
                    Log.w("PbMain", "Thread start !!!");
                    try {
                        basic.run(lavel);
                    } catch (InterpreterException e) {

                    }
                    thread = null;
                    //pause = false;
                    Log.w("PbMain", "Thread finish !!!");
                }
            }.setStartLine(s)
            ).start();
            thread = this;
        }
    }


    public void progStop() {
        if (thread != null) {
            pause = true;
            keyMaskCnt = 15;
            Log.w("PbMain", "progStop");
            refreshScreen();
        }
    }

    public void progRestart() {
        pause = false;
        Log.w("PbMain", "progRestart");
    }

    public boolean isProgStop() {
        return pause;
    }

    public boolean isProgExist() {
        return (thread == null ? false : true);
    }

    public boolean isProgRunning() {
        return ((thread != null && !pause) ? true : false);
    }

    public void progEnd() {
        thread = null;
    }
}
