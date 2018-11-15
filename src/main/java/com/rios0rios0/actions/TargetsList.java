package com.rios0rios0.actions;

import com.rios0rios0.utils.Console;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TargetsList {

    private String fileName = "%s.properties";

    public TargetsList(String listName) {
        this.fileName = String.format(this.fileName, listName);
    }

    public Map<String, String> listProperties() {
        InputStream inputStream;
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            Properties prop = new Properties();
            inputStream = TargetsList.class.getClassLoader().getResourceAsStream(this.fileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + this.fileName + "' not found in the classpath...");
            }
            @SuppressWarnings("unchecked")
            Enumeration<String> enums = (Enumeration<String>) prop.propertyNames();
            while (enums.hasMoreElements()) {
                String key = enums.nextElement();
                String value = prop.getProperty(key);
                result.put(key, value);
            }
        } catch (Exception e) {
            Console.showMsgError(e.getMessage());
        }
        return result;
    }
}