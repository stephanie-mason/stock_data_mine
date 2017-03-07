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
      firstQuery();
      createTable();



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

  /*============================================================================*/
  static void firstQuery() throws SQLException {
    PreparedStatement pstmt = connRead.prepareStatement(
    "select Industry, count(distinct Ticker) as TickerCnt " +
    " from Company natural join PriceVolume" +
    " group by Industry " +
    " order by TickerCnt DESC, Industry; ");
    ResultSet rs = pstmt.executeQuery();
    while (rs.next()) {
      System.out.printf(rs.getString(1) + rs.getString(2) + "%n");
    }
    pstmt.close();

    pstmt = connRead.prepareStatement(
    "select Ticker, min(TransDate), max(TransDate)," +
    " count(distinct TransDate) as TradingDays" +
    " from Company natural join PriceVolume" +
    " where Industry = 'Telecommunications Services'" +
    " and TransDate >= '2005.02.09' and TransDate <= '2014.08.18'" +
    " group by Ticker" +
    " having TradingDays >= 150" +
    " order by Ticker; ");
    rs = pstmt.executeQuery();
    while (rs.next()) {
    }
    pstmt.close();

    pstmt = connRead.prepareStatement(
    "select P.TransDate, P.openPrice, P.closePrice" +
    " from PriceVolume P" +
    " where Ticker = 'AMT' and TransDate >= '2005.02.09'" +
    " and TransDate <= '2014.08.18'; " );
    rs = pstmt.executeQuery();
    while (rs.next()) {
    }
  }

  /*============================================================================/*

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
}
