package com.seng360;

/**
 * Created by Tal on 11/6/2017.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server
{
    int port;
    ServerSocket serverSocket;

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
            Scanner sc = new Scanner(System.in);

            //Server is running always. This is done using this while(true) loop
            while(true)
            {
                String message = server.checkInput();
                System.out.println("Message received from client is : "+message);
                System.out.println("your reply: \n");

                String returnMessage;
                returnMessage = sc.nextLine() + "\n";
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
