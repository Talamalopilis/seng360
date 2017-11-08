package com.seng360;

/**
 * Created by Tal on 11/6/2017.
 */
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    String host;
    int port;

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
            Boolean stop = false;
            Client client = new Client();
            Scanner sc = new Scanner(System.in);
            while (!stop) {
                System.out.println("Message to server :\n");
                String message = sc.nextLine() + "\n";
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
            //Closing the socket
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}