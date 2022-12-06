import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

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
        System.out.println("Entered handShake()");
        //handshake message
        byte[] handShakeMsg = new byte[32];
        System.out.println("create 32 bytes");
        String handShakeString = new String("P2PFILESHARINGPROJ");
        byte[] handShake1 = handShakeString.getBytes();
        System.out.println("sets up handShake1");
        byte[] handShake2 = new byte[10];
        byte[] handShake3 = ByteBuffer.allocate(4).putInt(peer.getPeerID()).array();
        System.out.println("done with creating handShake 2 and 3");
        for (int i = 0; i < 32; i++) {
            System.out.println("Enters for loop");
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
            System.out.println("Entered run of msg");
            byte[] handShakeMsg = handShake();
            System.out.println("Passed handshake()");
            sendMessage(handShakeMsg);
            System.out.println("Sends message");
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
                            int haveMsgSize = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 1, 2)).getInt();
                            int haveIndex = ByteBuffer.wrap(Arrays.copyOfRange(inputMsg, 5, haveMsgSize)).getInt();
                            logManager.createLog(peer.getPeerID(), connectedPeerID, "receiveHave", haveIndex);
                            if (peer.checkIfNeighborBitfieldExists(connectedPeerID)) {
                                peer.updateNeighborBitfield(connectedPeerID, haveIndex);
                            } else {
                                //bitfield message is sent directly after handshake message
                                //however, if filePresent = false, that peer does not have to send a bitfield message
                                //so a peer may not exist in the neighborBitfield map
                                peer.addNeighborBitfield(connectedPeerID);
                            }
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
                            String possibleHandshakeHeader = inputMsg.toString();
                            String handShakeBeginning = possibleHandshakeHeader.substring(0, 18);
                            connectedPeerID = Integer.parseInt(possibleHandshakeHeader.substring(28));
                            if (handShakeBeginning == "P2PFILESHARINGPROJ") {
                                System.out.println("Handshake is good for peer#" + peer.getPeerID() + " with peer#" + connectedPeerID + "!");
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
