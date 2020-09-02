package filetransferutility.client;

import filetransferutility.main.FTConstants;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Connections {
    String authType;
    String username;
    String password = null;
    String passphrase = null;
    String keyFileLocation = null;
    String hostname;
    String port;

    private JSONParser parser = new JSONParser();
    private JSONObject jsonObj;
    private String[] connectionList;

    public Connections() {
        try {
            getConnections();
        } catch (IOException | ParseException e) {
            System.err.println(e);
        }
    }

    public void setConnections(String selectedConnection) throws IOException, ParseException {
        jsonObj = (JSONObject) parser.parse(new FileReader(FTConstants.CONNECTIONS_FILE));
        JSONObject connectionJSONObj = (JSONObject) jsonObj.get(selectedConnection);
        authType = (String) connectionJSONObj.get(FTConstants.AUTH_TYPE);
        username = (String) connectionJSONObj.get(FTConstants.USERNAME);
        hostname = (String) connectionJSONObj.get(FTConstants.HOST);
        port = (String) connectionJSONObj.get(FTConstants.PORT);
        password = (String) connectionJSONObj.get(FTConstants.PASSWORD);
        passphrase = (String) connectionJSONObj.get(FTConstants.PASSPHRASE);
        keyFileLocation = (String) connectionJSONObj.get(FTConstants.KEY_FILE);
    }

    public void getConnections() throws IOException, ParseException {
        jsonObj = (JSONObject) parser.parse(new FileReader(FTConstants.CONNECTIONS_FILE));
        Object[] keys = jsonObj.keySet().toArray();
        connectionList = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            connectionList[i] = keys[i].toString();
        }
    }

    public void addConnection(String serverType, String connectionName, String username, String host, String port, String passkey, String keyFileName) throws IOException {
        this.username = username;
        hostname = host;
        this.port = port;
        password = passkey;
        authType = serverType;
        if (authType.equalsIgnoreCase(FTConstants.KEY_BASED)) {
            password = null;
            passphrase = passkey;
            keyFileLocation = keyFileName;
            File keyFile = new File(keyFileLocation);
            FileUtils.copyFileToDirectory(keyFile, new File(FTConstants.KEYS_DIR));
            keyFileLocation = keyFile.getName();
        }
        System.out.println(connectionName);
        JSONObject newConnection = new JSONObject();
        newConnection.put(FTConstants.AUTH_TYPE, authType);
        newConnection.put(FTConstants.USERNAME, this.username);
        newConnection.put(FTConstants.HOST, hostname);
        newConnection.put(FTConstants.PORT, this.port);
        newConnection.put(FTConstants.PASSWORD, password);
        newConnection.put(FTConstants.PASSPHRASE, passphrase);
        newConnection.put(FTConstants.KEY_FILE, keyFileLocation);
        jsonObj.put(connectionName, newConnection);
        try (FileWriter writer = new FileWriter(FTConstants.CONNECTIONS_FILE)) {
            writer.write(jsonObj.toString());
            writer.flush();
        }
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String[] getConnectionList() {
        return connectionList;
    }

    public void setConnectionList(String[] connectionList) {
        this.connectionList = connectionList;
    }


}
