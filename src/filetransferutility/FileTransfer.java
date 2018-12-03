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
import javafx.scene.control.ProgressBar;

public class FileTransfer extends Connections {

    public com.jcraft.jsch.Session session;
    public Channel channel;
    public int file_size;
    public boolean upload_complete;

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

    public void upload(String Source, String Destination, ProgressBar progressbar) {
        try {
            System.out.println("Uploading " + Source + "\nto " + Destination + "\n");
            progressbar.setProgress(0.0);
            upload_complete = false;
            int put_status_mode = ChannelSftp.OVERWRITE;
            File f = new File(Source);
            ChannelSftp channelSftp = null;
            SftpATTRS attrs = null;
            //progressbar = new ProgressBar();
            while (!upload_complete) {
                file_size = 0;
                try {
                    channel = session.openChannel("sftp");
                    channel.connect();
                    channelSftp = (ChannelSftp) channel;
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
                    //progressbar.setValue(file_size);
                    channelSftp.put(new FileInputStream(f), f.getName(), new SftpProgressMonitor() {
                        long uploadedBytes = file_size;

                        @Override
                        public void init(int i, String Source, String Destination, long bytes) {
                            System.out.println("in init()");
                        }

                        @Override
                        public boolean count(long bytes) {
                            uploadedBytes += bytes;
                            //progressbar.setProgress(0.5);
                            progressbar.setProgress(uploadedBytes/file_size);
                            return (true);
                        }

                        @Override
                        public void end() {
                        }
                    }, put_status_mode);
                } catch (JSchException | SftpException ex) {
                    if (ex.toString().contains("session is down") || ex.toString().contains("socket write error”")) {
                        put_status_mode = ChannelSftp.RESUME;
                        connect();
                    }
                }
                if (file_size == f.length() || progressbar.getProgress()== 1.0){
                    upload_complete = true;
                    //frame.dispose();
                    System.out.println("Upload Complete");
                }
            }
        } catch (JSchException | FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
