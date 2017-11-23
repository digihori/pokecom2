package tk.horiuchi.pokecom2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import static tk.horiuchi.pokecom2.MainActivity.debug_info;
import static tk.horiuchi.pokecom2.MainActivity.vibrate_enable;

public class MyPreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        setTitle(R.string.app_name);

    }

    @Override
    public void onPause() {
        super.onPause();

        // 設定値をロード
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        debug_info = sp.getBoolean("debug_checkbox_key", false);
        vibrate_enable = sp.getBoolean("vibrator_checkbox_key", true);
    }
}
