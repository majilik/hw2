package hw2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;

@SuppressWarnings("serial")
public class MarketImpl extends UnicastRemoteObject implements Market {

    private List<Item> items;
    private List<Item> wished;
    private String marketName;
    Bank bankobj;

    public MarketImpl(String marketName) throws RemoteException {
        this.marketName = marketName;
        this.items = new ArrayList<>();
        this.wished = new ArrayList<Item>();
    }

    /**
     * Lists the items for sale in the market.
     * @return A String array where each element is information about an item
     * on sale.
     * @throws RemoteException 
     */
    @Override
    public String[] listItems() throws RemoteException {
        String[] result = new String[items.size()];
        for(int i = 0; i < result.length; i++)
        {
            result[i] = items.get(i).toString();
        }
        return result;
    }
    
    /**
     * Puts an item for sale on the market.
     * @param cl The client selling the item.
     * @param name The name of the item.
     * @param price The requested price for the item.
     * @throws RemoteException 
     */
    @Override
    public void sellItem(TraderClient cl, String name, float price) throws RemoteException {
        Item incoming = new Item(name, price, cl);
        items.add(incoming);
        checkWish(incoming);
    }
    
    /**
     * Buys an item on the market.
     * @param cl The client buying the item.
     * @param name The name of the item.
     * @param price The price of the item.
     * @throws RejectedException
     * @throws RemoteException 
     */
    @Override
    public void buyItem(TraderClient cl,String name, float price) throws RejectedException, RemoteException {
        for(Item i : items){
            if(i.getName().equals(name) && i.getPrice() == price){
                cl.getAccount().withdraw(price);

                i.getOwner().getAccount().deposit(price);
                i.getOwner().notify("Your item " + i.getName() + " was bought for " + i.getPrice() + " wupiupi's");
                items.remove(i);
                break;
            }
        }
    }

    /**
     * Puts a wish on an item on the market, the client gets notified
     * whenever the wished item is available on the market for less than
     * or equal to the wished price.
     * @param cl The client wishing the item.
     * @param name The name of the item.
     * @param price The wished price limit of the item.
     * @throws RemoteException 
     */
    @Override
    public void wishItem(TraderClient cl, String name, float price) throws RemoteException {
        wished.add(new Item(name, price, cl));
    }
    
    /**
     * Helper function for checking if an incoming item is wished by someone
     * and then handles the wish.
     * @param incoming The item being put on sale on the market.
     */
    private void checkWish(Item incoming){
        Iterator it = wished.iterator();
        while(it.hasNext()){
            Item wish = (Item)it.next();
            try
            {
                if(incoming.getName().equals(wish.getName()) && incoming.getPrice() <= wish.getPrice()){
                    wish.getOwner().notify("Your wished item " + wish.getName() + " is currently in "
                            + "stock for the price " + wish.getPrice() + " wupiupi's");
                    wished.remove(wish);
                }
            }
            catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Inner class representing an Item.
     */
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
            return String.format("%-30s %10.2f wupiupi's", name, price);
        }
    }
}