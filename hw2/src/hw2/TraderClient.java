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
    
    public void repl()
    {
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(traderName + "@" + DEFAULT_MARKET + ">");
            try {
                String userInput = consoleIn.readLine();
                execute(parse(userInput));
            } catch (RejectedException re) {
                System.out.println(re);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    static enum CommandName {
        newAccount, getAccount, deleteAccount, deposit, withdraw, balance, quit,
        help, list, listItems, sellItem, buyItem, wishItem;
    };
    
    private Command parse(String userInput) {
        if (userInput == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(userInput);
        if (tokenizer.countTokens() == 0) {
            return null;
        }

        TraderClient.CommandName commandName = null;
        String userName = null;
        float amount = 0;
        int userInputTokenNo = 1;

        while (tokenizer.hasMoreTokens()) {
            switch (userInputTokenNo) {
                case 1:
                    try {
                        String commandNameString = tokenizer.nextToken();
                        commandName = TraderClient.CommandName.valueOf(TraderClient.CommandName.class, commandNameString);
                    } catch (IllegalArgumentException commandDoesNotExist) {
                        System.out.println("Illegal command");
                        return null;
                    }
                    break;
                case 2:
                    userName = tokenizer.nextToken();
                    break;
                case 3:
                    try {
                        amount = Float.parseFloat(tokenizer.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Illegal amount");
                        return null;
                    }
                    break;
                default:
                    System.out.println("Illegal command");
                    return null;
            }
            userInputTokenNo++;
        }
        return new Command(commandName, userName, amount);
    }

    void execute(Command command) throws RemoteException, RejectedException {
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
                for (TraderClient.CommandName commandName : TraderClient.CommandName.values()) {
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

    private class Command {
        private String userName;
        private float amount;
        private TraderClient.CommandName commandName;

        private String getUserName() {
            return userName;
        }

        private float getAmount() {
            return amount;
        }

        private TraderClient.CommandName getCommandName() {
            return commandName;
        }

        private Command(TraderClient.CommandName commandName, String userName, float amount) {
            this.commandName = commandName;
            this.userName = userName;
            this.amount = amount;
        }
    }

    
    public static void main(String[] args) {
        new TraderClient().repl();
    }
}
