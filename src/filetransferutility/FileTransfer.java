package filetransferutility;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class FileTransfer extends Connections {

    public com.jcraft.jsch.Session session;
    public Channel channel;
    public int file_size;
    public boolean upload_complete;
    ProgressBar progress_bar;

    public FileTransfer() {
    }

    public FileTransfer(ProgressBar upload_progress_bar) {
        this.progress_bar = upload_progress_bar;
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

    public void upload(String Source, String Destination) {
        try {
            System.out.println("Uploading " + Source + "\nto " + Destination + "\n");
            Boolean new_upload = true;
            upload_complete = false;
            int put_status_mode = ChannelSftp.OVERWRITE;
            File f = new File(Source);
            ChannelSftp channelSftp = null;
            SftpATTRS attrs = null;
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
                        file_size = (int) attrs.getSize();
                    } catch (SftpException ex) {
                    }
                    //total_progress = (float) file_size / f.length();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        System.out.println(ex);
                    }
                    channelSftp.put(new FileInputStream(f), f.getName(), new SftpProgressMonitor() {
                        long uploadedBytes;

                        @Override
                        public void init(int i, String Source, String Destination, long bytes) {
                            uploadedBytes = file_size;
                        }

                        @Override
                        public boolean count(long bytes) {
                            uploadedBytes += bytes;
                            Platform.runLater(() -> {
                                progress_bar.setProgress((double) uploadedBytes / (double) f.length());
                            });
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ex) {
                                System.out.println(ex);
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

                if (file_size == f.length() || progress_bar.getProgress() >= 1.0) {
                    upload_complete = true;
                    //frame.dispose();
                    System.out.println("Upload Complete");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
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
}
