package hw2;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;

@SuppressWarnings("serial")
public class MarketImpl extends UnicastRemoteObject implements Market {

    private List<Item> items;
    private Map<Item, Account> wishList;
    private String marketName;
    Bank bankobj;
    private static final String DEFAULT_BANK_NAME = "BankOfHutta";
    

    public MarketImpl(String marketName) throws RemoteException {
        this.marketName = marketName;
        this.items = new ArrayList<>();
        this.wishList = new HashMap<>();
        
               
      
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
        Item incoming = new Item(name, price);
        try {
            
            acc.deposit(price);
            items.add(incoming);
            checkWish(incoming);
        } catch (RejectedException ex) {
            Logger.getLogger(MarketImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
    }
    
    @Override
    public void buyItem(Account acc,String name, float price) throws RejectedException, RemoteException {
        try {
            acc.withdraw(price);
            for(Item i : items){
                if(i.getName().equals(name)){
                    items.remove(i);
                    break;
                }
            }
        } catch (RejectedException ex) {
            Logger.getLogger(MarketImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void wishItem(Account acc, String name, float price) throws RemoteException {
        wishList.put(new Item(name, price), acc);
    }
    
    private void checkWish(Item incoming){
        Iterator it = wishList.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Item, Account> wish = (Map.Entry)it.next();
            Item i = wish.getKey();
            if(incoming.name.equals(i.name) && incoming.price <= i.price){
                // notify
            }
        }
    }

    private class Item {
        
        private String name;
        private float price;
        

        public Item(String name, float price) {
            this.name = name;
            this.price = price;
        }
        
        public String getName(){return this.name;}

        @Override
        public String toString() {
            return String.format("%s :: %.2f wuoiupi's", name, price);
        }

    }
}
