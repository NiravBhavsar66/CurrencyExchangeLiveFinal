package com.nb.currency.exchange.live.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.nb.currency.exchange.live.R;
import com.nb.currency.exchange.live.app.MyApplication;
import com.nb.currency.exchange.live.currency.CurrencyResponseModel;
import com.nb.currency.exchange.live.utility.CommonFunction;
import com.nb.currency.exchange.live.utility.ConnectivityReceiver;
import com.nb.currency.exchange.live.utility.Constant;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.et_request_value)
    EditText et_request_value;

    @BindView(R.id.btn_source_currency)
    Button btn_source_currency;

    @BindView(R.id.btn_destination_currency)
    Button btn_destination_currency;

    @BindView(R.id.btn_reverse)
    Button btn_reverse;

    @BindView(R.id.btn_convert)
    Button btn_convert;

    @BindView(R.id.tv_result_value)
    TextView tv_result_value;

    @BindView(R.id.pg)
    ProgressBar pg;

    @BindView(R.id.img_share)
    ImageView img_share;

    @BindView(R.id.banner_ad)
    AdView banner_ad;


    private String TAG = HomeActivity.class.getSimpleName();
    Intent i1, i2;
    String str_source, str_destiny;
    RequestQueue queue;
    Float rateR;

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String shp_source = "nameKey1";
    public static final String shp_destination = "nameKey2";
    SharedPreferences sharedpreferences;


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
                pg.setVisibility(View.VISIBLE);

                tv_result_value.setText("");
                str_source = str_source.substring(0, 3);
                str_destiny = str_destiny.substring(0, 3);

                String url = "http://query.yahooapis.com/v1/public/yql?q=select%20%2a%20from%20yahoo.finance.xchange%20where%20pair%20in%20%28%22"
                        + str_source + str_destiny
                        + "%22%29&format=json&env=store://datatables.org/alltableswithkeys&callback=";
//                        Log.d("", "" + url);

                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
//                                        System.out.println(response);
//                                        Log.d("", "" + response);
                                try {
                                    pg.setVisibility(View.GONE);
                                    Gson gson = new Gson();

                                    CurrencyResponseModel currencyResponseModel = gson.fromJson(response.toString(), CurrencyResponseModel.class);

                                    tv_result_value.setVisibility(View.VISIBLE);
                                    img_share.setVisibility(View.VISIBLE);
                                    rateR = Float.parseFloat(String.valueOf(currencyResponseModel.query.results.rate.Rate));
                                    float temp;
                                    if (!et_request_value.getText().toString().equals("")) {
                                        temp = Float.parseFloat(String.valueOf(et_request_value.getText().toString()));
                                    } else {
                                        temp = 1;
                                    }
                                    float res = temp * rateR;
                                    tv_result_value.setText(String.valueOf(res) + " " + str_destiny);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("", "Error " + error);
                                pg.setVisibility(View.GONE);
                            }
                        });
                queue.add(jsObjRequest);

            }
        } else {
            Toast.makeText(HomeActivity.this, getResources().getString(R.string.toast_enter_currency), Toast.LENGTH_SHORT).show();
        }
    }

    // Showing the status in Snackbar
    void showSnack() {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, getResources().getString(R.string.msg_no_internet), Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
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

        AdRequest adRequest = new AdRequest.Builder().build();
        banner_ad.loadAd(adRequest);


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

        coordinatorLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                coordinatorLayout.getWindowVisibleDisplayFrame(r);
                int heightDiff = coordinatorLayout.getRootView().getHeight() - (r.bottom - r.top);

                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    //ok now we know the keyboard is up...
                    banner_ad.setVisibility(View.GONE);

                } else {
                    //ok now we know the keyboard is down...
                    banner_ad.setVisibility(View.VISIBLE);

                }
            }
        });

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
