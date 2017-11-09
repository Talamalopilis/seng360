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

public class Server
{
    int port;
    ServerSocket serverSocket;
	public static Scanner reader = new Scanner(System.in);

    private static Socket socket;

    public Server()throws java.io.IOException{
        port = 25000;
        serverSocket = new ServerSocket(port);
        System.out.println("Server Started and listening to the port 25000");
    }

    public String checkInput()throws java.io.IOException{
        //Reading the message from the client
        socket = serverSocket.accept();
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
		
        String message = br.readLine();
        return message;
    }

    public void sendOutput(String returnMessage) throws java.io.IOException{
        //Sending the response back to the client.
        OutputStream os = socket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(returnMessage);
        bw.flush();
    }
	

    public static void main(String[] args)
    {
        try
        {
            Server server = new Server();
			int securityArray[] = new int[3];
			String securityStringServer = Seclib.initializeSecurityParameters(reader, securityArray);
			int securityInitializationFlag = 0;
			int clientRequestFlag = 0;
			int purgeFlag = 0; //Used to activate the line purge at beginning of first communication
			int rejectionFlag = 0;
			int confidentialityActivateFlag = 0;
			int AuthenticationActivateFlag = 0;			
			
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
				AuthenticationActivateFlag = 1;
			}				

            //Server is running always. This is done using this while(true) loop
            while(true)
            {
				if(purgeFlag == 0){
					reader.nextLine();
					purgeFlag = 1;
				}
				System.out.println("Now entering the True loop");

                String message = server.checkInput();
                System.out.println("Message received from client is : "+message);
                System.out.println("your reply: \n");
                String returnMessage;
                returnMessage = reader.nextLine() + "\n";
                //make sure to end messages with \n or client will stall

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
})
    {
        try
        {
            Server server = new Server();
			int securityArray[] = new int[3];
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

            //Server is running always. This is done using this while(true) loop
            while(true)
            {
				if(purgeFlag == 0){
					reader.nextLine();
					purgeFlag = 1;
				}
				System.out.println("Now entering the True loop");

                String message = server.checkInput();
                System.out.println("Message received from client is : "+message);
                System.out.println("your reply: \n");
                String returnMessage;
                returnMessage = reader.nextLine() + "\n";
                //make sure to end messages with \n or client will stall

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
