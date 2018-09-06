/**
 * Created by Student Name: Saumya Pandey.
 */
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

public class DictionaryClient {

    public static void main(String[] args) {
        Socket socket;
        ClientCmdLineArgs argsBean = new ClientCmdLineArgs();
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

        try {
            //connect to the server's port
            socket = new Socket(argsBean.getHost(), argsBean.getPort());
            //display dictionary client window
            JFrame frame = new JFrame("ClientWindow");
            ClientWindow window = new ClientWindow(socket);

            frame.setContentPane(window.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
            //error handle for connection failure
            JOptionPane.showMessageDialog(null,"Fail to connect to the server.","Error", JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }

    }
}
