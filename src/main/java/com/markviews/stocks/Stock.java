package com.markviews.stocks;

import com.google.gson.JsonArray;
import org.bukkit.Location;

public class Stock {

    public String name;
    public JsonArray prices;
    public JsonArray timestamps;
    public String timezone;
    public double low;
    public double high;
    public Location location;
    public Location textLocation;
    public double lastPrice = 0;

    Stock(String name, JsonArray prices, JsonArray timestamps, String timezone, double low, double high, Location location) {
        this.name = name;
        this.prices = prices;
        this.timestamps = timestamps;
        this.timezone = timezone;
        this.low = low;
        this.high = high;
        this.location = location;

        textLocation = new Location(location.getWorld(), location.getBlockX() + 5, location.getBlockY() + 3, location.getBlockZ());
    }
    
}
