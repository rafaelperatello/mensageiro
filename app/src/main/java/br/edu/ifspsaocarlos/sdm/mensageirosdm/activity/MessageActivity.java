package br.edu.ifspsaocarlos.sdm.mensageirosdm.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.adapter.MessageAdapter;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Message;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Subject;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.BigMessage;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Connection;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MessageActivity extends AppCompatActivity implements OnClickListener {
    private EditText editTextMessage;
    private FloatingActionButton buttonSend;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;

    private String contactId;
    private String userId;

    private Realm realm;
    private RealmResults<Message> resultMessages;
    private List<Message> messageList;

    private RequestQueue requestQueue;

    Contact contact;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                sendMessage();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // setup volley
        requestQueue = Volley.newRequestQueue(this);

        // setup realm
        realm = Realm.getDefaultInstance();

        // current user
        userId = Helpers.getUserId(this);

        // destinatário
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            contactId = extras.getString(Constants.SENDER_USER_KEY);
        }

        contact = realm.where(Contact.class).equalTo("id", contactId).findFirst();

        // setup toolBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(contact.getNome_completo());

        // bind views
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        editTextMessage = (EditText) findViewById(R.id.edit_message);
        buttonSend = (FloatingActionButton) findViewById(R.id.fab);
        buttonSend.setOnClickListener(this);

        // setup recycler
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(false);
        messageList = new ArrayList<>();

        // check messages
        setupQuery();
    }

    private void setupQuery() {
        resultMessages = realm.where(Message.class)
                .equalTo("destino_id", userId)
                .equalTo("origem_id", contactId)
                .or()
                .equalTo("destino_id", contactId)
                .equalTo("origem_id", userId)
                .findAll();

        resultMessages.addChangeListener(new RealmChangeListener<RealmResults<Message>>() {
            @Override
            public void onChange(RealmResults<Message> element) {
                if (element.size() > messageList.size()) {
                    updateAdapter(element);
                }
            }
        });

        setupAdapter();
    }

    private void setupAdapter() {
        for (int i = 0; i < resultMessages.size(); i++) {
            messageList.add(resultMessages.get(i));
        }

        // sort
        Collections.sort(messageList, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                int id1 = Integer.parseInt(lhs.getId());
                int id2 = Integer.parseInt(rhs.getId());

                return Integer.compare(id1, id2);
            }
        });

        adapter = new MessageAdapter(messageList, userId);
        recyclerView.setAdapter(adapter);
    }

    private void updateAdapter(RealmResults<Message> element) {
        List<Message> buffMessageList = element.subList(messageList.size(), element.size());

        for (int i = 0; i < buffMessageList.size(); i++) {
            messageList.add(buffMessageList.get(i));
            adapter.notifyItemInserted(adapter.getItemCount());
        }

        recyclerView.smoothScrollToPosition(adapter.getItemCount());
    }

    private boolean sendMessageIsAble(String message) {
        if (Connection.connectionVerify(this)) {
            if (message.length() > 0) {
                return true;
            }
        } else {
            Toast.makeText(this, "No momento não há conexão.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString();

        if (sendMessageIsAble(message)) {
            editTextMessage.setText("Enviando Mensagem...");
            editTextMessage.setEnabled(false);
            buttonSend.setEnabled(false);

            SendMessageThread sendMessageThread = new SendMessageThread();
            sendMessageThread.message = message;
            sendMessageThread.start();
        }
    }

    private void saveMessage(final Message message) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                bgRealm.copyToRealmOrUpdate(message);
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

    class SendMessageThread extends Thread {
        public String message;

        //Control
        boolean isWaitEnable;
        boolean isSuccessful;

        @Override
        public void run() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Constants.SERVER_URL);
            stringBuilder.append(Constants.MENSAGEM_PATH);

            Subject subject = new Subject(message);
            final BigMessage big = new BigMessage();

            while (subject.isReady()) {
                //Control
                isWaitEnable = true;
                isSuccessful = false;

                //Build json
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("origem_id", userId);
                    jsonObject.put("destino_id", contactId);
                    jsonObject.put("assunto", subject.finalSubject());
                    jsonObject.put("corpo", subject.mensagemToSend());
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                //Request
                JsonObjectRequest request = new JsonObjectRequest
                        (Request.Method.POST, stringBuilder.toString(), jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject json) {
                                isSuccessful = true;
                                isWaitEnable = false;

                                Message message = new Gson().fromJson(json.toString(), Message.class);
                                switch (big.bigMessageValidation(message)) {
                                    case BigMessage.BIG_MESSAGE_ENDED:
                                        saveMessage(big.getBigMessage());
                                        break;

                                    case BigMessage.BIG_MESSAGE_NOT_DETECTED:
                                        saveMessage(message);
                                        break;
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                isSuccessful = false;
                                isWaitEnable = false;
                            }
                        });

                requestQueue.add(request);

                //wait loop to next request
                while (isWaitEnable) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (!isSuccessful) {
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editTextMessage.setText("");
                        editTextMessage.setEnabled(true);
                        buttonSend.setEnabled(true);
                    }
                });
            }
            return;
        }
    }
}
