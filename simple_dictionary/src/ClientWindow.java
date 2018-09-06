/**
 * Created by Student Name: Saumya Pandey.
 */
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

//GUI for the dictionary client window
public class ClientWindow extends JFrame {
    private JPanel rootPanel;
    private JPanel titlePanel;
    private JLabel titleTextLabel;
    private JTextField searchTextField;
    private JButton searchButton;
    private JButton addButton;
    private JButton removeButton;
    private JTextArea responseTextArea;

    public  ClientWindow(){

    }

    public ClientWindow(Socket socket) {
        searchButton.addActionListener(e -> {
            if (buttonActionSend(socket, "search")) {
                String receivedMeaning;
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    receivedMeaning = in.readLine();
                    StringTokenizer stringTokenizer = new StringTokenizer(receivedMeaning, ";");
                    responseTextArea.setText("");
                    if (receivedMeaning.equals("")) {
                        JOptionPane.showMessageDialog(null, "No Such Word Found.", "Error", JOptionPane.PLAIN_MESSAGE);
                    }
                    while (stringTokenizer.hasMoreTokens()) {
                        //one definition per line
                        responseTextArea.append(stringTokenizer.nextToken() + "\n");
                    }
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "Cannot connect to the server", "Error", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });

        addButton.addActionListener(e -> {
            if (buttonActionSend(socket, "add")){
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    String wordExist = in.readLine();

                    if (wordExist.equals("exist")){
                        JOptionPane.showMessageDialog(null,"The Word Is Already Exist.", "Error", JOptionPane.PLAIN_MESSAGE);
                    }else{
                        //pop out the definition window
                        JFrame frame = new JFrame("DefinitionWindow");
                        DefinitionWindow defWindow = new DefinitionWindow(socket, frame,searchTextField.getText());
                        frame.setContentPane(defWindow.getRootPanel());
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        frame.pack();
                        frame.setVisible(true);
                    }

                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null,"Cannot connect to the server", "Error", JOptionPane.PLAIN_MESSAGE);
                }
            }});


        removeButton.addActionListener(e -> {
            if (buttonActionSend(socket, "remove")){
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    //reply from server that whether the word exist
                    String wordExist = in.readLine();
                    if (wordExist.equals("exist")) {
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                        int i = JOptionPane.showConfirmDialog(null, "Are you sure that you want to delete the word from dictionary?",
                                "Confirm", JOptionPane.YES_NO_OPTION);
                        if (i == JOptionPane.YES_OPTION) {
                            //confirm message to server and pop out successful status
                            out.write("Y" + "\n");
                            out.flush();
                            JOptionPane.showMessageDialog(null,"Word has been removed from dictionary.", "Success",JOptionPane.PLAIN_MESSAGE);
                        } else {
                            out.write("N" + "\n");
                            out.flush();
                        }
                    }else{
                        JOptionPane.showMessageDialog(null,"The word is not exist", "Error", JOptionPane.PLAIN_MESSAGE);
                    }
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null,"Cannot connect to the server", "Error", JOptionPane.PLAIN_MESSAGE);
                }
            }});
    }

    //send request and request word to server for further usage
    private boolean buttonActionSend (Socket socket, String command){
        if (searchTextField.getText().equals("")){
            return false;
        }
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            out.write(command +"\n");
            out.write(searchTextField.getText() + "\n");
            out.flush();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"Cannot connect to the server", "Error", JOptionPane.PLAIN_MESSAGE);
            return false;
        }
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

}
