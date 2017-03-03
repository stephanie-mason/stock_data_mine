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

class Assignment2 {
static Connection conn = null;

public static void main(String[] args) throws Exception {
  String readParamsFile = "readerparams.txt";
  String writeParamsFile = "writerparams.txt";
  if (args.length >= 1) {
    paramsFile = args[0];
  }
  Properties connectProps = new Properties();
  connectProps.load(new FileInputStream(readParamsFile));

  try {
    //Connect to the read database
    Class.forName("com.mysql.jdbc.Driver");
    String dburl = connectProps.getProperty("dburl");
    String username = connectProps.getProperty("user");
    conn = DriverManager.getConnection(dburl, connectProps);
    System.out.printf("Reader connection %s %s established.%n",
    dburl, username);

    //Get ticker/date input from user for as long as they want to give it
    Scanner sc = new Scanner(System.in);
    boolean continueLoop = true;
    ArrayList<StockDay> setOfStockDays = new ArrayList<StockDay>();

/*============================================================================/*
This is where the main functions get called
/*----------------------------------------------------------------------------*/
          if (findCompanyName(ticker)){
            if (numArgs == 3) {
              startDate = inputArgs[1];
              endDate = inputArgs [2];
              setOfStockDays = runDates(ticker, startDate, endDate);
              if(setOfStockDays.size() > 50)
                investStrategy(setOfStockDays);
              else System.out.println("Net cash: 0");
            }	else {
              setOfStockDays = runDates(ticker);
              if(setOfStockDays.size() > 50)
                investStrategy(setOfStockDays);
              else System.out.printf("Net cash: 0%n%n");
            }
        }
/*----------------------------------------------------------------------------*/



    }
    conn.close();
    System.out.printf("Connection closed.%n");
  } catch (SQLException ex) {
    System.out.printf("SQLException: %s%nSQLState: %s%nVendor Error: %s%n",
    ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
    conn.close();
  }
}

/*============================================================================/*

/*============================================================================*/
static boolean firstQuery(String ticker) throws SQLException {
  PreparedStatement pstmt = conn.prepareStatement(
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
