package Server;

import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

public class Server {

        private static ServerSocket serverSocket;
        private static Socket clientSocket;
        private static int maxClientsCount = 25;         // This chat server can accept up to maxClientsCount clients' connections.
        private static ClientThread[] threads = new ClientThread[maxClientsCount];
        private static Set<String> clients = new HashSet<>();




    public static void main(String args[]) {

        int portNumber = 2321;              // The default port number.

        try {
                serverSocket = new ServerSocket(portNumber);
            } catch (IOException e) {
                System.out.println(e);
            }

            while (true) {          //Create a client socket for each connection and pass it to a new client thread.
                try {
                    clientSocket = serverSocket.accept();
                    int i;
                    for (i = 0; i < maxClientsCount; i++) {
                        if (threads[i] == null) {
                            (threads[i] = new ClientThread(clientSocket, threads, clients)).start();
                            break;
                        }
                    }
                    if (i == maxClientsCount) {
                        PrintStream os = new PrintStream(clientSocket.getOutputStream());
                        os.println("Server too busy. Try later.");
                        os.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

