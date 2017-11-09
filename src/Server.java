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
import java.lang.StringBuffer;
import java.nio.file.*;

public class Server
{
    int port;
    ServerSocket serverSocket;
	public static Scanner reader = new Scanner(System.in);
	public static int securityArray[] = new int[3];
	public static int confidentialityActivateFlag = 0;
	public static int authenticationActivateFlag = 0;
	FileInputStream keyStream = new FileInputStream("keyfile.txt");
	String workingDirectory = System.getProperty("user.dir");
	Path path = Paths.get(workingDirectory+"/keyfile.txt");
	byte[] keyBytes = Files.readAllBytes(path);
	SecretKey sKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");	
	
	private static Socket socket;

    public Server()throws java.io.IOException{
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
		
	
		if (securityArray[0] == 1 && confidentialityActivateFlag == 1){
			System.out.println("Decrypting message");
			//String message = Seclib.decryptMessage();
			int len = dis.readInt();
			byte[] data = new byte[len];
			dis.readFully(data);
			String message = Seclib.decryptMessage(data, sKey);
			return message;
		} else{
			String message = br.readLine();
			return message;
		}
        
    }
	
	public void testEncryptionServerSide(String message){
		
		System.out.println("Calling encryption test");
		//byte[] encryptedMessage = Seclib.encryptMessage(message, sKey);		
	
	}

    public void sendOutput(String returnMessage) throws java.io.IOException{
        //Sending the response back to the client.
        OutputStream os = socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);		
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);
		
		if (securityArray[0] == 1 && confidentialityActivateFlag == 1){
			System.out.println("Encrypting message");			
		}else{
			System.out.println("Not encrypting message");
		}
        bw.write(returnMessage);
        bw.flush();
    }
	

    public static void main(String[] args)
    {
        try
        {
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
                System.out.println("Message sent to the client is : "+returnMessage);


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
