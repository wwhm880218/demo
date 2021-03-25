package com.example.eureka.tt;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * HWCE6851SSHClient based on jsch
 * examples: com.wow.remoteutils.SSHClientTest
 *
 *
 */
public class HWCE6851SSHClient {

    /**
     * Server Host IP Address，default value is localhost
     */
    private String host = "localhost";

    /**
     * Server SSH Port，default value is 22
     */
    private Integer port = 22;

    /**
     * SSH Login Username
     */
    private String username = "";

    /**
     * SSH Login Password
     */
    private String password = "";

    /**
     * JSch
     */
    private JSch jsch = null;

    /**
     * ssh session
     */
    private Session session = null;

    /**
     * ssh channel
     */
    private Channel channel = null;

    /**
     * timeout for session connection
     */
    private final Integer SESSION_TIMEOUT = 30000;

    /**
     * timeout for channel connection
     */
    private final Integer CHANNEL_TIMEOUT = 30000;

    /**
     * the interval for acquiring ret
     */
    private final Integer CYCLE_TIME = 100;

    public HWCE6851SSHClient() {
        // initialize
        jsch = new JSch();
    }

    public HWCE6851SSHClient setHost(String host) {
        this.host = host;
        return this;
    }

    public HWCE6851SSHClient setPort(Integer port) {
        this.port = port;
        return this;
    }

    public HWCE6851SSHClient setUsername(String username) {
        this.username = username;
        return this;
    }

    public HWCE6851SSHClient setPassword(String password) {
        this.password = password;
        return this;
    }


    /**
     * login to server
     *
     * @param username
     * @param password
     */
    public void login(String username, String password) {

        this.username = username;
        this.password = password;

        try {
            if (null == session) {

                session = jsch.getSession(this.username, this.host, this.port);
                session.setPassword(this.password);
                session.setUserInfo(new MyUserInfo());

                // It must not be recommended, but if you want to skip host-key check,
                // invoke following,
                session.setConfig("StrictHostKeyChecking", "no");
            }

            session.connect(SESSION_TIMEOUT);
        } catch (JSchException e) {
            System.err.println(e);
            this.logout();
        }
    }

    /**
     * login to server
     */
    public void login() {
        this.login(this.username, this.password);
    }

    /**
     * logout of server
     */
    public void logout() {
        this.session.disconnect();
    }

    /**
     * send command through the ssh session,return the ret of the channel
     *
     * @return
     */
    public String sendCmdexec(String command) {

        String ret = "";

        // judge whether the session or channel is connected
        if (!session.isConnected()) {
            this.login();
        }

        // open channel for sending command
        try {
            this.channel = session.openChannel("exec");
            ((ChannelExec) this.channel).setCommand(command);

            // no output stream
            this.channel.setInputStream(null);

            ((ChannelExec) channel).setErrStream(System.err);
            if (channel != null && session != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(this.channel.getInputStream()));
                this.channel.connect(CHANNEL_TIMEOUT);
                StringBuffer sb = new StringBuffer();
                String msg;
                while ((msg = in.readLine()) != null) {
                    sb.append(msg).append("\n");
                }
//                LOG.info("SSH message:" + sb);
                jsch.removeAllIdentity();
                return sb.toString();
            } else {
//                LOG.warn("Can't execute ssh command because Channel or Session is null!");
            }


        } catch (JSchException e) {
            e.printStackTrace();
            System.err.println(e);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e);
        } finally {
            // close channel
            this.channel.disconnect();
        }

        return ret;
    }

    public String sendCmdShell(List<String> commands) {

        String ret = "";

        // judge whether the session or channel is connected
        if (!session.isConnected()) {
            this.login();
        }

        // open channel for sending command
        try {
            this.channel = session.openChannel("shell");
            this.channel.connect(CHANNEL_TIMEOUT);

            // no output stream
            final InputStream in = channel.getInputStream();
            final OutputStream outputStream = channel.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
//            printWriter.println("sys");
            for (String command : commands) {
                printWriter.println(command);
            }
            printWriter.println("exit");
            printWriter.flush();

            // acquire for ret
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;

                    ret = new String(tmp, 0, i);
                    System.out.print(ret);
                }
                // quit the process of waiting for ret
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }

                // wait every 100ms
                try {
                    Thread.sleep(CYCLE_TIME);
                } catch (Exception ee) {
                    System.err.println(ee);
                }
            }

        } catch (JSchException e) {
            e.printStackTrace();
            System.err.println(e);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e);
        } finally {
            // close channel
            this.channel.disconnect();
        }

        return ret;
    }


    /**
     * customized userinfo
     */
    private static class MyUserInfo implements UserInfo {
        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public boolean promptPassword(String s) {
            return false;
        }

        @Override
        public boolean promptPassphrase(String s) {
            return false;
        }

        @Override
        public boolean promptYesNo(String s) {
            return true;
        }

        @Override
        public void showMessage(String s) {
            System.out.println("showMessage:" + s);
        }
    }
}
