package br.edu.ifspsaocarlos.sdm.mensageirosdm.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.adapter.MessageAdapter;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Message;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;
import io.realm.Realm;
import io.realm.RealmResults;

public class MessageActivity extends AppCompatActivity {
    private EditText editTextMessage;
    private FloatingActionButton buttonSend;

    private RecyclerView recyclerView;
    private MessageAdapter adapter;

    private String senderId;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // setup toolBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mensagens");

        // destinatário
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            senderId = extras.getString(Constants.SENDER_USER_KEY);
        }

        // bind views
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        editTextMessage = (EditText) findViewById(R.id.edit_message);
        buttonSend = (FloatingActionButton) findViewById(R.id.fab);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(false);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMenssage();
            }
        });

        setupAdapter();
    }

    private void setupAdapter() {
        // query
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Message> resultMessages = realm.where(Message.class)
                .equalTo("destino_id", Helpers.getUserId(this))
                .equalTo("origem_id", senderId)
                .findAll();


        // parse list
        List<Message> messageList = new ArrayList<>();
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

        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);
    }

    private void sendMenssage() {
        String message = editTextMessage.getText().toString();
        editTextMessage.setText("");
    }
}

//        //Debug
//        for (Message message : messageList) {
//            Log.d("SDM", "message: " + message.getId() + " sender: " + message.getOrigem_id() + " corpo: " + message.getCorpo());
//        }