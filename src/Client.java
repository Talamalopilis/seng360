/**
 * Created by Tal on 11/6/2017.
 * Last modified 11/8/2017 by Chase
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

public class Client
{
    String host;
	
    int port;
	static String securityStringClient;
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

    public Client() throws IOException {
        host = "localhost";
        port = 25000;
    }

    public void sendMessage(String message) throws Exception {
        //Send the message to the server
        InetAddress address = InetAddress.getByName(host);
        socket = new Socket(address, port);
        OutputStream os = socket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);
		DataOutputStream dos = new DataOutputStream(os);
		
		if (securityArray[0] == 1 && confidentialityActivateFlag == 1){
			System.out.println("Encrypting message");
			byte[] encryptedMessage = Seclib.encryptMessage(message, sKey);
			dos.writeInt(encryptedMessage.length);
			dos.write(encryptedMessage);
			System.out.println("Message sent to the server : " + encryptedMessage);
		} else{
			System.out.println("Not encrypting message");
				bw.write(message);
				System.out.println("Message sent to the server : " + message);	
			}
		
	
        bw.flush();
    }

    public String getMessage() throws IOException {
        //Get the return message from the server
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String message = br.readLine();
        return message;
    }
	
	
	
	
    public static void main(String args[])
    {
        try
        {

			securityStringClient = Seclib.initializeSecurityParameters(reader, securityArray);
            Boolean stop = false;
            Client client = new Client();
			int purgeFlag = 0; //Used to activate the line purge at beginning of first communication
		
			
            String securityAlert = "SecurityParametersIncoming \n";
            client.sendMessage(securityAlert);
			String securityAcknowledged = client.getMessage();
            System.out.println("Message received from the server : " +securityAcknowledged);
			
			
			String securitySettings = securityStringClient+"\n";
			client.sendMessage(securitySettings);
			String securitySettingsAcknowledged = client.getMessage();
            System.out.println("Message received from the server : " +securitySettingsAcknowledged);
			if(securitySettingsAcknowledged.equals("Security parameters of request are different than that of server, request denied. Relaunch Client.java to request new connection.")){
			
				System.out.println("Closing socket because message received was "+securitySettingsAcknowledged);
				
				try
				{
					socket.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			} else{
				System.out.println(securitySettingsAcknowledged);
			}
			
			//Out of first two messages, can now activate cryptography/signature
			
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
				if (securityArray[1] == 1){
					System.out.println("generating hash\n");
					hash = Seclib.messageHash(message);
					System.out.println(hash);
					client.sendMessage(hash+"\n");
					String ack = client.getMessage();
					assert ack == "ack";
				}
                client.sendMessage(message);
				if (securityArray[1] == 1){
					hash = client.getMessage();
					System.out.println("Got hash: "+hash);
					client.sendMessage("ack\n");
				}
                String returnMessage = client.getMessage();
				if (securityArray[1] == 1){
					System.out.println("comparing hash");
					assert hash == Seclib.messageHash(returnMessage);
					System.out.println("success!");
				}
                System.out.println("Message received from the server : " + returnMessage);

            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
			try
			{
				socket.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}		
			
            //Closing the socket

        }
    }
}
