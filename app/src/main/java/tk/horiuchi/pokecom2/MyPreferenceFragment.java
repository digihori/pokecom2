package tk.horiuchi.pokecom2;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by yoshimine on 2017/11/24.
 */

public class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }
}
