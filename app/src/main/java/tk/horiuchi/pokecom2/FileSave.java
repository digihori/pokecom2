package tk.horiuchi.pokecom2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class FileSave extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savefile);

    }


    public void btnOk(View view) {
        String filename = ((EditText)findViewById(R.id.dirPath)).getText().toString();
        if (filename.length() == 0) {
            filename = null;
        }
        // 返すデータ(Intent&Bundle)の作成
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
