package hw2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class TraderClient {
    
    private static final String DEFAULT_MARKET = "MosEisleySpacePort";
    
    private Market marketobj;
    private String traderName;
    
    public TraderClient() {
        try {
            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }
            marketobj = (Market) Naming.lookup(DEFAULT_MARKET);
        } catch (Exception e) {
            System.out.println("The runtime failed: " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Connected to market: " + DEFAULT_MARKET);
    }
    
    public void repl()
    {
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("Press any key to test items!");
            try {
                System.in.read();
                for(String itemListing : marketobj.listItems())
                {
                    System.out.println("| " + itemListing);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
    }
    
    public static void main(String[] args) {
        new TraderClient().repl();
    }
}
