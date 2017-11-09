/**
 * Created by Tal on 11/6/2017.
 * Last modified 11/8/2017 by Chase
 */
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import javax.crypto.*;
import java.security.*;
import java.util.*;
import java.lang.StringBuffer;

public class Client
{
    String host;
    int port;
	static String securityStringClient;
	public static Scanner reader = new Scanner(System.in);	

    private static Socket socket;

    public Client() throws IOException {
        host = "localhost";
        port = 25000;
    }

    public void sendMessage(String message) throws IOException {
        //Send the message to the server
        InetAddress address = InetAddress.getByName(host);
        socket = new Socket(address, port);
        OutputStream os = socket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);

        bw.write(message);
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
			int securityArray[] = new int[3];
			securityStringClient = Seclib.initializeSecurityParameters(reader, securityArray);
            Boolean stop = false;
            Client client = new Client();
			int purgeFlag = 0; //Used to activate the line purge at beginning of first communication
			
			//Problem start 
			
			
            String securityAlert = "SecurityParametersIncoming \n";
            client.sendMessage(securityAlert);
            System.out.println("Message sent to the server : "+securityAlert);
			String securityAcknowledged = client.getMessage();
            System.out.println("Message received from the server : " +securityAcknowledged);
			
			
			String securitySettings = securityStringClient+"\n";
			client.sendMessage(securitySettings);
			System.out.println("Message sent to the server : "+securitySettings);
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
				System.out.println("No need to close socket because message received was "+securitySettingsAcknowledged);
			}
			
			
			//Problem end
			
            while (!stop) {
				
				if(purgeFlag == 0){
					reader.nextLine();
					purgeFlag = 1;
				}

                System.out.println("Message to server :\n");
                String message = reader.nextLine() + "\n";
				if (securityArray[1] == 1){
					System.out.println("Integrity check\n");
					String hash = Seclib.messageHash(message);
				}
                client.sendMessage(message);
                System.out.println("Message sent to the server : " + message);

                String returnMessage = client.getMessage();
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
