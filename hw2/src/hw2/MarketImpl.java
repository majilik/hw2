package hw2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;

@SuppressWarnings("serial")
public class MarketImpl extends UnicastRemoteObject implements Market {

    private List<Item> wished;
    private List<Item> items;
    private String marketName;
    private HashMap<String, String> users;
    private HashMap<String, TraderClient> clients;
    private JDBC database;
    Bank bankobj;

    public MarketImpl(String marketName) throws RemoteException {
        this.marketName = marketName;
        init();
    }

    private void init() {
        this.wished = new ArrayList<>();
        this.clients = new HashMap<>();
        this.database = new JDBC();
        try {
            items = database.itemQuery();
            users = database.userQuery();
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
     * @param buyer The client buying the item.
     * @param name The name of the item.
     * @param price The price of the item.
     * @throws RejectedException
     * @throws RemoteException
     */
    @Override
    public void buyItem(String buyer, String name, float price) throws RejectedException, RemoteException {
        TraderClient cl = clients.get(buyer);
        for (Item i : items) {
            if (i.getName().equals(name) && i.getPrice() == price) {
                Account acct = cl.getAccount();
                if (acct != null)
                {
                    acct.withdraw(price);
                }
                else
                {
                    cl.notify("No account stated!");
                    break;
                }

                TraderClient ownerClient = clients.get(i.getOwner());
                if(ownerClient != null)
                {
                    //Only get money if you're logged in ;-) else Jabba The Hut takes them..
                    ownerClient.getAccount().deposit(price);
                    ownerClient.notify("Your item " + i.getName() + " was bought for " + i.getPrice() + " wupiupi's");
                }
                database.incrementBoughtItems(buyer);
                database.incrementSoldItems(i.getOwner());
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
                    TraderClient cl = clients.get(wish.getOwner());
                    if(cl != null)
                    {
                        cl.notify("Your wished item " + wish.getName() + " is currently in "
                            + "stock for the price " + wish.getPrice() + " wupiupi's");
                    }
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
    public boolean login(TraderClient cl, String owner, String password) throws RemoteException {
        if (users.containsKey(owner)) {
            if (password.equals(users.get(owner))) {
                clients.put(owner, cl);
                return true;
            }
        }
        return false;

    }

    @Override
    public void register(String owner, String password) throws RemoteException {
        database.register(owner, password);

    }

    @Override
    public String viewRecord(String username) throws RemoteException {
        return database.getRecord(username);
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

        /**
         * Gets the list of items persisted in the database.
         *
         * @return List of items
         * @throws SQLException
         */
        public List<Item> itemQuery() throws SQLException {
            final String query = "SELECT name, price, owner FROM Item";
            List<Item> list = new ArrayList<>();
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    String name = rs.getString(1);
                    float price = rs.getInt(2);
                    String owner = rs.getString(3);
                    list.add(new Item(name, price, owner));
                }
                rs.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
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
            HashMap<String, String> map = new HashMap<>();

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String username = rs.getString(1);
                    String password = rs.getString(2);
                    map.put(username, password);
                }
                rs.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }

            return map;
        }

        /**
         * Deletes an item from the database. Should be used to synchronize
         * database with current state.
         *
         * @param item
         */
        public void deleteItem(Item item) {
            final String update = String.format(Locale.US, "DELETE TOP(1) FROM Item WHERE "
                    + "name = '%s' AND price = %f AND owner = '%s'",
                    item.name, item.price, item.owner);
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(update);
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        /**
         * Adds an item to the database. Should be used to synchronize database
         * with current state.
         *
         * @param item
         */
        public void addItem(Item item) {
            final String update = String.format(Locale.US, "INSERT INTO Item (name, price, owner) "
                    + "VALUES ('%s', %f, '%s')", item.name, item.price, item.owner);

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(update);
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        
        public void incrementBoughtItems(String user)
        {   
            int boughtItems = 0;
            final String query = String.format("SELECT boughtItems FROM Activity WHERE username = '%s'", 
                    user);
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    boughtItems = rs.getInt(1);
                }
                rs.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            
            boughtItems++;
            
            String update = String.format("UPDATE Activity SET boughtItems = %d WHERE username = '%s'", boughtItems, user);

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(update);
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        
        public void incrementSoldItems(String user)
        {   
            int soldItems = 0;
            final String query = String.format("SELECT soldItems FROM Activity WHERE username = '%s'", 
                    user);
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    soldItems = rs.getInt(1);
                }
                rs.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            
            soldItems++;
            
            String update = String.format("UPDATE Activity SET soldItems = %d WHERE username = '%s'", 
                    soldItems, user);

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(update);
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        /**
         * Registers a user.
         *
         * @param username
         * @param password
         */
        public void register(String username, String password) {
            String update = String.format("INSERT INTO UserTable (name, password) "
                    + "VALUES ('%s', '%s')", username, password);

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(update);
            } catch (SQLException se) {
                se.printStackTrace();
            }
            
            update = String.format("INSERT INTO Activity (boughtItems, soldItems, username) "
                    + "VALUES (0, 0, '%s')", username);
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(update);
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        private String getRecord(String user) {
            final String query = String.format("SELECT boughtItems, soldItems FROM Activity WHERE username = '%s'", 
                    user);
            int bought = 0;
            int sold = 0;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    bought = rs.getInt(1);
                    sold = rs.getInt(2);
                }
                rs.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            return String.format("[Record]\nUser: %s \nBought: %d \nSold: %d", user, bought, sold);
        }

    }
}
