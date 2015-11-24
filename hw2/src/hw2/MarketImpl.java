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
//        items.add(new Item("Blaster Pistol", 230.47f));
//        items.add(new Item("Thermal Detonator", 102f));
//        items.add(new Item("Vibroblade", 199.99f));
//        items.add(new Item("Stimpack", 150f));
//        items.add(new Item("Red Power Crystal", 23999.99f));
//        items.add(new Item("Jawa Juice", 5f));
//        items.add(new Item("Hyperdrive Generator (Nubian)", 100000f));
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
        Item incoming = new Item(name, price, acc);
        items.add(incoming);
        checkWish(incoming);
        
        
       
    }
    
    @Override
    public void buyItem(Account acc,String name, float price) throws RejectedException, RemoteException {
        try {
            for(Item i : items){
                if(i.getName().equals(name) && i.getPrice() == price){
                    acc.withdraw(price);
                    
                    i.getOwner().deposit(price);
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
        wishList.put(new Item(name, price, acc), acc);
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
        private Account owner;
        

        public Item(String name, float price, Account owner) {
            this.name = name;
            this.price = price;
            this.owner = owner;
        }
        
        public String getName(){return this.name;}
        public float getPrice(){return this.price;}
        public Account getOwner(){return this.owner;}
        @Override
        public String toString() {
            return String.format("%-30s :: %10.2f wupiupi's", name, price);
        }

    }
}
