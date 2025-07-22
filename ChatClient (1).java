import java.net.*;
import java.io.*;

public class ChatClient implements Runnable {
    private ChatClientThread client = null;
    private Socket socket = null;
    private BufferedReader console = null;
    private Thread thread = null;
    private PrintStream out = null;

    public ChatClient(String serverName, int serverPort) {
        System.out.println("Establishing connection, please wait...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            console = new BufferedReader(new InputStreamReader(System.in));  // Use BufferedReader for input
            out = new PrintStream(socket.getOutputStream());

            if (thread == null) {
                client = new ChatClientThread(this, socket); // Separate thread for client
                thread = new Thread(this);
                thread.start();
            }
        } catch (UnknownHostException e) {
            System.out.println("Host unknown: " + e.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    @Override
    public void run() {
        while (thread != null) {
            try {
                String message = console.readLine(); // Send message to the server
                out.println(message);
                out.flush();
            } catch (IOException e) {
                System.out.println("Sending error: " + e.getMessage());
                stop();
            }
        }
    }

    public void handle(String msg) {
        if (msg.equals("quit")) {
            System.out.println("Goodbye, press RETURN to exit...");
            stop();
        } else {
            System.out.println(msg); // Display message from server
        }
    }

    public void stop() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt(); // Use interrupt instead of stop() to stop the thread safely
            thread = null;
        }
        try {
            if (console != null) console.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
        if (client != null) {
            client.close();
            client.stop();
        }
    }

    public static void main(String[] args) {
        ChatClient client = null;
        if (args.length != 2) {
            System.out.println("Usage: java ChatClient <host> <port>");
        } else {
            client = new ChatClient(args[0], Integer.parseInt(args[1]));
        }
    }
}

