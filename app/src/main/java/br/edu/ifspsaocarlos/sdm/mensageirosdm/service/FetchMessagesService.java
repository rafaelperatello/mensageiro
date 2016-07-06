package br.edu.ifspsaocarlos.sdm.mensageirosdm.service;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.activity.MessageActivity;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.ContactMessage;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Message;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class FetchMessagesService extends Service {
    private MyAsyncTask task;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SDM", "onCreate service ");

        task = new MyAsyncTask();
        task.execute();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
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
        Log.d("SDM", "onDestroy service ");

        task.cancel(true);
        super.onDestroy();
    }

    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        private final String REQUEST_TAG = "REQUEST_TAG";
        private String userId;

        private Context context;
        private RequestQueue requestQueue;
        private int requestSize;

        public MyAsyncTask() {
            super();
            this.context = getApplication();

            userId = Helpers.getUserId(context);
            requestQueue = Volley.newRequestQueue(context);
            requestSize = 0;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("SDM", "MyAsyncTask doInBackground ");

            Realm realm = Realm.getDefaultInstance();
            RealmQuery<Contact> queryContacts = realm.where(Contact.class);
            RealmResults<Contact> resultContacts = queryContacts.findAll();

            requestSize = resultContacts.size();

            // loop de requisição de mensagens de cada contato
            for (Contact contact : resultContacts.subList(0, resultContacts.size())) {
                ContactMessage conMessage = realm.where(ContactMessage.class).equalTo("id", contact.getId()).findFirst();

                if (conMessage != null) {
                    fetchMessages(conMessage.getLastMessageId(), conMessage.getId());
                } else {
                    fetchMessages("0", contact.getId());
                }
            }

            realm.close();

            // loop para esperar todas as requests finalizarem antes de começar o próximo burst
            while (!isCancelled() && (requestSize != 0)) {
                try {
                    Log.d("SDM", "doInBackground hold loop - request size :" + requestSize);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // agurado de 30s antes de recomeçar
            try {
                Log.d("SDM", "doInBackground sleep");
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            requestQueue.cancelAll(REQUEST_TAG);

            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            Log.d("SDM", "MyAsyncTask onPostExecute ");

            task = new MyAsyncTask();
            task.execute();
        }

        private void fetchMessages(String idMessage, String idContact) {
            // incrementa o id da última mensagem para que exiba somente mensagens novas
            Integer idMessageRequest = Integer.parseInt(idMessage);
            idMessageRequest++;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Constants.SERVER_URL);
            stringBuilder.append(Constants.MENSAGEM_PATH);
            stringBuilder.append("/" + idMessageRequest + "/");
            stringBuilder.append(idContact);
            stringBuilder.append("/");
            stringBuilder.append(userId);

            JsonObjectRequest request = new JsonObjectRequest
                    (Request.Method.GET, stringBuilder.toString(), null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject json) {
                            requestSize--;
                            parseMessageList(json);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            requestSize--;
                        }
                    });

            request.setTag(REQUEST_TAG);
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
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            saveMessages(messageList);
        }

        private void saveMessages(final List<Message> messageList) {
            if (messageList != null) {
                if (messageList.size() > 0) {

                    // Usado para atualizar indice
                    final Message mensagem = messageList.get(messageList.size() - 1);

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {
                            bgRealm.copyToRealmOrUpdate(messageList);
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            updateMessagesIndex(mensagem);
                            showNotification(messageList);
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                        }
                    });
                }
            }
        }

        private void updateMessagesIndex(Message mensagem) {
            final ContactMessage conMen = new ContactMessage();
            conMen.setId(mensagem.getOrigem_id());
            conMen.setLastMessageId(mensagem.getId());

            Realm realm = Realm.getDefaultInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    bgRealm.copyToRealmOrUpdate(conMen);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                }
            });
        }

        private void showNotification(List<Message> messageList) {
            Integer id = Integer.parseInt(messageList.get(0).getOrigem_id());
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(id);

            Realm realm = Realm.getDefaultInstance();
            Contact contato = realm.where(Contact.class).equalTo("id", messageList.get(0).getOrigem_id()).findFirst();

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_send_white_24dp)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle("Nova mensagem")
                    .setContentText(contato.getNome_completo());

            Intent resultIntent = new Intent(getApplicationContext(), MessageActivity.class);
            resultIntent.putExtra(Constants.SENDER_USER_KEY, messageList.get(0).getOrigem_id());

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(MessageActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);


            mNotificationManager.notify(id, mBuilder.build());
        }
    }
}

