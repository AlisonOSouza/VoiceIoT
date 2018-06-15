package com.alison.voiceiot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.json.JSONObject;

/**
 * Created by alison on 15/06/2018.
 */

public class VoiceActivity extends AppCompatActivity implements SensorEventListener {
    private static final int REQUEST_CODE = 1234;
    private Button speakButton;
    private SensorManager sManager;
    private TextToSpeech tts = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sManager.SENSOR_DELAY_NORMAL);

        voiceScreen();
    }

    public void voiceScreen() {
        setContentView(R.layout.activity_voice);
        speakButton = (Button) findViewById (R.id.speakButton);

        // Desabilitamos o botao caso nao tenha o servico
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
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
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Se foi chamado pela startVoiceRecognitionActivity() de voiceScreen()
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String type = "";
            String requisition = "";
            String path = "";

            for(int i = 0; i < matches.size(); i++) {
                final String message = matches.get(i).toLowerCase();
                // DEBUG
                System.err.println("Mensagem: " + message);

                if(message.contains("test")) {
                    type = "test";
                    requisition = "test";
                    break;
                } else if(message.contains("lâmpada") && (message.contains("desligar") || message.contains("apagar"))) {
                    requisition = "{\"roomLightOn\":0}";
                    type = "sendConfig";
                    path = "sendLightConfiguration";
                    break;
                } else if (message.contains("lâmpada") && (message.contains("ligar") || message.contains("acender"))) {
                    requisition = "{\"roomLightOn\":1}";
                    type = "sendConfig";
                    path = "sendLightConfiguration";
                    break;
                } else if(message.contains("lâmpada") && message.contains("verificar")) {
                    requisition = "roomLightOn";
                    type = "getConfig";
                    break;
                } else if(message.contains("tomada") && message.contains("desligar")) {
                    requisition = "{\"wemoOn\":0}";
                    type = "sendConfig";
                    path = "sendWemoConfiguration";
                    break;
                } else if (message.contains("tomada") && message.contains("ligar")) {
                    requisition = "{\"wemoOn\":1}";;
                    type = "sendConfig";
                    path = "sendWemoConfiguration";
                    break;
                } else if(message.contains("tomada") && message.contains("verificar")) {
                    requisition = "wemoOn";
                    type = "getConfig";
                    break;
                } else if (message.contains("persiana") && message.contains("abrir")) {
                    requisition = "{\"roomOpenShade\":1}";;
                    type = "sendConfig";
                    path = "sendShadeConfiguration";
                    break;
                } else if(message.contains("persiana") && message.contains("fechar")) {
                    requisition = "{\"roomOpenShade\":0}";
                    type = "sendConfig";
                    path = "sendShadeConfiguration";
                    break;
                } else if(message.contains("persiana") && message.contains("verificar")) {
                    requisition = "roomOpenShade";
                    type = "getConfig";
                    break;
                } else if(message.contains("ar condicionado") && message.contains("desligar")) {
                    requisition = "{\"acuAirOn\":0}";
                    type = "sendConfig";
                    path = "sendAirConfiguration";
                    break;
                } else if(message.contains("ar condicionado") && message.contains("ligar")) {
                    requisition = "{\"acuAirOn\":1}";
                    type = "sendConfig";
                    path = "sendAirConfiguration";
                    break;
                } else if(message.contains("ar condicionado") && message.contains("temperatura")) {
                    // split string in letters and numbers
                    String[] parts = message.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    int temperature = -1;
                    for(int j = 0; j < parts.length; j++) {
                        if(isNumeric(parts[j])) {
                            temperature = Integer.parseInt(parts[j]);
                            break;
                        }
                    }
                    if(temperature == -1) {
                        speech("Não foi possível identificar a temperatura informada.");
                        return;
                    }
                    requisition = "{\"acuAirOn\":1, \"acuTemperature\":"+String.valueOf(temperature)+"}";
                    type = "sendConfig";
                    path = "sendAirConfiguration";
                    break;
                } else if(message.contains("ar condicionado") && message.contains("verificar")) {
                    requisition = "acuAirOn";
                    type = "getConfig";
                    break;
                }
            }
            // Se não reconheceu nenhum comando.
            if(requisition == "") {
                speech("Comando não reconhecido. Por favor, tente novamente");
                try {
                    throw new Exception("Dispositivo ou comando não reconhecido");
                } catch (Exception e) {
                    return;
                }
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            final String ip_address = preferences.getString("ip_address", null);
            final String port_number = preferences.getString("port_number", "0");

            executeAsyncTaskConnection(ip_address, port_number, requisition, type, path);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void speech(final String text) {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    public boolean isNumeric(String str) {
        try {
            int d = Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public void executeAsyncTaskConnection(String ip, String port, String req, String type, String path) {
        final String ip_address = ip;
        final String port_number = port;
        final String requisition = req;
        final String typeReq = type;
        final String endPath = path;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //------------------------------------------------------------------------//
                //--                      REQUISIÇÃO DE TESTE                           --//
                //------------------------------------------------------------------------//
                // Cria a URL da requisição de teste.
                if(typeReq.equals("test")) {
                    URL testURL = null;
                    try {
                        testURL = new URL("http://" + ip_address + ":" + port_number + "/rest/test");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    //DEBUG
                    System.err.println("REQ: " + requisition);
                    testConnection(testURL);
                }
                //------------------------------------------------------------------------//
                //--                      REQUISIÇÃO DE AÇÃO                            --//
                //------------------------------------------------------------------------//
                else if(typeReq.equals("sendConfig")) {
                    // Cria a URL de requisição.
                    URL reqURL = null;
                    try {
                        reqURL = new URL("http://" + ip_address + ":" + port_number + "/rest/" + endPath);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    //DEBUG
                    System.err.println("REQ: " + requisition);
                    sendConfigConnection(reqURL, requisition);
                }
                //------------------------------------------------------------------------//
                //--                    REQUISIÇÃO DE VERIFICAÇÃO                       --//
                //------------------------------------------------------------------------//
                else if(typeReq.equals("getConfig")) {
                    URL reqURL = null;
                    try {
                        reqURL = new URL("http://" + ip_address + ":" + port_number + "/rest/getConfiguration");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    //DEBUG
                    System.err.println("REQ: " + requisition);
                    getConfigConnection(reqURL, requisition);
                }
            }
        });
    }

    public void testConnection(URL url) {
        HttpURLConnection myConnection = null;
        try {
            // Abre a conexão.
            myConnection = (HttpURLConnection) url.openConnection();
            // Setando os cabeçalhos e tipo de requisição.
            myConnection.setRequestProperty("User-Agent", "voiceiot");
            myConnection.setRequestProperty("Content-Type", "text/plain");
            myConnection.setRequestMethod("GET");
            // Garante permissão de leitura.
            myConnection.setDoInput(true);
            // Seta o timeout de espera pela resposta, em milissegundos.
            myConnection.setConnectTimeout(5000);
            // Envia a requisição.
            myConnection.connect();

            if(myConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Se código de resposta for igual a 200, então OK.
                speech("Conexão de teste realizada com sucesso!");
            }
            else {
                speech("Não foi possível conectar com o servidor Maniot.");
            }
        } catch (IOException e) {
            //DEBUG
            System.err.println("Error on testConnection");
            speech("Não foi possível conectar com o servidor Maniot.");
        } finally {
            myConnection.disconnect();
        }
    }

    public void sendConfigConnection(URL reqURL, String req) {
        // Cria a conexão de requisição.
        HttpURLConnection myConnection = null;
        try {
            // Abre a conexão.
            myConnection = (HttpURLConnection) reqURL.openConnection();
            // Setando os cabeçalhos e tipo de requisição.
            myConnection.setRequestProperty("User-Agent", "voiceiot");
            myConnection.setRequestProperty("Content-Type", "application/json");
            myConnection.setRequestMethod("POST");
            // Cria permissão de escrita.
            myConnection.setDoOutput(true);

            // Envia a requisição.
            myConnection.getOutputStream().write(req.getBytes());
            if(myConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Se o código de resposta for 200 (= HttpURLConnection.HTTP_OK), então dou um bip de confirmação.
                ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                tone.startTone(ToneGenerator.TONE_PROP_BEEP2);
            } else {
                // Senão, dou um aviso informando o erro.
                speech("Não foi possível conectar com o servidor Maniot.");
            }
        } catch (IOException e) {
            //DEBUG
            System.err.println("Error on sendConfigConnection");
            speech("Não foi possível conectar com o servidor Maniot.");
        } finally {
            myConnection.disconnect();
        }
    }

    public void getConfigConnection(URL reqURL, String req) {
        String response = "";
        HttpURLConnection myConnection = null;
        try {
            // Abre a conexão.
            myConnection = (HttpURLConnection) reqURL.openConnection();
            // Setando os cabeçalhos e tipo de requisição.
            myConnection.setRequestProperty("User-Agent", "voiceiot");
            myConnection.setRequestProperty("Content-Type", "application/json");
            myConnection.setRequestMethod("GET");
            // Garante permissão de leitura.
            myConnection.setDoInput(true);

            // Envia a requisição.
            myConnection.connect();
            if (myConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                String line = "";
                StringBuffer buffer = new StringBuffer();
                while((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();
                //DEBUG
                System.err.println(buffer.toString());

                JSONObject myResponse = new JSONObject(buffer.toString());
                response = myResponse.getString(req);

                if(req.equals("roomLightOn")) {
                    if(response.equals("0")) {
                        speech("A lâmpada está ligada");
                    } else {
                        speech("A lâmpada está desligada");
                    }
                } else if(req.equals("wemoOn")) {
                    if(response.equals("0")) {
                        speech("A tomada está desligada");
                    } else {
                        speech("A tomada está ligada");
                    }
                } else if(req.equals("roomOpenShade")) {
                    if(response.equals("0")) {
                        speech("A persiana está fechada");
                    } else {
                        speech("A persiana está aberta");
                    }
                } else if(req.equals("acuAirOn")) {
                    if(response.equals("0")) {
                        speech("O ar condicionado está desligado");
                    } else {
                        String temp = myResponse.getString("acuTemperature");
                        speech("O ar condicionado está ligado e a temperatura configurada é " + temp + " graus");
                    }
                }
            } else {
                speech("Não foi possível conectar com o servidor Maniot.");
            }
        } catch (Exception e) {
            //DEBUG
            System.err.println("Error on getConfigConnection");
            speech("Não foi possível conectar com o servidor Maniot.");
      } finally {
            myConnection.disconnect();
        }
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
    public void onSensorChanged(SensorEvent sensorEvent) {}

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
