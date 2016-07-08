package br.edu.ifspsaocarlos.sdm.mensageirosdm.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.network.VolleyHelper;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;

public class UserActivity extends AppCompatActivity {
    private EditText editName;
    private EditText editNickname;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        editName = (EditText) findViewById(R.id.edit_name);
        editNickname = (EditText) findViewById(R.id.edit_nickname);

        buttonSave = (Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserOnPreferences("400"); //405 rafa!
                checkUserInfo();
            }
        });
    }

    private void showHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void checkUserInfo() {
        String name = editName.getText().toString();
        String nickname = editNickname.getText().toString();

        if ((TextUtils.isEmpty(name)) || (TextUtils.isEmpty(nickname))) {
            Helpers.showDialog(this, R.string.dialog_content_error_invalid);
            return;
        }

        saveUser(name, nickname);
    }

    private void saveUserOnPreferences(String id) {
        SharedPreferences sharedpreferences = getSharedPreferences(Constants.PREFERENCES_KEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Constants.USER_KEY, id);
        editor.commit();

        showHome();
    }

    private void saveUser(String name, String nickname) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("nome_completo", name);
            jsonObject.put("apelido", nickname);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, Constants.SERVER_URL + Constants.CONTATO_PATH, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject json) {
                        try {
                            String id = json.getString("id");

                            if ((!TextUtils.isEmpty(id)) && (!"0".equals(id)))
                                saveUserOnPreferences(id);
                            else
                                Helpers.showDialog(UserActivity.this, R.string.dialog_content_error_saving_profile);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helpers.showDialog(UserActivity.this, R.string.dialog_content_error_saving_profile);
                    }
                });

        VolleyHelper.getInstance(this).addToRequestQueue(request);
    }
}
