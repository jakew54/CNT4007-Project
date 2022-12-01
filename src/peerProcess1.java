import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

import static java.lang.Integer.parseInt;

public class peerProcess1 {
      private static Vector<Peer> peers;
      private static int currentPeerID;
      private static PeerInfo peerInfoObj;
      private static Common commonObj;
      private static boolean isAllPeersDownloaded;
      private static ObjectOutputStream out; //stream write to socket
      private static ObjectInputStream in; //stream from from socket

      //in functions with clientID and hostID -> clientID refers to the peer MAKING the connection
      // and host ID refers to the peer BEING CONNECTED TO

    public static void createLog(int clientID, int hostID, String logType, int filler) {
        String fileNameClient = "log_peer_" + Integer.toString(clientID) + ".log";
        String fileNameHost = "log_peer_" + Integer.toString(hostID) + ".log";
        String clientLog = "";
        String hostLog = "";

        //calculating timestamp in EST time
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        int hour = Integer.parseInt(timeStamp.substring(11,13));
        hour = hour - 4;
        if (hour < 0) {
            hour = 24 + hour;
        }
        timeStamp = timeStamp.substring(0,11) + Integer.toString(hour) + timeStamp.substring(13,timeStamp.length());

        //switch case to build log based on which kind of log it should be (based on logType parameter)
        switch (logType) {
            case "tcpConnectionMade":
                //this means that the connection has been made between the peers
                //building client log
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " makes a connection to Peer ";
                clientLog += Integer.toString(hostID);
                clientLog += ".";
                //building host log
                hostLog += timeStamp;
                hostLog += ": Peer ";
                hostLog += Integer.toString(hostID);
                hostLog += " is connected from Peer ";
                hostLog += Integer.toString(clientID);
                hostLog += ".";
                break;
            case "changePrefNeighbors":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " has the preferred neighbors ";
                String tempNeighbors = "";
                Vector<Integer> tempNeighborsVec = peerInfoObj.getPeerWithID(clientID).getPreferredNeighbors();
                for (int i = 0; i < tempNeighborsVec.size(); i++) {
                    tempNeighbors += Integer.toString(tempNeighborsVec.get(i));
                    if (i < tempNeighborsVec.size() - 1) {
                        tempNeighbors += ",";
                    }
                }
                clientLog += tempNeighbors;
                clientLog += ".";
                break;
            case "changeOptUnchokeNeighbor":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " has the optimistically unchoked neighbor ";
                clientLog += Integer.toString(peerInfoObj.getPeerWithID(clientID).getOptimisticUnchokedNeighborID());
                clientLog += ".";
                break;
            case "unchoking":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " is unchoked by ";
                clientLog += Integer.toString(hostID);
                clientLog += ".";
                break;
            case "choking":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " is choked by ";
                clientLog += Integer.toString(hostID);
                clientLog += ".";
                break;
            case "receiveHave":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " received the 'have' message from ";
                clientLog += Integer.toString(hostID);
                clientLog += " for the piece ";
                clientLog += Integer.toString(filler);
                clientLog += ".";
                break;
            case "receiveInterested":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " received the 'interested' message from ";
                clientLog += Integer.toString(hostID);
                clientLog += ".";
                break;
            case "downloadingPiece":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " has downloaded the piece ";
                clientLog += Integer.toString(filler);
                clientLog += " from ";
                clientLog += Integer.toString(hostID);
                clientLog += ". Now the number of pieces it has is ";
                clientLog += Integer.toString(peerInfoObj.getPeerWithID(clientID).getNumPiecesDownloaded());
                clientLog += ".";
                break;
            case "downloadComplete":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " has downloaded the complete file.";
                break;
            default:
                break;
        }

        //Writing clientLog to correct files
        if (clientLog.length() > 0) {
            try {
                FileWriter clientWriter = new FileWriter(fileNameClient);
                clientWriter.write(clientLog);
            } catch (IOException e) {
                System.out.println("An error has occurred while creating the client log writer.");
                e.printStackTrace();
            }
        }
        //Writing hostLog to correct files
        if (hostLog.length() > 0) {
            try {
                FileWriter hostWriter = new FileWriter(fileNameHost);
                hostWriter.write(hostLog);
            } catch (IOException e) {
                System.out.println("An error has occurred while creating the host log writer.");
                e.printStackTrace();
            }
        }

    }

//    public static void createSocket(int clientID, int hostID, int peerPortNum) {
//        Peer client = peerInfoObj.getPeerWithID(clientID);
//        Peer host = peerInfoObj.getPeerWithID(hostID);
//        try {
//            requestSocket = new Socket(client.getPeerIP(), peerPortNum);
//            //initialize inputStream and outputStream
//            out = new ObjectOutputStream(requestSocket.getOutputStream());
//            out.flush();
//            in = new ObjectInputStream(requestSocket.getInputStream());
//
//            //get Input from standard input
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
//
//
//            ServerSocket listener = new ServerSocket(peerPortNum);
//            hostSocket = listener.accept();
//            new Handler(hostSocket, 1);
//            //initialize Input and Output streams
//            out = new ObjectOutputStream(hostSocket.getOutputStream());
//            out.flush();
//            in = new ObjectInputStream(hostSocket.getInputStream());
//
//            //client connection intialization
//            client.updateSocketsWithID(hostID, requestSocket);
//            client.setIsHandshakedListWithID(hostID, false);
//            client.setIsChokedListWithID(hostID, false);
//            //host connection initialization
//            host.updateSocketsWithID(clientID, hostSocket);
//            host.setIsChokedListWithID(clientID, false);
//
//        } catch (IOException ioException) {
//            ioException.printStackTrace();
//        }
//    }

    public static void createHandler(int clientID, int hostID) {
        Peer client = peerInfoObj.getPeerWithID(clientID);
        Peer host = peerInfoObj.getPeerWithID(hostID);
        try {
            Socket requestSocket1 = new Socket(host.getPeerIP(),host.getPeerPortNum());
            Socket socket1 = host.getMyServerSocket().accept();
            Socket requestSocket2 = new Socket(client.getPeerIP(),client.getPeerPortNum());
            Socket socket2 = client.getMyServerSocket().accept();
            Handler clientHandler = new Handler(socket1, hostID, clientID);
            client.setHandlers(hostID, clientHandler);
            Handler hostHandler = new Handler(socket2, clientID,hostID);
            host.setHandlers(clientID, hostHandler);
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
    }

    public static void sendMessages() {
        //handshake message
        String handshakeHeader = "P2PFILESHARINGPROJ";
        byte[] handshakeZeroBits = new byte[10];
        Arrays.fill(handshakeZeroBits, (byte) 0);
        Peer currentPeer = peerInfoObj.getPeerWithID(currentPeerID);
        Map<Integer,Handler> sockets = currentPeer.getHandlers();
        if (sockets.size() > 0) {
            //loop through every socket
            for (int i = sockets.size(); i >= 0; i--) {
                //booleans for if handshake message has been sent or not
                if (true) { //if socket has not been handshaked
                    //send handshake
                    //createLog(clientID,hostID,"tcpConnectionMade",0);
                    //sends message with message type 5: bitfield
                } else { //if socked has been handshaked
                    //check if inputStream has message in it
                        //if inputStream has message
                            //read message
                            //send whatever response message should be sent
                            //create log based on message type sent
                        //else if inputStream has no message
                            //idk rip in peace
                }
            }
        }
        //if unchokingInterval has passed or if peer.preferredNeighbors is empty
            //peer must determine which peers are its preferred neighbors
        //if optimisticUnchokingInterval has passed or if peer.OptimisticNeighbor is empty
            //peer must then determine its optimistic unchoked neighbor
    }

    public static void peerProcess(int peerID) { //this is called somewhere with each peerID i guess
        //initialize peer with PeerInfo and Common properties
        peerInfoObj = new PeerInfo();
        Peer currentPeer = peerInfoObj.getPeerWithID(peerID);
        peers = peerInfoObj.getPeers();
        currentPeerID = peerID;
        commonObj = new Common();
        int currentPeerConnectionID;
        if (peerID == 1000){
            currentPeerConnectionID = 1000;
        }
        else {
            currentPeerConnectionID = peerID - 1;
        }

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
            createHandler(currentPeerID, currentPeerConnectionID);
            //get client handler connected to current peer connection ID and run it
            peerInfoObj.getPeerWithID(currentPeerID).getHandlers().get(currentPeerConnectionID).start();
            //get host handler (currentPeerConnectionID) connected to the current peer and run it
            peerInfoObj.getPeerWithID(currentPeerConnectionID).getHandlers().get(currentPeerConnectionID).start();
        }
        //send messages in while loop -> condition is while (there is still at least one peer without a full file)
        //exchange messages/pieces with connected peers
        boolean temp = true;
        while (!isAllPeersDownloaded) {//(there is still at least one peer without a full file))
            sendMessages();
            temp=true;
            for (int i = 0; i < peers.size(); i++) {
                if (!peers.get(i).getFilePresent()) {
                    temp = false;
                }
            }
            isAllPeersDownloaded = temp;
        }
        //terminate peer process once ALL peers have complete file
        //terminate all connections (sockets)
        //terminate all processes
        //no runaway processes (does that mean just have every peerProcess hit return so it stops the function?)
        return;
    }

    public static void main(String[] args) {
        //idk what's going on here




        createHandler(1001,1002);

    }
}
