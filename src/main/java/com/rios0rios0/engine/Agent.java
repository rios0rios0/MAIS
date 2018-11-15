package com.rios0rios0.engine;

import com.rios0rios0.info.Report;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Agent extends Remote {

    Report syn() throws RemoteException;

    void ack(Report report) throws RemoteException;
}