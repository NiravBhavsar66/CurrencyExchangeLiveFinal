package com.nb.currency.exchange.live.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nb.currency.exchange.live.R;
import com.nb.currency.exchange.live.app.MyApplication;
import com.nb.currency.exchange.live.utility.CommonFunction;
import com.nb.currency.exchange.live.utility.ConnectivityReceiver;
import com.nb.currency.exchange.live.utility.Constant;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    @BindView(R.id.cl_home)
    ConstraintLayout cl_home;

    @BindView(R.id.et_request_value)
    AppCompatEditText et_request_value;

    @BindView(R.id.btn_source_currency)
    AppCompatButton btn_source_currency;

    @BindView(R.id.btn_destination_currency)
    AppCompatButton btn_destination_currency;

    @BindView(R.id.btn_reverse)
    AppCompatButton btn_reverse;

    @BindView(R.id.btn_convert)
    AppCompatButton btn_convert;

    @BindView(R.id.tv_result_value)
    AppCompatTextView tv_result_value;

    @BindView(R.id.pg)
    ProgressBar pg;

    @BindView(R.id.img_share)
    AppCompatImageView img_share;


    private String TAG = HomeActivity.class.getSimpleName();
    Intent i1, i2;
    String str_source, str_destiny;
    RequestQueue queue;
    Float rateR;

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String shp_source = "nameKey1";
    public static final String shp_destination = "nameKey2";
    SharedPreferences sharedpreferences;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        Log.d("Github Update :", "this is to test the github vcs update");

        onCreateProcess();
        onIntializeProcess();
    }

    @OnClick(R.id.btn_destination_currency)
    public void destCurrencyClick() {
        img_share.setVisibility(View.GONE);
//                v.startAnimation(animTranslate);

        tv_result_value.setVisibility(View.INVISIBLE);
        i2 = new Intent(HomeActivity.this, CurrencyListActivity.class);
        startActivityForResult(i2, 2);
    }

    @OnClick(R.id.btn_convert)
    public void convertClick() {

        convertProcess();
    }

    @OnClick(R.id.btn_source_currency)
    public void srcCurrencyClick() {
        img_share.setVisibility(View.GONE);
//                v.startAnimation(animTranslate);

        tv_result_value.setVisibility(View.INVISIBLE);
        i1 = new Intent(HomeActivity.this, CurrencyListActivity.class);
        startActivityForResult(i1, 1);
    }

    @OnClick(R.id.btn_reverse)
    public void reverseClick() {
        tv_result_value.setVisibility(View.INVISIBLE);
        img_share.setVisibility(View.GONE);

        String str_source = btn_source_currency.getText().toString().trim();
        String str_dest = btn_destination_currency.getText().toString().trim();

        btn_source_currency.setText(str_dest);
        btn_destination_currency.setText(str_source);
    }

    @OnClick(R.id.img_share)
    public void shareContent() {
        String text_share = getString(R.string.text_share) + getPackageName();
        String src_curr_val = "1";
        if (!et_request_value.getText().toString().trim().isEmpty())
            src_curr_val = et_request_value.getText().toString().trim();

        String shareBody = text_share + "\n" + src_curr_val + " " + str_source + " - " + str_destiny + " = " + tv_result_value.getText().toString().trim();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle b = data.getExtras();
                if (b != null) {
                    if (b.getString(Constant.key_curr) != null)
                        btn_source_currency.setText(b.getString(Constant.key_curr));
                }
            } else if (resultCode == 0) {
                System.out.println("RESULT CANCELLED");
            }
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle b = data.getExtras();
                if (b != null) {
                    if (b.getString(Constant.key_curr) != null)
                        btn_destination_currency.setText(b.getString(Constant.key_curr));
                }
            } else if (resultCode == 0) {
                System.out.println("RESULT CANCELLED");
            }
        }
    }

    private void convertProcess() {

        img_share.setVisibility(View.GONE);
        CommonFunction.hideKeyboard(HomeActivity.this);

        if (!CommonFunction.checkConnection()) {
            showSnack();
            return;
        }

        str_source = btn_source_currency.getText().toString().trim();
        str_destiny = btn_destination_currency.getText().toString().trim();

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(shp_source, str_source);
        editor.putString(shp_destination, str_destiny);
        editor.apply();

        if (!str_source.isEmpty() && !str_destiny.isEmpty()) {
            if (str_source.equals(str_destiny)) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_select_diff_curr), Toast.LENGTH_SHORT).show();
            } else {

                str_source = str_source.substring(0, 3);
                str_destiny = str_destiny.substring(0, 3);

                pg.setVisibility(View.VISIBLE);

                tv_result_value.setText("");
                str_source = str_source.substring(0, 3);
                str_destiny = str_destiny.substring(0, 3);


                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < Constant.buffer_sec * 1000) {
                    if (Constant.lastJSon != null) {

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Do something after 1200ms
                                pg.setVisibility(View.GONE);
//                                Toast.makeText(getApplicationContext(), "Buffer Click", Toast.LENGTH_SHORT).show();
                                processConvertrate(Constant.lastJSon);
                            }
                        }, 1200);
                    } else
                        doRequestLiveExchangeCall();
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                doRequestLiveExchangeCall();

            }
        } else {
            Toast.makeText(HomeActivity.this, getResources().getString(R.string.toast_enter_currency), Toast.LENGTH_SHORT).show();
        }
    }

    private void doRequestLiveExchangeCall() {
        String url = getString(R.string.live_api) + Constant.CurrencyLayerApiKey;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Constant.lastJSon = response;
                        processConvertrate(Constant.lastJSon);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                                Log.d("", "Error " + error);
                        if (Constant.lastJSon != null)
                            processConvertrate(Constant.lastJSon);
                        else
                            pg.setVisibility(View.GONE);
                    }
                });
        queue.add(jsObjRequest);
    }

    // Showing the status in Snackbar
    void showSnack() {

        Snackbar snackbar = Snackbar
                .make(cl_home, getResources().getString(R.string.msg_no_internet), Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (!isConnected)
            showSnack();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rate_us:
                rateIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void rateIntent() {
        openPlayStore(HomeActivity.this);
    }

    void openPlayStore(Context context) {
        String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void onCreateProcess() {

        // get action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.show();


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        /*final Animation animTranslate = AnimationUtils.loadAnimation(this,
                R.anim.anim_translate);*/

        queue = Volley.newRequestQueue(this);

        tv_result_value.setVisibility(View.INVISIBLE);

        if (sharedpreferences.contains(shp_source)) {
            btn_source_currency.setText(sharedpreferences.getString(shp_source, ""));

        }
        if (sharedpreferences.contains(shp_destination)) {
            btn_destination_currency.setText(sharedpreferences.getString(shp_destination, ""));
        }


    }

    private void onIntializeProcess() {

        et_request_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                img_share.setVisibility(View.GONE);
            }
        });
    }

    private void processConvertrate(JSONObject response) {

        try {
            pg.setVisibility(View.GONE);

            if (response.getString(Constant.key_success).equalsIgnoreCase(Constant.key_true)) {
                JSONObject jsonObject1 = response.getJSONObject(Constant.key_quotes);
                tv_result_value.setVisibility(View.VISIBLE);
                img_share.setVisibility(View.VISIBLE);
                String result = CommonFunction.getConvertRates(jsonObject1, str_source, str_destiny, et_request_value.getText().toString().trim());
                if (!result.isEmpty()) {
                    tv_result_value.setText(result + " " + str_destiny);
                } else {
                    tv_result_value.setText(result);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Pls Try again later", Toast.LENGTH_SHORT).show();
                Log.d("Err : ", response.getJSONObject(Constant.key_error).getString(Constant.key_info));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    private void doRequestCurrentConvert(String source, String destination, String str_total) {
//        tv_result_value.setVisibility(View.VISIBLE);
//        String result = CommonFunction.getConvertRates(Constant.dummyJSon, source, destination, str_total);
//        if (!result.isEmpty()) {
//            tv_result_value.setText(result + " " + str_destiny);
//        } else {
//            tv_result_value.setText(result);
//        }
//    }


     /*pg.setVisibility(View.GONE);
    Gson gson = new Gson();

    CurrencyResponseModel currencyResponseModel = gson.fromJson(response.toString(), CurrencyResponseModel.class);

    JSONObject query = response.getJSONObject("query");
    // Log.d("","Query  "+query);
    JSONObject resul = query.getJSONObject("results");
    // Log.d("","Resulty "+resul);
    JSONObject rate = resul.getJSONObject("rate");
    String rat = rate.getString("Rate");

                                        tv_result_value.setVisibility(View.VISIBLE);
                                        img_share.setVisibility(View.VISIBLE);
    rateR = Float.parseFloat(String.valueOf(rat));
    float temp;
                                        if (!et_request_value.getText().toString().equals("")) {
        temp = Float.parseFloat(String.valueOf(et_request_value.getText().toString()));
    } else {
        temp = 1;
    }
    float res = temp * rateR;
                                        tv_result_value.setText(String.valueOf(res) + " " + str_destiny);

                                    Log.d("", "rate " + rate);
} catch (JSONException e) {
        Log.d("", "Err " + e);
        e.printStackTrace();
        }

        }*/
}
