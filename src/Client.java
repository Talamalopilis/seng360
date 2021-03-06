/**
 * Created by Tal on 11/6/2017.
 * Last modified 11/9/2017 by Chase
 */
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.*;
import java.lang.StringBuffer;
import java.nio.file.*;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyFactory;

public class Client
{

	static boolean debug = false; //Change to true for debug messages

    String host;
	
    int port;
	static String securityStringClient;
	public static Scanner reader = new Scanner(System.in);
	public static int securityArray[] = new int[3];	
	public static int confidentialityActivateFlag = 0;
	public static int authenticationActivateFlag = 0;		
	FileInputStream secKeyStream = new FileInputStream("keyfile.txt");
	FileInputStream pubKeyStream = new FileInputStream("publickey.txt");	
	FileInputStream privKeyStream = new FileInputStream("privatekey.txt");		
	static String workingDirectory = System.getProperty("user.dir");
	static Path secKeyPath = Paths.get(workingDirectory+"/keyfile.txt");
	static Path pubKeyPath = Paths.get(workingDirectory+"/publickey.txt");
	static Path privKeyPath = Paths.get(workingDirectory+"/privatekey.txt");
	static byte[] secKeyBytes;
	static byte[] pubKeyBytes;
	static byte[] privKeyBytes;
	static SecretKey sKey;
	static PublicKey publicKey;
	static PrivateKey privateKey;
	static KeyFactory keyFactory;

    private static Socket socket;

    public Client() throws IOException {
        host = "localhost";
        port = 25000;
    }
	
	public static void setKeyData() throws Exception{
		secKeyBytes = Files.readAllBytes(secKeyPath);
		pubKeyBytes = Files.readAllBytes(pubKeyPath);
		privKeyBytes = Files.readAllBytes(privKeyPath);	
		publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyBytes));
		privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));
		sKey = new SecretKeySpec(secKeyBytes, 0, secKeyBytes.length, "AES");
	}
	
	//Send message to server
	
    public void sendMessage(String message) throws Exception {
        InetAddress address = InetAddress.getByName(host);
        socket = new Socket(address, port);
        OutputStream os = socket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);
		DataOutputStream dos = new DataOutputStream(os);
		
		if (securityArray[0] == 1 && confidentialityActivateFlag == 1){ //if encryption active
			if (debug == true) System.out.println("Encrypting message");
			byte[] encryptedMessage = Seclib.encryptMessage(message, sKey);
			dos.writeInt(encryptedMessage.length);
			dos.write(encryptedMessage);
			if (debug == true) System.out.println("Message sent to the server : " + new String(encryptedMessage));
		} else{
			if (debug == true) System.out.println("Not encrypting message");
				bw.write(message);
				if (debug == true) System.out.println("Message sent to the server : " + message);	
			}
			
		if(securityArray[2] == 1 && authenticationActivateFlag == 1){  //if authentication active
			if (debug == true) System.out.println("About to create signature");
			byte[] signatureToSend = Seclib.createSignature(privateKey, message);
			dos.writeInt(signatureToSend.length);
			dos.write(signatureToSend);
		}	
		
        bw.flush();
    }
	
	//Receive message from server

    public String getMessage() throws Exception {
		InputStream is = socket.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		DataInputStream dis = new DataInputStream(is);
		String message = "";

		if (securityArray[0] == 1 && confidentialityActivateFlag == 1){ //if encryption active
			int len = dis.readInt();
			byte[] data = new byte[len];
			dis.readFully(data);
			if (debug == true) System.out.println("Decrypting message");
			message = Seclib.decryptMessage(data, sKey);
		} else{
			message = br.readLine();
		}
		
		if (securityArray[2] == 1 && authenticationActivateFlag == 1){ //if authentication active
		
				int len = dis.readInt();
				byte[] data = new byte[len];
				dis.readFully(data);
				if (debug == true) System.out.println("Checking signature");
				boolean verification = Seclib.verifySignature(publicKey, data);
				if(verification == true){
					if (debug == true) System.out.println("Signature verified");
				} else{
					System.out.println("WARNING: Could not verify signature of incoming message");
				}
		}		
		
		return message;
	}
	
    public static void main(String args[])
    {
        try {
		
			setKeyData();
			securityStringClient = Seclib.initializeSecurityParameters(reader, securityArray);
            Boolean stop = false;
            Client client = new Client();
			int purgeFlag = 0;
		
            String securityAlert = "SecurityParametersIncoming \n";
            client.sendMessage(securityAlert);
			String securityAcknowledged = client.getMessage();
            if (debug == true) System.out.println("Message received from the server : " +securityAcknowledged);
			
			String securitySettings = securityStringClient+"\n";
			client.sendMessage(securitySettings);
			String securitySettingsAcknowledged = client.getMessage();
            if (debug == true) System.out.println("Message received from the server : " +securitySettingsAcknowledged);
			if(securitySettingsAcknowledged.equals("Security parameters of request are different than that of server, request denied. Relaunch Client.java to request new connection.")){
				System.out.println("Security parameters of request are different than that of server, request denied. Relaunch Client.java to request new connection.\n");
				try{
					socket.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			} else{
				if (debug == true) System.out.println(securitySettingsAcknowledged);
			}
			
			//Out of initial exchange, can now activate security features
			
			if (securityArray[0] == 1){
				confidentialityActivateFlag = 1;
			}
			
			if (securityArray[2] == 1){
				authenticationActivateFlag = 1;
			}			
			
            while (!stop) {
				
				if(purgeFlag == 0){
					reader.nextLine();
					purgeFlag = 1;
				}
				String hash = null;
                System.out.println("Message to server :\n");
                String message = reader.nextLine() + "\n";
				if (securityArray[1] == 1){  //if integrity check active
					if (debug == true) System.out.println("generating hash\n");
					hash = Seclib.messageHash(message);
					if (debug == true) System.out.println(hash);
					client.sendMessage(hash+"\n");
					String ack = client.getMessage();
					assert ack == "ack";
				}
                client.sendMessage(message);
				System.out.println("Waiting for response from server");
				if (securityArray[1] == 1){
					hash = client.getMessage();
					if (debug == true) System.out.println("Got hash: "+hash);
					client.sendMessage("ack\n");
				}
				
                String returnMessage = client.getMessage();
				if (securityArray[1] == 1){
					if (debug == true) System.out.println("comparing hash");
					assert hash == Seclib.messageHash(returnMessage);
					if (debug == true) System.out.println("success!");
				}
                System.out.println("Message received from the server : " + returnMessage);

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
			try {
				socket.close();
			} catch(Exception e) {
				e.printStackTrace();
			}		
			
            //Closing the socket

        }
    }
}
