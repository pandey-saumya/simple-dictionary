/**
 * Created by Student Name: Saumya Pandey.
 */
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class DictionaryServer {
    private JPanel rootPanel;
    private JButton shutdownButton;
    private JTextArea LogTextArea;
    private JLabel dictionaryLabel;
    private JLabel portLabel;

    public void setLogTextArea(String nextLine) {
        LogTextArea.append(nextLine);
    }

    public void exit(){
        //create server's log book
        PrintWriter serverLog = null;
        try{
            serverLog = new PrintWriter(new FileOutputStream("ServerLog.txt", true));
        }catch (FileNotFoundException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Cannot find the server log book",
                    "Error", JOptionPane.PLAIN_MESSAGE);
        }
        LogTextArea.append("Server shutdown " +
                new Timestamp(System.currentTimeMillis()) +"\n");
        serverLog.println(LogTextArea.getText());
        serverLog.close();
        System.exit(0);
    }

    public DictionaryServer() {
        shutdownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = JOptionPane.showConfirmDialog(null,
                        "Are you sure to shut down the server?", "Shutdown",JOptionPane.YES_NO_OPTION);
                if (i==JOptionPane.YES_OPTION) {
                    exit();
                }
            }
        });
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("ServerWindow");
        DictionaryServer server = new DictionaryServer();
        frame.setContentPane(server.rootPanel);
        DefaultCaret caret = (DefaultCaret)server.LogTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        //Object that will store the parsed command line arguments
        ServerCmdLineArgs argsBean = new ServerCmdLineArgs();
        //Parser provided by args4j
        CmdLineParser parser = new CmdLineParser(argsBean);
        try {
            //Parse the arguments
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            //Print the usage to help the user understand the arguments expected
            //by the program
            parser.printUsage(System.err);
        }

        ServerSocket listeningSocket = null;
        Socket clientSocket = null;
        //create socket at terminal's input port
        server.dictionaryLabel.setText("Dictionary data: "+argsBean.getFile());
        server.portLabel.setText("Port: "+argsBean.getPort());
        File source = new File(argsBean.getFile());
        if (!source.exists()){
            JOptionPane.showMessageDialog(null,
                    "Dictionary data not found, please check path and restart.\n", "Error",
                    JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
        try {
            listeningSocket = new ServerSocket(argsBean.getPort());
        //just to keep track of clients
            server.LogTextArea.append("Dictionary Server is up and running, waiting for connection. " +
                    new Timestamp(System.currentTimeMillis()) +"\n");
            int clientNum = 0;
            while (true) {
                clientSocket = listeningSocket.accept();
                clientNum++;
                server.LogTextArea.append("++++++++++++++++++++++++++++++++++++++++++++++++\n");
                server.LogTextArea.append("Client connection number " + clientNum + " accepted: " +
                        new Timestamp(System.currentTimeMillis()) +"\n");
                server.LogTextArea.append("Remote Port: " + clientSocket.getPort()+"\n");
                server.LogTextArea.append("Remote Hostname: " + clientSocket.getInetAddress().getHostName()+"\n");
                server.LogTextArea.append("Local Port: " + clientSocket.getLocalPort()+"\n");
                //create new thread for the client
                WorkerThread workerThread = new WorkerThread(clientSocket, argsBean.getFile(),clientNum,server);
                workerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            server.LogTextArea.append("Failure create socket on port "+argsBean.getPort()+
                    new Timestamp(System.currentTimeMillis()) +"\n");
        } finally {
            if (listeningSocket != null) {
                try {
                    listeningSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

