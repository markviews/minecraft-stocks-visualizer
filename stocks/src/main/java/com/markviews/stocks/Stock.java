package com.markviews.stocks;

import com.google.gson.JsonArray;

public class Stock {

    public String name;
    public JsonArray vals;
    public double low;
    public double high;

    Stock(String name, JsonArray vals, double low, double high) {
        this.name = name;
        this.vals = vals;
        this.low = low;
        this.high = high;
    }
    
}
