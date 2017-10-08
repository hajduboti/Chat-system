package Server;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

    // The client socket

    private static Socket clientSocket;
    private static PrintStream os;
    private static DataInputStream is = null;
    private static BufferedReader inputLine;
    private static boolean closed;

    public static void main(String[] args) {

        int portNumber = 2321; // The default port.
        String host = "localhost";  // The default IP

        try {
            clientSocket = new Socket(host, portNumber);       // Open a socket on a given IP and port. Open input and output streams.
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());

        } catch (UnknownHostException e) {
            System.err.println("Don't know about IP " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host " + host);
        }

        if (clientSocket != null && os != null && is != null) {
            try {

                new Thread(new Client()).start();  // Create a thread to read from the server.
                while (!closed) {
                    os.println(inputLine.readLine());
                }
                os.close();         //Close the output stream, close the input stream, close the socket.
                is.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }

    public void run() {

        //Keep on reading from the socket till we receive "SERVER_CLOSE" from the server. Once we received that then we want to break.
        String responseLine;
        try {
            while ((responseLine = is.readLine()) != null) {
                System.out.println(responseLine);
                if (responseLine.indexOf("SERVER_CLOSE") != -1)
                    break;
            }
            closed = true;
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }

}