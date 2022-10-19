import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class PeerInfo {

    private Vector<Peer> peers = new Vector<Peer>();

    public PeerInfo() {
        readConfigFile("PeerInfo.cfg");
    }

    private Vector<Peer> readConfigFile(String file) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            String[] peerInfo = line.split(" ");
            boolean filePresent = false;
            if (peerInfo[3].equals("1")){
                filePresent = true;
            }

           peers.add(new Peer(peerInfo[0], peerInfo[1],peerInfo[2],filePresent));

            while (line!=null){
                line = reader.readLine();

                peerInfo = line.split(" ");
                 filePresent = false;
                if (peerInfo[3].equals("1")){
                    filePresent = true;
                }

                peers.add(new Peer(peerInfo[0], peerInfo[1],peerInfo[2],filePresent));
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return peers;
    }


}
