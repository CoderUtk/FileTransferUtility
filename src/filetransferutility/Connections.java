package filetransferutility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Connections extends FileTransferUI {

    public Connections() {
        try {
            get_connections();
        } catch (IOException | ParseException e) {
            System.err.println(e);
        }
    }
    String c_serverType;
    String c_selectedConnection;
    String c_connectionName;
    String c_username;
    String c_password = null;
    String c_passphrase = null;
    String c_keyFileLocation=null;
    String c_hostname;
    String c_port;
    String c_sourceFile;
    String c_destination;
    String[] connections;

    public void set_connections(String selectedConnection) throws FileNotFoundException, IOException, ParseException {
        c_selectedConnection = selectedConnection;
        json_object = (JSONObject) parser.parse(new FileReader("Connections.json"));
        JSONObject r_connection = (JSONObject) json_object.get(c_selectedConnection);
        c_serverType = (String) r_connection.get("ServerType");
        c_username = (String) r_connection.get("Username");
        c_hostname = (String) r_connection.get("HostName");
        c_port = (String) r_connection.get("Port");
        c_password = (String) r_connection.get("Password");
        c_passphrase = (String) r_connection.get("Passphrase");
        c_keyFileLocation = (String) r_connection.get("KeyFile");
    }

    public void get_connections() throws FileNotFoundException, IOException, ParseException {
        json_object = (JSONObject) parser.parse(new FileReader("Connections.json"));
        Object[] keys = json_object.keySet().toArray();
        connections = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            connections[i] = keys[i].toString();
        }
    }

    void add_connection(String serverType, String connectionName, String username, String host, String port, String passkey, String keyFile) throws IOException {
        File key_file = null;
        c_connectionName = connectionName;
        c_username = username;
        c_hostname = host;
        c_port = port;
        c_password = passkey;
        c_serverType = serverType;
        if (c_serverType.equalsIgnoreCase("Cloud")) {
            c_password = null;
            c_passphrase = passkey;
            c_keyFileLocation = keyFile;
            key_file = new File(c_keyFileLocation);
            FileUtils.copyFileToDirectory(key_file,new File("Keys//"));
            c_keyFileLocation = key_file.getName();
        }
        System.out.println(c_connectionName);
        JSONObject newConnection = new JSONObject();
        newConnection.put("ServerType", c_serverType);
        newConnection.put("Username", c_username);
        newConnection.put("HostName", c_hostname);
        newConnection.put("Port", c_port);
        newConnection.put("Password", c_password);
        newConnection.put("Passphrase", c_passphrase);
        newConnection.put("KeyFile", c_keyFileLocation);
        json_object.put(c_connectionName, newConnection);
        FileWriter writer = new FileWriter("Connections.json");
        writer.write(json_object.toString());
        writer.flush();
        writer.close();
    }

}
