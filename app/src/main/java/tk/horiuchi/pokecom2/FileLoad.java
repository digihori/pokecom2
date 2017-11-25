package tk.horiuchi.pokecom2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.File;

import static tk.horiuchi.pokecom2.MainActivity.src_path;

public class FileLoad extends Activity implements FileSelectDialog.OnFileSelectDialogListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadfile);


    }

    /**
     * ファイル選択イベント
     */
    public void onClickFileSelect(View view) {

        // ファイル選択ダイアログを表示
        FileSelectDialog dialog = new FileSelectDialog(this);
        dialog.setOnFileSelectDialogListener(this);

        // 表示
        //dialog.show(Environment.getExternalStorageDirectory().getPath());
        dialog.show(src_path);
    }

    /**
     * ファイル選択完了イベント
     */
    @Override
    public void onClickFileSelect(File file) {

        if (file != null) {
            // 選択ファイルを設定
            EditText txt=(EditText)findViewById(R.id.dirPath);
            String dir = file.getPath();
            txt.setText(dir);
            txt.setSelection(dir.length());

            String[] talken = dir.split("/", 0);
            //if (talken.length == 0) return;
            String filename = talken[talken.length - 1];
            String[] talken2 = filename.split("\\.", 0);
            if (talken2.length < 2) return;
            Log.w("FileLoad", String.format("%s | %s", talken2[0], talken2[1]));


        }
    }

    public void btnOk(View view) {
        String filename = ((EditText)findViewById(R.id.dirPath)).getText().toString();
        if (filename.length() == 0) {
            filename = null;
        }
        Intent data = new Intent();
        data.putExtra("filePath", filename);

        setResult(RESULT_OK, data);
        finish();
    }

    public void btnCancel(View view) {
        Intent data = new Intent();
        data.putExtra("key.canceledData", "CANCEL");
        setResult(RESULT_CANCELED, data);
        finish();
    }


}
