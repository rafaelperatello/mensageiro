package br.edu.ifspsaocarlos.sdm.mensageirosdm.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.adapter.ContactAdapter;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.User;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;

    private Realm realm;
    private RealmConfiguration realmConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the Realm configuration
        realmConfig = new RealmConfiguration.Builder(this).build();
        // Open the Realm for the UI thread.
        realm = Realm.getInstance(realmConfig);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);

        FetchUsersTask fetchUsersTask = new FetchUsersTask();
        fetchUsersTask.execute("http://www.nobile.pro.br/sdm/mensageiro/contato");
    }

    /*
    private void checkUser() {
        RealmQuery<User> query = realm.where(User.class);
        query.count();

        Log.e("SDM", "query size: " + query.count());

        if (query.count() <= 0) {
            showUserActivity();
        }
    }

    private void showUserActivity() {
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
    }*/

    private class FetchUsersTask extends AsyncTask<String, Void, List<Contact>> {

        @Override
        protected List<Contact> doInBackground(String... params) {
            List<Contact> contactList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();

            try {
                HttpURLConnection conexao = (HttpURLConnection) (new URL(params[0])).openConnection();

                if (conexao.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conexao.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String temp;

                    while ((temp = br.readLine()) != null) {
                        sb.append(temp);
                    }

                    JSONObject jsonObject = new JSONObject(sb.toString());
                    JSONArray jsonArray = jsonObject.getJSONArray("contatos");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Log.e("SDM", json.toString());

                        Contact contact = new Contact();
                        contact.setName(json.getString("nome_completo"));
                        contact.setMessage(json.getString("apelido"));
                        contact.setTime(json.getString("id"));

                        contactList.add(contact);
                    }
                }

            } catch (IOException ioe) {
                Log.e("SDM", "Erro na recuperação de texto");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("SDM", "Erro no parse do json");
            }

            return contactList;
        }

        @Override
        protected void onPostExecute(List<Contact> contactList) {
            contactAdapter = new ContactAdapter(contactList);
            recyclerView.setAdapter(contactAdapter);
        }
    }
}
