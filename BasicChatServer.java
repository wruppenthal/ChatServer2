/**
 * Created by WilliamRuppenthal on 1/15/15.
 */
import java.net.*;
import java.io.*;

public class BasicChatServer {
    public static void main(String [] args) {
        //Check port exists
//        if (args.length < 1) {
//            System.out.println("Usage: ChatServer <port>");
//            System.exit(1);
//        }

        //This is the server socket to accept connections
        ServerSocket serverSocket = null;

        //Create the server socket
        try {
            serverSocket = new ServerSocket(12345);
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            System.exit(1);
        }

        // In the main thread, continuously listen for new clients and spin off threads for them.
        while (true) {
            try {
                //Get a new client
                Socket clientSocket = serverSocket.accept();

                //Create a thread for it and start!
                ChatThread clientThread = new ChatThread(clientSocket);
                new Thread(clientThread).start();


            } catch (IOException e) {
                System.out.println("Accept failed: " + e);
                System.exit(1);
            }
            if(ChatThread.threads.size()==0)
                break;
        }
    }
}