/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw2;

import java.rmi.Remote;
import java.rmi.RemoteException;
import se.kth.id2212.ex2.bankrmi.Account;

/**
 *
 * @author majil
 */
public interface TraderClient extends Remote {
    
    public Account getAccount() throws RemoteException;
    
    public void notify(String message) throws RemoteException;
    
}
