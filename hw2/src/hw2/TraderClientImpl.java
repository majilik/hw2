package hw2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.StringTokenizer;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;

@SuppressWarnings("serial")
public class TraderClientImpl extends UnicastRemoteObject implements TraderClient{

    private static final String DEFAULT_MARKET = "MosEisleySpacePort";
    private static final String DEFAULT_BANK = "BankOfHutta";

    private Account account;
    private Market marketobj;
    private Bank bankobj;
    private String traderName;
    private String password;
    private BufferedReader consoleIn;

    
    // set up the registry and create RMI of band and marketplace.
    public TraderClientImpl() throws RemoteException {
       
        
        try {
            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }
            marketobj = (Market) Naming.lookup(DEFAULT_MARKET);
            bankobj = (Bank) Naming.lookup(DEFAULT_BANK);
        } catch (Exception e) {
            System.out.println("The runtime failed: " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Connected to market: " + DEFAULT_MARKET);
        System.out.println("Connected to bank: " + DEFAULT_BANK);
        consoleIn = new BufferedReader(new InputStreamReader(System.in));
    }

    // main loop
    public void repl() {
        while (true) {
            try {
                String userInput = consoleIn.readLine();
                Command cmd = parse(userInput);
                if (cmd instanceof BankCommand) {
                    execute((BankCommand) cmd);
                } else if (cmd instanceof MarketCommand) {
                    execute((MarketCommand) cmd);
                } else {
                    System.out.println("Illegal command type");
                }
            } catch (RejectedException re) {
                System.out.println(re);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Account getAccount() throws RemoteException {
        return account;
    }

    @Override
    //Used as a callback
    public void notify(String message) throws RemoteException {
        System.out.println("Notification: " + message);
    }

    private void initAccount(){
        try{
           account = bankobj.getAccount(traderName);
           if(account == null)
               account = bankobj.newAccount(traderName);
        }
        catch(RejectedException rje)
        {
            rje.printStackTrace();
        }
        catch(RemoteException rme)
        {
            rme.printStackTrace();
        }
        
    }

    // command enums
    
    static enum BankCommandName {
        newAccount, getAccount, deleteAccount, deposit, withdraw, balance, quit,
        help, list;
    };

    static enum MarketCommandName {
        listItems, sellItem, buyItem, wishItem, viewRecord;
    };

    // parses the command, 
    private Command parse(String userInput) {
        if (userInput == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(userInput);
        if (tokenizer.countTokens() == 0) {
            return null;
        }

        TraderClientImpl.BankCommandName bankCommandName = null;
        TraderClientImpl.MarketCommandName marketCommandName = null;
        
        // Test if the command is a bank command
        
        boolean isBankCommand = true;

        if (tokenizer.hasMoreTokens()) {
            String commandToken = tokenizer.nextToken();

            //Get command name
            try {
                String commandNameString = commandToken;
                bankCommandName = TraderClientImpl.BankCommandName.valueOf(TraderClientImpl.BankCommandName.class, commandNameString);
            } catch (IllegalArgumentException commandDoesNotExist) {
                isBankCommand = false;
            }
            
            if (isBankCommand)
            {
                String userName = null;
                float amount = 0;
                
                if (tokenizer.hasMoreTokens())
                {
                    userName = tokenizer.nextToken();
                    
                    if (tokenizer.hasMoreTokens())
                    {
                        try {
                            amount = Float.parseFloat(tokenizer.nextToken());
                        } catch (NumberFormatException e) {
                            System.out.println("Illegal amount");
                            return null;
                        }
                    }
                }
                return new BankCommand(bankCommandName, userName, amount);
            }
            
            // If not, try if it is a market command
            
            //Get command name
            try {
                String commandNameString = commandToken;
                marketCommandName = TraderClientImpl.MarketCommandName.valueOf(TraderClientImpl.MarketCommandName.class, commandNameString);
            } catch (IllegalArgumentException commandDoesNotExist) {
                System.out.println("Illegal command");
                return null;
            }
            
            String itemName = null;
            Float price = null;

            if (tokenizer.hasMoreTokens())
            {
                itemName = tokenizer.nextToken();

                if (tokenizer.hasMoreTokens())
                {
                    try {
                        price = Float.parseFloat(tokenizer.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Illegal amount");
                        return null;
                    }
                }
            }
            return new MarketCommand(marketCommandName, itemName, price);
        }
        return null;
    }

    // executes the parsed Bank command
    
    void execute(BankCommand command) throws RemoteException, RejectedException {
        if (command == null) {
            return;
        }

        switch (command.getCommandName()) {
            case list:
                try {
                    for (String accountHolder : bankobj.listAccounts()) {
                        System.out.println(accountHolder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                return;
            case quit:
                System.exit(0);
            case help:
                for (TraderClientImpl.BankCommandName commandName : TraderClientImpl.BankCommandName.values()) {
                    System.out.println(commandName);
                }
                return;
        }

        // all further commands require a name to be specified
        String userName = command.getUserName();
        if (userName == null) {
            userName = traderName;
        }

        if (userName == null) {
            System.out.println("name is not specified");
            return;
        }

        switch (command.getCommandName()) {
            case newAccount:
                account = bankobj.newAccount(userName);
                return;
            case deleteAccount:
                if(bankobj.deleteAccount(userName))
                {
                    account = null;
                    traderName = null;
                }
                return;
        }

        // all further commands require a Account reference
        Account acc = bankobj.getAccount(userName);
        if (acc == null) {
            System.out.println("No account for " + userName);
            return;
        } else {
            account = acc;
            traderName = userName;
        }

        switch (command.getCommandName()) {
            case getAccount:
                System.out.println(account);
                break;
            case deposit:
                account.deposit(command.getAmount());
                break;
            case withdraw:
                account.withdraw(command.getAmount());
                break;
            case balance:
                System.out.println("balance: " + account.getBalance() + " wupiupi's");
                break;
            default:
                System.out.println("Illegal command");
        }
    }
    
    
    // executes the parsed Market command
    void execute(MarketCommand command) throws RemoteException, RejectedException {
        if (command == null) {
            return;
        }
        String itemName = command.getItemName();
        Float price = command.getPrice();

        switch (command.getCommandName()) {
            case viewRecord:
                System.out.println(marketobj.viewRecord(traderName));
                return;
            case listItems:
                String[] listItems = marketobj.listItems();
                if (listItems.length == 0)
                {
                    System.out.println("The Market is empty");
                }
                else
                {
                    System.out.printf("%-30s %10s %s%n", "[Item]", " ", "[Price]");
                    for (String item : listItems)
                        System.out.println("¤ " + item);
                }
                return;
            case buyItem:
                if (account != null)
                {
                    if (itemName != null)
                    {
                        if (price != null)
                        {
                            marketobj.buyItem(traderName, itemName, price);
                        }
                        else
                            System.out.println("Price not specified");
                    }
                    else
                        System.out.println("Item not specified");
                }
                else
                    System.out.println("Account not specified");
                return;
            case wishItem:
                if (account != null)
                {
                    if (itemName != null)
                    {
                        if (price != null)
                        {
                            marketobj.wishItem(traderName, itemName, price);
                        }
                        else
                            System.out.println("Price not specified");
                    }
                    else
                        System.out.println("Item not specified");
                }
                else
                    System.out.println("Account not specified");
                return;
            case sellItem:
                if (account != null)
                {
                    if (itemName != null)
                    {
                        if (price != null)
                        {
                            marketobj.sellItem(traderName, itemName, price);
                        }
                        else
                            System.out.println("Price not specified");
                    }
                    else
                        System.out.println("Item not specified");
                }
                else
                    System.out.println("Account not specified");
                return;
            default:
                System.out.println("Illegal command");
        }
    }
    
    public void login() throws RemoteException{
        loginCredentials(false);
        while(!(marketobj.login(this, traderName, password)))
        {
            System.out.println("Login failed, please try again: ");
            loginCredentials(false);
        }
        System.out.println("Login successful");
    }
    
    public void register() throws RemoteException{
        loginCredentials(true);
        marketobj.register(traderName, password);
        if(!marketobj.login(this, traderName, password))
            System.out.println("These are not the droids you're looking for.");
    }
    
    public void loginCredentials(boolean register){
        try
        {
            traderName = consoleIn.readLine();
            password = consoleIn.readLine();
            while(password.length() < 8 && register)
            {
                System.out.println("Password too short (8 characters minimum).");
                password = consoleIn.readLine();
            }
        }
        catch (IOException ie)
        {
            ie.printStackTrace();
        }
    }
    
    public String getTraderName(){
        return traderName;
    }

    //Superclass only for limiting return type
    private class Command {
    };
    
    
    // inner classes used for the different commands.  
    
    private class BankCommand extends Command {

        private String userName;
        private float amount;
        private TraderClientImpl.BankCommandName commandName;

        private String getUserName() {
            return userName;
        }

        private float getAmount() {
            return amount;
        }

        private TraderClientImpl.BankCommandName getCommandName() {
            return commandName;
        }

        private BankCommand(TraderClientImpl.BankCommandName commandName, String userName, float amount) {
            this.commandName = commandName;
            this.userName = userName;
            this.amount = amount;
        }
    }

    private class MarketCommand extends Command {

        private String itemName;
        private Float price;
        private TraderClientImpl.MarketCommandName commandName;

        private String getItemName() {
            return itemName;
        }

        private Float getPrice() {
            return price;
        }

        private TraderClientImpl.MarketCommandName getCommandName() {
            return commandName;
        }

        private MarketCommand(TraderClientImpl.MarketCommandName commandName, String itemName, Float price) {
            this.commandName = commandName;
            this.itemName = itemName;
            this.price = price;
        }
    }

    public static void main(String[] args) {
        
        try {
            TraderClient client = new TraderClientImpl();
            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }
            
            System.out.println("Press 1 for login, 2 for register or other "
                    + "for selling death sticks.");
            Scanner in = new Scanner(System.in);
            switch (in.nextInt()) {
                case 1:
                    ((TraderClientImpl)client).login();
                    break;
                case 2:
                    ((TraderClientImpl)client).register();
                    break;
                default:
                    System.out.println("You don't wanna sell me death sticks.");
                    return;
            }
            
            String name = ((TraderClientImpl)client).getTraderName();
            Naming.rebind(name, client);
            ((TraderClientImpl)client).initAccount();
            System.out.println(name + " is ready.");
            ((TraderClientImpl) client).repl();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
