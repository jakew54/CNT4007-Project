import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;


public class LogManager {
    private Peer peer;
    private Logger logger;
    private FileHandler fileHandler;


    public LogManager(Peer peer) {
        this.peer = peer;
        String logAddy = "log_peer_" + peer.getPeerID() + ".log";
        this.logger = Logger.getLogger(Integer.toString(peer.getPeerID()));
        try {
            fileHandler = new FileHandler(logAddy);
            this.logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (IOException e) {
            System.err.println("IO exception in log manager");
            e.printStackTrace();
        }
    }

    public void createLog(int clientID, int hostID, String logType, int filler) {
        String clientLog = "";
        String hostLog = "";

        //calculating timestamp in EST time
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        int hour = Integer.parseInt(timeStamp.substring(11,13));
        hour = hour - 4;
        if (hour < 0) {
            hour = 24 + hour;
        }
        timeStamp = timeStamp.substring(0,11) + hour + timeStamp.substring(13,timeStamp.length());

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
                Vector<Integer> tempNeighborsVec = peer.getPreferredNeighbors();
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
                clientLog += Integer.toString(peer.getOptimisticUnchokedNeighborID());
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
            case "receiveNotInterested":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " received the 'not interested' message from ";
                clientLog += Integer.toString(hostID);
                clientLog += ".";
                break;
            case "receiveBitfield":
                clientLog += timeStamp;
                clientLog += ": Peer ";
                clientLog += Integer.toString(clientID);
                clientLog += " received the 'bitfield' message from ";
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
                clientLog += Integer.toString(peer.getNumPiecesDownloaded());
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
            logger.info(clientLog);
        }
        //Writing hostLog to correct files
        if (hostLog.length() > 0) {
            logger.info(hostLog);
        }

    }
}
