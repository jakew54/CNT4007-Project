import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MessageManager implements Runnable{
    private Peer peer;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int connectedPeerID;
    private LogManager logManager;
    private byte[] bitMasks1;
    private byte[] bitMasks2;

    public MessageManager(Peer peer, ObjectOutputStream out, ObjectInputStream in, LogManager logger) throws IOException {
        this.peer = peer;
        this.in = in;
        this.out = out;
        this.logManager = logger;
    }

    public BitSet bytesToBitSet(byte[] byteArr) {
        BitSet bitSet = new BitSet();
        for (int i = 0; i < byteArr.length * 8; i++) {
            if ((byteArr[byteArr.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bitSet.set(i);
            }
        }
        return bitSet;
    }

    public int getConnectedPeerID(){
        return this.connectedPeerID;
    }

    public boolean allPeersDoNotHaveFile2() {
        if (peer.getNeighborBitFields2().isEmpty()) {
            return true;
        }
        if (!peer.checkIfBitField2IsFull(peer.getBitField2())) {
            return true;
        }
        for (int i = 0; i < peer.getNeighborBitFields2().size(); i++) {
            if (!peer.checkIfBitField2IsFull(peer.getNeighborBitFields2().get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean allPeersDoNotHaveFile() {

        if (peer.getNeighborBitfields().isEmpty()){
            return true;
        }

        if (!peer.checkIfBitFieldIsFull(peer.getBitField())) {
            return true;
        }

        for (int i = 0; i < peer.getNeighborBitfields().size(); i++) {
            if (!peer.checkIfBitFieldIsFull(peer.getNeighborBitfields().get(i))) {
                return true;
            }
        }

//        if (peer.getFilePresent()) {
//            for (Map.Entry<Integer, byte[]> p : peer.getNeighborBitfields().entrySet()) {
//                for (int i = 0; i < p.getValue().length; i++) {
//                    for (int j = 0; j < 8; j++) {
//                        if ((p.getValue()[i] >> j & 1) == 0) {
//                            return true;
//                        }
//                    }
//                }
//            }
//        }
//        else{
//            return true;
//        }
        return false;
    }



    public void sendMessage(byte[] message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    public byte[] handShake() {

        //handshake message
        byte[] handShakeMsg = new byte[32];
        String handShakeString = new String("P2PFILESHARINGPROJ");
        byte[] handShake1 = handShakeString.getBytes();
        byte[] handShake2 = new byte[10];
        byte[] handShake3 = ByteBuffer.allocate(4).putInt(peer.getPeerID()).array();

        for (int i = 0; i < 32; i++) {
            if (i < 18) {
                handShakeMsg[i] = handShake1[i];
            } else if (i < 28) {
                handShakeMsg[i] = handShake2[i - 18];
            } else {
                handShakeMsg[i] = handShake3[i - 28];
            }
        }

        return handShakeMsg;
    }

    public byte[] createMessage(int messageType, int payload) {
        byte[] msg = new byte[0];
        byte[] messageLength, messageTypeArr, messagePayload;
        ByteArrayOutputStream outByte;
        switch(messageType) {
            case 0:
                //choke
            case 1:
                //unchoke
            case 2:
                //interested
            case 3:
                //not interested
                outByte = new ByteArrayOutputStream();
                messageLength = new byte[4];
                messageLength =  ByteBuffer.allocate(4).putInt(1).array();

                messageTypeArr = new byte[1];
                messageTypeArr[0] = (byte) messageType;

                try {
                    outByte.write(messageLength);
                    outByte.write(messageTypeArr);
                    outByte.close();
                    return outByte.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 4:
                //have
                outByte = new ByteArrayOutputStream();
                messageLength = new byte[4];
                messageLength =  ByteBuffer.allocate(4).putInt(1 + 4).array();

                messageTypeArr = new byte[1];
                messageTypeArr[0] = (byte) messageType;

                messagePayload = ByteBuffer.allocate(4).putInt(payload).array();

                try {
                    outByte.write(messageLength);
                    outByte.write(messageTypeArr);
                    outByte.write(messagePayload);
                    outByte.close();
                    return outByte.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 5:
                //bitfield
                outByte = new ByteArrayOutputStream();
                messageLength = new byte[4];
                //messageLength =  ByteBuffer.allocate(4).putInt(1 + peer.getBitField().length).array();
                messageLength =  ByteBuffer.allocate(4).putInt(1 + peer.getBitField2().toByteArray().length).array();

                messageTypeArr = new byte[1];
                messageTypeArr[0] = (byte) messageType;

                //messagePayload = peer.getBitField();
                messagePayload = peer.getBitField2().toByteArray();

                try {
                    outByte.write(messageLength);
                    outByte.write(messageTypeArr);
                    outByte.write(messagePayload);
                    outByte.close();
                    return outByte.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 6:
                //request

                peer.setRequestedPiecesFromNeighbors(connectedPeerID, payload);

                outByte = new ByteArrayOutputStream();
                messageLength = new byte[4];
                messageLength =  ByteBuffer.allocate(4).putInt(1 + 4).array();

                messageTypeArr = new byte[1];
                messageTypeArr[0] = (byte) messageType;

                messagePayload = ByteBuffer.allocate(4).putInt(payload).array();

                try {
                    outByte.write(messageLength);
                    outByte.write(messageTypeArr);
                    outByte.write(messagePayload);
                    outByte.close();
                    return outByte.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 7:
                //piece
                outByte = new ByteArrayOutputStream();
                messageLength = new byte[4];
                messageLength =  ByteBuffer.allocate(4).putInt(1 + 4 + peer.getFileDataAtIndex(payload).length).array();

                messageTypeArr = new byte[1];
                messageTypeArr[0] = (byte) messageType;


                messagePayload = ByteBuffer.allocate(4).putInt(payload).array();

                try {
                    outByte.write(messageLength);
                    outByte.write(messageTypeArr);
                    outByte.write(messagePayload);
                    outByte.write(peer.getFileDataAtIndex(payload));
                    outByte.close();
                    return outByte.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Invalid message type when trying to create message.");
                break;
        }

        return msg;
    }

    public void run() {

        try {
            byte[] handShakeMsg = handShake();
            sendMessage(handShakeMsg);
            Vector<Integer> temp = new Vector<>();

            int currMsgType = -1;
            while (allPeersDoNotHaveFile2()) {
                try {
                    System.out.println("Is bit field 2 full: " + peer.checkIfBitField2IsFull(peer.getBitField2()));
                    temp = peer.getIndecesOfFalse(peer.getBitField2());
                    for (int i = 0; i < temp.size(); i++) {
                        System.out.print("Falses: " + temp.get(i) + ", ");
                    }
                    System.out.println();
                    if (peer.checkIfBitField2IsFull(peer.getBitField2()) && !peer.getFilePresent()) {
                        peer.setFilePresent(true);
                        peer.writeDataToFile();
                    }
                    byte[] inputMsg = (byte[]) in.readObject();
                    Vector<Integer> allInterestingPieces;
                    currMsgType = inputMsg[4];
                    switch (currMsgType) {
                        case 0:
                            //choke
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "choking", 1);
                            if (peer.getRequestedPiecesFromNeighbors().containsKey(connectedPeerID)) {
                                peer.removeRequestedPiecesFromNeighbors(connectedPeerID);
                            }
                            break;
                        case 1:
                            //unchoke
                            allInterestingPieces = peer.getNeighborBitField2ForAllInterestingPieces(connectedPeerID);
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "unchoking", 1);
                            for (int i = 0; i < allInterestingPieces.size(); i++) {
                                if (peer.getRequestedPiecesFromNeighbors().containsValue(allInterestingPieces.get(i))) {
                                    allInterestingPieces.remove(allInterestingPieces.get(i));
                                }
                            }
                            if (!allInterestingPieces.isEmpty()) {
                                Random random = new Random();
                                int temp = 0;
                                temp = allInterestingPieces.get(random.nextInt(allInterestingPieces.size()));
                                sendMessage(createMessage(6, temp));
                            }
                            break;
                        case 2:
                            //interested
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "receiveInterested", 1);
                            peer.addInterestedNeighbor(connectedPeerID);
                            break;
                        case 3:
                            //not interested
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "receiveNotInterested", 1);
                            peer.removeInterestedNeighbor(connectedPeerID);
                            break;
                        case 4:
                            //have
                            int haveMsgSize = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 0, 4)).getInt();
                            int haveIndex = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 5, inputMsg.length)).getInt();
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "receiveHave", haveIndex);
                            if (peer.checkIfNeighborBitField2Exists(connectedPeerID)) {
                                peer.updateNeighborBitfields2(connectedPeerID, haveIndex);
                            } else {
                                //bitfield message is sent directly after handshake message
                                //however, if filePresent = false, that peer does not have to send a bitfield message
                                //so a peer may not exist in the neighborBitfield map
                                peer.setNeighborBitFields2(connectedPeerID);
                                peer.updateNeighborBitfields2(connectedPeerID, haveIndex);
                            }
                            //check if you want it
                            //if (peer.checkIfPeerHasPieceAtIndex(haveIndex)) {
                            if (peer.checkNeighborBitfieldForInterestingPieces2(connectedPeerID)) {
                                // interested
                                sendMessage(createMessage(2, -1));
                            } else {
                                // not interested
                                sendMessage(createMessage(3, -1));
                            }
                            break;
                        case 5:
                            //bitfield
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "receiveBitfield", 1);
                            int bitFieldMsgSize = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 0, 4)).getInt();
                            byte[] neighborBitFieldGiven = new byte[bitFieldMsgSize - 1];
                            ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 5, inputMsg.length)).get(neighborBitFieldGiven, 0, neighborBitFieldGiven.length);
                            BitSet bitSet = bytesToBitSet(neighborBitFieldGiven);
                            peer.addNeighborBitField2(connectedPeerID, bitSet);
                            if (peer.checkNeighborBitfieldForInterestingPieces2(connectedPeerID)) {
                                //contains at least 1 interesting piece
                                sendMessage(createMessage(2, -1));
                            } else {
                                //bitfields are the same - no interesting pieces
                                sendMessage(createMessage(3, -1));
                            }
                            break;
                        case 6:
                            //request
                            //receive req, send piece
                            int requestedPieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 5, 9)).getInt();
                            //TODO
                            sendMessage(createMessage(7, requestedPieceIndex));
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "receiveRequest", requestedPieceIndex);
                            break;
                        case 7:
                            //piece
                            //receive piece, send new request
                            int pieceMsgLength = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 0, 4)).getInt();
                            int pieceIndexReceived = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 5, 9)).getInt();
                            byte[] byteReceived = Arrays.copyOfRange(inputMsg, 9,pieceMsgLength);
                            peer.updateFileData(pieceIndexReceived, byteReceived);
                            peer.updateBitField2(pieceIndexReceived);
                            peer.removeRequestedPiecesFromNeighbors(pieceIndexReceived);
                            peer.addDownloadedPieceToDownloadedFromNeighborMap(connectedPeerID);
                            for (int i = 0; i < peer.getMsgManagerList().size(); i++) {
                                //sending have to everyone because peer got a new piece
                                peer.getMsgManagerList().get(i).sendMessage(peer.getMsgManagerList().get(i).createMessage(4, pieceIndexReceived));

                            }
                            allInterestingPieces = peer.getNeighborBitField2ForAllInterestingPieces(connectedPeerID);
                            //send request message for piece peer both does not have and has not yet requested
                            for (int i = 0; i < allInterestingPieces.size(); i++) {
                                if (peer.getRequestedPiecesFromNeighbors().containsValue(allInterestingPieces.get(i))) {
                                    allInterestingPieces.remove(allInterestingPieces.get(i));
                                }
                            }
                            if (!allInterestingPieces.isEmpty()) {
                                Random random = new Random();
                                int temp = 0;
                                temp = allInterestingPieces.get(random.nextInt(allInterestingPieces.size()));
                                sendMessage(createMessage(6, temp));
                            }
                            //TODO not done
                            break;
                        default:
                            //handshake

                            byte[] possibleHandshakeHeader = Arrays.copyOfRange(inputMsg, 0, 18);
                            connectedPeerID = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 28,32)).getInt();
                            String handshakerHeaderString = new String(possibleHandshakeHeader, StandardCharsets.UTF_8);
                            if (Objects.equals(handshakerHeaderString, "P2PFILESHARINGPROJ")) {
                                System.out.println("Handshake is good for Peer #" + peer.getPeerID() + " with Peer #" + connectedPeerID + "!");
                                //send bitfield msg
                                if (peer.getFilePresent()) {
                                    sendMessage(createMessage(5, -1));
                                }
                            }
                            peer.setNeighborBitFields2(connectedPeerID);
                            peer.addNeighborToDownloadedPieceFromNeighborMap(connectedPeerID);
                            peer.updateChokedNeighbors(connectedPeerID);
                            break;
                    }
                } catch (IOException ioE) {
                    System.err.println("IOexception oh no");
                } catch (ClassNotFoundException cnfE) {
                    System.err.println("ClassNotFoundException darnit");
                }
            }

        } catch(Exception e){
            System.out.println("Catch error");
            e.printStackTrace();
            //close any threads or whatever are still running (cleanup)
        }

    }

}
