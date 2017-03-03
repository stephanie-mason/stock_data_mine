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
  static boolean firstQuery(String ticker) throws SQLException {
    PreparedStatement pstmt = connRead.prepareStatement(
    "select Name " +
    " from Company " +
    " where Ticker = ?");
    pstmt.setString(1, ticker);
    ResultSet rs = pstmt.executeQuery();

    if (rs.next()) {
      System.out.printf(rs.getString(1) + "%n");
      return true;
    } else {
      System.out.printf("%s not found in database.%n", ticker);
      return false;
    }
  }
}
