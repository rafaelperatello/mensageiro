package br.edu.ifspsaocarlos.sdm.mensageirosdm.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.adapter.ContactAdapter;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.network.VolleyHelper;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;
import io.realm.Realm;
import io.realm.RealmConfiguration;

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
        recyclerView.setHasFixedSize(false);

        fetchUsers();
    }

    private void fetchUsers() {
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, Constants.SERVER_URL + Constants.CONTATO_PATH, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject json) {
                        parseUserList(json);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helpers.showDialog(MainActivity.this, R.string.dialog_content_error_fetching_user);
                    }
                });

        VolleyHelper.getInstance(this).addToRequestQueue(request);
    }

    private void parseUserList(JSONObject jsonRoot) {
        List<Contact> contactList = new ArrayList<>();

        try {
            JSONArray jsonArray = jsonRoot.getJSONArray("contatos");
            Gson gson = new Gson();

            for (int i = 0; i < jsonArray.length(); i++) {
                Contact contact = gson.fromJson(jsonArray.getJSONObject(i).toString(), Contact.class);
                contactList.add(contact);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        contactAdapter = new ContactAdapter(contactList);
        recyclerView.setAdapter(contactAdapter);
    }
}
