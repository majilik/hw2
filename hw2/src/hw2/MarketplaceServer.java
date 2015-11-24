package hw2;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.BankImpl;

public class MarketplaceServer {
    
    private static final String MARKET = "MosEisleySpacePort";
    private static final String BANK = "BankOfHutta";
    
    public MarketplaceServer(String marketName, String bankName) {
        try {
            Market marketobj = new MarketImpl(marketName);
            Bank bankobj = new BankImpl(bankName);
            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }
            Naming.rebind(marketName, marketobj);
            Naming.rebind(bankName, bankobj);
            System.out.println(marketName + " is ready.");
            System.out.println(bankName + " is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args) {
        new MarketplaceServer(MARKET, BANK);
    }
}