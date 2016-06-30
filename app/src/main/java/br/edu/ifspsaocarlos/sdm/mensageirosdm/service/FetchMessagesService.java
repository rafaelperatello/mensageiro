package br.edu.ifspsaocarlos.sdm.mensageirosdm.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class FetchMessagesService extends Service implements Runnable {
    private boolean stopThread = false;
    private int requestSize;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SDM", "onCreate service ");
        try {
            new Thread(this).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SDM", "onStartCommand service ");
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SDM", "onDestroy service ");
        stopThread = true;
    }

    @Override
    public void run() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String userId = Helpers.getUserId(this);

        Log.d("SDM", "run service ");
        while (!stopThread) {
            try {
                Realm realm = Realm.getDefaultInstance();

                RealmQuery<Contact> queryContacts = realm.where(Contact.class);
                RealmResults<Contact> resultContacts = queryContacts.findAll();

                requestSize = resultContacts.size();

                Log.d("SDM", "queryContacts service " + queryContacts.toString());

                for (Contact contact : resultContacts.subList(0, resultContacts.size())) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(Constants.SERVER_URL);
                    stringBuilder.append(Constants.MENSAGEM_PATH);
                    stringBuilder.append("/0/");
                    stringBuilder.append(contact.getId());
                    stringBuilder.append("/");
                    stringBuilder.append(userId);

                    Log.d("SDM", "request: " + stringBuilder.toString());

                    JsonObjectRequest request = new JsonObjectRequest
                            (Request.Method.GET, stringBuilder.toString(), null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject json) {
                                    requestSize--;
                                    Log.d("SDM", "onResponse service " + requestSize);
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    requestSize--;
                                    Log.d("SDM", "onResponse service " + requestSize);
                                }
                            });

                    requestQueue.add(request);
                }

                while (requestSize != 0) {
                    Thread.sleep(10);
                }

                Thread.sleep(5000);

                Log.d("SDM", "endRequestBurst service ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
