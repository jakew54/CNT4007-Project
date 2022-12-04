import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Peer {

    //MAP: peer 1006
    // Keys: IDS (int)   VALUES: isChoked ()
    //1. 1001,false
    //2. 1002,true
    //3. 1003,true


    private int peerID;
    private String peerIP;
    private int peerPortNum;
    private boolean filePresent;
    private byte[] bitField;
    private byte[][] fileData; //first index is byte in bitfield, second index is bit within that byte
    //private boolean unChoked;
    //private boolean interested;
    //private Map<Integer, Socket> sockets = new HashMap<Integer, Socket>();
    private Hashtable<Integer, Peer> peerMap = new Hashtable<Integer, Peer>();

    private Vector<Integer> preferredNeighbors = new Vector<>();
    private int optimisticUnchokedNeighborID;
    private Map<Integer, Boolean> isChokedList = new HashMap<Integer, Boolean>();
    private Map<Integer, Boolean> isHandshakedList = new HashMap<Integer, Boolean>();

    //private ServerSocket myServerSocket;




    public Peer(int peerID, String peerIP, int peerPortNum, boolean filePresent) throws IOException {
        this.peerID = peerID;
        this.peerIP = peerIP;
        this.peerPortNum = peerPortNum;
        this.filePresent = filePresent;

    }

    public void createBitField(int size, int numPieces) {
        bitField = new byte[size];
        if (filePresent) {
            for (int i = 0; i < size - 1; i++) {
                for (int j = 0; j < 8; j++) {
                    bitField[i] = (byte) (bitField[i] | (1 << j));
                }
            }
            if ((size * 8) > numPieces) {
                int extraBitsNum = (size * 8) - numPieces;
                for (int i = 0; i < extraBitsNum; i++) {
                    bitField[size-1] = (byte) (bitField[size-1] & ~(1 << i));
                }
            }
        }
    }


    public int getPeerID() {
        return peerID;
    }

    public void setPeerID(int peerID){
        this.peerID = peerID;
    }

    public String getPeerIP(){
        return peerIP;
    }

    public void setPeerIP(String peerIP){
        this.peerIP=peerIP;
    }

    public int getPeerPortNum(){
        return peerPortNum;
    }

    public void setPeerPortNum(int peerPortNum){
        this.peerPortNum=peerPortNum;
    }

    public boolean getFilePresent() {
        return filePresent;
    }

    public void setFilePresent(boolean filePresent){
        this.filePresent=filePresent;
    }

    public void setPeerMap(Hashtable<Integer, Peer> peers) {
        this.peerMap = peers;
    }

    public Hashtable<Integer, Peer> getPeerMap() {
        return this.peerMap;
    }

    public void setBitField(byte[] bitField) {
        this.bitField = bitField;
    }

    public byte[] getBitField() {
        return bitField;
    }

   /* public void setUnChoked(boolean unChoked) {
        this.unChoked = unChoked;
    }

    public boolean getUnChoked() {
        return unChoked;
    }

    public void setInterested(boolean interested) {
        this.interested = interested;
    }

    public boolean getInterested() {
        return interested;
    }*/

    public void setPreferredNeighbors(Vector<Integer> preferredNeighbors) {
        this.preferredNeighbors = preferredNeighbors;
    }

    public Vector<Integer> getPreferredNeighbors() {
        return preferredNeighbors;
    }

    public void setOptimisticUnchokedNeighborID(int optimisticUnchokedNeighborID) {
        this.optimisticUnchokedNeighborID = optimisticUnchokedNeighborID;
    }

    public int getOptimisticUnchokedNeighborID() {
        return optimisticUnchokedNeighborID;
    }

    public void setIsChokedListWithID(int ID, boolean isChoked) {
        this.isChokedList.put(ID,isChoked);
    }

    public boolean getIsChokedListWithID(int ID) {
        return isChokedList.get(ID);
    }

    public int getNumPiecesDownloaded() {
        int total = 0;
        for (int i = 0; i < bitField.length; i++) {
            if (bitField[i] == (byte) 1) {
                total++;
            }
        }
        return total;
    }

    public void setIsHandshakedListWithID(int ID, boolean isHandshaked) {
        this.isHandshakedList.put(ID,isHandshaked);
    }

    public boolean getIsHandshakedListWithID(int ID) {
        return isHandshakedList.get(ID);
    }


}
