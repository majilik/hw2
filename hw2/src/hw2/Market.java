package hw2;

import java.rmi.Remote;
import java.rmi.RemoteException;
import se.kth.id2212.ex2.bankrmi.RejectedException;

public interface Market extends Remote {
    public String[] listItems() throws RemoteException;
    
    public void sellItem(String owner, String name, float price) throws RemoteException;
    
    public void buyItem(String owner, String name, float price) 
            throws RejectedException, RemoteException;
    
    public void wishItem(String owner, String name, float price) throws RemoteException;
    
    public boolean login(TraderClient cl, String owner, String password) throws RemoteException;
    
    public void register(String owner, String password) throws RemoteException;
    
    
}
