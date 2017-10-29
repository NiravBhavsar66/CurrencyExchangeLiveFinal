package com.nb.currency.exchange.live.currency;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Nirav on 03-09-2017.
 */

public class CurrencyResponseModel implements Serializable{

    @SerializedName("query")
    public Query query;

    public static class Rate implements Serializable{
        @SerializedName("id")
        public String id;
        @SerializedName("Name")
        public String Name;
        @SerializedName("Rate")
        public String Rate;
        @SerializedName("Date")
        public String Date;
        @SerializedName("Time")
        public String Time;
        @SerializedName("Ask")
        public String Ask;
        @SerializedName("Bid")
        public String Bid;
    }

    public static class Results implements Serializable{
        @SerializedName("rate")
        public Rate rate;
    }

    public static class Query implements Serializable{
        @SerializedName("count")
        public int count;
        @SerializedName("created")
        public String created;
        @SerializedName("lang")
        public String lang;
        @SerializedName("results")
        public Results results;
    }
}
