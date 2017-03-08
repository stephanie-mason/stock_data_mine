/*============================================================================/*
| Assignment3.java                                                             |
|                                                                              |
| CSCI 330 - Winter 2017                                                       |
|                                                                              |
| Simple data mining of stock database                                         |
| by Stephanie Mason                                                           |
/*============================================================================*/

import java.util.Properties;
import java.util.Scanner;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.sql.*;

class Assignment3 {
  static Connection connRead = null;
  static Connection connWrite = null;

  public static void main(String[] args) throws Exception {
    String readParamsFile = "readerparams.txt";
    String writeParamsFile = "writerparams.txt";
    Properties connectProps = new Properties();
    ArrayList<String> industryList = new ArrayList<String>();


    try {
      //Connect to the read database
      Class.forName("com.mysql.jdbc.Driver");

      connectProps.load(new FileInputStream(readParamsFile));
      String dburl = connectProps.getProperty("dburl");
      String username = connectProps.getProperty("user");
      connRead = DriverManager.getConnection(dburl+"?useSSL=false", connectProps);
      System.out.printf("Reader connection established.%n",
      dburl, username);

      //Connect to write database
      connectProps.load(new FileInputStream(writeParamsFile));
      dburl = connectProps.getProperty("dburl");
      username = connectProps.getProperty("user");
      connWrite = DriverManager.getConnection(dburl+"?useSSL=false", connectProps);
      System.out.printf("Writer connection established.%n",
      dburl, username);

      //Main functions called here
      industryList = getIndustries();
      //createTable();
      //updateTable("lalala");

      /*
      for(int i = 0; i < 2; i++) {
      String currIndustry = industryList.get(i);
      System.out.printf("Processing " + currIndustry + "%n");
      determineTradingInterval(currIndustry);
    }
    */

    determineTradingInterval("Telecommunications Services");

    connRead.close();
    connWrite.close();
    System.out.printf("All connections closed.%n");
  } catch (SQLException ex) {
    System.out.printf("SQLException: %s%nSQLState: %s%nVendor Error: %s%n",
    ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
    connRead.close();
    connWrite.close();
    System.out.printf("All connections closed.%n");
  }
}

/*============================================================================/*
Create a performance table and populate with necessary relations
/*============================================================================*/
static void createTable() throws SQLException {
  PreparedStatement pstmt = connWrite.prepareStatement(
  "drop table if exists Performance;");
  pstmt.executeUpdate();

  pstmt = connWrite.prepareStatement(
  "create table Performance (" +
  " Industry CHAR(30)," +
  " Ticker CHAR(6)," +
  " EndDate CHAR(10)," +
  " TickerReturn CHAR(12)," +
  " IndustryReturn CHAR(12));" );
  pstmt.executeUpdate();
  pstmt.close();
}

/*============================================================================/*

/*============================================================================*/
static ArrayList<String> getIndustries() throws SQLException {
  PreparedStatement pstmt;
  ArrayList<String> buildList = new ArrayList<String>();

  pstmt = connRead.prepareStatement(
  "select distinct Industry" +
  " from Company" +
  " order by Industry; ");
  ResultSet rs = pstmt.executeQuery();

  rs.last();
  int numIndustries = rs.getRow();
  System.out.printf("%d industries found %n", numIndustries);

  rs.beforeFirst();
  while (rs.next()) {
    String currString = rs.getString(1);
    buildList.add(currString);
  }
  pstmt.close();

  return buildList;
}

/*============================================================================/*
inserting into tables...
/*============================================================================*/
static void updateTable(String something) throws SQLException {
  PreparedStatement pstmt;

  //for each in some list...
  pstmt = connWrite.prepareStatement(
  "insert into Performance (Industry) values('" + something + "');");
  pstmt.executeUpdate();
  pstmt.close();
}

/*============================================================================/*
determineTradingInterval

Within a given industry, finds the appropriate trading interval.
Intervals must satisfy the following condition:
* Be >= 150 days
* Begin on the date of the newest Ticker (max(min(TransDate))
* End on the earliest date available across tickers (min(max(TransDate))

Tickers with less than 150 days are not included in the interval comparison
/*============================================================================*/
static void determineTradingInterval(String industry) throws SQLException {
  PreparedStatement pstmt;
  String ticker = null;
  String earliestDate = null;
  String latestDate = null;
  Double numTradeDays = null;

  pstmt = connRead.prepareStatement(
  "select Ticker, min(TransDate), max(TransDate)," +
  " count(distinct TransDate) as TradingDays" +
  " from Company natural join PriceVolume" +
  " where Industry = '" + industry + "'" +
  " and TransDate >= " +
  "  (select min(TransDate) as minDate" +
  "  from Company natural left outer join PriceVolume" +
  "  where Industry = '" + industry + "'" +
  "  group by Ticker" +
  "  order by minDate desc" +
  "  limit 1)" +
  " and TransDate <= " +
  "  (select max(TransDate) as maxDate" +
  "  from Company natural left outer join PriceVolume" +
  "  where Industry = '" + industry + "' and TransDate is not null" +
  "  group by Ticker" +
  "  order by maxDate asc" +
  "  limit 1) " +
  " group by Ticker " +
  " having TradingDays >= 150 " +
  " order by Ticker;  ");
  ResultSet rs = pstmt.executeQuery();


  // Get data from the first in the list
  rs.next();
  ticker = rs.getString(1);
  earliestDate = rs.getString(2);
  latestDate = rs.getString(3);
  numTradeDays = Double.parseDouble(rs.getString(4));

  // Get total number of tickers
  int numTickers = 1;
  while (rs.next()) {
    numTickers++;
  }
  pstmt.close();

  System.out.printf("%d accepted tickers for %s(%s - %s), %.2f   common dates%n",
  numTickers, industry, earliestDate, latestDate, numTradeDays);

  double numTradeIntervals = Math.floor((numTradeDays)/60);
  System.out.printf("Intervals: %.2f  %n", numTradeIntervals);

  getIntervalDates(ticker, earliestDate, latestDate, numTradeIntervals);

}

/*============================================================================/*
getIntervalDates

Uses the first ticker (given as input) in an industry to find the 60 day
interval start and end dates
/*============================================================================*/
static void getIntervalDates(String ticker, String earliestDate, String latestDate, Double numTradeIntervals) throws SQLException {
  PreparedStatement pstmt;
  ArrayList<String> startDates = new ArrayList<String>();
  ArrayList<String> endDates = new ArrayList<String>();

  pstmt = connRead.prepareStatement(
  "select P.TransDate, P.openPrice, P.closePrice" +
  " from PriceVolume P" +
  " where Ticker = '" + ticker + "' and TransDate >= '" + earliestDate + "'" +
  " and TransDate <= '" + latestDate + "';");

  ResultSet rs = pstmt.executeQuery();

  int dayCount = 1;
  int intervalCount = 0;

  //System.out.println("INTERVAL     DAY      START     DAY NUM     END");

  while (rs.next()){
    String currDate = rs.getString(1);

    if(intervalCount < numTradeIntervals) {
      if((dayCount-1)%60 == 0) {
        startDates.add(currDate);
        //System.out.printf("%d              %d", intervalCount+1, dayCount);
      }

      if(dayCount%60 == 0) {
        endDates.add(currDate);
        //System.out.printf("        %s          %d          %s%n", startDates.get(intervalCount), dayCount, endDates.get(intervalCount));
        intervalCount++;
      }

      dayCount++;
    }

  }
}

/*============================================================================/*
industryReturns

Does something?
/*============================================================================*/
static void industryReturns() {
  // ON THE FIRST DAY OF AN INTERVAL
  // or the given stock in the given interval (see rules above for definition of
  // “first trading day”), we invest D dollars in stock X, buying our shares at the opening price on that
  // day. We will call the opening price openPrice. We assume that we can purchase fractional shares,
  // since the number of shares bought will be D/openPrice and is unlikely to be an integer.


  // ON THE LAST DAY OF AN INTERVAL
  // we sell all our shares at the closing
  // price for that day, which we will call closePrice.

  //Double tickerReturn = (closePrice/openPrice) - 1;

  // Now, assuming there are m stocks in industry group Y with m ≥ 2, we buy a "basket" of stocks
  // by invest D/(m−1) dollars in each stock in the industry, except for ticker X. We buy the stocks at
  // the opening price on the first trading day for each ticker, same as we did for the ticker X.



}

}
