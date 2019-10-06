package com.nb.currency.exchange.live.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.nb.currency.exchange.live.R;
import com.nb.currency.exchange.live.currency.CurrencyListAdapter;
import com.nb.currency.exchange.live.utility.Constant;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nirav on 02-03-2015.
 */
public class CurrencyListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @BindView(R.id.edt_search)
    AppCompatEditText edt_search;

    @BindView(R.id.recy_currency_list)
    RecyclerView recy_currency_list;

    private CurrencyListAdapter currencyListAdapter;

    Intent i1;

    private ArrayList<String> currencyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);
        ButterKnife.bind(this);

        // get action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setTitle(getResources().getString(R.string.title_curr_list));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        init();

    }

    private void init() {
        fillCurrencyList();

        setAdapter();

        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(edt_search.getText().toString().trim());
            }
        });

    }


    public void setAdapter() {
        if (currencyList != null && currencyList.size() > 0) {
            if (currencyListAdapter == null) {
                currencyListAdapter = new CurrencyListAdapter();
                currencyListAdapter.setOnItemClickListener(this);
            }

            currencyListAdapter.doReferesh(currencyList);
            if (recy_currency_list.getAdapter() == null) {
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
                recy_currency_list.setLayoutManager(mLayoutManager);
                recy_currency_list.setItemAnimator(new DefaultItemAnimator());
                recy_currency_list.setAdapter(currencyListAdapter);
            }
        }
    }

    private void fillCurrencyList() {
        currencyList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.currency_list)));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        i1 = new Intent(CurrencyListActivity.this, HomeActivity.class);
//        i1.putExtra(Constant.key_curr, ((AppCompatTextView) view).getText().toString().trim());

        AppCompatTextView card = (AppCompatTextView) ((CardView) view).getChildAt(0);
        i1.putExtra(Constant.key_curr, card.getText().toString().trim());

        setResult(Activity.RESULT_OK, i1);
        finish();
    }

    private String randomString() {
        try {
            String AB = "0123456789";
            SecureRandom rnd = new SecureRandom();
            return String.valueOf((AB.charAt(rnd.nextInt(AB.length()))));
        } catch (Exception e) {
            return "0"; // here password is static in case of some error
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    void filter(String text) {
        ArrayList<String> temp = new ArrayList<>();
        for (int k = 0; k < currencyList.size(); k++) {
            if (currencyList.get(k).toLowerCase().contains(text.toLowerCase()))
                temp.add(currencyList.get(k));
        }
        currencyListAdapter.doReferesh(temp);
    }

}
