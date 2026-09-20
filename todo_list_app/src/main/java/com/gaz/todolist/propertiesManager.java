package com.gaz.todolist;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class propertiesManager {
    /**
     * Creates a properties file.
     */
    public static void createProperties() {
        Path p = Path.of("app.properties");
        try {
            Writer w = Files.newBufferedWriter(p);
            Properties props = new Properties();
            props.setProperty("APP_NAME", "GAZ-TODO-LIST");
            props.setProperty("ACCOUNT", "");
            props.setProperty("AUTOSAVE", "");
            props.store(w, "Created properties file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the account property
     * @param account
     * @throws IOException 
     */
    public static void setAccountProperty(String account) throws IOException {
        FileOutputStream out = new FileOutputStream("app.properties");
        FileInputStream in = new FileInputStream("app.properties");
        Properties prop = new Properties();
        prop.load(in);
        in.close();
        prop.setProperty("ACCOUNT", account);
        prop.store(out, "Set the account property.");
        out.close();
    }

    /**
     * Loads the Account Property if it exists.
     * @return String
     */
    public static String loadAccountProperty() {
        String account = "";
        Properties properties = new Properties();
        try(FileInputStream in = new FileInputStream("app.properties")) {
            properties.load(in);
            account = "" + properties.getProperty("app.ACCOUNT");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return account;
    }

    /**
     * Sets the autosave property
     * @param autosave
     * @throws IOException 
     */
    public static void setAutosaveProperty(boolean autosave) throws IOException {
        FileOutputStream out = new FileOutputStream("app.properties");
        FileInputStream in = new FileInputStream("app.properties");
        Properties prop = new Properties();
        prop.load(in);
        in.close();
        prop.setProperty("AUTOSAVE", "" + autosave);
        prop.store(out, "Set the autosave property.");
        out.close();
    }
}
