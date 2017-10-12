package Server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

class ClientThread extends Thread {

    private String clientName;
    private DataInputStream is;
    private PrintStream os;
    private Socket clientSocket;
    private final ClientThread[] threads;
    private int maxClientsCount;
    private Set<String> clients = new HashSet<>();

    public ClientThread(Socket clientSocket, ClientThread[] threads, Set<String> clients) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
        this.clients = clients;
    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        ClientThread[] threads = this.threads;
        Set<String> clients = this.clients;
        String name;
        try {

            is = new DataInputStream(clientSocket.getInputStream()); // Create input and output streams for this client.
            os = new PrintStream(clientSocket.getOutputStream());
            os.println("Enter your name.");
            name = is.readLine();

            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {       // Accept the new client
                        clientName = name;
                        clients.add(name);
                        os.println("J_OK");
                        break;
                    }
                }

                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) { //Print out the name, ip and port of the new client
                        threads[i].os.println("JOIN " + name + " " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
                    }
                }
            }

            //os.println("");
            while (true) {
                String line = is.readLine();
                if (line.startsWith("/quit")) {
                    break;
                }
                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i].clientName != null) {
                            threads[i].os.println("DATA " + name + ": " + line);
                        }
                    }
                }
            }
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        synchronized (this) {
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && threads[i] != this
                        && threads[i].clientName != null) {
                    threads[i].os.println("QUIT");
                    clients.remove(clientName);
                    threads[i].os.println("LIST " + clients);
                }
            }
        }

        synchronized (this) {                           // Set the current thread variable to null so that a new client could be accepted by the server.
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] == this) {
                    threads[i] = null;
                }
            }
        }

        try {
            is.close();                //Close the output stream, close the input stream, close the socket.
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        os.print("SERVER_CLOSE");
        os.close();
        try {
            clientSocket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}