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

    public MessageManager(Peer peer, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.peer = peer;
        this.in = in;
        this.out = out;
        this.logManager = new LogManager(peer);
    }

    public boolean allPeersDoNotHaveFile() {

        if (peer.getNeighborBitfields().isEmpty()){
            return true;
        }

        if (peer.getFilePresent()) {
            for (Map.Entry<Integer, byte[]> p : peer.getNeighborBitfields().entrySet()) {
                for (int i = 0; i < p.getValue().length; i++) {
                    for (int j = 0; j < 8; j++) {
                        if ((p.getValue()[i] >> j & 1) == 0) {
                            return true;
                        }
                    }
                }
            }
        }
        else{
            return true;
        }
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
        System.out.println("Enters handShake()");
        //handshake message
        byte[] handShakeMsg = new byte[32];
        String handShakeString = new String("P2PFILESHARINGPROJ");
        byte[] handShake1 = handShakeString.getBytes();
        byte[] handShake2 = new byte[10];
        byte[] handShake3 = ByteBuffer.allocate(4).putInt(peer.getPeerID()).array();
        System.out.println("Right before handshake for loop");
        for (int i = 0; i < 32; i++) {
            if (i < 18) {
                handShakeMsg[i] = handShake1[i];
            } else if (i < 28) {
                handShakeMsg[i] = handShake2[i - 18];
            } else {
                handShakeMsg[i] = handShake3[i - 28];
            }
        }
        System.out.println("Before returning handShake");
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
                break;
            case 5:
                //bitfield
                outByte = new ByteArrayOutputStream();
                messageLength = new byte[4];
                messageLength =  ByteBuffer.allocate(4).putInt(1 + peer.getBitField().length).array();

                messageTypeArr = new byte[1];
                messageTypeArr[0] = (byte) messageType;

                messagePayload = peer.getBitField();

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
                Vector<Integer> allInterestingPieces = peer.getNeighborBitfieldForAllInterestingPieces(connectedPeerID);
                //send request message for piece peer both does not have and has not yet requested
                for (int i = 0; i < allInterestingPieces.size(); i++) {
                    if (peer.getRequestedPiecesFromNeighbors().containsValue(allInterestingPieces.get(i))) {
                        allInterestingPieces.remove(allInterestingPieces.get(i));
                    }
                }

                Random random = new Random();
                int temp = 0;
                temp = allInterestingPieces.get(random.nextInt(allInterestingPieces.size()));

                outByte = new ByteArrayOutputStream();
                messageLength = new byte[4];
                messageLength =  ByteBuffer.allocate(4).putInt(1 + 4).array();

                messageTypeArr = new byte[1];
                messageTypeArr[0] = (byte) messageType;

                messagePayload = ByteBuffer.allocate(4).putInt(temp).array();

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
            default:
                System.out.println("Invalid message type when trying to create message.");
                break;
        }

        return msg;
    }

    public void run() {
        System.out.println("Enters msg manager run()");
        try {
            byte[] handShakeMsg = handShake();
            sendMessage(handShakeMsg);
            System.out.println("Finishes sending handshake message");
            int currMsgType = -1;
            while (allPeersDoNotHaveFile()) {
                System.out.println("Enters doAllHaveFile while loop");
                try {
                    byte[] inputMsg = (byte[]) in.readObject();
                    currMsgType = inputMsg[4];
                    System.out.println("currMsgType: " + currMsgType);
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
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "unchoking", 1);
                            sendMessage(createMessage(6, -1));
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
                            if (peer.checkIfNeighborBitfieldExists(connectedPeerID)) {
                                peer.updateNeighborBitfield(connectedPeerID, haveIndex);
                            } else {
                                //bitfield message is sent directly after handshake message
                                //however, if filePresent = false, that peer does not have to send a bitfield message
                                //so a peer may not exist in the neighborBitfield map
                                peer.addNeighborBitfield(connectedPeerID);
                                peer.updateNeighborBitfield(connectedPeerID, haveIndex);
                            }
                            //check if you want it
                            if (peer.checkIfPeerHasPieceAtIndex(haveIndex)) {
                                //not interested - already has piece
                                sendMessage(createMessage(3, -1));
                            } else {
                                //interested
                                sendMessage(createMessage(2, -1));
                            }
                            break;
                        case 5:
                            //bitfield
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "receiveBitfield", 1);
                            int bitFieldMsgSize = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 0, 4)).getInt();
                            byte[] neighborBitFieldGiven = new byte[bitFieldMsgSize - 1];
                            ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 5, inputMsg.length)).get(neighborBitFieldGiven, 0, neighborBitFieldGiven.length);
                            peer.setNeighborBitfields(connectedPeerID, neighborBitFieldGiven);
                            if (peer.checkNeighborBitfieldForInterestingPieces(connectedPeerID)) {
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
                            sendMessage(createMessage(7, requestedPieceIndex));
                            break;
                        case 7:
                            //piece
                            //receive piece, send new request
                            peer.addDownloadedPieceToDownloadedFromNeighborMap(connectedPeerID);
                            break;
                        default:
                            //handshake
                            System.out.println("Enters default of Switch for handshake ");
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
                            break;
                    }
                } catch (IOException ioE) {
                    System.err.println("IOexception oh no");
                } catch (ClassNotFoundException cnfE) {
                    System.err.println("ClassNotFoundException darnit");
                }
            }
            System.out.println("End of while loop");
        } catch(Exception e){
            System.out.println("Catch error");
            e.printStackTrace();
            //close any threads or whatever are still running (cleanup)
        }
        System.out.println("After try and catch");
    }

}
