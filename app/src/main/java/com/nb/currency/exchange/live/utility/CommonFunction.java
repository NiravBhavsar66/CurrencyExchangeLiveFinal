package com.nb.currency.exchange.live.utility;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by mac on 6/20/17.
 */

public class CommonFunction {

    public static boolean checkConnection() {
        return ConnectivityReceiver.isConnected();
    }

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getConvertRates(JSONObject jsonObject1, String source, String destination, String str_total) {
        try {

            String str_source = Constant.USD + source;
            String str_destination = Constant.USD + destination;

            String src = jsonObject1.getString(str_source);
            String dest = jsonObject1.getString(str_destination);

            if (src != null && !(src.isEmpty()) && dest != null && !(dest.isEmpty())) {
                return new DecimalFormat("0.00").format(convertProcess(src, dest, str_total));
            } else
                return "";

        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static double convertProcess(String str_source, String str_destination, String str_total) {

        double d_source = Double.parseDouble(str_source);

        double d_destination = Double.parseDouble(str_destination);

        double d_total = 1;

        if (!str_total.isEmpty()) {
            d_total = Double.parseDouble(str_total);
        }


        double d = 1 / d_source;
        return d_total * d * d_destination;
    }

}
