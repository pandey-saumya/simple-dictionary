/**
 * Created by Student Name: Saumya Pandey.
 */


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.StringTokenizer;

public class WorkerThread extends Thread {

	private Socket clientSocket;
	private int clientNum;
	private DictionaryServer server;
	private String dictionaryPath;

	//thread constructor
	public WorkerThread(Socket clientSocket, String path, int clientNum, DictionaryServer server) {
		this.clientSocket = clientSocket;
		this.clientNum = clientNum;
		this.server = server;
		this.dictionaryPath= path;
	}
	
	@Override
	public void run() {
        try {
		//Get the input/output streams for reading/writing data from/to the socket
		    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
		    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            //the first input stream is always the command (add,search,remove)
		    String clientCommand = null;
		    //thread keep running forever unless client close their window
		    while((clientCommand=in.readLine())!=null) {

		        //for maintenance propose and keep track clients' requests
                server.setLogTextArea("***********************************\n");
		        server.setLogTextArea("Message from client " + clientNum + ": " + clientCommand + " "+
                        new Timestamp(System.currentTimeMillis()) +"\n");

                switch (clientCommand) {
                    case "search": {
                        String word = in.readLine();
                         server.setLogTextArea("client "+ clientNum+" search for " + "\""+word+"\""+"\n");
                        //search whether word exist in dictionary.xml
                        if (search(word)) {
                            //send back the definition to client
                            searchAction(out, word);
                        }else{
                            out.write("\n");
                            out.flush();
                        }
                        break;
                    }
                    case "add": {
                        String word = in.readLine();
                        server.setLogTextArea("client "+ clientNum+" wants to add " + "\""+ word+"\""+"\n");
                        //search whether word exist in dictionary.xml
                        if (search(word)) {
                            out.write("exist" + "\n");
                            out.flush();
                            server.setLogTextArea("     the word already exists.\n");
                        } else {
                            out.write("null\n");
                            out.flush();
                            server.setLogTextArea("     the word is not recorded.\n");
                        }
                        break;
                    }
                    case "confirm": {
                        String word = in.readLine();
                            addAction(in, word);
                            server.setLogTextArea(word + " added\n");
                        break;
                    }
                    case "remove": {
                        String word = in.readLine();
                        server.setLogTextArea("client "+ clientNum+" wants to remove " + "\""+word+"\""+"\n");
                        if (search(word)) {
                            out.write("exist" + "\n");
                            out.flush();
                            server.setLogTextArea("     the word exists.\n");
                            //waiting confirm to remove the word
                            String confirm = in.readLine();
                            if (confirm.equals("Y")) {
                                removeAction(word);
                                server.setLogTextArea(word + " removed from dictionary.\n");
                            }
                        }else{
                            out.write("null\n");
                            out.flush();
                            server.setLogTextArea("     the word is not recorded.\n");
                        }
                        break;
                    }
                }
                server.setLogTextArea("client "+ clientNum+"'s response sent\n");
            }
        } catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(clientSocket != null) {
				try {
					clientSocket.close();
					server.setLogTextArea("client "+ clientNum+ " disconnected " +
                            new Timestamp(System.currentTimeMillis()) +"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

    private synchronized void removeAction(String word) {
	    //read dictionary file and make node list for words
	    Document document = readDocument();
        NodeList list = document.getElementsByTagName("word");
        //search and remove the target element
        for (int i = 0; i < list.getLength(); i++) {

            Node node = list.item(i);
            Element e = (Element) node;

            NodeList nodeList = e.getElementsByTagName("name");
            String removeWord = nodeList.item(0).getChildNodes().item(0)
                    .getNodeValue();
            //target found
            if (removeWord.equals(word)) {
                node.getParentNode().removeChild(node);
            }
        }
        //save the update
        saveDocument(document);
    }

    private synchronized Document readDocument() {
	    File dictionaryXml = null;
        try {
            dictionaryXml = new File(dictionaryPath);
            if (!dictionaryXml.exists()){
                throw new Exception();
            }
        }catch (Exception e){
            server.setLogTextArea("Dictionary Source File not found at "+dictionaryPath+"\n");
        }
        server.setLogTextArea("read\n");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try{
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(dictionaryXml);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return document;
    }

    private synchronized void saveDocument(Document document) {
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Transformer transformer = factory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(dictionaryPath));
            transformer.transform(domSource, streamResult);
            server.setLogTextArea("Dictionary Updated\n");
        } catch (TransformerException e) {
            e.printStackTrace();
            server.setLogTextArea("Fail to update dictionary\n");
        }
    }

    private synchronized boolean search(String word) {
        Document document = readDocument();
        NodeList list = document.getElementsByTagName("word");
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String dictionaryWord = element.getElementsByTagName("name").item(0).getTextContent();
                if (dictionaryWord.equalsIgnoreCase(word)) {
                    return true;
                } else if (i == list.getLength() - 1) {
                    return false;
                }
            }
        }
        return true;
	}

    private synchronized void addAction(BufferedReader in, String newWordName) {
        Document document = readDocument();
        String definition = null;
        try {
            definition = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Element dict = document.getDocumentElement();
        Element word = document.createElement("word");
        Element name = document.createElement("name");
        Element def = document.createElement("definition");

        name.appendChild(document.createTextNode(newWordName));
        def.appendChild(document.createTextNode(definition));

        dict.appendChild(word);
        word.appendChild(name);
        word.appendChild(def);

        saveDocument(document);
        server.setLogTextArea("client "+ clientNum+" add "+newWordName+" with definition:\n");
        StringTokenizer tokenizer = new StringTokenizer(definition,";");
        while (tokenizer.hasMoreTokens()){
            server.setLogTextArea(tokenizer.nextToken()+"\n");
        }
    }


    private synchronized void searchAction(BufferedWriter out, String word) {
        Document document = readDocument();
        NodeList list = document.getElementsByTagName("word");
        for (int i =0;i<list.getLength();i++){
            Node node = list.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE){
                Element element = (Element) node;
                String dictionaryWord = element.getElementsByTagName("name").item(0).getTextContent();
                String definition = element.getElementsByTagName("definition").item(0).getTextContent();
                try {
                    if (dictionaryWord.equalsIgnoreCase(word)) {
                        out.write(definition + "\n");
                        out.flush();
                        server.setLogTextArea(definition+"\n");
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    server.setLogTextArea("Cannot write in BufferedWriter\n");
                }
            }
        }
    }
}
