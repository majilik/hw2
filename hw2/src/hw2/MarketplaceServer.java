package hw2;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class MarketplaceServer {
    
    private static final String MARKET = "MosEisleySpacePort";
    
    public MarketplaceServer(String marketName) {
        try {
            Market marketobj = new MarketImpl(marketName);
            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }
            Naming.rebind(marketName, marketobj);
            System.out.println(marketName + " is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MarketplaceServer(MARKET);
    }
}