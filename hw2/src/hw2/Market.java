package hw2;

import java.rmi.Remote;
import java.rmi.RemoteException;
import se.kth.id2212.ex2.bankrmi.RejectedException;

public interface Market extends Remote {
    public String[] listItems() throws RemoteException;
    
    public void sellItem(TraderClient cl, String name, float price) throws RemoteException;
    
    public void buyItem(TraderClient cl, String name, float price) 
            throws RejectedException, RemoteException;
    
    public void wishItem(TraderClient cl, String name, float price) throws RemoteException;
    
    
    
}
