import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Handshake {
    private String headerBeginning = "P2PFILESHARINGPROJ";
    private byte[] zeroBits;
    private byte[] peerID;



   public Handshake(int peerIdentification) {


       byte[] newPeerID = new byte[4];
       byte[] newZeroBits = new byte[10];
       //setPeerID(newPeerID);



   }






    public byte[] getZeroBits() {
        return zeroBits;
    }

    public void setZeroBits(byte[] zeroBits) {
        this.zeroBits=zeroBits;
    }

    public byte[] getPeerID() {
        return peerID;
    }

    public void setPeerID(byte[] peerID){
        this.peerID = peerID;
    }


}





