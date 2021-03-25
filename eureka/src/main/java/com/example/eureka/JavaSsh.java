package com.example.eureka;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSsh implements Runnable {
    /**
     * 退格
     */
    private static final String BACKSPACE = new String(new byte[] { 8 });

    /**
     * ESC
     */
    private static final String ESC = new String(new byte[] { 27 });

    /**
     * 空格
     */
    private static final String BLANKSPACE = new String(new byte[] { 32 });

    /**
     * 回车
     */
    private static final String ENTER = new String(new byte[] { 13 });

    /**
     * 某些设备回显数据中的控制字符
     */
    private static final String[] PREFIX_STRS = { BACKSPACE + "+" + BLANKSPACE + "+" + BACKSPACE + "+",
            "(" + ESC + "\\[\\d+[A-Z]" + BLANKSPACE + "*)+" };

    private int sleepTime = 200;

    /**
     * 连接超时(单次命令总耗时)
     */
    private int timeout = 50000;

    /**
     * 保存当前命令的回显信息
     */
    protected StringBuffer currEcho;

    /**
     * 保存所有的回显信息
     */
    protected StringBuffer totalEcho;

    private String ip;
    private int port;
    private String endEcho = "#,?,>,:";
    private String moreEcho = "---- More ----";
    private String moreCmd = BLANKSPACE;
    private JSch jsch = null;
    private Session session;
    private Channel channel;

    @Override
    public void run() {
        InputStream is;
        try {
            is = channel.getInputStream();
            String echo = readOneEcho(is);
            while (echo != null) {
                currEcho.append(echo);
                String[] lineStr = echo.split("\\n");
                if (lineStr != null && lineStr.length > 0) {
                    String lastLineStr = lineStr[lineStr.length - 1];
                    if (lastLineStr != null && lastLineStr.indexOf(moreEcho) > 0) {
                        totalEcho.append(echo.replace(lastLineStr, ""));
                    } else {
                        totalEcho.append(echo);
                    }
                }
                echo = readOneEcho(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String readOneEcho(InputStream instr) {
        byte[] buff = new byte[1024];
        int ret_read = 0;
        try {
            ret_read = instr.read(buff);
        } catch (IOException e) {
            return null;
        }
        if (ret_read > 0) {
            String result = new String(buff, 0, ret_read);
            for (String PREFIX_STR : PREFIX_STRS) {
                result = result.replaceFirst(PREFIX_STR, "");
            }
            try {
                return new String(result.getBytes(), "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public JavaSsh(String ip, int port, String endEcho, String moreEcho) {
        this.ip = ip;
        this.port = port;
        if (endEcho != null) {
            this.endEcho = endEcho;
        }
        if (moreEcho != null) {
            this.moreEcho = moreEcho;
        }
        totalEcho = new StringBuffer();
        currEcho = new StringBuffer();
    }

    private void close() {
        if (session != null) {
            session.disconnect();
        }
        if (channel != null) {
            channel.disconnect();
        }
    }

    private boolean login(String[] cmds) {
        String user = cmds[0];
        String passWord = cmds[1];
        jsch = new JSch();
        try {
            session = jsch.getSession(user, this.ip, this.port);
            session.setPassword(passWord);
            UserInfo ui = new SSHUserInfo() {
                public void showMessage(String message) {
                }

                public boolean promptYesNo(String message) {
                    return true;
                }
            };
            session.setUserInfo(ui);
            session.connect(30000);
            channel = session.openChannel("shell");
            channel.connect(3000);
            new Thread(this).start();
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
            }
            return true;
        } catch (JSchException e) {
            return false;
        }
    }

    protected void sendCommand(String command, boolean sendEnter) {
        try {
            OutputStream os = channel.getOutputStream();
            os.write(command.getBytes());
            os.flush();
            if (sendEnter) {
                currEcho = new StringBuffer();
                os.write(ENTER.getBytes());
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean containsEchoEnd(String echo) {
        boolean contains = false;
        if (endEcho == null || endEcho.trim().equals("")) {
            return contains;
        }
        String[] eds = endEcho.split(",");
        for (String ed : eds) {
            if (echo.trim().endsWith(ed)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private String runCommand(String command, boolean ifEnter) {
        currEcho = new StringBuffer();
        sendCommand(command, ifEnter);
        int time = 0;
        if (endEcho == null || endEcho.equals("")) {
            while (currEcho.toString().equals("")) {
                try {
                    Thread.sleep(sleepTime);
                    time += sleepTime;
                    if (time >= timeout) {
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            while (!containsEchoEnd(currEcho.toString())) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time += sleepTime;
                if (time >= timeout) {
                    break;
                }
                String[] lineStrs = currEcho.toString().split("\\n");
                if (lineStrs != null && lineStrs.length > 0) {
                    if (moreEcho != null && lineStrs[lineStrs.length - 1] != null
                            && lineStrs[lineStrs.length - 1].contains(moreEcho)) {
                        sendCommand(moreCmd, false);
                        currEcho.append("\n");
                        time = 0;
                        continue;
                    }
                }
            }
        }
        return currEcho.toString();
    }

    private String batchCommand(String[] cmds, int[] othernEenterCmds) {
        StringBuffer sb = new StringBuffer();
        for (int i = 2; i < cmds.length; i++) {
            String cmd = cmds[i];
            if (cmd.equals("")) {
                continue;
            }
            boolean ifInputEnter = false;
            if (othernEenterCmds != null) {
                for (int c : othernEenterCmds) {
                    if (c == i) {
                        ifInputEnter = true;
                        break;
                    }
                }
            }
            cmd += (char) 10;
            String resultEcho = runCommand(cmd, ifInputEnter);
            sb.append(resultEcho);
        }
        close();
        return totalEcho.toString();
    }

    public String executive(String[] cmds, int[] othernEenterCmds) {
        if (cmds == null || cmds.length < 3) {
            return null;
        }
        if (login(cmds)) {
            return batchCommand(cmds, othernEenterCmds);
        }
        return null;
    }

    private abstract class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassword() {
            return null;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
        }

        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
                                                  boolean[] echo) {
            return null;
        }
    }
    public static void main(String[] args) {
        String ip = "172.27.5.202";
        int port = 22;
        JavaSsh JavaSsh2 = new JavaSsh(ip, port, null, null);
        String username = "auto_CDA";
        String password = "CDA@yanfabu2019";
        String[] cmds = { username, password,"ping 9.1.1.1","ping 9.1.1.3"};
        String executive = JavaSsh2.executive(cmds, null);
        System.out.println(executive);
        String[] excuResult = executive.split("<H3C>");
        //结果解析
        Pattern h3cLoss = Pattern.compile("\\d+.\\d+% packet loss");
        Pattern h3cIp = Pattern.compile("Ping statistics for (([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(%[\\p{N}\\p{L}]+)?");
        Pattern h3cLoss1 = Pattern.compile("\\d+.\\d+%");
        Pattern h3cIp1 = Pattern.compile("(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(%[\\p{N}\\p{L}]+)?");
        Map<String, String> result = new HashMap<>();
        for (String str : excuResult) {
            Matcher matcherLoss = h3cLoss.matcher(str);
            Matcher matcherIp = h3cIp.matcher(str);
            if (matcherIp.find()) {
                if (matcherLoss.find()) {
                    result.put(matcherIp.group(), matcherLoss.group());
                }else {
                    result.put(matcherIp.group(), "0");
                }
            }
        }
        System.out.println(result);
        Map<String, String> targetLossMap = new HashMap<>();
        for (Map.Entry<String, String> map : result.entrySet()) {
            Matcher matcherLoss1 = h3cLoss1.matcher(map.getValue());
            Matcher matcherIp1 = h3cIp1.matcher(map.getKey());
            if (matcherLoss1.find() && matcherIp1.find()) {
                targetLossMap.put(matcherIp1.group(), matcherLoss1.group());
            }

        }
        System.out.println(targetLossMap);
    }
}