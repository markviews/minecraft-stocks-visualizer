
## About

This Bukkit plugin was developed for a school assignment as a fun way to visualise stock market prices.

## /stocks

`/stocks` with no arguments will stop the current command.

`/stocks NVDA GOOG etc..` Displays daily stock prices over past year along with building representing it's value.

![ezgif-5-e62e992983](https://github.com/markviews/minecraft-stocks-visualizer/assets/46404728/3012c4dd-f25a-4d32-932d-b9b53850291f)

## How to use

1. [Setup PaperMC server](https://docs.papermc.io/paper/getting-started)
2. Download world.zip from releases. extract to server folder
3. Download stocks.jar. put in plugins folder
4. Join server, type `/stocks NVDA GOOG etc..`

## How it works

1. Stocks are downloaded from yahoo finance `https://query1.finance.yahoo.com/v8/finance/chart/NVDA?interval=1d&range=1y`
2. Stocks are cached to `plugins/stocks/NVDA.json`
3. Prices displayed are the stock's closing price per day
4. Buildings are selected from 1-100 by comparing price to min and max closing prices for all listed stocks.
5. Buildings are copied from this grid
![2024-04-25_00 06 18](https://github.com/markviews/minecraft-stocks-visualizer/assets/46404728/ac056011-bd26-4f2a-9cd4-59541bc09a90)
