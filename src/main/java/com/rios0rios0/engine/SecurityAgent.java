package com.rios0rios0.engine;

import com.rios0rios0.actions.TargetsList;
import com.rios0rios0.info.Report;
import com.rios0rios0.utils.Commands;
import com.rios0rios0.utils.Console;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SecurityAgent extends DefaultAgent implements Agent {

    private static final String SERVICE_NAME = "MASAgent";

    private static final int CONNECTION_TIMEOUT = 500;

    private HashMap<String, String> neighbours = new HashMap<>();

    private HashSet<Agent> neighboursAgents = new HashSet<>();

    private HashMap<Agent, Report> reportsAgents = new HashMap<>();

    private String hostName = "";

    private String hostAddress = "";

    private final Runnable scanThread = new Runnable() {
        public void run() {
            scanNetwork();
        }
    };

    private final Runnable sendThread = new Runnable() {
        public void run() {
            sendReportAgents();
        }
    };

    private final Runnable actionThread = new Runnable() {
        public void run() {
            actingProcess();
        }
    };

    @SuppressWarnings("InfiniteLoopStatement")
    private void scanNetwork() {
        while (true) {
            try {
                searchNeighbours();
                synchronized (scanThread) {
                    scanThread.wait(5000);
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                Console.showMsgError(("Thread interrompida: ").concat(e.getMessage()));
            }
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void sendReportAgents() {
        while (true) {
            try {
                if (!this.neighboursAgents.isEmpty()) {
                    for (Agent agent : this.neighboursAgents) {
                        agent.ack(SecurityAgent.this.syn());
                        Console.showSyn(SecurityAgent.this.syn().toString());
                    }
                }
                synchronized (sendThread) {
                    sendThread.wait(15000);
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                Console.showMsgError(("Thread interrompida: ").concat(e.getMessage()));
            }
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void actingProcess() {
        while (true) {
            try {
                blacklistProcess();
                whitelistProcess();
                synchronized (actionThread) {
                    actionThread.wait(15000);
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                Console.showMsgError(("Thread interrompida: ").concat(e.getMessage()));
            }
        }
    }

    public SecurityAgent() {
        try {
            if (isSingle()) {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                    setHostName(InetAddress.getLocalHost().getCanonicalHostName());
                    Console.showMsgInfo(("HostName => ").concat(getHostName()));
                    setHostAddress(socket.getLocalAddress().getHostAddress());
                    Console.showMsgInfo(("HostAddress => ").concat(getHostAddress()));
                    Console.showMsgSuccess("Iniciando Threads...");
                    ExecutorService executor = Executors.newFixedThreadPool(3);
                    executor.execute(scanThread);
                    executor.execute(sendThread);
                    executor.execute(actionThread);
                    init();
                } catch (Exception e) {
                    Console.showMsgError(("Erro desconhecido: ").concat(e.getMessage()));
                }
            } else {
                System.exit(1);
            }
        } catch (Exception e) {
            Console.showMsgError(("Erro desconhecido: ").concat(e.getMessage()));
        }
    }

    private void init() {
        try {
            Console.showMsgSuccess("Iniciando servidor de recebimento...");
            System.setProperty("java.rmi.server.hostname", getHostAddress());
            Agent agent = (Agent) UnicastRemoteObject.exportObject(this, 0);
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(SERVICE_NAME, agent);
            Console.showMsgSuccess("Servidor iniciado.");
        } catch (Exception e) {
            Console.showMsgError(("Erro desconhecido: ").concat(e.getMessage()));
        }
    }

    private boolean isSingle() {
        try {
            Console.showMsgSuccess("Examinando critério de singularidade...");
            Agent agent = (Agent)
                    Naming.lookup("//localhost:" + Registry.REGISTRY_PORT + "/" + SERVICE_NAME);
            Console.showMsgError(String.format("O agente não é o único %s no dispositivo atual...", SERVICE_NAME));
            return (agent == null);
        } catch (ConnectException e) {
            return true;
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            Console.showMsgError(("Erro desconhecido: ").concat(e.getMessage()));
            return false;
        }
    }

    private ProcessInfo stackContains(List<ProcessInfo> executionStack, String processName) {
        for (ProcessInfo process : executionStack) {
            if (process.getName().equals(processName) && !process.getUser().equals("root")) {
                return process;
            }
        }
        return null;
    }

    private void upProcess(String processName) {
        Report report = new Report(this);
        List<ProcessInfo> tmpStack = report.getExecutionStack();
        ProcessInfo process = stackContains(tmpStack, processName);
        TargetsList targets = new TargetsList("whitelist");
        Map<String, String> whitelist = targets.listProperties();
        if (process == null) {
            Console.showMsgInfo(String.format("Iniciando processo '%s' (%s)...", processName, whitelist.get(processName).split(";")[0]));
            try {
                boolean result;
                result = Commands.executeCommandRoot(whitelist.get(processName).split(";")[0]);
                if (result) {
                    Console.showMsgSuccess(String.format("Processo '%s' iniciado!", processName));
                } else {
                    Console.showMsgError(String.format("Erro ao iniciar processo: '%s'...", processName));
                }
            } catch (Exception e) {
                Console.showMsgError(String.format("Processo '%s' não iniciado. Talvez seja inexistente...", processName));
            }
        }
    }

    private void downProcess(String processName) {
        Report report = new Report(this);
        List<ProcessInfo> tmpStack = report.getExecutionStack();
        ProcessInfo process = stackContains(tmpStack, processName);
        TargetsList targets = new TargetsList("blacklist");
        Map<String, String> blacklist = targets.listProperties();
        targets = new TargetsList("whitelist");
        Map<String, String> whitelist = targets.listProperties();
        if ((process != null) && tmpStack.contains(process)) {
            Console.showMsgInfo(String.format("Derrubando processo '%s' (%s)...", process.getName(), process.getCommand()));
            try {
                boolean result = JProcesses.killProcess(Integer.parseInt(process.getPid())).isSuccess();
                if (result) {
                    Console.showMsgSuccess(String.format("Processo '%s' morto!", process.getName()));
                } else {
                    Console.showMsgInfo(String.format("Forçando desligamento do processo: '%s'...", process.getName()));
                    String command = whitelist.get(process.getName());
                    if ((command != null) && (!command.equals(""))) {
                        result = Commands.executeCommandRoot(command.split(";")[1]);
                    } else {
                        command = blacklist.get(process.getName());
                        if ((command != null) && (!command.equals(""))) {
                            result = Commands.executeCommandRoot(command + " " + process.getPid());
                        } else {
                            result = false;
                            Console.showMsgError(String.format("Processo '%s' desconhecido!", process.getName()));
                        }
                    }
                    if (result) {
                        Console.showMsgSuccess(String.format("Processo '%s' morto!", process.getName()));
                    } else {
                        Console.showMsgError(String.format("Erro ao derrubar processo: '%s'...", process.getName()));
                    }
                }
            } catch (Exception e) {
                Console.showMsgError(e.getMessage());
            }
        }
    }

    private void shutdownDevice() {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("shutdown -h now");
            System.exit(100);
        } catch (Exception e) {
            Console.showMsgError(e.getMessage());
        }
    }

    private void blacklistProcess() {
        TargetsList targets = new TargetsList("blacklist");
        Map<String, String> blacklist = targets.listProperties();
        for (String processName : blacklist.keySet()) {
            List<String> hasYes = new ArrayList<>();
            List<String> hasNo = new ArrayList<>();
            Iterator iterator = this.reportsAgents.keySet().iterator();
            while (iterator.hasNext()) {
                Agent tmpAgent = (Agent) iterator.next();
                List<ProcessInfo> tmpStack = this.reportsAgents.get(tmpAgent).getExecutionStack();
                if (stackContains(tmpStack, processName) != null) {
                    hasYes.add(this.reportsAgents.get(tmpAgent).getHostAdress());
                } else {
                    hasNo.add(this.reportsAgents.get(tmpAgent).getHostAdress());
                }
            }
            if (!hasYes.isEmpty()) {
                if (hasYes.size() > hasNo.size()) {
                    Console.showMsgError(String.format("Desligamento preventivo. "
                            + "%d agentes ao redor, executam processos não permitidos...", hasYes.size()));
                    shutdownDevice();
                } else {
                    Console.showMsgError(String.format("Processo malicioso detectado em: %s", Console.implode(",", hasYes)));
                    downProcess(processName);
                }
            } else {
                downProcess(processName);
            }
        }
    }

    private void whitelistProcess() {
        TargetsList targets = new TargetsList("whitelist");
        Map<String, String> whitelist = targets.listProperties();
        for (String processName : whitelist.keySet()) {
            List<String> hasYes = new ArrayList<>();
            List<String> hasNo = new ArrayList<>();
            Iterator iterator = this.reportsAgents.keySet().iterator();
            while (iterator.hasNext()) {
                Agent tmpAgent = (Agent) iterator.next();
                List<ProcessInfo> tmpStack = this.reportsAgents.get(tmpAgent).getExecutionStack();
                if (stackContains(tmpStack, processName) != null) {
                    hasYes.add(this.reportsAgents.get(tmpAgent).getHostAdress());
                } else {
                    hasNo.add(this.reportsAgents.get(tmpAgent).getHostAdress());
                }
            }
            if (!hasYes.isEmpty()) {
                if (hasYes.size() > hasNo.size()) {
                    upProcess(processName);
                } else {
                    downProcess(processName);
                }
            } else {
                upProcess(processName);
            }
        }
    }

    private void removeOfflineNeighbour(String hostAddress) {
        Iterator iterator = this.neighbours.keySet().iterator();
        while (iterator.hasNext()) {
            try {
                if (iterator.next().equals(hostAddress)) {
                    Console.showMsgInfo(String.format("Dispositivo offline => %s (%s)",
                            this.neighbours.get(hostAddress), hostAddress));
                    iterator.remove();
                }
            } catch (Exception e) {
                Console.showMsgError(("Erro desconhecido: ").concat(e.getMessage()));
            }
        }
    }

    private void removeOfflineAgent() {
        Iterator iterator = this.neighboursAgents.iterator();
        while (iterator.hasNext()) {
            Agent agent = (Agent) iterator.next();
            try {
                agent.syn();
            } catch (ConnectException e) {
                Console.showMsgError(String.format("%s shutdown (motivo desconhecido) => %s (%s)",
                        SERVICE_NAME,
                        this.reportsAgents.get(agent).getHostName(),
                        this.reportsAgents.get(agent).getHostAdress()));
                Console.showMsg(("Última comunicação => ").concat(this.reportsAgents.get(agent).getTimestamp()));
                this.reportsAgents.remove(agent);
                iterator.remove();
            } catch (RemoteException e) {
                Console.showMsgError(("Erro desconhecido: ").concat(e.getMessage()));
            }
        }
    }

    private void searchNeighbours() {
        try {
            for (int i = 1; i < 255; i++) {
                String hostAddress = getHostAddress().substring(0, getHostAddress().lastIndexOf('.') + 1) + i;
                if ((!hostAddress.equals(getHostAddress())) && (InetAddress.getByName(hostAddress).isReachable(CONNECTION_TIMEOUT))) {
                    String hostName = InetAddress.getByName(hostAddress).getCanonicalHostName();
                    try {
                        Agent agent = (Agent)
                                Naming.lookup("//" + hostAddress + ":" + Registry.REGISTRY_PORT + "/" + SERVICE_NAME);
                        if (agent != null) {
                            if (!this.neighboursAgents.contains(agent)) {
                                this.neighbours.remove(hostAddress);
                                this.neighboursAgents.add(agent);
                                Console.showMsgInfo(String.format("%s encontrado => %s (%s)", SERVICE_NAME, hostName, hostAddress));
                            }
                            this.ack(agent.syn());
                        }
                    } catch (ConnectException e) {
                        if (!this.neighbours.containsKey(hostAddress)) {
                            this.neighbours.put(hostAddress, hostName);
                            Console.showMsg(String.format("Dispositivo => %s (%S)", hostName, hostAddress));
                        } else {
                            if (!hostName.equals(this.neighbours.get(hostAddress))) {
                                this.neighbours.put(hostAddress, hostName);
                                Console.showMsg(String.format("Dispositivo => %s (%S)", hostName, hostAddress));
                            }
                        }
                    } catch (RemoteException | NotBoundException | MalformedURLException e) {
                        Console.showMsgError(("Erro desconhecido: ").concat(e.getMessage()));
                    }
                } else {
                    removeOfflineNeighbour(hostAddress);
                    removeOfflineAgent();
                }
            }
        } catch (
                Exception e) {
            Console.showMsgError(("Erro desconhecido: ").concat(e.getMessage()));
        }
    }

    public Report syn() {
        return new Report(this);
    }

    public void ack(Report report) {
        try {
            Agent agent = (Agent)
                    Naming.lookup("//" + report.getHostAdress() + ":" + Registry.REGISTRY_PORT + "/" + SERVICE_NAME);
            if (agent != null) {
                if (!this.neighboursAgents.contains(agent)) {
                    this.neighbours.remove(report.getHostAdress());
                    this.neighboursAgents.add(agent);
                    Console.showMsgInfo(String.format("%s encontrado => %s (%s)", SERVICE_NAME, report.getHostName(), report.getHostAdress()));
                }
                this.reportsAgents.put(agent, report);
            }
            Console.showAck(report.toString());
        } catch (ConnectException e) {
            //TODO: more 1 decision in here
            Console.showAck(report.toString());
            Console.showMsgError("Report perdido. O agente foi finalizado, após envio do report...");
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            Console.showMsgError(("Erro desconhecido: ").concat(e.getMessage()));
        }
    }

    public String getHostName() {
        return hostName;
    }

    private void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    private void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }
}