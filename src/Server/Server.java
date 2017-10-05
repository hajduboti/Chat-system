package Server;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server {

        private static ServerSocket serverSocket;
        private static Socket clientSocket;

        // This chat server can accept up to maxClientsCount clients' connections.
        private static int maxClientsCount = 25;
        private static clientThread[] threads = new clientThread[maxClientsCount];

    public static void main(String args[]) {

            // The default port number.
            int portNumber = 2321;

            try {
                serverSocket = new ServerSocket(portNumber);
            } catch (IOException e) {
                System.out.println(e);
            }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
            while (true) {
                try {
                    clientSocket = serverSocket.accept();
                    int i = 0;
                    for (i = 0; i < maxClientsCount; i++) {
                        if (threads[i] == null) {
                            (threads[i] = new clientThread(clientSocket, threads)).start();
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

    /*
     * The chat client thread. This client thread opens the input and the output
     * streams for a particular client, ask the client's name, informs all the
     * clients connected to the server about the fact that a new client has joined
     * the chat room, and as long as it receive data, echos that data back to all
     * other clients. The thread broadcast the incoming messages to all clients and
     * routes the private message to the particular client. When a client leaves the
     * chat room this thread informs also all the clients about that and terminates.
     */
    class clientThread extends Thread {

        private String clientName = null;
        private DataInputStream is = null;
        private PrintStream os = null;
        private Socket clientSocket = null;
        private final clientThread[] threads;
        private int maxClientsCount;



        public clientThread(Socket clientSocket, clientThread[] threads) {
            this.clientSocket = clientSocket;
            this.threads = threads;
            maxClientsCount = threads.length;
        }

        public void run() {
            int maxClientsCount = this.maxClientsCount;
            clientThread[] threads = this.threads;

            try {
      /*
       * Create input and output streams for this client.
       */
                is = new DataInputStream(clientSocket.getInputStream());
                os = new PrintStream(clientSocket.getOutputStream());
                String name;
                InetAddress thisIp = InetAddress.getLocalHost();
                while (true) {
                    os.println("Enter your name.");
                    name = is.readLine().trim();
                    if (name.indexOf('@') == -1) {
                        break;
                    } else {
                        os.println("The name should not contain '@' character.");
                    }
                }

      /* Welcome the new the client. */
                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i] == this) {
                            clientName = "@" + name;
                            os.println("J_OK");
                            break;
                        }
                        String nameOfClient = threads[i].clientName;
                        String IP = threads[i].clientSocket.getInetAddress().getHostName();
                        int port = threads[i].clientSocket.getPort();

                    }
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i] != this) {
                            threads[i].os.println("JOIN " + name + " " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
                        }
                    }
                }
                os.println("");
      /* Start the conversation. */
                while (true) {
                    String line = is.readLine();
                    if (line.startsWith("/quit")) {
                        break;
                    }
        /* If the message is private sent it to the given client. */
                    if (line.startsWith("@")) {
                        String[] words = line.split("\\s", 2);
                        if (words.length > 1 && words[1] != null) {
                            words[1] = words[1].trim();
                            if (!words[1].isEmpty()) {
                                synchronized (this) {
                                    for (int i = 0; i < maxClientsCount; i++) {
                                        if (threads[i] != null && threads[i] != this
                                                && threads[i].clientName != null
                                                && threads[i].clientName.equals(words[0])) {
                                            threads[i].os.println("DATA " + name + ": " + words[1]);
                    /*
                     * Echo this message to let the client know the private
                     * message was sent.
                     */
                                            this.os.println(">" + name + "> " + words[1]);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
          /* The message is public, broadcast it to all other clients. */
                        synchronized (this) {
                            for (int i = 0; i < maxClientsCount; i++) {
                                if (threads[i] != null && threads[i].clientName != null) {
                                    threads[i].os.println("DATA " + name + ": " + line);
                                }
                            }
                        }
                    }
                }
                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i] != this
                                && threads[i].clientName != null) {
                            threads[i].os.println("QUIT");

                        }
                    }
                }
                os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] == this) {
                            threads[i] = null;
                        }
                    }
                }
      /*
       * Close the output stream, close the input stream, close the socket.
       */
                is.close();
                os.close();
                clientSocket.close();
            } catch (IOException e) {
            }
        }
}
