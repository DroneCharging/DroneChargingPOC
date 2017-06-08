package blockchain.drones;

import java.util.concurrent.ConcurrentHashMap;

class Cache extends ConcurrentHashMap<String, Transaction> {

    private Cache() {
        super();
    }

    private static final Cache activeTransactions = new Cache();

    //private static final HashMap<String, Transaction> completedTransactions = new HashMap<>();

    public static void addActive(Transaction t) {
        activeTransactions.put(t.getPad().getID(), t);
    }

    public static boolean containsActive(Transaction t) {
        return t.equals(activeTransactions.get(t.getPad().getID()));
    }

    public static boolean removeActive(Transaction t)  {
        if (activeTransactions.remove(t.getPad().getID(), t)) {
            CompletedCache.addCompleted(t);
            return true;
        }
        return false;
    }

    public static boolean containsActive(String padKey) {
        return activeTransactions.containsKey(padKey);
    }

    public static Transaction getTransaction(String padKey) {
        return activeTransactions.get(padKey);
    }
}
