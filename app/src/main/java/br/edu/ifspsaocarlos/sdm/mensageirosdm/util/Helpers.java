package br.edu.ifspsaocarlos.sdm.mensageirosdm.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.afollestad.materialdialogs.MaterialDialog;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;

public class Helpers {

    public static MaterialDialog showDialog(Context context, int contentRes) {
        return new MaterialDialog.Builder(context)
                .content(contentRes)
                .positiveText(R.string.dialog_button_ok)
                .show();
    }

    public static String getUserId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Constants.PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedpreferences.getString(Constants.USER_KEY, "");
    }
}
