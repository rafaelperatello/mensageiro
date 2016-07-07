package br.edu.ifspsaocarlos.sdm.mensageirosdm.util;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by rapha on 7/4/2016.
 */
public class Connection {
    public static boolean connectionVerify(Context context) {
        boolean conectado = false;
        if (context != null) {
            ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conectivtyManager.getActiveNetworkInfo() != null
                    && conectivtyManager.getActiveNetworkInfo().isAvailable()
                    && conectivtyManager.getActiveNetworkInfo().isConnected()) {
                conectado = true;
            }
        }
        return conectado;
    }
}
