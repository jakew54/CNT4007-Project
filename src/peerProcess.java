
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Map;

public class peerProcess {
    public static void main(String arg[]) throws FileNotFoundException {
        Hashtable<Integer, Peer> peers = new Hashtable<Integer, Peer>();
        PeerInfo peerInfo = new PeerInfo();
        Common commonInfo = new Common();
        int peerID;

        for (int i = 0; i < peerInfo.getPeers().size(); i++) {
            peers.put(peerInfo.getPeers().get(i).getPeerID(), peerInfo.getPeers().get(i));
        }

        peerID = Integer.parseInt(arg[0]);
        peers.get(peerID).setPeerMap(peers);
        LogManager logger = new LogManager(peers.get(peerID));

        Server server = new Server(peers.get(peerID));
        Thread serverThread = new Thread(server);
        serverThread.start();

        System.out.println("Server for peer #" + peerID + " has started!");


        for (Map.Entry<Integer, Peer> p : peers.entrySet()) {
            if (p.getKey() < peerID) {
                Client client = new Client(peers.get(peerID), p.getValue());
                client.connectPeer();
                System.out.println("Peer #" + p.getKey() + " connected to Peer #" + peerID + " successfully!");
                //log
                logger.createLog(peerID, p.getKey(), "tcpConnectionMade", 0);
            }
        }
        System.out.println("Peer #" + peerID + " host duties fulfilled!");
    }
}
