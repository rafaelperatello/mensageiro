package br.edu.ifspsaocarlos.sdm.mensageirosdm.application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by rapha on 6/30/2016.
 */
public class MyApplication extends Application {
    String currentMessagingUser;

    @Override
    public void onCreate() {
        super.onCreate();

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public String getCurrentMessagingUser() {
        return currentMessagingUser;
    }

    public void setCurrentMessagingUser(String currentMessagingUser) {
        this.currentMessagingUser = currentMessagingUser;
    }
}

