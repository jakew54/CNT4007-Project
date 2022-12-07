
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class peerProcess {
    public static void main(String arg[]) throws IOException {
        HashMap<Integer, Peer> peers = new HashMap<Integer, Peer>();
        PeerInfo peerInfo = new PeerInfo();
        Common commonInfo = new Common();
        int peerID;
        Peer tempPeer;
        for (int i = 0; i < peerInfo.getPeers().size(); i++) {
            tempPeer = peerInfo.getPeers().get(i);
            tempPeer.createBitField(commonInfo.getBitFieldSize(), commonInfo.getNumOfPieces());
            tempPeer.setNumPreferredNeighbors(commonInfo.getNumOfPreferredNeighbors());
            tempPeer.setUnchokingInterval(commonInfo.getUnchokingInterval());
            tempPeer.setOptimisticUnchokingInterval(commonInfo.getOptimisticChokingInterval());
            tempPeer.setFileName(commonInfo.getFilename());
            tempPeer.setFileSize(commonInfo.getFileSize());
            tempPeer.setPieceSize(commonInfo.getPieceSize());
            //System.out.println("File Present: "+ tempPeer.getFilePresent());
            if (tempPeer.getFilePresent()) {
                //System.out.println("Enter file present IF");
                String filePath = "peer_" + tempPeer.getPeerID() + "/" + commonInfo.getFilename();
                byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
                tempPeer.setFileData(fileBytes, commonInfo.getBitFieldSize());
                //System.out.println(Arrays.toString(fileBytes));
            }

            peers.put(peerInfo.getPeers().get(i).getPeerID(), peerInfo.getPeers().get(i));
        }

        peerID = Integer.parseInt(arg[0]);
        LogManager logger = new LogManager(peers.get(peerID));

        Server server = new Server(peers.get(peerID));
        Thread serverThread = new Thread(server);
        serverThread.start();

        System.out.println("Server for peer #" + peerID + " has started!");


        for (Map.Entry<Integer, Peer> p : peers.entrySet()) {
            if (p.getKey() < peerID) {
                Client client = new Client(peers.get(peerID), p.getValue());
                client.connectPeer();
                //log
                logger.createLog(peerID, p.getKey(), "tcpConnectionMade", 0);
            }
        }
        System.out.println("Peer #" + peerID + " host duties fulfilled!");

        //TODO thread sitch
        //all on thread
        //calculatePreferredNeighbors()
        //LogManager logging = new LogManager(peer);
        //logging.createLog(peer.getPeerID(), connectedPeerID, "changePrefNeighbors", 1); //connectedPeerID is set in MsgManager

    }
}
