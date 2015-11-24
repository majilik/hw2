package hw2;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;

@SuppressWarnings("serial")
public class MarketImpl extends UnicastRemoteObject implements Market {

    private List<Item> items;
    private String marketName;
    Bank bankobj;
    private static final String DEFAULT_BANK_NAME = "BankOfHutta";
    

    public MarketImpl(String marketName) throws RemoteException {
        this.marketName = marketName;
        this.items = new ArrayList<>();
      
    }

    @Override
    public String[] listItems() throws RemoteException {
        String[] result = new String[items.size()];
        for(int i = 0; i < result.length; i++)
        {
            result[i] = items.get(i).toString();
        }
        return result;
    }

    @Override
    public void sellItem(Account acc, String name, float price) throws RemoteException {
       
    }

    @Override
    public void buyItem(Account acc,String name, float price) throws RejectedException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void wishItem(Account acc, String name, float price) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class Item {
        
        private String name;
        private float price;

        public Item(String name, float price) {
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() {
            return String.format("%s :: %.2f wuoiupi's", name, price);
        }

    }
}
