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
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class FetchMessagesService extends Service {
    private boolean isServiceDestroyed;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SDM", "onCreate service ");
        isServiceDestroyed = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        new Thread() {
            public void run() {
                Log.d("SDM", "run!!");
                MyAsyncTask tarefa = new MyAsyncTask(getApplication());

                while (!isServiceDestroyed) {
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
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (tarefa != null) {
                    tarefa.cancel(false);
                }
            }
        }.start();

        return Service.START_STICKY;
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

        isServiceDestroyed = true;
        stopSelf();
    }


    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private RequestQueue requestQueue;
        private String userId;
        private int requestSize;

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

            RealmQuery<Contact> queryContacts = realm.where(Contact.class);
            RealmResults<Contact> resultContacts = queryContacts.findAll();
            requestSize = resultContacts.size();

            for (final Contact contact : resultContacts.subList(0, resultContacts.size())) {
                ContactMessage conMenssage = realm.where(ContactMessage.class).equalTo("id", contact.getId()).findFirst();

                if (conMenssage != null) {
                    Log.d("SDM", "FOR: " + conMenssage.getLastMessageId());
                    fetchMessages(conMenssage.getLastMessageId(), conMenssage.getId());
                } else {
                    Log.d("SDM", "FOR: " + "0");
                    fetchMessages("0", contact.getId());
                }
            }

            return null;
        }

        private void fetchMessages(String idMenssage, String idContact) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Constants.SERVER_URL);
            stringBuilder.append(Constants.MENSAGEM_PATH);
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

                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {
                            Log.d("SDM", "execute message");
                            bgRealm.copyToRealmOrUpdate(messageList);
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


                    // Atualiza indice da ultima mensagem
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
}

