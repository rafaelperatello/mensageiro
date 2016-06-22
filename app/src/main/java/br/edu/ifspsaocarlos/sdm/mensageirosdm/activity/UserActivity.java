package br.edu.ifspsaocarlos.sdm.mensageirosdm.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;

public class UserActivity extends AppCompatActivity {
    private final String PREFERENCES_KEY = "PREFERENCES";
    private final String USER_KEY = "USER";

    private SharedPreferences sharedpreferences;

    private EditText editName;
    private EditText editNickname;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        sharedpreferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);

        editName = (EditText) findViewById(R.id.edit_name);
        editNickname = (EditText) findViewById(R.id.edit_nickname);

        buttonSave = (Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUser();
            }
        });

        checkUser();
    }

    private void checkUser() {
        if (sharedpreferences.contains(USER_KEY)) {
            showHome();
        }
    }

    private void showHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void saveUser() {
        String name = editName.getText().toString();
        String nickname = editNickname.getText().toString();

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("nome_completo", name);
            jsonObject.put("apelido", nickname);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SaveUsersTask saveUsersTask = new SaveUsersTask();
        saveUsersTask.execute(jsonObject);
    }

    private void saveUserOnPreferences(String id) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(USER_KEY, id);
        editor.commit();
    }

    private class SaveUsersTask extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected String doInBackground(JSONObject... params) {
            StringBuilder sb = new StringBuilder();

            try {
                HttpURLConnection conexao = (HttpURLConnection) (new URL("http://www.nobile.pro.br/sdm/mensageiro/contato")).openConnection();
                conexao.setRequestMethod("POST");
                conexao.setDoOutput(true);

                if (params[0] != null) {
                    OutputStreamWriter wr = new OutputStreamWriter(conexao.getOutputStream());
                    wr.write(params[0].toString());
                    wr.flush();
                    wr.close();
                }

                if (conexao.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    InputStream is = conexao.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String temp;

                    while ((temp = br.readLine()) != null) {
                        sb.append(temp);
                    }

                    Log.e("SDM", sb.toString());
                }

            } catch (IOException ioe) {
                Log.e("SDM", "Erro na recuperação de texto");
            }

            return sb.toString();
        }

        @Override
        protected void onPostExecute(String data) {
            JSONObject json = null;
            try {
                json = new JSONObject(data);

                if (data.contains("id")) {
                    saveUserOnPreferences(json.getString("id"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
