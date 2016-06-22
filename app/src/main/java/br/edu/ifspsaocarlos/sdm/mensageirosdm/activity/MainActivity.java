package br.edu.ifspsaocarlos.sdm.mensageirosdm.activity;

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

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);

        FetchUsersTask fetchUsersTask = new FetchUsersTask();
        fetchUsersTask.execute("http://www.nobile.pro.br/sdm/mensageiro/contato");
    }

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


/*
        List<Contact> contactList = new ArrayList<>();
        contactList.add(new Contact("Contato 1", "Mensagem 1", "12:00"));
        contactList.add(new Contact("Contato 2", "Mensagem 2", "12:00"));
        contactList.add(new Contact("Contato 3", "Mensagem 3", "12:00"));
*/
