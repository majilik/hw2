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
                    
                    i.getOwner().getAccount().desposit(price);
                    items.remove(i);
                    break;
                }
            }
        } catch (RejectedException ex) {
            Logger.getLogger(MarketImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void wishItem(TraderClient cl, String name, float price) throws RemoteException {
        wished.add(new Item(name, price, cl.getAccount()));
    }
    
    private void checkWish(Item incoming){
        Iterator it = wished.iterator();
        while(it.hasNext()){
            Item wish = (Item)it.next();
            
            if(incoming.name.equals(wish.name) && incoming.price <= wish.price){
                incoming.getOwner().notify("Your wished item " + wish.name + " is currently in "
                        + "stock for the price " + wish.price);
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
