import java.net.*;
import java.io.*;

public class ChatServer implements Runnable {
    private ChatServerThread[] clients = new ChatServerThread[50];
    private ServerSocket server = null;
    private Thread thread = null;
    private int clientCount = 0;
    private volatile boolean isRunning = true;

    public ChatServer(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait...");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);
            start();
        } catch (IOException e) {
            System.out.println("Cannot bind to port " + port + ": " + e.getMessage());
        }
    }

    public void run() {
        while (isRunning) {
            try {
                System.out.println("Waiting for a client...");
                addThread(server.accept());
            } catch (IOException e) {
                System.out.println("Server accept error: " + e.getMessage());
                stop();
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        isRunning = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing server: " + e.getMessage());
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void handle(int ID, String input) {
        if (input.equals("quit")) {
            clients[findClient(ID)].send("quit");
            remove(ID);
        } else {
            System.out.println(ID + ": " + input);
            for (int i = 0; i < clientCount; i++) {
                clients[i].send(ID + ": " + input);
            }
        }
    }

    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ChatServerThread closing = clients[pos];
            System.out.println("Removing client thread: " + ID + " at " + pos);
            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
            clientCount--;
            try {
                closing.close();
            } catch (IOException e) {
                System.out.println("Error closing thread: " + e.getMessage());
            }
            closing.stopThread();
        }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            System.out.println("Client accepted: " + socket);
            clients[clientCount] = new ChatServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            } catch (IOException e) {
                System.out.println("Error opening thread: " + e.getMessage());
            }
        } else {
            System.out.println("Client refused; maximum " + clients.length + " reached.");
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ChatServer <port>");
        } else {
            new ChatServer(Integer.parseInt(args[0]));
        }
    }
}
