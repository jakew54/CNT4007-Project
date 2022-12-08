import java.net.*;
import java.io.*;

public class Client {
    private Peer peer;
    private Peer hostPeer;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    LogManager logger;
    private Socket requestSocket;

    public Client(Peer peer, Peer hostPeer, LogManager logger) {
        this.peer = peer;
        this.hostPeer = hostPeer;
        this.logger = logger;
    }

    public void connectPeer() {
        try {
            requestSocket = new Socket(hostPeer.getPeerIP(), hostPeer.getPeerPortNum());
            in = new ObjectInputStream(requestSocket.getInputStream());
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            MessageManager msgManager = new MessageManager(peer, out, in, logger);
            Thread msgThread = new Thread(msgManager);
            msgThread.start();
            peer.addConnectedPeer(hostPeer.getPeerID());
            hostPeer.addConnectedPeer(peer.getPeerID());
            peer.addMsgManagerToList(msgManager);



        } catch (UnknownHostException e) {
            System.err.println("Connecting to an unknown host (Client.java)");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO exception in connectPeers()");
            e.printStackTrace();
        }
    }
}
