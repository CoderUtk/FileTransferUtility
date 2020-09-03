package filetransferutility.client;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;
import filetransferutility.main.Messages;
import filetransferutility.utils.Utils;
import filetransferutility.utils.ZipUtils;
import filetransferutility.main.FTConstants;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class FileTransfer extends Connections {

    private com.jcraft.jsch.Session session;
    private Channel channel;
    private int fileSize;
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
    private BiConsumer<Double, Double> progressUpdate;

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
        if (authType.equalsIgnoreCase(FTConstants.KEY_BASED)) {
            jsch.addIdentity("Keys\\" + keyFileLocation);
        }
        int portno = Integer.parseInt(port);
        session = jsch.getSession(username, hostname, portno);
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
            return password;
        }

        @Override
        public boolean promptYesNo(String str) {
            return true;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
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

    public void upload(String source, String destination) throws IOException {
        System.out.println("Uploading " + source + "\nto " + destination + "\n");
        File f = new File(source);
        if (f.isDirectory()) {
            uploadFolderToServer(f, source, destination);
        } else {
            uploadFileToServer(source, destination);
        }
    }

    public void uploadFileToServer(String source, String destination) {
        boolean isUploadComplete = false;
        System.out.println("From 1: " + Thread.currentThread());
        Boolean isUploadNew = true;
        isUploadComplete = false;
        int putStatusMode = ChannelSftp.OVERWRITE;
        File sourceFile = new File(source);
        ChannelSftp channelSftp = null;
        SftpATTRS attrs = null;
        //channelSftp.
        while (!isUploadComplete) {
            fileSize = 0;
            try {
                channel = session.openChannel("sftp");
                if (!channel.isConnected()) {
                    channel.connect();
                }
                channelSftp = (ChannelSftp) channel;
                destination = destination.replace(FTConstants.HOME, channelSftp.getHome());
                attrs = channelSftp.lstat(destination);
                if (attrs == null) {
                    channelSftp.mkdir(destination);
                }
                channelSftp.cd(destination);
                recomputeFileSize(channelSftp, destination, sourceFile, isUploadNew);
                //total_progress = (float) file_size / f.length();
                System.out.println("From 2: " + Thread.currentThread());
                try (FileInputStream fis = new FileInputStream(sourceFile)) {
                    channelSftp.put(fis, sourceFile.getName(), new SftpProgressMonitor() {
                        long uploadedBytes;

                        @Override
                        public void init(int i, String source, String destination, long bytes) {
                            uploadedBytes = fileSize;
                        }

                        @Override
                        public boolean count(long bytes) {
                            uploadedBytes += bytes;
                            if (progressUpdate != null) {
                                progress.set((double) uploadedBytes / (double) sourceFile.length());
                                progressUpdate.accept(progress.getValue() * 100.00, 100.00);
                            }
                            return (true);
                        }

                        @Override
                        public void end() {
                        }
                    }, putStatusMode);
                }
            } catch (JSchException | SftpException ex) {
                if (ex.toString().contains(Messages.SESSION_DOWN_MSG) || ex.toString().contains(Messages.SOCKET_WRITE_ERR_MSG)) {
                    isUploadNew = false;
                    putStatusMode = ChannelSftp.RESUME;
                    reconnect();
                }
            } catch (IOException e) {
                return;
            }
            System.out.println(progress.getValue());
            if (fileSize == sourceFile.length() || progress.getValue() == 1.0) {
                isUploadComplete = true;
                progressUpdate.accept(100.00, 100.00);
            }
        }
        System.out.println("From 3: " + Thread.currentThread());
        Thread.yield();
        System.out.println("From 4: " + Thread.currentThread());
    }

    private void recomputeFileSize(ChannelSftp channelSftp, String destination, File file, Boolean isUploadNew) {
        try {
            SftpATTRS attrs = channelSftp.lstat(destination + "/" + file.getName());
            fileSize = isUploadNew.equals(Boolean.TRUE) ? 0 : (int) attrs.getSize();
        } catch (SftpException ex) {
        }
    }


    public void uploadFolderToServer(File f, String source, String destination) throws IOException {
        String destinationFolderName = f.getName();
        File tempDir = new File(FTConstants.TEMP_DIRECTORY);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        FileUtils.copyDirectory(FileUtils.getFile(source), tempDir);
        ZipUtils zipUtil = new ZipUtils();
        zipUtil.zipFolder(FTConstants.TEMP_DIRECTORY + destinationFolderName, FTConstants.TEMP_DIRECTORY + destinationFolderName + ".zip");
        uploadFileToServer(FTConstants.TEMP_DIRECTORY + destinationFolderName + ".zip", destination);
        unzipDestinationFolder(destination, destinationFolderName);
        Utils.deleteFileFolder(tempDir);
    }

    void unzipDestinationFolder(String destination, String folderName) throws IOException {
        File unzipScript = new File(FTConstants.SCRIPTS_PATH + "Unzip.sh");
        StringBuilder input = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(unzipScript))) {
            String line;
            while ((line = br.readLine()) != null) {
                input.append(line).append(System.lineSeparator());
            }
            String destinationZip = folderName + ".zip";
            String inputStr = input.toString();
            inputStr = inputStr.replaceFirst("[^#*]destination=\".*\"", "\ndestination=\"" + destination + "\"");
            inputStr = inputStr.replaceFirst("[^#*]destinationZip=\".*\"", "\ndestinationZip=\"" + destinationZip + "\"");
            inputStr = inputStr.replaceAll("\r\n", "\n");
            try (FileOutputStream fileOut = new FileOutputStream(unzipScript)) {
                fileOut.write(inputStr.getBytes());
            }
            executeScript("Unzip.sh");
        }
    }

    public void download(String source, String destination) throws SftpException, JSchException, IOException {
        System.out.println("Downloading " + source + " to " + destination);
        try {
            channel = session.openChannel("sftp");
            if (!channel.isConnected()) {
                channel.connect();
            }
            ChannelSftp channelSftp = (ChannelSftp) channel;
            if (source.contains(FTConstants.HOME)) {
                source = source.replace(FTConstants.HOME, channelSftp.getHome());
            }
            SftpATTRS attrs = channelSftp.lstat(source);
            if (attrs == null) {
                System.out.println(Messages.FILE_NOT_FOUND_MSG);
                return;
            }
            if (attrs.isDir()) {
                downloadFolderFromServer(source, destination, channelSftp, attrs);
            } else {
                String sourceFileName = getSourceFileName(source, channelSftp);
                downloadFileFromServer(source, destination, channelSftp, attrs, sourceFileName);
            }
        } catch (SftpException ex) {
            System.err.println(ex.toString());
        }
    }

    void downloadFileFromServer(String source, String destination, ChannelSftp channelSftp, SftpATTRS attrs, String sourceFileName) throws SftpException {
        boolean isDownloadNew = true;
        boolean isDownloadComplete = false;
        int getStatusMode = ChannelSftp.OVERWRITE;
        long sourceFileSize = attrs.getSize();
        File destFile = new File(destination + File.pathSeparator + sourceFileName);
        while (!isDownloadComplete) {
            fileSize = 0;
            try {
                channel = session.openChannel("sftp");
                if (!channel.isConnected()) {
                    channel.connect();
                }
                fileSize = isDownloadNew ? 0 : (int) destFile.length();
                channelSftp.get(source, destination, new SftpProgressMonitor() {
                    long downloadedBytes;

                    @Override
                    public void init(int i, String string, String string1, long l) {
                        downloadedBytes = fileSize;
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
                }, getStatusMode);
            } catch (JSchException | SftpException ex) {
                if (ex.toString().contains(Messages.SESSION_DOWN_MSG) || ex.toString().contains(Messages.SOCKET_WRITE_ERR_MSG)) {
                    isDownloadNew = false;
                    getStatusMode = ChannelSftp.RESUME;
                    reconnect();
                }
            }
            if (fileSize == sourceFileSize || progress.getValue() == 1.0) {
                isDownloadComplete = true;
                progressUpdate.accept(100.00, 100.00);
            }
        }
    }

    void downloadFolderFromServer(String source, String destination, ChannelSftp channelSftp, SftpATTRS attrs) throws IOException, SftpException {
        executeFolderDownloadScript(source);
        source += ".zip";
        try {
            attrs = channelSftp.lstat(source);
        } catch (SftpException ex) {
            ex.toString();
        }
        String sourceFileName = getSourceFileName(source, channelSftp);
        downloadFileFromServer(source, destination, channelSftp, attrs, sourceFileName);
        ZipUtils appzip = new ZipUtils();
        //create output folder
        new File(destination + File.pathSeparator);
        appzip.unZipIt(destination + "/" + sourceFileName, destination + "/");
        Utils.deleteFileFolder(new File(destination + File.pathSeparator + sourceFileName));
    }

    public void executeFolderDownloadScript(String source) throws IOException {
        File zipScript = new File("Scripts/" + "zipFolder.sh");
        StringBuilder input = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(zipScript))) {
            String line;
            while ((line = br.readLine()) != null) {
                input.append(line).append(System.lineSeparator());
            }
            String inputStr = input.toString();
            inputStr = inputStr.replaceFirst("[^#*]source=\".*\"", "\nsource=\"" + source + "\"");
            inputStr = inputStr.replaceAll("\r\n", "\n");
            try (FileOutputStream fileOut = new FileOutputStream(zipScript)) {
                fileOut.write(inputStr.getBytes());
            }
            executeScript("zipFolder.sh");
        }
    }

    public Boolean executeScript(String scriptName) {
        try {
            String scriptOutput = "";
            StringBuilder command = new StringBuilder();
            File scriptFile = new File("Scripts/" + scriptName);
            try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    command.append(line).append("\n");
                }
            }
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command.toString());
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
                    scriptOutput = new String(tmp, 0, i);
                    System.out.println(scriptOutput);
                }
                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                sleep(500);
            }

            channel.disconnect();

        } catch (JSchException | IOException e) {
            System.out.println(e);
        }
        return true;

    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ee) {
            System.err.println("");
        }
    }

    String getSourceFileName(String source, ChannelSftp channelSftp) throws SftpException {
        List<ChannelSftp.LsEntry> sourceFileObject = new ArrayList<>(channelSftp.ls(source));
        return sourceFileObject.get(0).getFilename();
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
