import java.util.*;

public class BankDatabase {
    HashMap<String, Accounts> accountsMap;
    HashMap<String, Transaction> transactionMap;
    LinkedList<QueueToken> bankQueue;
    TreeSet<Accounts> sortedAccounts;
    TreeMap<String, ArrayList<Loan>> loanMap;
Stack<String>stackOperation;

}
