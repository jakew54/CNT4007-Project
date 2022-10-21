import java.net.*;
import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;

public class peerProcess {
      private static Vector<Peer> peers;
      private static int currentPeerID;
      private static Socket requestSocket;
      private static Socket hostSocket;
      private static PeerInfo peerInfoObj;
      private static Common commonObj;

      //in functions with clientID and hostID -> clientID refers to the peer MAKING the connection
      // and host ID refers to the peer BEING CONNECTED TO

    public static void createLog(int clientID, int hostID, String logType) {
        String fileNameClient = "log_peer_" + Integer.toString(clientID) + ".log";
        String fileNameHost = "log_peer_" + Integer.toString(hostID) + ".log";
        String clientLog = "";
        String hostLog = "";
        switch (logType) {
            case "tcpConnectionMade":
                //this means that the connection has been made between the peers
            case "changePrefNeighbors":
        }
    }

    public static void createSocket(int clientID, int hostID, int peerPortNum, Peer currentPeer) {
        try {
            requestSocket = new Socket(currentPeer.getPeerIP(), peerPortNum);
            ServerSocket listener = new ServerSocket(peerPortNum);
            hostSocket = listener.accept();
            peerInfoObj.getPeerWithID(clientID).updateSockets(requestSocket);
            peerInfoObj.getPeerWithID(hostID).updateSockets(hostSocket);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void sendMessages() {
        //handshake message
        String handshakeHeader = "P2PFILESHARINGPROJ";
        byte[] handshakeZeroBits = new byte[10];
        Arrays.fill(handshakeZeroBits, (byte) 0);
        if (peerInfoObj.getPeerWithID(currentPeerID).getSockets().size() > 0) {
            //send handshake
            //send bitfield message
            //
        }
        //peer must determine which peers are its preferred neighbors
        //peer must then determine its optimistic unchoked neighbor -
        //peer must update its preffered neighbros every cycle of the unchokingInterval
    }

    public static void peerProcess(int peerID) { //this is called somewhere with each peerID i guess
        //initialize peer with PeerInfo and Common properties
        peerInfoObj = new PeerInfo();
        Peer currentPeer = peerInfoObj.getPeerWithID(peerID);
        commonObj = new Common();
        int currentPeerConnectionID = peerID - 1;
        Vector<Socket> sockets = new Vector<>();

        if (currentPeer.getFilePresent()) { //has file
            byte[] bitField = new byte[commonObj.getNumOfPieces()];
            Arrays.fill(bitField, (byte) 1);
            currentPeer.setBitField(bitField);
        } else { //does not have file
            byte[] bitField = new byte[commonObj.getNumOfPieces()];
            Arrays.fill(bitField, (byte) 0);
            currentPeer.setBitField(bitField);
        }
        //make connections to all peers that have started before it
        while (currentPeerConnectionID != 1000) {
            //make socket / connection with peer
            createSocket(currentPeerID, currentPeerConnectionID, currentPeer.getPeerPortNum(), currentPeer);
        }
        sendMessages();
        //exchange messages/pieces with connected peers
        //terminate peer process once ALL peers have complete file
        return;
    }

    public static void main(String[] args) {
        //idk what's going on here
        PeerInfo peerInfoObj = new PeerInfo();
        peers = peerInfoObj.getPeers();
        currentPeerID = peers.get(0).getPeerID();
        for (int i = 0; i < peers.size(); i++) {
            peerProcess(currentPeerID);
        }
    }
}
