package com.nb.currency.exchange.live.currency;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.nb.currency.exchange.live.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mac on 6/23/17.
 */

public class CurrencyListAdapter extends RecyclerView.Adapter<CurrencyListAdapter.MyViewHolder> {

    private ArrayList<String> currencyList;
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;
    private ArrayList<Integer> mSectionPositions;

    public CurrencyListAdapter() {
    }

    public void doReferesh(ArrayList<String> mCurrencyList) {
        currencyList = mCurrencyList;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        CurrencyListAdapter mAdapter;

        @BindView(R.id.tv_currency)
        AppCompatTextView tv_currency;

        MyViewHolder(View view, CurrencyListAdapter mAdapter) {
            super(view);
            ButterKnife.bind(this, view);

            this.mAdapter = mAdapter;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(MyViewHolder.this);
        }

        @Override
        public boolean onLongClick(View v) {
            mAdapter.onItemLongHolderClick(MyViewHolder.this);
            return true;
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(MyViewHolder holder) {
        if (onItemClickListener != null)
            onItemClickListener.onItemClick(null, holder.itemView, holder.getAdapterPosition(), holder.getItemId());
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    private void onItemLongHolderClick(MyViewHolder holder) {
        if (onItemLongClickListener != null)
            onItemLongClickListener.onItemLongClick(null, holder.itemView, holder.getAdapterPosition(), holder.getItemId());
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_currency_list, parent, false);

        return new MyViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tv_currency.setText(currencyList.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return currencyList.size();
    }
}