package br.edu.ifspsaocarlos.sdm.mensageirosdm.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.ContactMessage;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Message;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;

class MyAsyncTask extends AsyncTask<Void, Void, Void> {
    private RequestQueue requestQueue;
    private String userId;
    private RealmQuery<Contact> queryContacts;
    private RealmResults<Contact> resultContacts;
    private RealmResults<ContactMessage> resultContactMessages;
    private int requestSize;
    private Context context;

    public MyAsyncTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("SDM", "onPreExecute");
        requestQueue = Volley.newRequestQueue(context);
        userId = Helpers.getUserId(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("SDM", "doInBackground");
        Realm realm = Realm.getDefaultInstance();

        queryContacts = realm.where(Contact.class);
        resultContacts = queryContacts.findAll();
        requestSize = resultContacts.size();

        for (final Contact contact : resultContacts.subList(0, resultContacts.size())) {
            ContactMessage conMenssage = realm.where(ContactMessage.class).equalTo("id", contact.getId()).findFirst();

            if (conMenssage != null)
            {
                Log.d("SDM", "FOR: " + conMenssage.getLastMessageId());
                Executar(conMenssage.getLastMessageId(), conMenssage.getId());
            }
            else
            {
                Log.d("SDM", "FOR: " + "0");
                Executar("0", contact.getId());
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void Executar(String idMenssage, String idContact) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.SERVER_URL);
        stringBuilder.append(Constants.MENSAGEM_PATH);
        //stringBuilder.append("/0/");
        stringBuilder.append("/" + idMenssage + "/");
        stringBuilder.append(idContact);
        stringBuilder.append("/");
        stringBuilder.append(userId);

        Log.d("SDM", "request: " + stringBuilder.toString());

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, stringBuilder.toString(), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject json) {
                        requestSize--;
                        Log.d("SDM", "onResponse service " + requestSize);
                        parseMessageList(json);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        requestSize--;
                        Log.d("SDM", "onErrorResponse service " + requestSize);
                    }
                });

        requestQueue.add(request);
    }


    private void parseMessageList(JSONObject jsonRoot) {
        List<Message> messageList = new ArrayList<>();

        try {
            JSONArray jsonArray = jsonRoot.getJSONArray("mensagens");
            Gson gson = new Gson();

            for (int i = 0; i < jsonArray.length(); i++) {
                Message message = gson.fromJson(jsonArray.getJSONObject(i).toString(), Message.class);
                messageList.add(message);
                Log.d("SDM", "Message: " + message.getId() + " de: " + message.getOrigem_id());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        saveMessages(messageList);
    }

    private void saveMessages(final List<Message> messageList) {
        Log.d("SDM", "saveMessages");
        if (messageList != null) {
            if (messageList.size() > 0) {
                Realm realm = Realm.getDefaultInstance();
                //        realm.executeTransaction(new Realm.Transaction() {
                //            @Override
                //            public void execute(Realm realm) {
                //                realm.copyToRealmOrUpdate(contactList);
                //            }
                //        });
                //        updateAdapter();

                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        Log.d("SDM", "execute message");
                        bgRealm.copyToRealmOrUpdate(messageList);
                        //                    bgRealm.commitTransaction();
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Log.d("SDM", "onSuccess message: ");
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        Log.d("SDM", "onError message: " + error.toString());
                    }
                });

                Message mensagem = messageList.get(messageList.size() - 1);
                final ContactMessage conMen = new ContactMessage();
                conMen.setId(mensagem.getOrigem_id());
                conMen.setLastMessageId(mensagem.getId());
                Log.d("SDM", "===================");
                Log.d("SDM", conMen.getId() + " - " + conMen.getLastMessageId());
                Log.d("SDM", "===================");
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        Log.d("SDM", "execute relação");
                        bgRealm.copyToRealmOrUpdate(conMen);
                        //                    bgRealm.commitTransaction();
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Log.d("SDM", "onSuccess relação: ");
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        Log.d("SDM", "onError relação: " + error.toString());
                    }
                });
            }
        }
    }

    @Override
    protected void onPostExecute(Void s) {
        super.onPostExecute(s);
        Log.d("SDM", "onPostExecute");
    }
}


public class FetchMessagesService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SDM", "onCreate service ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d("SDM", "onStartCommand service ");
//        final AsyncTask<Void, Void, Void> tarefa = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                Log.d("SDM", "onPreExecute");
//                requestQueue = Volley.newRequestQueue(getApplicationContext());
//                userId = Helpers.getUserId(getApplicationContext());
//            }
//            @Override
//            protected Void doInBackground(Void... params) {
//                Log.d("SDM", "doInBackground");
//                Realm realm = Realm.getDefaultInstance();
//
//                queryContacts = realm.where(Contact.class);
//                resultContacts = queryContacts.findAll();
//
//                requestSize = resultContacts.size();
//
//                for (Contact contact : resultContacts.subList(0, resultContacts.size())) {
//                    StringBuilder stringBuilder = new StringBuilder();
//                    stringBuilder.append(Constants.SERVER_URL);
//                    stringBuilder.append(Constants.MENSAGEM_PATH);
//                    stringBuilder.append("/0/");
//                    stringBuilder.append(contact.getId());
//                    stringBuilder.append("/");
//                    stringBuilder.append(userId);
//
//                    Log.d("SDM", "request: " + stringBuilder.toString());
//
//                    JsonObjectRequest request = new JsonObjectRequest
//                            (Request.Method.GET, stringBuilder.toString(), null, new Response.Listener<JSONObject>() {
//
//                                @Override
//                                public void onResponse(JSONObject json) {
//                                    requestSize--;
//                                    Log.d("SDM", "onResponse service " + requestSize);
//                                }
//                            }, new Response.ErrorListener() {
//
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    requestSize--;
//                                    Log.d("SDM", "onErrorResponse service " + requestSize);
//                                }
//                            });
//
//                    requestQueue.add(request);
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                return null;
//            }
//            @Override
//            protected void onPostExecute(Void s) {
//                super.onPostExecute(s);
//                Log.d("SDM", "onPostExecute");
//            }
//        };

        new Thread() {
            public void run() {
                Log.d("SDM", "run!!");
                MyAsyncTask tarefa = new MyAsyncTask(getApplication());
                while (true) {
                    try {
                        switch (tarefa.getStatus()) {
                            case PENDING:
                                tarefa.execute();
                                break;

                            case FINISHED:
                                tarefa = new MyAsyncTask(getApplication());
                                break;

                            default:
                                Thread.sleep(100);
                        }
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();


//trava o main thread
//        try {
//            handler = new Handler();
//
//            new Thread(){
//                public void run() {
//                    Log.d("SDM", "run service ");
//                    requestQueue = Volley.newRequestQueue(getApplicationContext());
//                    userId = Helpers.getUserId(getApplicationContext());
//
//                    while (!stopThread) {
//                        try {
//                            handler.post(new Runnable() {
//                                public void run() {
//                                    Realm realm = Realm.getDefaultInstance();
//
//                                    queryContacts = realm.where(Contact.class);
//                                    resultContacts = queryContacts.findAll();
//
//                                    requestSize = resultContacts.size();
//                                    if (resultContacts.isLoaded()) {
//                                        Log.d("SDM", "OK");
//                                        for (Contact contact : resultContacts.subList(0, resultContacts.size())) {
//                                            StringBuilder stringBuilder = new StringBuilder();
//                                            stringBuilder.append(Constants.SERVER_URL);
//                                            stringBuilder.append(Constants.MENSAGEM_PATH);
//                                            stringBuilder.append("/0/");
//                                            stringBuilder.append(contact.getId());
//                                            stringBuilder.append("/");
//                                            stringBuilder.append(userId);
//
//                                            Log.d("SDM", "request: " + stringBuilder.toString());
//
//                                            JsonObjectRequest request = new JsonObjectRequest
//                                                    (Request.Method.GET, stringBuilder.toString(), null, new Response.Listener<JSONObject>() {
//
//                                                        @Override
//                                                        public void onResponse(JSONObject json) {
//                                                            requestSize--;
//                                                            Log.d("SDM", "onResponse service " + requestSize);
//                                                        }
//                                                    }, new Response.ErrorListener() {
//
//                                                        @Override
//                                                        public void onErrorResponse(VolleyError error) {
//                                                            requestSize--;
//                                                            Log.d("SDM", "onResponse service " + requestSize);
//                                                        }
//                                                    });
//
//                                            requestQueue.add(request);
//                                            try {
//                                                Thread.sleep(500);
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    }
//                                }
//                            });
//                            Log.d("SDM", "endRequestBurst service ");
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

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
        stopSelf();
    }
}


//===========ANTES
//public class FetchMessagesService extends Service implements Runnable {
//    private boolean stopThread = false;
//    private int requestSize;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.d("SDM", "onCreate service ");
//        try {
//            new Thread(this).start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("SDM", "onStartCommand service ");
//        return Service.START_NOT_STICKY;
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d("SDM", "onDestroy service ");
//        stopThread = true;
//    }
//
//    @Override
//    public void run() {
//        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//        String userId = Helpers.getUserId(this);
//
//        Log.d("SDM", "run service ");
//        while (!stopThread) {
//            try {
//                Realm realm = Realm.getDefaultInstance();
//
//                RealmQuery<Contact> queryContacts = realm.where(Contact.class);
//                RealmResults<Contact> resultContacts = queryContacts.findAll();
//
//                requestSize = resultContacts.size();
//
//                Log.d("SDM", "queryContacts service " + queryContacts.toString());
//
//                for (Contact contact : resultContacts.subList(0, resultContacts.size())) {
//                    StringBuilder stringBuilder = new StringBuilder();
//                    stringBuilder.append(Constants.SERVER_URL);
//                    stringBuilder.append(Constants.MENSAGEM_PATH);
//                    stringBuilder.append("/0/");
//                    stringBuilder.append(contact.getId());
//                    stringBuilder.append("/");
//                    stringBuilder.append(userId);
//
//                    Log.d("SDM", "request: " + stringBuilder.toString());
//
//                    JsonObjectRequest request = new JsonObjectRequest
//                            (Request.Method.GET, stringBuilder.toString(), null, new Response.Listener<JSONObject>() {
//
//                                @Override
//                                public void onResponse(JSONObject json) {
//                                    requestSize--;
//                                    Log.d("SDM", "onResponse service " + requestSize);
//                                }
//                            }, new Response.ErrorListener() {
//
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    requestSize--;
//                                    Log.d("SDM", "onResponse service " + requestSize);
//                                }
//                            });
//
//                    requestQueue.add(request);
//                }
//
//                while (requestSize != 0) {
//                    Thread.sleep(10);
//                }
//
//                Thread.sleep(5000);
//
//                Log.d("SDM", "endRequestBurst service ");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
