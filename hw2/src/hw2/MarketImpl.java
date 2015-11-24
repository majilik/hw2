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
    private List<Item> wished;
    private String marketName;
    Bank bankobj;
    private static final String DEFAULT_BANK_NAME = "BankOfHutta";
    

    public MarketImpl(String marketName) throws RemoteException {
        this.marketName = marketName;
        this.items = new ArrayList<>();
        this.wished = new ArrayList<Item>();
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
    public void sellItem(TraderClient cl, String name, float price) throws RemoteException {
        Item incoming = new Item(name, price, cl);
        items.add(incoming);
        checkWish(incoming);
        
        
       
    }
    
    @Override
    public void buyItem(TraderClient cl,String name, float price) throws RejectedException, RemoteException {
        try {
            for(Item i : items){
                if(i.getName().equals(name) && i.getPrice() == price){
                    cl.getAccount().withdraw(price);
                    
                    i.getOwner().getAccount().deposit(price);
                    i.getOwner().notify("Your item " + i.getName() + " was bought for " + i.getPrice());
                    items.remove(i);
                    break;
                }
            }
        } catch (RejectedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void wishItem(TraderClient cl, String name, float price) throws RemoteException {
        wished.add(new Item(name, price, cl));
    }
    
    private void checkWish(Item incoming){
        Iterator it = wished.iterator();
        while(it.hasNext()){
            Item wish = (Item)it.next();
            try
            {
                if(incoming.getName().equals(wish.getName()) && incoming.getPrice() <= wish.getPrice()){
                    wish.getOwner().notify("Your wished item " + wish.getName() + " is currently in "
                            + "stock for the price " + wish.getPrice());
                }
            }
            catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class Item {
        
        private String name;
        private float price;
        private TraderClient cl;
        

        public Item(String name, float price, TraderClient cl) {
            this.name = name;
            this.price = price;
            this.cl = cl;
        }
        
        public String getName(){return this.name;}
        public float getPrice(){return this.price;}
        public TraderClient getOwner(){return this.cl;}
        @Override
        public String toString() {
            return String.format("%-30s :: %10.2f wupiupi's", name, price);
        }

    }
}
