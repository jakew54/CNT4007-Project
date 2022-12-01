import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;

public class PeerInfo {

    private Vector<Peer> peers = new Vector<Peer>();
    private int totalNumOfPeers;


    public PeerInfo() {
        readConfigFile("PeerInfo.cfg");
    }

    public int getTotalNumOfPeers(){
        return totalNumOfPeers;
    }

    public Vector<Peer> getPeers() {
        return peers;
    }

    public void setTotalNumOfPeers(int totalNumOfPeers){
        this.totalNumOfPeers=totalNumOfPeers;
    }

    public Peer getPeerWithID(int peerID) {
        for (int i = 0; i < peers.size(); i++) {
            if (peers.get(i).getPeerID() == peerID) {
                return peers.get(i);
            }
        }
        return null;
    }

    public void readConfigFile(String file) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            String[] peerInfo = line.split(" ");
            boolean filePresent = false;
            if (peerInfo[3].equals("1")){
                filePresent = true;
            }

            Peer tempPeer = new Peer(parseInt(peerInfo[0]), peerInfo[1],parseInt(peerInfo[2]),filePresent);

            peers.add(tempPeer);

            while (line!=null){
                line = reader.readLine();
                if (line==null){
                    break;
                }

                peerInfo = line.split(" ");
                 filePresent = false;
                if (peerInfo[3].equals("1")){
                    filePresent = true;
                }

                peers.add(new Peer(parseInt(peerInfo[0]), peerInfo[1],parseInt(peerInfo[2]),filePresent));
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setTotalNumOfPeers(peers.size());
        return;
    }

    public static void main(String[] args) {
        PeerInfo obj = new PeerInfo();
        Vector<Peer> test1 = obj.getPeers();
       for (int i = 0; i < test1.size(); i++){
           System.out.print("Peer ID: " + test1.get(i).getFilePresent());
       }


    }
}


