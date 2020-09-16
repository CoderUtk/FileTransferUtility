package filetransferutility.main;

import java.io.File;

public class FTConstants {

    private FTConstants() {
    }

    public static final String TITLE = "File Transfer Utility";
    public static final String END_TITLE = "Closing";
    public static final String FXML_PATH = "resources/FileTransferUI.fxml";
    public static final String ICON_PATH = "resources/icon.png";
    public static final String TEMP_DIRECTORY = "temp" + File.pathSeparator;
    public static final String PASSWORD_BASED = "Password Based";
    public static final String KEY_BASED = "Key Based";
    public static final String CONNECTIONS_FILE = "Connections.json";
    public static final String SCRIPTS_PATH = "Scripts" + File.pathSeparator;
    public static final String HOME = "$HOME";
    public static final String KEYS_DIR = "Keys" + File.pathSeparator;
    public static final String FILE_CHOOSER_ICON = "resources/fileOpen.png";
    public static final String FOLDER_CHOOSER_ICON = "resources/folderOpen.png";
    public static final String UPLOAD = "Upload";
    public static final String DOWNLOAD = "Download";
    public static final String AUTH_TYPE = "AuthenticationType";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String PASSPHRASE = "Passphrase";
    public static final String PORT = "Port";
    public static final String HOST = "Hostname";
    public static final String KEY_FILE = "KeyFile";
    public static final String GREEN_COLOR = "#00cc99";
    public static final String RED_COLOR = "#ff0000";
    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String CHARSET = "UTF-8";
    public static final String SECRET_KEY = "coder_utk";
    public static final String SALT = "030920";
    public static final String SHA_512 = "PBKDF2WithHmacSHA512";
    public static final String AES_ENCRYPTION = "AES";
    public static final int ITERATION_COUNT = 40000;
    public static final  int KEY_LENGTH = 128;


}
