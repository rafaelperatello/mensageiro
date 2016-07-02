package br.edu.ifspsaocarlos.sdm.mensageirosdm.application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by rapha on 6/30/2016.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}

