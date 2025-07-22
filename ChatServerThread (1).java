import java.net.*;
import java.io.*;

public class ChatServerThread extends Thread {
    private ChatServer server = null;
    private Socket socket = null;
    private DataInputStream in = null;
    private PrintStream out = null;
    private int ID = -1;
    private volatile boolean isRunning = true;

    public ChatServerThread(ChatServer server, Socket socket) {
        super();
        this.server = server;
        this.socket = socket;
        this.ID = socket.getPort();
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
            out.flush();
        }
    }

    public int getID() {
        return ID;
    }

    public void run() {
        System.out.println("Server thread " + ID + " running");
        while (isRunning) {
            try {
                String input = in.readLine();
                if (input != null) {
                    server.handle(ID, input);
                } else {
                    stopThread();
                }
            } catch (IOException e) {
                System.out.println(ID + " error reading: " + e.getMessage());
                server.remove(ID);
                stopThread();
            }
        }
    }

    public void open() throws IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new PrintStream(socket.getOutputStream());
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
        if (in != null) in.close();
        if (out != null) out.close();
    }

    public void stopThread() {
        isRunning = false;
        interrupt();
    }
}

