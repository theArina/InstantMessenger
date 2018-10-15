import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends JFrame {

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP;
    private Socket connection;

    public Client(String host) {
        super("Client mofo!");
        serverIP = host;
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
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(300, 150);
        setVisible(true);
    }

    public void startRunning() {
        try {
            connectToServer();
            setupStreams();
            whileChatting();
        } catch (EOFException e) {
            showMessage("\nClient terminated connection");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeCrap();
        }
    }

    private void connectToServer() throws IOException {
        showMessage("Attempting connection...\n");
        connection = new Socket(InetAddress.getByName(serverIP), 6589);
        showMessage("Connected to: " + connection.getInetAddress().getHostName());
    }

    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\nDude your streams are now good to go!\n");
    }

    private void whileChatting() {
        ableToType(true);
        do {
            try {
                message = (String)input.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException | IOException e) {
                showMessage("\nI dont know that object type");
            }
        } while (!message.equals("SERVER - END"));
    }

    private void closeCrap() {
        showMessage("\nClosing crap down...");
        ableToType(false);
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
            output.writeObject("CLIENT - " + message);
            output.flush();
            showMessage("\nCLIENT - " + message);
        } catch (IOException e) {
            chatWindow.append("\nSomething messed up sending message hoss!");
        }
    }

    private void showMessage(final String message) {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                    chatWindow.append(message);
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

        Client jeffery = new Client("127.0.0.1");
        jeffery.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jeffery.startRunning();
    }
}
