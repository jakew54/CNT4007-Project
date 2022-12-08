import java.util.BitSet;
import java.util.Map;

public class PreferredNeighborsCalculator extends Thread{

    private Peer peer;
    private LogManager logManager;

    public PreferredNeighborsCalculator(Peer peer, LogManager logger) {
        this.peer = peer;
        this.logManager = logger;
    }

    public boolean allPeersDoNotHaveFile() {
        if (peer.getNeighborBitFields2().isEmpty()) {
            return true;
        }
        if (!peer.checkIfBitField2IsFull(peer.getBitField2())) {
            return true;
        }
        for (Map.Entry<Integer, BitSet> p : peer.getNeighborBitFields2().entrySet()) {
            if (!peer.checkIfBitField2IsFull(p.getValue())) {
                return true;
            }
        }
        return false;
    }

    public void run() {
        boolean isFirstRun = true;
        while (allPeersDoNotHaveFile()) {
            if (!isFirstRun) {
                try {
                    Thread.sleep(peer.getUnchokingInterval() * 1000);
                } catch (InterruptedException e) {
                    System.err.println("PrefNeighborsCalculator Sleep error");
                    e.printStackTrace();
                }
            }
            peer.calculatePreferredNeighbors();
            logManager.createLog(peer.getPeerID(), 0, "changePrefNeighbors", -1);
            isFirstRun = false;
        }
    }
}
