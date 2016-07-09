package br.edu.ifspsaocarlos.sdm.mensageirosdm.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.widget.TextView;

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
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Connection;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;
import io.realm.Realm;

public class ContactActivity extends AppCompatActivity implements ContactAdapter.OnContactClickListener {

    private ProgressDialog dialog;
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private boolean stopThread;
    private int count;

    @Override
    public void onContactClickListener(int position) {
        saveContact(contactAdapter.getItem(position));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        count = 0;
        dialog = new ProgressDialog(this);

        // setup toolBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Adicionar Contato");

        TextView tvConnection = new TextView(this);
        tvConnection.setText(R.string.txt_no_connection);
        tvConnection.setGravity(Gravity.CENTER);
        setContentView(tvConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Handler handler = new Handler();

        stopThread = false;
        new Thread() {
            public void run() {
                try {
                    boolean ok = false;
                    while ((!ok) && (!stopThread)) {
                        ok = Connection.connectionVerify(getBaseContext());

                        if (ok) {
                            handler.post(new Runnable() {
                                public void run() {
                                    loadUsers();
                                }
                            });
                        }
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopThread = true;
    }

    private void loadUsers() {
        setContentView(R.layout.activity_contact);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(false);

        fetchUsers();
    }

    private void fetchUsers() {
        dialog.setTitle(R.string.pb_dialog_title);
        dialog.setMessage(getString(R.string.pb_dialog_message));
        dialog.show();

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, Constants.SERVER_URL + Constants.CONTATO_PATH, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject json) {
                        parseUserList(json);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            count++;
                            if (count > 3) {
                                Helpers.showDialog(ContactActivity.this, R.string.dialog_content_error_fetching_user);
                            } else {
                                fetchUsers();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

                if (!contact.getId().equals(Helpers.getUserId(this)))
                    contactList.add(contact);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        updateAdapter(contactList);
        dialog.dismiss();
    }

    private void updateAdapter(List<Contact> contactList) {
        contactAdapter = new ContactAdapter(contactList, this);
        recyclerView.setAdapter(contactAdapter);
    }

    private void saveContact(final Contact contactList) {
        if (contactList != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    bgRealm.copyToRealmOrUpdate(contactList);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    onBackPressed();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    onBackPressed();
                }
            });
        }
    }
}
