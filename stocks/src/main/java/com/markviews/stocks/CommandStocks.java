package com.markviews.stocks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class CommandStocks implements CommandExecutor {

    private World world;
    private ArrayList<Stock> stocks = new ArrayList<>();
    private double lowest = -1;
    private double highest = -1;
    private int day = 1;

    private static BukkitTask currentTask;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // stop previous task
        if (currentTask != null) {
            currentTask.cancel();
        }

        // reset stuff
        String killCmd = "kill @e[type=minecraft:text_display]";
        Stocks.instance.getServer().dispatchCommand(Stocks.instance.getServer().getConsoleSender(), killCmd);
        world = Bukkit.getWorld("world");
        day = 1;
        lowest = -1;
        highest = -1;
        stocks.clear();

        if (args.length == 0) {
            return true;
        }

        for(String stockName: args) {
            boolean success = getStock(stockName.toUpperCase());
            if(!success) {
                sender.sendMessage(stockName + " failed to load!");
                return true;
            }
        }

        // create text for price of each
        for(Stock stock: stocks) {
            Location loc = stock.textLocation;
            String name = stock.name;
            String cmd = "summon text_display " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " {billboard:\"vertical\",Tags:[\"" + name + "\"],text:'[{\"bold\":true,\"text\":\"" + name + ": \"},{\"color\":\"dark_green\",\"text\":\"$\"}]'}";
            Stocks.instance.getServer().dispatchCommand(Stocks.instance.getServer().getConsoleSender(), cmd);
        }

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                DisplayIndex(day);

                day++;
                if (day == 101) {
                    sender.sendMessage("Done");
                    cancel();
                }
            }
        };
        currentTask = runnable.runTaskTimer(Stocks.instance, 0, 20);

        return true;
    }

    private void DisplayIndex(int index) {
        for(Stock stock : stocks) {
            double price = Math.round(stock.prices.get(index).getAsDouble() * 100) / 100.0;
            int percent = (int)Math.round(((price - lowest) / (highest - lowest)) * 100);

            copyPlotToPosition(percent, stock.location);

            String color = "dark_green";
            String particle = "composter";
            if (price < stock.lastPrice) {
                color = "red";
                particle = "minecraft:item minecraft:red_wool";
            }
            stock.lastPrice = price;

            int unix = stock.timestamps.get(index).getAsInt();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone(stock.timezone));
            String date = sdf.format(new Date(unix*1000L));

            // update text
            String name = stock.name;
            String cmd_text = "data merge entity @e[tag=" + name + ",limit=1] {billboard:\"vertical\",Tags:[\"" + name + "\"],text:'[{\"bold\":true,\"text\":\"" + name + ": \"},{\"color\":\"" + color + "\",\"text\":\"$" + price + "\"},{\"text\":\"\\\\n" + date + "\"}]'}";
            Stocks.instance.getServer().dispatchCommand(Stocks.instance.getServer().getConsoleSender(), cmd_text);

            // couldn't get good looking negative particle..
            if (particle.equals("minecraft:item minecraft:red_wool")) return;

            // spawn particle
            Location loc = stock.location;
            double x = loc.getX() + 5;
            double y = loc.getY();
            double z = loc.getZ() + 5;
            String cmd_particle = "particle " + particle + " " + x + " " + y + " " + z + " 2 2 2 1 200 normal";
            Stocks.instance.getServer().dispatchCommand(Stocks.instance.getServer().getConsoleSender(), cmd_particle);
        }
    }

    private void copyPlotToPosition(int plotNumber, Location loc) {
        int xOffset = ((plotNumber - 1) % 10) * 10;
        int zOffset = ((plotNumber - 1) / 10) * 10;

        int x1 = xOffset;
        int y1 = -61;
        int z1 = zOffset;

        int x2 = xOffset + 9;
        int y2 = -50;
        int z2 = zOffset + 9;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        String cmd = "clone " + x1 + " " + y1 + " " + z1 + " " + x2 + " " + y2 + " " + z2 + " " + x + " " + y + " " + z;
        Stocks.instance.getServer().dispatchCommand(Stocks.instance.getServer().getConsoleSender(), cmd);
}

    public boolean getStock(String stock) {
        try {
            String filePath = "plugins/stocks/" + stock + ".json";
            Path path = Paths.get(filePath);

            // if we already have this stock downloaded
            File file = new File(filePath);
            if(file.exists() && !file.isDirectory()) {
                System.out.println("Reading from file: " + stock);
                String json = Files.readString(path);
                ParseJSON(json);
                return true;
            }

            System.out.println("Getting data from yahoo: " + stock);
            String json = getHTML("https://query1.finance.yahoo.com/v8/finance/chart/" + stock + "?interval=1d&range=1y");
            Files.write(path, json.getBytes());
            ParseJSON(json);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private void ParseJSON(String json) {
        try {
            JsonObject jsonString = new JsonParser().parse(json).getAsJsonObject();
            JsonObject jsonChart = jsonString.getAsJsonObject("chart");
            JsonArray resultArray = jsonChart.getAsJsonArray("result");
            JsonObject resultObject = resultArray.get(0).getAsJsonObject();

            JsonObject indicatorsObject = resultObject.getAsJsonObject("indicators");
            JsonArray quoteArray = indicatorsObject.getAsJsonArray("quote");
            JsonObject quoteObject = quoteArray.get(0).getAsJsonObject();
            JsonArray prices = quoteObject.getAsJsonArray("close");
            JsonArray timestamps = resultObject.getAsJsonArray("timestamp");

            JsonObject metaObject = resultObject.getAsJsonObject("meta");
            String name = metaObject.get("symbol").getAsString();
            String timezone = metaObject.get("timezone").getAsString();

            // loop values to find low / high for this stock
            double low = -1;
            double high = -1;
            for(JsonElement num : prices) {
                double val = num.getAsDouble();
                if (low == -1 || val < low) low = val;
                if (high == -1 || val > high) high = val;
            }

            // update global min / max
            if (lowest == -1 || low < lowest) lowest = low;
            if (highest == -1 || high > highest) highest = high;

            Location loc = new Location(world, 49, -61, 125);
            loc.setX(loc.getX() + stocks.size() * 11);

            Stock stock = new Stock(name, prices, timestamps, timezone, low, high, loc);
            stocks.add(stock);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // https://stackoverflow.com/questions/1485708/how-do-i-do-a-http-get-in-java
    public String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        HttpURLConnection conn = (HttpURLConnection) new URL(urlToRead).openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null;) {
                result.append(line);
            }
        }
        return result.toString();
    }

}