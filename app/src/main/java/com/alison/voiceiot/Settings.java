package com.alison.voiceiot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by alison on 20/11/17.
 */

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializa o aplicativo com as configuraçoes padrao.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // Mostra as configuraçoes na tela.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            //SharedPreferences pref = this.getActivity().getSharedPreferences("", Context.MODE_PRIVATE);
            //IP_ADDRESS = pref.getString()

            //SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
            //IP_ADDRESS = getResources().getString("ip_address", );
        }
    }
}
