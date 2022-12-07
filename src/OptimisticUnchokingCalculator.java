import java.util.Map;

public class OptimisticUnchokingCalculator extends Thread{

    private Peer peer;
    private LogManager logManager;

    public OptimisticUnchokingCalculator(Peer peer) {
        this.peer = peer;
        this.logManager = new LogManager(peer);
    }

    public boolean allPeersDoNotHaveFile() {

        if (peer.getNeighborBitfields().isEmpty()){
            return true;
        }

        if (peer.getFilePresent()) {
            for (Map.Entry<Integer, byte[]> p : peer.getNeighborBitfields().entrySet()) {
                for (int i = 0; i < p.getValue().length; i++) {
                    for (int j = 0; j < 8; j++) {
                        if ((p.getValue()[i] >> j & 1) == 0) {
                            return true;
                        }
                    }
                }
            }
        }
        else{
            return true;
        }
        return false;
    }

    public void run() {
        boolean isFirstRun = true;
        while (allPeersDoNotHaveFile()) {
            if (!isFirstRun) {
                try {
                    Thread.sleep(peer.getOptimisticUnchokingInterval() * 1000);
                } catch (InterruptedException e) {
                    System.err.println("PrefNeighborsCalculator Sleep error");
                    e.printStackTrace();
                }
            }
            peer.calculateOptimisticallyUnchokedNeighbor();
            logManager.createLog(peer.getPeerID(), -1, "changeOptUnchokeNeighbor", -1);
        }
    }
}
