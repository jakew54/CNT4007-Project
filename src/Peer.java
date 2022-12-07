import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import static java.lang.Math.random;

public class Peer {

    private int peerID;
    private String peerIP;
    private int peerPortNum;
    private boolean filePresent;
    private byte[] bitField;
    private byte[][] fileData; //first index is byte in bitfield, second index is bit within that byte
    private int numPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;


    private Vector<Integer> interestedNeighbors = new Vector<>();
    private Vector<Integer> preferredNeighbors;
    private Vector<Integer> chokedNeighbors;
    private HashMap<Integer, byte[]> neighborBitfields = new HashMap<>();
    private HashMap<Integer, Integer> numPiecesDownloadedFromNeighbor = new HashMap<>(); // <neighborID, numDownloaded>
    private int optimisticUnchokedNeighborID;
    private HashMap<Integer, Boolean> isChokedList = new HashMap<Integer, Boolean>();





    public Peer(int peerID, String peerIP, int peerPortNum, boolean filePresent) throws IOException {
        this.peerID = peerID;
        this.peerIP = peerIP;
        this.peerPortNum = peerPortNum;
        this.filePresent = filePresent;
    }

    public void addDownloadedPieceToDownloadedFromNeighborMap(int neighborID) {
        numPiecesDownloadedFromNeighbor.put(neighborID, numPiecesDownloadedFromNeighbor.get(neighborID) + 1);
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
        } //0 is default value so we dont have to do anything with !filePresent
    }

    public void setBitField(byte[] bitField) {
        this.bitField = bitField;
    }

    public byte[] getBitField() {
        return bitField;
    }

    public void addInterestedNeighbor(int interestedPeerID) {
        if (!(interestedNeighbors.contains(interestedPeerID))) {
            interestedNeighbors.add(interestedPeerID);
        }
    }

    public void removeInterestedNeighbor(int nonInterestedPeerID) {
        if (interestedNeighbors.contains(nonInterestedPeerID)) {
            interestedNeighbors.remove(nonInterestedPeerID);
        }
    }

    public void calculatePreferredNeighbors() {
        preferredNeighbors.clear();
        if (!filePresent) {
            HashMap<Integer, Float> neighborDownloadTimes = new HashMap<>();

            for (Map.Entry<Integer, Integer> p : numPiecesDownloadedFromNeighbor.entrySet()) {
                neighborDownloadTimes.put(p.getKey(), ((float) p.getValue()) / ((float) unchokingInterval));
            }

            float max;
            Vector<Integer> currMaxIDs = new Vector<>();
            for (int i = 0; i < numPreferredNeighbors; i++) {
                max = -1;
                for (Map.Entry<Integer, Float> entry : neighborDownloadTimes.entrySet()) {
                    if (entry.getValue() > max) {
                        currMaxIDs.clear();
                        max = entry.getValue();
                        currMaxIDs.add(entry.getKey());
                    } else if (entry.getValue().equals(max)) {
                        currMaxIDs.add(entry.getKey());
                    }
                }
                if (currMaxIDs.size() > 1) {
                    Random random = new Random();
                    int temp = random.nextInt(currMaxIDs.size());
                    preferredNeighbors.add(currMaxIDs.get(temp));
                    neighborDownloadTimes.remove(currMaxIDs.get(temp));
                } else {
                    preferredNeighbors.add(currMaxIDs.get(0));
                    neighborDownloadTimes.remove(currMaxIDs.get(0));
                }
            }
        } else { //file is present
            //randomly choose from interested neighbors
            Vector<Integer> intNeighborsTemp = (Vector) interestedNeighbors.clone();
            for (int i = 0; i < numPreferredNeighbors; i++) {
                Random random = new Random();
                int temp = random.nextInt(intNeighborsTemp.size());
                preferredNeighbors.add(temp);
                intNeighborsTemp.remove(temp);
            }
        }
        //TODO maybe don't log if list hasn't changed
        //log change of preferred neighbors
        //choke any neighbors that are unchoked but not in preferredNeighbors && not optimistically unchoked
        //send choke messages
        //unchoke any neighbors in preferredNeighbors that are choked
        //send unchoke messages
        return;
    }

    public void calculateOptimisticallyUnchokedNeighbor() {
        //get a list of the interestedNeighbors that are not contained within the preferredNeighbors
        //then randomly choose one from this list to be unchoked
    }

    public void addNeighborBitfield(int neighborID) {
        int numOfPieces = (getFileSize()/getPieceSize()) + 1;
        int bitFieldSize = numOfPieces / 8;
        if (numOfPieces % 8 != 0) {
            bitFieldSize += 1;
        }
        byte[] bitFieldTemp = new byte[bitFieldSize];
        neighborBitfields.put(neighborID, bitFieldTemp);
    }

    public void setNeighborBitfields(int neighborID, byte[] givenBitfield) {
        neighborBitfields.put(neighborID, givenBitfield);
    }

    public HashMap<Integer, byte[]> getNeighborBitfields() {
        return neighborBitfields;
    }

    public void updateNeighborBitfield(int neighborID, int pieceIndex) {
        int pieceIndex1 = pieceIndex / 8; //determines which byte in bitfield pieceIndex is in
        int pieceIndex2 = pieceIndex % 8; //determines which bit within that byte pieceIndex is
        neighborBitfields.get(neighborID)[pieceIndex1] = (byte) (bitField[pieceIndex1] | (1 << pieceIndex2));
    }

    public boolean checkIfNeighborBitfieldExists(int neighborID) {
        if (neighborBitfields.containsKey(neighborID)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkIfPeerHasPieceAtIndex(int pieceIndex) {
        int pieceIndex1 = pieceIndex / 8; //determines which byte in bitfield pieceIndex is in
        int pieceIndex2 = pieceIndex % 8; //determines which bit within that byte pieceIndex is
        return (bitField[pieceIndex1] >> pieceIndex2 & 1) == 1;
    }

    public boolean checkNeighborBitfieldForInterestingPieces(int neighborID) {
        for (int i = 0; i < bitField.length; i++) {
            for (int j = 0; j < 8; j++) {
                if((bitField[i] >> j & 1) != ((neighborBitfields.get(neighborID))[i] >> j & 1)) {
                    return true;
                }
            }
        }
        return false;
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

    public void setNumPreferredNeighbors(int numPreferredNeighbors) {
        this.numPreferredNeighbors = numPreferredNeighbors;
    }

    public int getNumPreferredNeighbors() {
        return numPreferredNeighbors;
    }

    public void setUnchokingInterval(int unchokingInterval) {
        this.unchokingInterval = unchokingInterval;
    }

    public int getUnchokingInterval() {
        return unchokingInterval;
    }

    public void setOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public void setFileName(String fileName) {
        this.fileName = new String(fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }

    public int getPieceSize() {
        return pieceSize;
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

}
