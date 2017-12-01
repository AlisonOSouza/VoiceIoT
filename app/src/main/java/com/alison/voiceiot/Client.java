package com.alison.voiceiot;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by alison on 22/11/17.
 */

public class Client extends AsyncTask<Void, Void, Void> {

    private String address;
    private int port_num;
    private String textSend;
    private String response;
    private String user;
    private String psswd;
    private static Context context;
    private String msg;

    Client(String addr, int port, String textSend, String user, String psswd, String response, Context context) {
        address = addr;
        port_num = port;
        this.textSend = textSend;
        this.user = user;
        this.psswd = psswd;
        this.response = response;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        Socket socket = null;

        try {
            socket = new Socket(address, port_num);

            PrintStream saida =  new PrintStream(socket.getOutputStream());

            String send = user + "#" + psswd + "#" + textSend;

            saida.print(send);

            byte[] bytes = new byte[textSend.length()];
            int aux = socket.getInputStream().read(bytes);
            msg = new String(bytes);

        } catch (UnknownHostException e) {
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if(socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //textResponse.setText(response);
        if(textSend.equals(msg)) {
            Toast.makeText(context, "Mensagem enviada!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
        super.onPostExecute(result);
    }
}
