package com.rios0rios0;

import com.rios0rios0.engine.SecurityAgent;
import com.rios0rios0.utils.Console;

/**
 * Class that run the Agent.
 * <p>
 * MAIS allows to interact with multiple system processes.
 * It is possible to communicate between Operating System, and communicate the processes correlated.
 *
 * @author Felipe Rios (rios0rios0)
 */
public class Main {

    public static void main(String[] args) {
        Console.showMsgSuccess("Iniciando o agente no dispositivo atual...");
        new SecurityAgent();
    }
}