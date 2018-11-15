package com.rios0rios0.info;

import com.rios0rios0.engine.SecurityAgent;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Report implements Serializable {

    private static final long serialVersionUID = 4614256212045810187L;

    private Timestamp timestamp;

    private String hostName;

    private String hostAdress;

    private List<ProcessInfo> executionStack;

    public Report(SecurityAgent agent) {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        setHostName(agent.getHostName());
        setHostAdress(agent.getHostAddress());
        setExecutionStack();
    }

    public String getTimestamp() {
        Date date = new Date(this.timestamp.getTime());
        DateFormat formatted = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return formatted.format(date);
    }

    public String getHostName() {
        return hostName;
    }

    private void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostAdress() {
        return hostAdress;
    }

    private void setHostAdress(String hostAdress) {
        this.hostAdress = hostAdress;
    }

    public List<ProcessInfo> getExecutionStack() {
        return executionStack;
    }

    private void setExecutionStack() {
        this.executionStack = JProcesses.getProcessList();
    }

    @Override
    public String toString() {
        return "==> Tempo       => " + getTimestamp() +
                "\n          HostName    => " + getHostName() +
                "\n          HostAddress => " + getHostAdress();
    }
}