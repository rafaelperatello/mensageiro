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
import br.edu.ifspsaocarlos.sdm.mensageirosdm.application.MyApplication;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.ContactMessage;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Message;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.BigMessage;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class FetchMessagesService extends Service {
    private MyAsyncTask task;
    private MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        myApplication = ((MyApplication) getApplication());
        task = new MyAsyncTask();
        task.execute();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        task.cancel(true);
        super.onDestroy();
    }

    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        private String userId;
        private boolean isFirstUse;

        private Context context;
        private RequestQueue requestQueue;
        private int requestSize;

        public MyAsyncTask() {
            super();
            this.context = getApplication();

            userId = Helpers.getUserId(context);
            isFirstUse = Helpers.isFirstUse(context);

            requestQueue = Volley.newRequestQueue(context);
            requestSize = 0;

        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm realm = Realm.getDefaultInstance();
            RealmQuery<Contact> queryContacts = realm.where(Contact.class);
            RealmResults<Contact> resultContacts = queryContacts.findAll();
            requestSize = resultContacts.size() * 2;


            // loop de requisição de mensagens de cada contato
            for (Contact contact : resultContacts.subList(0, resultContacts.size())) {
                try {
                    ContactMessage conMessage = realm.where(ContactMessage.class).equalTo("id", contact.getId()).findFirst();
                    if (conMessage != null) {
                        fetchMessages(conMessage.getLastFromContact(), conMessage.getId(), userId);
                        fetchMessages(conMessage.getLastToContact(), userId, conMessage.getId());
                    } else {
                        fetchMessages("0", contact.getId(), userId);
                        fetchMessages("0", userId, contact.getId());
                    }
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            realm.close();

            // loop para esperar todas as requests finalizarem antes de começar o próximo burst
            while (!isCancelled() && (requestSize != 0)) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            requestQueue.cancelAll(Constants.REQUEST_TAG);
            Helpers.updateFirstUse(context);

            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            task = new MyAsyncTask();
            task.execute();
        }

        private void fetchMessages(String idMessage, String idContact, String idUser) {
            // verifica se é null pois pode não estar no realm ainda!
            if (idMessage == null) {
                idMessage = "0";
            }
            // incrementa o id da última mensagem para que exiba somente mensagens novas
            Integer idMessageRequest = Integer.parseInt(idMessage);
            idMessageRequest++;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Constants.SERVER_URL);
            stringBuilder.append(Constants.MENSAGEM_PATH);
            stringBuilder.append("/" + idMessageRequest + "/");
            stringBuilder.append(idContact);
            stringBuilder.append("/");
            stringBuilder.append(idUser);

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

            request.setTag(Constants.REQUEST_TAG);
            requestQueue.add(request);
        }

        private void parseMessageList(JSONObject jsonRoot) {
            List<Message> messageList = new ArrayList<>();

            try {
                JSONArray jsonArray = jsonRoot.getJSONArray("mensagens");
                Gson gson = new Gson();

                BigMessage big = new BigMessage();

                for (int i = 0; i < jsonArray.length(); i++) {
                    Message message = gson.fromJson(jsonArray.getJSONObject(i).toString(), Message.class);
                    switch (big.bigMessageValidation(message)) {
                        case BigMessage.BIG_MESSAGE_ENDED:
                            messageList.add(big.getBigMessage());
                            break;

                        case BigMessage.BIG_MESSAGE_NOT_DETECTED:
                            messageList.add(message);
                            break;
                    }
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

                            if (!userId.equals(messageList.get(0).getOrigem_id()))
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
            ContactMessage conMenssage;
            final ContactMessage conMenssageNew;

            Realm realm = Realm.getDefaultInstance();
            if (!mensagem.getOrigem_id().equals(userId)) {
                conMenssage = realm.where(ContactMessage.class).equalTo("id", mensagem.getOrigem_id()).findFirst();
            } else {
                conMenssage = realm.where(ContactMessage.class).equalTo("id", mensagem.getDestino_id()).findFirst();
            }

            conMenssageNew = new ContactMessage();
            if (conMenssage != null) {
                conMenssageNew.setId(conMenssage.getId());
                conMenssageNew.setLastToContact(conMenssage.getLastToContact());
                conMenssageNew.setLastFromContact(conMenssage.getLastFromContact());
            }

            if (!mensagem.getOrigem_id().equals(userId)) {
                conMenssageNew.setId(mensagem.getOrigem_id());
                conMenssageNew.setLastFromContact(mensagem.getId());
            } else {
                conMenssageNew.setId(mensagem.getDestino_id());
                conMenssageNew.setLastToContact(mensagem.getId());
            }

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    bgRealm.copyToRealmOrUpdate(conMenssageNew);
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
            String currentMessagingUser = myApplication.getCurrentMessagingUser();

            // check first use
            if (!isFirstUse) {

                // verifica se o usuario esta conversando com o usuario da notificação
                if (!messageList.get(0).getOrigem_id().equals(currentMessagingUser)) {

                    Integer id = Integer.parseInt(messageList.get(0).getOrigem_id());
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(id);

                    Realm realm = Realm.getDefaultInstance();
                    Contact contact = realm.where(Contact.class).equalTo("id", messageList.get(0).getOrigem_id()).findFirst();

                    if (contact != null) {
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_send_white_24dp)
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(true)
                                .setContentTitle("Nova mensagem")
                                .setContentText(contact.getNome_completo());

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
        }
    }
}
