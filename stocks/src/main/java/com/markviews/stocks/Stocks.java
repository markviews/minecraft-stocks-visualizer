package com.markviews.stocks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public final class Stocks extends JavaPlugin {

    private World world;
    private ArrayList<Stock> stocks = new ArrayList<>();
    private double lowest = -1;
    private double highest = -1;


    @Override
    public void onEnable() {
        this.getCommand("stocks").setExecutor(new CommandStocks());

        world = Bukkit.getWorld("world");
        ListFiles();
        //CreatePlots();
        DisplayIndex(0);
        DisplayIndex(1);
        DisplayIndex(20);
    }

    private void DisplayIndex(int index) {

        for(Stock stock : stocks) {
            double val = stock.vals.get(index).getAsDouble();
            System.out.println(stock.name + " " + index + " " + val);
        }

    }

    private void CreatePlots() {
        for (int i = 1; i <= 100; i++) {
            int xOffset = ((i - 1) % 10) * 10;
            int zOffset = ((i - 1) / 10) * 10;

            // sign
            Block b = new Location(world, xOffset, -60, zOffset).getBlock();
            b.setType(Material.OAK_SIGN);
            Sign sign = (Sign) b.getState();
            sign.setEditable(true);
            sign.setLine(0, "Plot #" + i);
            sign.update();

            for (int x = 0; x < 10; x++) {
                for (int z = 0; z < 10; z++) {
                    Block block = new Location(world, xOffset + x, -61, zOffset + z).getBlock();

                    // border or fill
                    if (x == 0 || x == 9 || z == 0 || z == 9) {
                        block.setType(Material.GRAY_WOOL);
                    } else {
                        block.setType(Material.GRASS_BLOCK);
                    }

                }
            }

        }
    }

    private void ParseFile(String file) {

        try {

            Path path = Paths.get("plugins/stocks/" + file);
            String jsonStr = Files.readString(path);

            JsonObject jsonString = new JsonParser().parse(jsonStr).getAsJsonObject();
            JsonObject jsonChart = jsonString.getAsJsonObject("chart");
            JsonArray resultArray = jsonChart.getAsJsonArray("result");
            JsonObject resultObject = resultArray.get(0).getAsJsonObject();

            JsonObject indicatorsObject = resultObject.getAsJsonObject("indicators");
            JsonArray quoteArray = indicatorsObject.getAsJsonArray("quote");
            JsonObject quoteObject = quoteArray.get(0).getAsJsonObject();
            JsonArray closeArray = quoteObject.getAsJsonArray("close");

            JsonObject metaObject = resultObject.getAsJsonObject("meta");
            String name = metaObject.get("symbol").getAsString();
            double low = metaObject.get("fiftyTwoWeekLow").getAsDouble();
            double high = metaObject.get("fiftyTwoWeekHigh").getAsDouble();

            if (lowest == -1 || low < lowest) {
                lowest = low;
            }

            if (highest == -1 || high > highest) {
                highest = high;
            }

            Stock stock = new Stock(name, closeArray, low, high);
            stocks.add(stock);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void ListFiles() {

        File folder = new File("plugins/stocks/");
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles == null) return;

        for (File listOfFile : listOfFiles) {
            if (!listOfFile.isFile()) continue;

            String fileName = listOfFile.getName();
            ParseFile(fileName);
        }

    }

}
