import java.net.*;
import java.io.*;

public class Client {
    private Peer peer;
    private Peer hostPeer;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client(Peer peer, Peer hostPeer) {
        this.peer = peer;
        this.hostPeer = hostPeer;
    }

    public void connectPeer() {
        try {
            Socket requestSocket = new Socket(hostPeer.getPeerIP(), hostPeer.getPeerPortNum());
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
            MessageManager msgManager = new MessageManager(peer, out, in);
            System.out.println("Made msg Manager for client");
            Thread msgThread = new Thread(msgManager);
            msgThread.start();


        } catch (UnknownHostException e) {
            System.err.println("Connecting to an unknown host (Client.java)");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO exception in connectPeers()");
            e.printStackTrace();
        }
    }
}
