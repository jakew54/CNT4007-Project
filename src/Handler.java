import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

public class Handler extends Thread {

    private String message;    //message received from the client
    private String MESSAGE;    //uppercase message send to the client
    private Socket connection;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private boolean isHandshaked;
    private int peerID;		//peerID of peer
    private int myID;
    private static PeerInfo peerInfoObj;

    public Handler(Socket connection, int peerID, int myID) {
        this.connection = connection;
        this.peerID = peerID;
        this.isHandshaked = false;
        this.myID = myID;
        peerInfoObj = new PeerInfo();
    }

    public void run() {
        try{
            //initialize Input and Output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());
            if (in.readObject() == null) {
                try {
                    //handshake message
                    String handshakeHeader = "P2PFILESHARINGPROJ";
                    byte[] handshakeZeroBits = new byte[10];
                    Arrays.fill(handshakeZeroBits, (byte) 0);
                    handshakeHeader.concat(String.valueOf(handshakeZeroBits)); //HEY RIGHT HERE ZERO BITS
                    handshakeHeader.concat(Integer.toString(peerID));
                    out.writeObject(handshakeHeader);
                    out.flush();

                } catch(IOException ioException){
                    ioException.printStackTrace();
                }
            } else {
                //read handshake message and return it
                message = (String) in.readObject();
                String handshakeHeader = message.substring(0,18);
                if (!(handshakeHeader.equals("P2PFILESHARINGPROJ"))) {
                    in.close();
                    out.close();
                    connection.close();
                    System.out.println("HANDSHAKE WAS NOT ACCEPTED IN " + myID);
                    return;
                }
                String peerIDinHandshake = message.substring(27);
                if (Integer.parseInt(peerIDinHandshake) != myID) {
                    in.close();
                    out.close();
                    connection.close();
                    System.out.println("HANDSHAKE WASN'T ACCEPTED IN " + myID);
                    return;
                }
            }

            try{
                while(true)
                {
                    //receive the message sent from the client
                    message = (String)in.readObject();
                    //show the message to the user
                    System.out.println("Receive message: " + message + " from client " + peerID);
                    //Capitalize all letters in the message
                    MESSAGE = message.toUpperCase();
                    //send MESSAGE back to the client
                    sendMessage(MESSAGE);
                }
            }
            catch(ClassNotFoundException classnot){
                System.err.println("Data received in unknown format");
            }
        }
        catch(IOException ioException){
            System.out.println("Disconnect with Client " + peerID);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally{
            //Close connections
            try{
                in.close();
                out.close();
                connection.close();
            }
            catch(IOException ioException){
                System.out.println("Disconnect with Client " + peerID);
            }
        }
    }

    //send a message to the output stream
    public void sendMessage(String msg)
    {
        //handshake message
        String handshakeHeader = "P2PFILESHARINGPROJ";
        byte[] handshakeZeroBits = new byte[10];
        Arrays.fill(handshakeZeroBits, (byte) 0);
        Peer currentPeer = peerInfoObj.getPeerWithID(currentPeerID);
        Map<Integer,Socket> sockets = currentPeer.getSockets();
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

}




