import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.io.File;

import static java.lang.Math.random;

public class Peer {

    private int peerID;
    private String peerIP;
    private int peerPortNum;
    private boolean filePresent;
    private byte[] bitField;
    private BitSet bitField2;
    private HashMap<Integer, BitSet> neighborBitFields2 = new HashMap<>();
    private Vector<byte[]> fileData = new Vector<>();
    private int numPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;
    private int numOfPieces;


    private Vector<Integer> connectedPeers = new Vector<>();
    private Vector<Integer> interestedNeighbors = new Vector<>();
    private Vector<Integer> preferredNeighbors = new Vector<>();
    private Vector<Integer> chokedNeighbors = new Vector<>();
    private HashMap<Integer, byte[]> neighborBitfields = new HashMap<>();
    private HashMap<Integer, Integer> numPiecesDownloadedFromNeighbor = new HashMap<>(); // <neighborID, numDownloaded>
    private int optimisticUnchokedNeighborID;
    private HashMap<Integer, Boolean> isChokedList = new HashMap<Integer, Boolean>();
    private Vector<MessageManager> msgManagerList = new Vector<>();
    private HashMap<Integer, Integer> requestedPiecesFromNeighbors = new HashMap<>(); // <neighborID, pieceIndex>





    public Peer(int peerID, String peerIP, int peerPortNum, boolean filePresent) throws IOException {
        this.peerID = peerID;
        this.peerIP = peerIP;
        this.peerPortNum = peerPortNum;
        this.filePresent = filePresent;
        this.optimisticUnchokedNeighborID = 0;
    }

    public void setBitField2() {
        if (filePresent) {
            bitField2 = new BitSet(numOfPieces);
            bitField2.set(0,numOfPieces,true);
        } else {
            bitField2 = new BitSet(numOfPieces);
            bitField2.set(0,numOfPieces,false);
        }
    }

    public BitSet getBitField2() {
        return bitField2;
    }

    public void updateBitField2(int pieceIndex) {
        //never have to set it to false
        bitField2.set(pieceIndex, true);
    }

    public boolean checkIfBitField2IsFull(BitSet currentBitField) {
        for (int i = 0; i < numOfPieces; i++) {
            if (!currentBitField.get(i)) {
                return false;
            }
        }
        return true;
    }

    public HashMap<Integer, BitSet> getNeighborBitFields2() {
        return neighborBitFields2;
    }

    public void setNeighborBitFields2(int neighborId) {
        neighborBitFields2.put(neighborId, new BitSet(numOfPieces));
    }

    public void updateNeighborBitfields2(int neighborID, int pieceIndex) {
        neighborBitFields2.get(neighborID).set(pieceIndex, true);
    }

    public void setRequestedPiecesFromNeighbors(int neighborID, int pieceIndex) {
        requestedPiecesFromNeighbors.put(neighborID, pieceIndex);
    }

    public HashMap<Integer, Integer> getRequestedPiecesFromNeighbors() {
        return requestedPiecesFromNeighbors;
    }

    public void removeRequestedPiecesFromNeighbors(int neighborID) {
        requestedPiecesFromNeighbors.remove(neighborID);
    }

    public void addConnectedPeer(Integer connectedID) {
        this.connectedPeers.add(connectedID);
    }

    public Vector<Integer> getConnectedPeers() {
        return connectedPeers;
    }

    public void addNeighborToDownloadedPieceFromNeighborMap(int neighborID) {
        numPiecesDownloadedFromNeighbor.put(neighborID, 0);
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
                for (int i = extraBitsNum; i < 8; i++) {
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

    public void updateBitField(int pieceIndex) {
        int pieceIndex1 = pieceIndex / 8;
        int pieceIndex2 = pieceIndex % 8;
        bitField[pieceIndex1] = (byte) (bitField[pieceIndex1] | (1 << pieceIndex2));
        return;
    }

    public boolean checkIfBitFieldIsFull(byte[] currentBitfield) {
        int possibleExtraBitsHandler = 0;
        for (int i = 0; i < currentBitfield.length; i++) {
            if (i == currentBitfield.length - 1) {
                possibleExtraBitsHandler = (currentBitfield.length * 8) - numOfPieces;
            }
            for (int j = 0; j < 8 - possibleExtraBitsHandler; j++) {
                if ((currentBitfield[i] >> j & 1) == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public void addInterestedNeighbor(int interestedPeerID) {
        if (!(interestedNeighbors.contains(interestedPeerID))) {
            interestedNeighbors.add(interestedPeerID);
        }
    }

    public void removeInterestedNeighbor(int nonInterestedPeerID) {
        if (interestedNeighbors.contains(nonInterestedPeerID)) {
            for (int i = 0; i < interestedNeighbors.size(); i++) {
                if (interestedNeighbors.get(i) == nonInterestedPeerID) {
                    interestedNeighbors.remove(i);
                }
            }
        }
    }

    public void calculatePreferredNeighbors() {
        //TODO Suppose that peer C is randomly chosen as the optimistically unchoked neighbor of peer
        //A. Because peer A is sending data to peer C, peer A may become one of peer C’s
        //preferred neighbors, in which case peer C would start to send data to peer A. If the rate
        //at which peer C sends data to peer A is high enough, peer C could then, in turn, become
        //one of peer A’s preferred neighbors. Note that in this case, peer C may be a preferred
        //neighbor and optimistically unchoked neighbor at the same time. This kind of situation is
        //allowed. In the next optimistic unchoking interval, another peer will be selected as an
        //optimistically unchoked neighbor.

        if (!interestedNeighbors.isEmpty()) {
            if (!(preferredNeighbors.isEmpty())) {
                preferredNeighbors.clear();
            }
            if (!filePresent) {
                HashMap<Integer, Float> neighborDownloadTimes = new HashMap<>();
                for (int i =0; i < interestedNeighbors.size(); i++) {
                    neighborDownloadTimes.put(interestedNeighbors.get(i),(float) 0);
                }

                for (Map.Entry<Integer, Integer> p : numPiecesDownloadedFromNeighbor.entrySet()) {
                    if (neighborDownloadTimes.containsKey(p.getKey())) {
                        neighborDownloadTimes.put(p.getKey(), ((float) p.getValue()) / ((float) unchokingInterval));
                    }
                }

                float max;
                Vector<Integer> currMaxIDs = new Vector<>();
                for (int i = 0; i < Math.min(numPreferredNeighbors, neighborDownloadTimes.size()); i++) {
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
                    if (!currMaxIDs.isEmpty()) {
                        Random random = new Random();
                        int temp = random.nextInt(currMaxIDs.size());
                        preferredNeighbors.add(currMaxIDs.get(temp));
                        neighborDownloadTimes.remove(currMaxIDs.get(temp));
                    }
                }
            } else { //file is present
                //randomly choose from interested neighbors
                Vector<Integer> intNeighborsTemp = new Vector<>(interestedNeighbors);

                for (int i = 0; i < Math.min(numPreferredNeighbors, interestedNeighbors.size()); i++) {
                    Random random = new Random();
                    int temp = random.nextInt(intNeighborsTemp.size());
                    preferredNeighbors.add(intNeighborsTemp.get(temp));
                    intNeighborsTemp.remove(temp);
                }
            }
            //TODO maybe don't log if list hasn't changed
            for (int i = 0; i < connectedPeers.size(); i++) {
                if (!(chokedNeighbors.contains(connectedPeers.get((i))))) {

                    if (!(preferredNeighbors.contains(connectedPeers.get(i))) && !(connectedPeers.get(i) == optimisticUnchokedNeighborID)) {
                        msgManagerList.get(findMsgManagerFromNeighborID(connectedPeers.get(i))).sendMessage(msgManagerList.get(findMsgManagerFromNeighborID(connectedPeers.get(i))).createMessage(0, -1));
                        chokedNeighbors.add(connectedPeers.get(i));
                    }
                }
            }
            for (int i = 0; i < preferredNeighbors.size(); i++) {
                if (chokedNeighbors.contains(preferredNeighbors.get(i))) {
                    msgManagerList.get(findMsgManagerFromNeighborID(preferredNeighbors.get(i))).sendMessage(msgManagerList.get(findMsgManagerFromNeighborID(preferredNeighbors.get(i))).createMessage(1, -1));
                    chokedNeighbors.remove(preferredNeighbors.get(i));
                }
            }
            for (Map.Entry<Integer, Integer> p : numPiecesDownloadedFromNeighbor.entrySet()) {
                p.setValue(0);
            }
        }
        return;
    }

    public void calculateOptimisticallyUnchokedNeighbor() {

        if (interestedNeighbors.size() > numPreferredNeighbors) {
            Vector<Integer> tempChokedandInterested = new Vector<>();
            for (int i = 0; i < Math.min(chokedNeighbors.size(), interestedNeighbors.size()); i++) {
                if (chokedNeighbors.size() <= interestedNeighbors.size()) {
                    if (interestedNeighbors.contains(chokedNeighbors.get(i))) {
                        tempChokedandInterested.add(chokedNeighbors.get(i));
                    }
                } else {
                    if (chokedNeighbors.contains(interestedNeighbors.get(i))) {
                        tempChokedandInterested.add(interestedNeighbors.get(i));
                    }
                }
            }

            Random random = new Random();
            int temp = 0;
            if (!tempChokedandInterested.isEmpty()) {
                temp = tempChokedandInterested.get(random.nextInt(tempChokedandInterested.size()));

                if (optimisticUnchokedNeighborID > 0) {
                    msgManagerList.get(findMsgManagerFromNeighborID(optimisticUnchokedNeighborID)).sendMessage(msgManagerList.get(findMsgManagerFromNeighborID(optimisticUnchokedNeighborID)).createMessage(0, -1));
                    chokedNeighbors.add(optimisticUnchokedNeighborID);
                }

                optimisticUnchokedNeighborID = temp;
                chokedNeighbors.remove(temp);
                msgManagerList.get(findMsgManagerFromNeighborID(optimisticUnchokedNeighborID)).sendMessage(msgManagerList.get(findMsgManagerFromNeighborID(optimisticUnchokedNeighborID)).createMessage(1, -1));
            }
        }
        return;
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

    public void addNeighborBitField2(int neighborID, BitSet bitSet) {
        neighborBitFields2.put(neighborID, bitSet);
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

    public boolean checkIfNeighborBitField2Exists(int neighborID) {
        if (neighborBitFields2.containsKey(neighborID)) {
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

    public boolean checkIfPeerHasPieceAtIndex2(int pieceIndex) {
        return(bitField2.get(pieceIndex));
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

    public boolean checkNeighborBitfieldForInterestingPieces2(int neighborID) {
        for (int i = 0; i < numOfPieces; i++) {
            if (neighborBitFields2.get(neighborID).get(i) == true && bitField2.get(i) == false) {
                return true;
            }
        }
        return false;
    }

    public Vector<Integer> getNeighborBitfieldForAllInterestingPieces(int neighborID) {
        Vector<Integer> temp = new Vector<>();
        for (int i = 0; i < bitField.length; i++) {
            for (int j = 0; j < 8; j++) {
                if((bitField[i] >> j & 1) != ((neighborBitfields.get(neighborID))[i] >> j & 1)) {
                    temp.add((i*8) + j);
                }
            }
        }
        return temp;
    }

    public Vector<Integer> getNeighborBitField2ForAllInterestingPieces(int neighborID) {
        Vector<Integer> temp = new Vector<>();
        for (int i = 0; i < numOfPieces; i++) {
            if (bitField2.get(i) == false && neighborBitFields2.get(neighborID).get(i) == true) {
                temp.add(i);
            }
        }
        return temp;
    }

    public int getNumOfPieces() {
        return numOfPieces;
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

    public void addMsgManagerToList(MessageManager msgMan) {
        msgManagerList.add(msgMan);
    }

    public Vector<MessageManager> getMsgManagerList() {
        return msgManagerList;
    }

    public int findMsgManagerFromNeighborID(int neighborID){

        for (int i = 0; i < msgManagerList.size(); i++){
            if (msgManagerList.get(i).getConnectedPeerID() == neighborID){
                return i;
            }
        }
        System.out.println("findMsgManagerFromNeighborID failed to find matching index");
        return -1;
    }

    public void createFileData(int numPiece) {
        fileData.setSize(numOfPieces);
        for (int i = 0; i < numOfPieces; i++) {
            fileData.set(i, new byte[pieceSize]);
        }
    }

    public void updateFileData(int pieceIndex, byte[] piece) {
        fileData.set(pieceIndex, piece);
    }

    public byte[] getFileDataAtIndex(int pieceIndex) {
        return fileData.get(pieceIndex);
    }

    public void updateChokedNeighbors(int neighborID){
        this.chokedNeighbors.add(neighborID);
    }

    public void writeDataToFile() throws IOException {
        String destinationFileName = "/peer_" + peerID + "/tree.jpg";
        File file = new File(destinationFileName);
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        for (int i = 0; i < fileData.size(); i++) {
            fos.write(fileData.get(i));
        }
        fos.close();
    }

    public void setNumofPieces(int num) {
        this.numOfPieces = num;
    }
}
