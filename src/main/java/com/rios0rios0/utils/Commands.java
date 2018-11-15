package com.rios0rios0.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Commands {

    private static final String SUDO_PASSWD = "!@#$%";

    public static boolean executeCommandRoot(String cmd) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "echo " + SUDO_PASSWD + " | sudo -S " + cmd});
        } catch (IOException e) {
            return false;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        //String line;
        try {
            /*while ((line = br.readLine()) != null) {
                System.out.println(line);
            }*/
            br.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}