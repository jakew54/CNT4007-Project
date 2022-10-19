import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Peer {

    private String peerID;
    private String peerIP;
    private String peerPortNum;
    private boolean filePresent;


    public Peer(String peerID, String peerIP, String peerPortNum, boolean filePresent){
        this.peerID = peerID;
        this.peerIP = peerIP;
        this.peerPortNum = peerPortNum;
        this.filePresent = filePresent;
    }

    public String getPeerID() {
        return peerID;
    }

    public void setPeerID(String peerID){
        this.peerID = peerID;
    }

    public String getPeerIP(){
        return peerIP;
    }

    public void setPeerIP(String peerIP){
        this.peerIP=peerIP;
    }

    public String getPeerPortNum(){
        return peerPortNum;
    }

    public void setPeerPortNum(String peerPortNum){
        this.peerPortNum=peerPortNum;
    }

    public boolean filePresent() {
        return filePresent;
    }

    public void setFilePresent(boolean filePresent){
        this.filePresent=filePresent;
    }


}
