package com.markviews.stocks;

import org.bukkit.plugin.java.JavaPlugin;

public final class Stocks extends JavaPlugin {

    public static Stocks instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("stocks").setExecutor(new CommandStocks());
    }

    /*
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

                    // border
                    if (x == 0 || x == 9 || z == 0 || z == 9) {
                        block.setType(Material.GRAY_WOOL);
                    }

                }
            }

        }
    }
    */

}
