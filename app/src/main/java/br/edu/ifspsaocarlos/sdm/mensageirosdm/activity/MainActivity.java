package br.edu.ifspsaocarlos.sdm.mensageirosdm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.adapter.ContactAdapter;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.service.FetchMessagesService;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Constants;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.util.Helpers;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements ContactAdapter.OnContactClickListener {
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.about) {
            startAboutActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateAdapter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(false);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startContactActivity();
            }
        });

        /**
         * Verifica se esta cadastrado para só depois startar o que tem que startar. Sem isso ele
         * insere você na lista de usuários pois ele acaba percorrendo o fetUser duas vezes.
         */
        if (checkUser()) {
            updateAdapter();
            startMessagesService();
        }
    }

    @Override
    public void onContactClickListener(int position) {
        startMessageActivity(contactAdapter.getItem(position).getId());
    }

    private boolean checkUser() {
        String userId = Helpers.getUserId(this);

        if (TextUtils.isEmpty(userId)) {
            Intent intent = new Intent(this, UserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return false;
        }
        return true;
    }


    private void updateAdapter() {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Contact> query = realm.where(Contact.class);
        RealmResults<Contact> result = query.findAll();

        contactAdapter = new ContactAdapter(result.subList(0, result.size()), this);
        recyclerView.setAdapter(contactAdapter);
    }

    private void startContactActivity() {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivityForResult(intent, 1);
    }

    private void startMessageActivity(String recipientId) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(Constants.SENDER_USER_KEY, recipientId);
        startActivity(intent);
    }

    private void startMessagesService() {
        Intent i = new Intent(this, FetchMessagesService.class);
        startService(i);
    }

    private void startAboutActivity() {
        Intent intentNovo = new Intent(this, AboutActivity.class);
        startActivity(intentNovo);
    }
}