/**
 * Created by Student Name: Saumya Pandey.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

//the GUI that can let user to add definition to the new word
public class DefinitionWindow extends JFrame{
    //the
    private JPanel rootPanel;
    private JLabel infoLabel;
    private JTextArea defTextArea;
    private JButton cancelButton;
    private JButton confirmButton;
    private JLabel notifLabel;

    //default constructor, never gonna use it since client need to connect to server first
    public DefinitionWindow(){
    }

    public DefinitionWindow(Socket socket, JFrame frame, String word){
        confirmButton.addActionListener(e -> {
            //confirm the definition is correct and give chance to modify
            int i = JOptionPane.showConfirmDialog(null, "Are you sure about your definition?",
                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
                        "UTF-8"));
                //confirm
                if (i==JOptionPane.YES_OPTION){
                    //write and send definition to server
                    if (defTextArea.getText().equals("")){
                        JOptionPane.showMessageDialog(null,
                                "Seems you did not type in anything!","Error",
                                JOptionPane.PLAIN_MESSAGE);
                        setVisible(false);
                    }else {
                        out.write("confirm\n");
                        out.write(word + "\n");
                        out.write(defTextArea.getText() + "\n");
                        out.flush();
                        setVisible(false);
                        frame.dispose();
                    }
                    //close definition window
                }else {
                    //need modify
                    setVisible(false);
                }
            }catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,"Fail to Add Definition ", "Error",
                        JOptionPane.PLAIN_MESSAGE);
            }
        });
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}
