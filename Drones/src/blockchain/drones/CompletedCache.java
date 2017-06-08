package blockchain.drones;


import java.util.concurrent.ConcurrentHashMap;

class CompletedCache extends ConcurrentHashMap<String, Transaction> {

    private CompletedCache() {
        super();
    }

    private static final CompletedCache completed = new CompletedCache();

    public static void addCompleted(Transaction t) {
        completed.put(t.getPad().getID(), t);
    }

    public static boolean containsCompleted(Transaction t) {
        return t.equals(completed.get(t.getPad().getID()));
    }

    public static boolean removeActive(Transaction t) {
        return completed.remove(t.getPad().getID(), t);
    }
}
