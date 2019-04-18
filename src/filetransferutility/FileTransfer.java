package filetransferutility;

import Utils.ZipUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import org.apache.commons.io.FileUtils;

public class FileTransfer extends Connections {

    public com.jcraft.jsch.Session session;
    public Channel channel;
    public int file_size;
    public boolean upload_complete;
    public boolean download_complete;
    //ProgressBar progress_bar;
    String outputFolderPath = "Output/";
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
    private BiConsumer<Double, Double> progressUpdate;

    public FileTransfer() {
    }

    public double getProgress() {
        return progressProperty().get();
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }

    public void setProgressUpdate(BiConsumer<Double, Double> progressUpdate) {
        this.progressUpdate = progressUpdate;
        //progressUpdate.accept(file_size, file_size);
    }

    public void connect() throws JSchException {
        JSch jsch = new JSch();
        if (c_serverType.equalsIgnoreCase("Cloud")) {
            jsch.addIdentity("Keys\\" + c_keyFileLocation);
        }
        int portno = Integer.parseInt(c_port);
        session = jsch.getSession(c_username, c_hostname, portno);
        UserInfo ui = new MyUserInfo();
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setUserInfo(ui);
        session.connect();
    }

    public class MyUserInfo implements UserInfo {

        @Override
        public String getPassword() {
            return c_password;
        }

        @Override
        public boolean promptYesNo(String str) {
            str = "Yes";
            return true;
        }

        @Override
        public String getPassphrase() {
            return c_passphrase;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return true;
        }

        @Override
        public void showMessage(String message) {
        }

        @Override
        public boolean promptPassword(String string) {
            return true;
        }
    }

    void upload(String Source, String Destination) throws IOException, JSchException {
        System.out.println("Uploading " + Source + "\nto " + Destination + "\n");
        File f = new File(Source);
        if (f.isDirectory()) {
            uploadFolderToServer(f, Source, Destination);
        } else {
            uploadFileToServer(Source, Destination);
        }
    }

    void uploadFileToServer(String Source, String Destination) {
        try {
            System.out.println("From 1: " + Thread.currentThread());
            Boolean new_upload = true;
            upload_complete = false;
            int put_status_mode = ChannelSftp.OVERWRITE;
            File f = new File(Source);
            ChannelSftp channelSftp = null;
            SftpATTRS attrs = null;
            //channelSftp.
            while (!upload_complete) {
                file_size = 0;
                try {
                    channel = session.openChannel("sftp");
                    if (!channel.isConnected()) {
                        channel.connect();
                    }
                    channelSftp = (ChannelSftp) channel;
                    if (Destination.contains("$HOME")) {
                        Destination = Destination.replace("$HOME", channelSftp.getHome());
                    }
                    attrs = channelSftp.lstat(Destination);
                    if (attrs == null) {
                        channelSftp.mkdir(Destination);
                    }
                    channelSftp.cd(Destination);
                    try {
                        attrs = channelSftp.lstat(Destination + "/" + f.getName());
                        file_size = new_upload ? 0 : (int) attrs.getSize();
                    } catch (SftpException ex) {
                    }
                    //total_progress = (float) file_size / f.length();
                    System.out.println("From 2: " + Thread.currentThread());
                    channelSftp.put(new FileInputStream(f), f.getName(), new SftpProgressMonitor() {
                        long uploadedBytes;

                        @Override
                        public void init(int i, String Source, String Destination, long bytes) {
                            uploadedBytes = file_size;
                        }

                        @Override
                        public boolean count(long bytes) {
                            uploadedBytes += bytes;
                            if (progressUpdate != null) {
                                progress.set((double) uploadedBytes / (double) f.length());
                                progressUpdate.accept(progress.getValue() * 100.00, 100.00);
                            }
                            return (true);
                        }

                        @Override
                        public void end() {
                        }
                    }, put_status_mode);
                } catch (JSchException | SftpException ex) {
                    if (ex.toString().contains("session is down") || ex.toString().contains("socket write error”")) {
                        new_upload = false;
                        put_status_mode = ChannelSftp.RESUME;
                        reconnect();
                    }
                }
                System.out.println(progress.getValue());
                if (file_size == f.length() || progress.getValue() == 1.0) {
                    upload_complete = true;
                    progressUpdate.accept(100.00, 100.00);
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        System.out.println("From 3: " + Thread.currentThread());
        Thread.yield();
        System.out.println("From 4: " + Thread.currentThread());
    }

    void uploadFolderToServer(File f, String Source, String Destination) throws IOException, JSchException {
        String destinationFolderName = f.getName();
        FileUtils.copyDirectory(FileUtils.getFile(Source), FileUtils.getFile("temp/" + f.getName()));
        ZipUtils appZip = new ZipUtils();
        appZip.zipFolder("temp/" + destinationFolderName, "temp/" + destinationFolderName + ".zip");
        uploadFileToServer("temp/" + destinationFolderName + ".zip", Destination);
        unzipDestinationFolder(Destination, destinationFolderName);
        File index = new File("temp/" + destinationFolderName);
        String[] entries = index.list();
        for (String s : entries) {
            File currentFile = new File(index.getPath(), s);
            currentFile.delete();
        }
        File zipFile = new File("temp/" + destinationFolderName + ".zip");
        zipFile.delete();
    }

    void unzipDestinationFolder(String destination, String folderName) throws IOException, JSchException {
        String command = "";
        File unzipScript = new File("Scripts/" + "Unzip.sh");
        String inputStr = "";
        try (BufferedReader br = new BufferedReader(new FileReader(unzipScript))) {
            String line;
            while ((line = br.readLine()) != null) {
                inputStr += line + System.lineSeparator();
            }
            String destinationZip = folderName + ".zip";
            inputStr = inputStr.replaceFirst("[^#*]destination=\"\\w*\"", "\ndestination=\"" + destination + "\"");
            inputStr = inputStr.replaceFirst("[^#*]destinationZip=\"\\w*\"", "\ndestinationZip=\"" + destinationZip + "\"");
            inputStr = inputStr.replaceAll("\r\n", "\n");
            FileOutputStream fileOut = new FileOutputStream(unzipScript);
            fileOut.write(inputStr.getBytes());
            fileOut.close();
            executeScript("Unzip.sh");
        }
    }

    void download(String Source, String Destination) throws SftpException, JSchException, IOException {
        System.out.println("Downloading " + Source + " to " + Destination);
        try {
            channel = session.openChannel("sftp");
            if (!channel.isConnected()) {
                channel.connect();
            }
            ChannelSftp channelSftp = (ChannelSftp) channel;
            if (Source.contains("$HOME")) {
                Source = Source.replace("$HOME", channelSftp.getHome());
            }
            SftpATTRS attrs = channelSftp.lstat(Source);
            if (attrs == null) {
                System.out.println("Given file / folder  not present at the mentioned path");
                return;
            }
            if (attrs.isDir()) {
                downloadFolderFromServer(Source, Destination, channelSftp, attrs);
            } else {
                String sourceFileName = getSourceFileName(Source, channelSftp);
                downloadFileFromServer(Source, Destination, channelSftp, attrs, sourceFileName);
            }
        } catch (SftpException ex) {
            System.err.println(ex.toString());
        }
    }

    void downloadFileFromServer(String Source, String Destination, ChannelSftp channelSftp, SftpATTRS attrs, String sourceFileName) throws SftpException {
        Boolean new_download = true;
        download_complete = false;
        int get_status_mode = ChannelSftp.OVERWRITE;
        long sourceFileSize = attrs.getSize();
        File destFile = new File(Destination + "/" + sourceFileName);
        while (!download_complete) {
            file_size = 0;
            try {
                channel = session.openChannel("sftp");
                if (!channel.isConnected()) {
                    channel.connect();
                }
                file_size = new_download ? 0 : (int) destFile.length();
                channelSftp.get(Source, Destination, new SftpProgressMonitor() {
                    long downloadedBytes;

                    @Override
                    public void init(int i, String string, String string1, long l) {
                        downloadedBytes = file_size;
                    }

                    @Override
                    public boolean count(long bytes) {
                        downloadedBytes += bytes;
                        if (progressUpdate != null) {
                            progress.set((double) downloadedBytes / (double) sourceFileSize);
                            progressUpdate.accept(progress.getValue() * 100.00, 100.00);
                        }
                        return (true);
                    }

                    @Override
                    public void end() {
                    }
                }, get_status_mode);
            } catch (JSchException | SftpException ex) {
                if (ex.toString().contains("session is down") || ex.toString().contains("socket write error”")) {
                    new_download = false;
                    get_status_mode = ChannelSftp.RESUME;
                    reconnect();
                }
            }
            if (file_size == sourceFileSize || (int) getProgress() == 100) {
                download_complete = true;
                progressUpdate.accept(100.00, 100.00);
            }
        }
    }

    void downloadFolderFromServer(String Source, String Destination, ChannelSftp channelSftp, SftpATTRS attrs) throws IOException, SftpException {
        executeFolderDownloadScript(Source);
        Source += ".zip";
        try {
            attrs = channelSftp.lstat(Source);
        } catch (SftpException ex) {
            ex.toString();
        }
        String sourceFileName = getSourceFileName(Source, channelSftp);
        downloadFileFromServer(Source, Destination, channelSftp, attrs, sourceFileName);
        ZipUtils appzip = new ZipUtils();
        appzip.unZipIt(Destination + "/" + sourceFileName, Destination);
        new File(Destination + "/" + sourceFileName).delete();
    }

    public void executeFolderDownloadScript(String Source) throws FileNotFoundException, IOException {
        String command = "";
        File zipScript = new File("Scripts/" + "zipFolder.sh");
        String inputStr = "";
        try (BufferedReader br = new BufferedReader(new FileReader(zipScript))) {
            String line;
            while ((line = br.readLine()) != null) {
                inputStr += line + System.lineSeparator();
            }

            inputStr = inputStr.replaceFirst("[^#*]source=\"\\w*\"", "\nsource=\"" + Source + "\"");
            inputStr = inputStr.replaceAll("\r\n", "\n");
            try (FileOutputStream fileOut = new FileOutputStream(zipScript)) {
                fileOut.write(inputStr.getBytes());
            }
            executeScript("zipFolder.sh");
        }
    }

    public Boolean executeScript(String ScriptName) {
        try {
            String script_output = "";
            String command = "";
            File scriptFile = new File("Scripts/" + ScriptName);
            try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    command += line + "\n";
                }
            }
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            channel.connect();
            InputStream in = channel.getInputStream();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    script_output = new String(tmp, 0, i);
                    System.out.println(script_output);
                }
                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ee) {
                }
            }

            channel.disconnect();

        } catch (JSchException | IOException e) {
            System.out.println(e);
        }
        return true;

    }

    String getSourceFileName(String Source, ChannelSftp channelSftp) throws SftpException {
        Vector<ChannelSftp.LsEntry> sourceFileObject = channelSftp.ls(Source);
        String sourceFileName = sourceFileObject.get(0).getFilename();
        return sourceFileName;
    }

    public void reconnect() {
        while (!channel.isConnected()) {
            try {
                System.out.println("Reconnecting..");
                connect();
                channel = session.openChannel("sftp");
                channel.connect();
            } catch (JSchException e) {
                System.out.println(e);
            }
        }
    }

    void removeFileFromServer(String filePath, ChannelSftp channelSftp) throws SftpException {
        channelSftp.rm(filePath);
    }
}
