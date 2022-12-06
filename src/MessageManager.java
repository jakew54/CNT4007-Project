import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

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

    private boolean doAllHaveFile() {
        for (Map.Entry<Integer, Peer> p : peer.getPeerMap().entrySet()) {
            if (!p.getValue().getFilePresent()) {
                return false;
            }
        }
        return true;
    }

    private void sendMessage(byte[] message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private byte[] handShake() {

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

    private byte[] createMessage(int messageType) {
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
                break;
            case 7:
                //piece
                break;
            default:
                System.out.println("wrong message type dumbass");
                break;
        }

        return msg;
    }

    public void run() {
        try {
            byte[] handShakeMsg = handShake();
            sendMessage(handShakeMsg);
            int currMsgType = -1;
            while (!doAllHaveFile()) {
                try {
                    byte[] inputMsg = (byte[]) in.readObject();
                    currMsgType = inputMsg[4];
                    switch (currMsgType) {
                        case 0:
                            //choke

                            break;
                        case 1:
                            //unchoke
                            break;
                        case 2:
                            //interested
                            break;
                        case 3:
                            //not interested
                            break;
                        case 4:
                            //have
                            /*int haveMsgSize = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 1, 2)).getInt();
                            int haveIndex = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 5, haveMsgSize)).getInt();
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "receiveHave", haveIndex);
                            if (peer.checkIfNeighborBitfieldExists(connectedPeerID)) {
                                peer.updateNeighborBitfield(connectedPeerID, haveIndex);
                            } else {
                                //bitfield message is sent directly after handshake message
                                //however, if filePresent = false, that peer does not have to send a bitfield message
                                //so a peer may not exist in the neighborBitfield map
                                peer.addNeighborBitfield(connectedPeerID);
                            }*/
                            break;
                        case 5:
                            //bitfield
                            String bitFieldInput = inputMsg.toString();
                            int bitFieldMsgSize = ByteBuffer.wrap(bitFieldInput.substring(1,5).getBytes(StandardCharsets.UTF_8)).getInt();
                            break;
                        case 6:
                            //request
                            break;
                        case 7:
                            //piece
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
                                    sendMessage(createMessage(5));
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
        }catch(Exception e){
            System.out.println("Catch error");
            e.printStackTrace();
            //close any threads or whatever are still running (cleanup)
        }
    }

}
