package com.alison.voiceiot;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.w3c.dom.Text;

/**
 * Created by alison on 02/11/17.
 */

public class VoiceActivity extends AppCompatActivity implements SensorEventListener {

    public static final String EXTRA_MESSAGE = "com.alison.voiceiot.MESSAGE";

    private static final int REQUEST_CODE = 1234;
    private String telaAtiva;
    private Button speakButton;
    private SensorManager sManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        telaAtiva = "inicial";
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sManager.SENSOR_DELAY_NORMAL);

        voice_tela();
    }

    public void voice_tela() {
        setContentView(R.layout.activity_voice);
        speakButton = (Button) findViewById (R.id.speakButton);

        // Desabilitamos o botao caso nao tenha o servico
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            speakButton.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Reconhecedor de voz não encontrado", Toast.LENGTH_LONG).show();
        }
        speakButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });
    }

    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale agora");
        int requestCode = 1234;
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            for(int i = 0; i < matches.size(); i++) {

                //Intent intent = new Intent(this, DisplayMessageActivity.class);
                String message = matches.get(i);
                /*intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
                //break; // O break pega apenas uma palavra. Sem ele, pega a frase inteira.
                */
                //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                //String syncConnPref = sharedPref.getString(R.xml.preferences., "");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String ip_address = preferences.getString("ip_address", null);
                int port_num = Integer.parseInt(preferences.getString("port_number", "0"));
                String user = preferences.getString("user", null);
                String password = preferences.getString("password", null);
                String resp = "";
                Client myClient = new Client(ip_address, port_num, message, user, password, resp, getApplicationContext());
                myClient.execute();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
