/**
 * Created by Tal on 11/6/2017.
 * Last modified 11/8/2017 by Chase
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.DSAPrivateKeySpec;
import java.lang.StringBuffer;
import java.nio.file.*;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyFactory;

public class Server
{

	static boolean debug = false;
	
	String host;
    int port;
    ServerSocket serverSocket;
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
	
	public static void setKeyData() throws Exception{
		secKeyBytes = Files.readAllBytes(secKeyPath);
		pubKeyBytes = Files.readAllBytes(pubKeyPath);
		privKeyBytes = Files.readAllBytes(privKeyPath);	
		publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyBytes));
		privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));

		sKey = new SecretKeySpec(secKeyBytes, 0, secKeyBytes.length, "AES");
	}
	
    public Server()throws java.io.IOException{
		host = "localhost";
        port = 25000;
        serverSocket = new ServerSocket(port);
        System.out.println("Server Started and listening to the port 25000");
    }

    public String checkInput() throws Exception {
		System.out.println("checkInput() called from Server");
		
        //Reading the message from the client
        socket = serverSocket.accept();
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
		DataInputStream dis = new DataInputStream(is);
		
		String message = "";
		
	
		if (securityArray[0] == 1 && confidentialityActivateFlag == 1){
		
				int len = dis.readInt();
				byte[] data = new byte[len];
				dis.readFully(data);
				System.out.println("Decrypting message");
				message = Seclib.decryptMessage(data, sKey);
			
		} else{
			message = br.readLine();
		}
		
		if (securityArray[2] == 1 && authenticationActivateFlag == 1){ //sent signature now
		
				int len = dis.readInt();
				byte[] data = new byte[len];
				dis.readFully(data);
				System.out.println("Checking signature");
				boolean verification = Seclib.verifySignature(publicKey, data);
				if(verification == true){
					System.out.println("Signature verified");
				} else{
					System.out.println("WARNING: Could not verify signature of incoming message");
				}
		}
		        
		return message;
    }

    public void sendOutput(String message) throws Exception {
		OutputStream os = socket.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);
		BufferedWriter bw = new BufferedWriter(osw);
		DataOutputStream dos = new DataOutputStream(os);

		if (securityArray[0] == 1 && confidentialityActivateFlag == 1){
			System.out.println("Encrypting message");
			byte[] encryptedMessage = Seclib.encryptMessage(message, sKey);
			dos.writeInt(encryptedMessage.length);
			dos.write(encryptedMessage);
			System.out.println("Message sent to the client : " + new String(encryptedMessage));
		} else{
			System.out.println("Not encrypting message");
			bw.write(message);
			System.out.println("Message sent to the client : " + message);
		}
		
		if(securityArray[2] == 1 && authenticationActivateFlag == 1){
			System.out.println("About to create signature");
			byte[] signatureToSend = Seclib.createSignature(privateKey, message);
			System.out.println("About to send signatureToSend");
			dos.writeInt(signatureToSend.length);
			dos.write(signatureToSend);
		}		

		bw.flush();
    }
	

    public static void main(String[] args)
    {
        try
        {
			setKeyData();
            Server server = new Server();
			String securityStringServer = Seclib.initializeSecurityParameters(reader, securityArray);
			int securityInitializationFlag = 0;
			int clientRequestFlag = 0;
			int purgeFlag = 0; //Used to activate the line purge at beginning of first communication
			int rejectionFlag = 0;			
			
            while(securityInitializationFlag == 0)
            {
                String message = server.checkInput();
                System.out.println("Message received from client is : "+message);

                String returnMessage = "\n";
				
				if(clientRequestFlag == 1){
					if(securityStringServer.equals(message) && rejectionFlag == 0){
						returnMessage = "Security parameters accepted \n";
						securityInitializationFlag = 1;
					} else{
						
						returnMessage = "Security parameters of request are different than that of server, request denied. Relaunch Client.java to request new connection.\n";
						rejectionFlag = 1;
						server.sendOutput(returnMessage);
						System.out.println("Message sent to the client is : "+returnMessage);
						
					}
				}
				
				if(message.equals("SecurityParametersIncoming ")){
					returnMessage = "Request acknowledged \n";
					clientRequestFlag = 1;
					rejectionFlag = 0;
				}
				
				
                //make sure to end messages with \n or client will stall

                server.sendOutput(returnMessage);
                System.out.println("Message sent to the client is : "+returnMessage);
				
				
            }
			
			
			System.out.println("Now exiting the securityInitializationFlag loop");
			securityInitializationFlag = 0;
			clientRequestFlag = 0;	

			if (securityArray[0] == 1){
				confidentialityActivateFlag = 1;
			}
			
			if (securityArray[2] == 1){
				authenticationActivateFlag = 1;
			}				

            //Server is running always. This is done using this while(true) loop
            while(true)
            {
				if(purgeFlag == 0){
					reader.nextLine();
					purgeFlag = 1;
				}
				String hash= null;
				if (securityArray[1] == 1){
					hash = server.checkInput();
					System.out.println("Got hash: "+hash);
					server.sendOutput("ack\n");
				}
				String message = server.checkInput();
				if (securityArray[1] == 1){
					System.out.println("comparing hash");
					assert hash == Seclib.messageHash(message);
					System.out.println("success!");
				}
                System.out.println("Message received from client is : "+message);
                System.out.println("your reply: \n");

                String returnMessage;
                returnMessage = reader.nextLine() + "\n";
                //make sure to end messages with \n or client will stall
				if (securityArray[1] == 1){
					System.out.println("generating hash\n");
					hash = Seclib.messageHash(message);
					System.out.println(hash);
					server.sendOutput(hash+"\n");
					String ack = server.checkInput();
					assert ack == "ack";
				}
                server.sendOutput(returnMessage);


            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
    }
};
	static PrivateKey privateKey;
	static KeyFactory keyFactory;
	
	private static Socket socket;
	
	public static void setKeyData() throws Exception{
		secKeyBytes = Files.readAllBytes(secKeyPath);
		pubKeyBytes = Files.readAllBytes(pubKeyPath);
		privKeyBytes = Files.readAllBytes(privKeyPath);	
		publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyBytes));
		privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));

		sKey = new SecretKeySpec(secKeyBytes, 0, secKeyBytes.length, "AES");
	}
	
    public Server()throws java.io.IOException{
		host = "localhost";
        port = 25000;
        serverSocket = new ServerSocket(port);
        System.out.println("Server Started and listening to the port 25000");
    }

    public String checkInput() throws Exception {
		System.out.println("checkInput() called from Server");
		
        //Reading the message from the client
        socket = serverSocket.accept();
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
		DataInputStream dis = new DataInputStream(is);
		
		String message = "";
		
	
		if (securityArray[0] == 1 && confidentialityActivateFlag == 1){
		
				int len = dis.readInt();
				byte[] data = new byte[len];
				dis.readFully(data);
				System.out.println("Decrypting message");
				message = Seclib.decryptMessage(data, sKey);
			
		} else{
			message = br.readLine();
		}
		
		if (securityArray[2] == 1 && authenticationActivateFlag == 1){ //sent signature now
		
				int len = dis.readInt();
				byte[] data = new byte[len];
				dis.readFully(data);
				System.out.println("Checking signature");
				boolean verification = Seclib.verifySignature(publicKey, data);
				if(verification == true){
					System.out.println("Signature verified");
				} else{
					System.out.println("WARNING: Could not verify signature of incoming message");
				}
		}
		        
		return message;
    }

    public void sendOutput(String message) throws Exception {
		OutputStream os = socket.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);
		BufferedWriter bw = new BufferedWriter(osw);
		DataOutputStream dos = new DataOutputStream(os);

		if (securityArray[0] == 1 && confidentialityActivateFlag == 1){
			System.out.println("Encrypting message");
			byte[] encryptedMessage = Seclib.encryptMessage(message, sKey);
			dos.writeInt(encryptedMessage.length);
			dos.write(encryptedMessage);
			System.out.println("Message sent to the client : " + new String(encryptedMessage));
		} else{
			System.out.println("Not encrypting message");
			bw.write(message);
			System.out.println("Message sent to the client : " + message);
		}

		bw.flush();
    }
	

    public static void main(String[] args)
    {
        try
        {
			setKeyData();
            Server server = new Server();
			String securityStringServer = Seclib.initializeSecurityParameters(reader, securityArray);
			int securityInitializationFlag = 0;
			int clientRequestFlag = 0;
			int purgeFlag = 0; //Used to activate the line purge at beginning of first communication
			int rejectionFlag = 0;			
			
            while(securityInitializationFlag == 0)
            {
                String message = server.checkInput();
                System.out.println("Message received from client is : "+message);

                String returnMessage = "\n";
				
				if(clientRequestFlag == 1){
					if(securityStringServer.equals(message) && rejectionFlag == 0){
						returnMessage = "Security parameters accepted \n";
						securityInitializationFlag = 1;
					} else{
						
						returnMessage = "Security parameters of request are different than that of server, request denied. Relaunch Client.java to request new connection.\n";
						rejectionFlag = 1;
						server.sendOutput(returnMessage);
						System.out.println("Message sent to the client is : "+returnMessage);
						
					}
				}
				
				if(message.equals("SecurityParametersIncoming ")){
					returnMessage = "Request acknowledged \n";
					clientRequestFlag = 1;
					rejectionFlag = 0;
				}
				
				
                //make sure to end messages with \n or client will stall

                server.sendOutput(returnMessage);
                System.out.println("Message sent to the client is : "+returnMessage);
				
				
            }
			
			
			System.out.println("Now exiting the securityInitializationFlag loop");
			securityInitializationFlag = 0;
			clientRequestFlag = 0;	

			if (securityArray[0] == 1){
				confidentialityActivateFlag = 1;
			}
			
			if (securityArray[2] == 1){
				authenticationActivateFlag = 1;
			}				

            //Server is running always. This is done using this while(true) loop
            while(true)
            {
				if(purgeFlag == 0){
					reader.nextLine();
					purgeFlag = 1;
				}
				String hash= null;
				if (securityArray[1] == 1){
					hash = server.checkInput();
					System.out.println("Got hash: "+hash);
					server.sendOutput("ack\n");
				}
				String message = server.checkInput();
				if (securityArray[1] == 1){
					System.out.println("comparing hash");
					assert hash == Seclib.messageHash(message);
					System.out.println("success!");
				}
                System.out.println("Message received from client is : "+message);
                System.out.println("your reply: \n");

                String returnMessage;
                returnMessage = reader.nextLine() + "\n";
                //make sure to end messages with \n or client will stall
				if (securityArray[1] == 1){
					System.out.println("generating hash\n");
					hash = Seclib.messageHash(message);
					System.out.println(hash);
					server.sendOutput(hash+"\n");
					String ack = server.checkInput();
					assert ack == "ack";
				}
                server.sendOutput(returnMessage);


            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
    }
}
