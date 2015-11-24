package hw2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.StringTokenizer;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;

public class TraderClient {

    private static final String DEFAULT_MARKET = "MosEisleySpacePort";
    private static final String DEFAULT_BANK = "BankOfHutta";

    private Account account;
    private Market marketobj;
    private Bank bankobj;
    private String traderName;

    public TraderClient() {
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
    }

    public void repl() {
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(traderName + "@" + DEFAULT_MARKET + ">");
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

    static enum BankCommandName {
        newAccount, getAccount, deleteAccount, deposit, withdraw, balance, quit,
        help, list;
    };

    static enum MarketCommandName {
        listItems, sellItem, buyItem, wishItem;
    };

    private Command parse(String userInput) {
        if (userInput == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(userInput);
        if (tokenizer.countTokens() == 0) {
            return null;
        }

        TraderClient.BankCommandName bankCommandName = null;
        TraderClient.MarketCommandName marketCommandName = null;
        
        boolean isBankCommand = true;

        if (tokenizer.hasMoreTokens()) {

            //Get command name
            try {
                String commandNameString = tokenizer.nextToken();
                bankCommandName = TraderClient.BankCommandName.valueOf(TraderClient.BankCommandName.class, commandNameString);
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
            
            //Get command name
            try {
                String commandNameString = tokenizer.nextToken();
                marketCommandName = TraderClient.MarketCommandName.valueOf(TraderClient.MarketCommandName.class, commandNameString);
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
                for (TraderClient.BankCommandName commandName : TraderClient.BankCommandName.values()) {
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
                traderName = userName;
                bankobj.newAccount(userName);
                return;
            case deleteAccount:
                traderName = userName;
                bankobj.deleteAccount(userName);
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
                System.out.println("balance: $" + account.getBalance());
                break;
            default:
                System.out.println("Illegal command");
        }
    }

    void execute(MarketCommand command) throws RemoteException, RejectedException {
        if (command == null) {
            return;
        }

        switch (command.getCommandName()) {
            case listItems:
                System.out.println("Market Items");
                for (String item : marketobj.listItems())
                    System.out.println("¤ " + item);
        }
    }

    //Superclass only for limiting return type
    private class Command {
    };

    private class BankCommand extends Command {

        private String userName;
        private float amount;
        private TraderClient.BankCommandName commandName;

        private String getUserName() {
            return userName;
        }

        private float getAmount() {
            return amount;
        }

        private TraderClient.BankCommandName getCommandName() {
            return commandName;
        }

        private BankCommand(TraderClient.BankCommandName commandName, String userName, float amount) {
            this.commandName = commandName;
            this.userName = userName;
            this.amount = amount;
        }
    }

    private class MarketCommand extends Command {

        private String itemName;
        private Float price;
        private TraderClient.MarketCommandName commandName;

        private String getItemName() {
            return itemName;
        }

        private Float getPrice() {
            return price;
        }

        private TraderClient.MarketCommandName getCommandName() {
            return commandName;
        }

        private MarketCommand(TraderClient.MarketCommandName commandName, String itemName, Float price) {
            this.commandName = commandName;
            this.itemName = itemName;
            this.price = price;
        }
    }

    public static void main(String[] args) {
        new TraderClient().repl();
    }
}
