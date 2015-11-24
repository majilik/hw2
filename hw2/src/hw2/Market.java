package hw2;

import java.rmi.Remote;
import java.rmi.RemoteException;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.RejectedException;

public interface Market extends Remote {
    public String[] listItems() throws RemoteException;
    
    public void sellItem(Account acc, String name, float price) throws RemoteException;
    
    public void buyItem(Account acc, String name, float price) 
            throws RejectedException, RemoteException;
    
    public void wishItem(Account acc, String name, float price) throws RemoteException;
    
    
}
