package hw2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;

@SuppressWarnings("serial")
public class MarketImpl extends UnicastRemoteObject implements Market {

    private List<Item> wished;
    private List<Item> items;
    private String marketName;
    private HashMap<String, String> users;
    private HashMap<String, TraderClient> owners;
    private JDBC database;
    Bank bankobj;

    public MarketImpl(String marketName) throws RemoteException {
        this.marketName = marketName;
        init();
    }

    private void init() {
        this.wished = new ArrayList<>();
        this.owners = new HashMap<>();
        this.database = new JDBC();
        try {
            items = database.itemQuery();
        } catch (SQLException ex) {
            Logger.getLogger(MarketImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Lists the items for sale in the market.
     *
     * @return A String array where each element is information about an item on
     * sale.
     * @throws RemoteException
     */
    @Override
    public String[] listItems() throws RemoteException {

        String[] result = new String[items.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = items.get(i).toString();
        }
        return result;
    }

    /**
     * Puts an item for sale on the market.
     *
     * @param owner The client selling the item.
     * @param name The name of the item.
     * @param price The requested price for the item.
     * @throws RemoteException
     */
    @Override
    public void sellItem(String owner, String name, float price) throws RemoteException {
        Item incoming = new Item(name, price, owner);
        database.addItem(incoming);
        items.add(incoming);
        checkWish(incoming);
    }

    /**
     * Buys an item on the market.
     *
     * @param owner The client buying the item.
     * @param name The name of the item.
     * @param price The price of the item.
     * @throws RejectedException
     * @throws RemoteException
     */
    @Override
    public void buyItem(String owner, String name, float price) throws RejectedException, RemoteException {
        TraderClient cl = owners.get(owner);
        for (Item i : items) {
            if (i.getName().equals(name) && i.getPrice() == price) {
                cl.getAccount().withdraw(price);

                TraderClient ownerClient = owners.get(i.getOwner());
                ownerClient.getAccount().deposit(price);
                ownerClient.notify("Your item " + i.getName() + " was bought for " + i.getPrice() + " wupiupi's");
                database.deleteItem(i);
                items.remove(i);
                break;
            }
        }
    }

    /**
     * Puts a wish on an item on the market, the client gets notified whenever
     * the wished item is available on the market for less than or equal to the
     * wished price.
     *
     * @param owner The client wishing the item.
     * @param name The name of the item.
     * @param price The wished price limit of the item.
     * @throws RemoteException
     */
    @Override
    public void wishItem(String owner, String name, float price) throws RemoteException {
        wished.add(new Item(name, price, owner));
    }

    /**
     * Helper function for checking if an incoming item is wished by someone and
     * then handles the wish.
     *
     * @param incoming The item being put on sale on the market.
     */
    private void checkWish(Item incoming) {
        List<Item> removeList = new ArrayList<>();
        Iterator it = wished.iterator();
        while (it.hasNext()) {
            Item wish = (Item) it.next();
            try {
                if (incoming.getName().equals(wish.getName()) && incoming.getPrice() <= wish.getPrice()) {
                    TraderClient cl = owners.get(wish.getOwner());
                    cl.notify("Your wished item " + wish.getName() + " is currently in "
                            + "stock for the price " + wish.getPrice() + " wupiupi's");
                    removeList.add(wish);
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        for (Item wish : removeList) {
            wished.remove(wish);
        }

    }

    @Override
    public boolean login(TraderClient cl, String owner, String password) {
        if (users.containsKey(owner)) {
            if (password.equals(users.get(owner))) {
                owners.put(owner, cl);
                return true;
            }
        }
        return false;

    }

    @Override
    public void register(String owner, String password) {
        database.register(owner, password);

    }

    /**
     * Inner class representing an Item.
     */
    private class Item {

        private String name;
        private float price;
        private String owner;

        public Item(String name, float price, String owner) {
            this.name = name;
            this.price = price;
            this.owner = owner;
        }

        public String getName() {
            return this.name;
        }

        public float getPrice() {
            return this.price;
        }

        public String getOwner() {
            return this.owner;
        }

        @Override
        public String toString() {
            return String.format("%-30s %10.2f wupiupi's", name, price);
        }
    }

    private class JDBC {

        static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        static final String DB_URL = "jdbc:sqlserver://localhost:1433";

        static final String USER = "hw3";
        static final String PASS = "hw3";

        Connection conn = null;

        public JDBC() {
            try {
                init();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        private void init() throws ClassNotFoundException {
            try {
                Class.forName(JDBC_DRIVER);
                System.out.println("Connecting to Mos Eisly trade database...");
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        private ResultSet executeQuery(String query) {
            ResultSet tmp = null;
            try (Statement stmt = conn.createStatement()) {
                tmp = stmt.executeQuery(query);
            } catch (SQLException se) {
                se.printStackTrace();
            }
            return tmp;
        }
        
        private boolean executeUpdate(String update) {
            int updatedRows = 0;
            try (Statement stmt = conn.createStatement()) {
                updatedRows = stmt.executeUpdate(update);
            } catch (SQLException se) {
                se.printStackTrace();
            }
            return updatedRows > 0;
        }

        /**
         * Gets the list of items persisted in the database.
         *
         * @return List of items
         * @throws SQLException
         */
        public List<Item> itemQuery() throws SQLException {
            final String query = "SELECT name, price, owner FROM Item";

            ResultSet rs = executeQuery(query);
            List<Item> list = new ArrayList<>();

            while (rs.next()) {
                String name = rs.getString(1);
                float price = rs.getInt(2);
                String owner = rs.getString(3);
                list.add(new Item(name, price, owner));
            }
            rs.close();
            return list;
        }

        /**
         * Gets the users persisted in the database
         *
         * @return HashMap of username password pairs
         * @throws SQLException
         */
        public HashMap<String, String> userQuery() throws SQLException {
            final String query = "SELECT name, password FROM UserTable";
            
            ResultSet rs = executeQuery(query);
            HashMap<String, String> map = new HashMap<>();

            while (rs.next()) {
                String username = rs.getString(1);
                String password = rs.getString(2);
                map.put(username, password);
            }
            rs.close();
            return map;
        }

        /**
         * Deletes an item from the database. Should be used to synchronize
         * database with current state.
         *
         * @param item
         */
        public void deleteItem(Item item) {
            final String update = String.format("DELETE TOP 1 FROM Item WHERE "
                    + "name = %s AND price = %f AND owner = %s",
                    item.name, item.price, item.owner);

            executeUpdate(update);
        }

        /**
         * Adds an item to the database. Should be used to synchronize database
         * with current state.
         *
         * @param item
         */
        public void addItem(Item item) {
            final String update = String.format("INSERT INTO Item (name, price, owner) "
                    + "VALUES (%s, %f, %s)", item.name, item.price, item.owner);
            
            executeUpdate(update);
        }

        /**
         * Registers a user.
         * @param username
         * @param password
         */
        public void register(String username, String password) {
            final String update = String.format("INSERT INTO UserTable (name, password) "
                    + "VALUES (%s, %s)", username, password);
            
            executeUpdate(update);
        }

    }
}
