import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;

    public Server() {
        super("Buckys Instant Messenger");
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage(e.getActionCommand());
                    userText.setText("");
                }
            }
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(300, 150);
        setVisible(true);
    }

    public void startRunning() {
        try {
            server = new ServerSocket(6589, 100);
            while (true) {
                try {
                    waitForConnection();
                    setupStreams();
                    whileChatting();
                } catch (EOFException e) {
                    showMessage("\nServer ended the connection!");
                } finally {
                    closeCrap();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForConnection() throws IOException {
        showMessage("Waiting for someone to connect...\n");
        connection = server.accept();
        showMessage("Now connected to " + connection.getInetAddress().getHostName());
    }

    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\nStreams are now setup!");
    }

    private void whileChatting() {
        String message = "You are now connected!";
        sendMessage(message);
        ableToType(true);
        do {
            try {
                message = (String)input.readObject();
                showMessage("\n" + message);
            } catch (IOException | ClassNotFoundException e) {
                showMessage("\nidk wtf that user send!");
            }
        } while (!message.equals("CLIENT - END"));
    }

    private void closeCrap() {
        showMessage("\nClosing connections...\n");
        ableToType(true);
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {
            output.writeObject("SERVER - " + message);
            output.flush();
            showMessage("\nSERVER - " + message);
        } catch (IOException e) {
            chatWindow.append("\nERROR: DUDE I CANT SEND THAT MESSAGE");
        }
    }

    private void showMessage(final String text) {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    chatWindow.append(text);
                }
            }
        );
    }

    private void ableToType(final boolean tof) {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    userText.setEditable(tof);
                }
            }
        );
    }

    public static void main(String[] args) {
        Server tom = new Server();
        tom.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tom.startRunning();
    }
}
