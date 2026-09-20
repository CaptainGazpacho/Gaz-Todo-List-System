package com.gaz.todolist;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.UUID;

import io.github.cdimascio.dotenv.Dotenv;

public class databaseManager {

    /**
     * Creates a new database file if it does not already exist
     */
    public static void createDatabase() {

        String[] listTables = {"TODO_LIST", "MANUAL_TODO_LIST", "PLAN_TODO_LIST", "DELEGATE_TODO_LIST", "HOLD_TODO_LIST", "RECURRING_TODO_LIST", "COMPLETE_TODO_LIST"};

        try {
            File myObj = new File("todo_list_app\\sql\\todo_list.db");
            if (myObj.createNewFile()) {
            } else {
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace(); 
        }

        String url = "jdbc:sqlite:todo_list_app\\sql\\todo_list.db";

        String createAccountTableQuery = """
                CREATE TABLE IF NOT EXISTS ACCOUNT (
                    "ACCOUNT_NAME"	TEXT PRIMARY KEY,
                    "PASSWORD"	TEXT,
                    "SESSION_TOKEN" TEXT,
                    "AUTOSAVE_STATUS" TEXT
                );
                """;

        String createAccountLogTableQuery = """
                CREATE TABLE IF NOT EXISTS ACCOUNT_LOG (
                    "TIMESTAMP"	TEXT,
                    "TABLE_NAME" TEXT,
                    "ACTION" TEXT,
                    "RECORD_ID" TEXT
                );
                """;

        String createAccountInsertLogTriggerQuery = """
                CREATE TRIGGER IF NOT EXISTS ACCOUNT_INSERT_LOG AFTER INSERT ON ACCOUNT
                BEGIN
                    INSERT INTO ACCOUNT_LOG (TIMESTAMP, TABLE_NAME, ACTION, RECORD_ID) VALUES (datetime(current_timestamp, 'localtime'), 'ACCOUNT', 'INSERT', new.ACCOUNT_NAME);
                END;
                """;

        String createAccountUpdateLogTriggerQuery = """
                CREATE TRIGGER IF NOT EXISTS ACCOUNT_UPDATE_LOG AFTER UPDATE ON ACCOUNT
                BEGIN
                    INSERT INTO ACCOUNT_LOG (TIMESTAMP, TABLE_NAME, ACTION, RECORD_ID) VALUES (datetime(current_timestamp, 'localtime'), 'ACCOUNT', 'UPDATE', new.ACCOUNT_NAME);
                END;
                """;

        String createAccountDeleteLogTriggerQuery = """
                CREATE TRIGGER IF NOT EXISTS ACCOUNT_DELETE_LOG AFTER DELETE ON ACCOUNT
                BEGIN
                    INSERT INTO ACCOUNT_LOG (TIMESTAMP, TABLE_NAME, ACTION, RECORD_ID) VALUES (datetime(current_timestamp, 'localtime'), 'ACCOUNT', 'DELETE', old.ACCOUNT_NAME);
                END;
                """;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                PreparedStatement createAccountTableStmt = conn.prepareStatement(createAccountTableQuery);
                createAccountTableStmt.execute();

                PreparedStatement createAccountLogTableStmt = conn.prepareStatement(createAccountLogTableQuery);
                createAccountLogTableStmt.execute();

                PreparedStatement createAccountInsertLogTriggerStmt = conn.prepareStatement(createAccountInsertLogTriggerQuery);
                createAccountInsertLogTriggerStmt.execute();

                PreparedStatement createAccountUpdateLogTriggerStmt = conn.prepareStatement(createAccountUpdateLogTriggerQuery);
                createAccountUpdateLogTriggerStmt.execute();

                PreparedStatement createAccountDeleteLogTriggerStmt = conn.prepareStatement(createAccountDeleteLogTriggerQuery);
                createAccountDeleteLogTriggerStmt.execute();

                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        for(String tableName:listTables) {
            String createTableQuery = """
                    CREATE TABLE IF NOT EXISTS %s (
                        "ACCOUNT"	TEXT,
                        "TASK_ID"	TEXT PRIMARY KEY,
                        "RANK"	INTEGER,
                        "TASK"	TEXT,
                        "DEADLINE"	INTEGER,
                        "SCHEDULED_TIME"	TEXT,
                        "MANUAL"	TEXT,
                        "RECURRING"	TEXT,
                        "SIZE"	INTEGER,
                        "STATUS"	INTEGER
                    );
                    """.formatted(tableName);

            String generateLogTable = """
                    CREATE TABLE IF NOT EXISTS %s_LOG (
                        "TIMESTAMP"	TEXT,
                        "TABLE_NAME" TEXT,
                        "ACTION" TEXT,
                        "RECORD_ID" TEXT
                    );
                    """.formatted(tableName);

            String createInsertLogTriggerQuery = """
                    CREATE TRIGGER IF NOT EXISTS %s_INSERT_LOG AFTER INSERT ON %s
                    BEGIN
                        INSERT INTO %s_LOG (TIMESTAMP, TABLE_NAME, ACTION, RECORD_ID) VALUES (datetime(current_timestamp, 'localtime'), '%s', 'INSERT', new.TASK_ID);
                    END;
                    """.formatted(tableName, tableName, tableName, tableName, tableName);

            String createUpdateLogTriggerQuery = """
                    CREATE TRIGGER IF NOT EXISTS %s_UPDATE_LOG AFTER UPDATE ON %s
                    BEGIN
                        INSERT INTO %s_LOG (TIMESTAMP, TABLE_NAME, ACTION, RECORD_ID) VALUES (datetime(current_timestamp, 'localtime'), '%s', 'UPDATE', new.TASK_ID);
                    END;
                    """.formatted(tableName, tableName, tableName, tableName, tableName);

            String createDeleteLogTriggerQuery = """
                    CREATE TRIGGER IF NOT EXISTS %s_DELETE_LOG AFTER DELETE ON %s
                    BEGIN
                        INSERT INTO %s_LOG (TIMESTAMP, TABLE_NAME, ACTION, RECORD_ID) VALUES (datetime(current_timestamp, 'localtime'), '%s', 'DELETE', old.TASK_ID);
                    END;
                    """.formatted(tableName, tableName, tableName, tableName, tableName);

            try (Connection conn = DriverManager.getConnection(url)) {
                if (conn != null) {
                    PreparedStatement createTableStmt = conn.prepareStatement(createTableQuery);
                    createTableStmt.execute();

                    PreparedStatement generateLogTableStmt = conn.prepareStatement(generateLogTable);
                    generateLogTableStmt.execute();

                    PreparedStatement createInsertLogTriggerStmt = conn.prepareStatement(createInsertLogTriggerQuery);
                    createInsertLogTriggerStmt.execute();

                    PreparedStatement createUpdateLogTriggerStmt = conn.prepareStatement(createUpdateLogTriggerQuery);
                    createUpdateLogTriggerStmt.execute();

                    PreparedStatement createDeleteLogTriggerStmt = conn.prepareStatement(createDeleteLogTriggerQuery);
                    createDeleteLogTriggerStmt.execute();

                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Loads data from the database.
     * @return A todoList object containing the loaded data.
     */
    public static todoList loadData() {
        Dotenv dotenv = Dotenv.load();

        todoList todoList = new todoList();

        String url = "jdbc:sqlite:todo_list_app\\sql\\todo_list.db";

        String loadQuery = """
                SELECT * FROM TODO_LIST WHERE ACCOUNT = ?;
                """;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                PreparedStatement loadStmt = conn.prepareStatement(loadQuery);
                loadStmt.setString(1, dotenv.get("ACCOUNT"));
                ResultSet resultSet = loadStmt.executeQuery();
                while (resultSet.next()) {
                    String account = resultSet.getString("ACCOUNT");
                    String taskID = resultSet.getString("TASK_ID");
                    int rank = resultSet.getInt("RANK");
                    String task = resultSet.getString("TASK");
                    LocalDateTime deadline = LocalDateTime.parse(resultSet.getString("DEADLINE"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    LocalDateTime scheduledTime = LocalDateTime.parse(resultSet.getString("SCHEDULED_TIME"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    Boolean manual = Boolean.parseBoolean(resultSet.getString("MANUAL"));
                    Boolean recurring = Boolean.parseBoolean(resultSet.getString("RECURRING"));
                    scale size = scale.values()[resultSet.getInt("SIZE")];
                    progress status = progress.values()[resultSet.getInt("STATUS")];

                    todoList.loadExistingItem(account, taskID, rank, task, deadline, scheduledTime, manual, recurring, size, status);
                }

                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return todoList;
    }

    /**
     * Connects to Database and saves the list
     * @param listName
     * @param todo
     */
    public static void saveToDatabase(String listName, ArrayList<todoItem> todo) {
        String table = " " + listName;

        String url = "jdbc:sqlite:todo_list_app\\sql\\todo_list.db";

        String mergeQuery = """
                INSERT OR REPLACE INTO %s
                    (ACCOUNT, TASK_ID, RANK, TASK, DEADLINE, SCHEDULED_TIME, MANUAL, RECURRING, SIZE, STATUS)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """.formatted(table);

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                PreparedStatement mergeStmt = conn.prepareStatement(mergeQuery);

                for(todoItem item:todo) {
                    mergeStmt.setString(1, item.getAccount());
                    mergeStmt.setString(2, item.getTaskID());
                    mergeStmt.setInt(3, item.getRank());
                    mergeStmt.setString(4, item.getTask());
                    mergeStmt.setString(5, item.getDeadline());
                    mergeStmt.setString(6, item.getScheduledTime());
                    mergeStmt.setString(7, item.getManual());
                    mergeStmt.setString(8, item.getRecurring());
                    mergeStmt.setInt(9, item.getSize());
                    mergeStmt.setInt(10, item.getStatus());

                    mergeStmt.execute();
                }

                todo = null;
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Inserts an account record into the account table
     * @param account
     * @param password
     */
    public static void registerAccount(String account, String password) {
        String url = "jdbc:sqlite:todo_list_app\\sql\\todo_list.db";

        String encryptedPassword = "" + password.hashCode();

        String insertQuery = """
                INSERT INTO ACCOUNT (ACCOUNT_NAME, PASSWORD, SESSION_TOKEN, AUTOSAVE_STATUS)
                    VALUES (?, ?, ?, ?);
                """;

        try(Connection conn = DriverManager.getConnection(url)) {
            if(conn != null) {
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

                insertStmt.setString(1, account);
                insertStmt.setString(2, encryptedPassword);
                insertStmt.setString(3, UUID.randomUUID().toString());
                insertStmt.setString(4, "FALSE");

                insertStmt.execute();

                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
