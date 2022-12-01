import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Handshake {

    private byte[] completeHeader;
    private static String headerBeginning = "P2PFILESHARINGPROJ";
    private byte[] zeroBits;
    private byte[] peerID;



   public Handshake(int peerIdentification) {

       byte[] newCompleteHeader = new byte[18];


       byte[] newPeerID = new byte[4];
       newPeerID = ByteBuffer.allocate(4).putInt(peerIdentification).array();
       setPeerID(newPeerID);
       byte[] newZeroBits = new byte[10];
       setZeroBits(newZeroBits);
       //setPeerID(newPeerID);



   }


    public byte[] getCompleteHeader() {
        return completeHeader;
    }

    public void setCompleteHeader(byte[] completeHeader){
       this.completeHeader=completeHeader;
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

    public static String getHeaderBeginning() {
       return headerBeginning;
    }


}





