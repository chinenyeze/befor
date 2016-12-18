/*
 * Copyright (c) 2016 Samuel Chinenyeze <sjchinenyeze@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package monitor;

import helper.Helper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JTextArea;

public class EC2Server {

    private final String pemFile;
    private final String user;
    private final String ip;
    private final int port;
    private final JTextArea txtOutput;

    public EC2Server(String pemFile, String user, String ip, int port, JTextArea txtOutput) {
        this.pemFile = pemFile;
        this.user = user;
        this.ip = ip;
        this.port = port;
        this.txtOutput = txtOutput;
    }

    //setUp
    public void setUp() {
        setUpSocketServers();
        setUpServerAgent();
        setUpThrottler();
        setUpStress();
    }

    //cleanUp
    public void cleanUp() {
        try {
            //Remove any old files for agent and throttler, uninstall stress.
            //yes | head -n 10 |... means remove and send yes 10 times for confirmation
            String command
                    = "rm ServerAgent*;" //server agent
                    + "rm startAgent*;"
                    + "rm -rf lib;"
                    + "rm LICENSE;"
                    + "rm CMDRunner.jar;"
                    + "rm slow;" //stress and throttle
                    + "rm rand;"
                    + "yes | head -n 10 | sudo apt-get remove stress;"
                    + "rm -rf sigar;" //Socket server components
                    + "rm sigar.zip;"
                    + "rm BandwidthLatencyServer*;"
                    + "rm CPUMemoryAvailServer*;";
            sendCommand(connectToEC2(), command);
            Helper.display(txtOutput, "Cleanup completed.");
        } catch (JSchException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        }
    }

    //stress
    public void stress(int cpu_value, int mem_value, int time_value) {
        if (time_value > 0) {
            String command = null;
            if (cpu_value > 0 && mem_value > 0) {
                command = "if which stress &>/dev/null; then "
                        + "sudo nohup stress --cpu " + cpu_value + " --vm " + mem_value + " --timeout " + time_value + "s > /tmp/out.txt &"
                        + "echo 'CPU and Memory stress started.';"
                        + "echo 'Stress will stop automatically after timeout';"
                        + " else "
                        + "echo \"Stress installation not found.\";"
                        + "fi";
            } else if (cpu_value > 0) {
                command = "if which stress &>/dev/null; then "
                        + "sudo nohup stress --cpu " + cpu_value + " --timeout " + time_value + "s > /tmp/out.txt &"
                        + "echo 'CPU stress started.';"
                        + "echo 'Stress will stop automatically after timeout';"
                        + " else "
                        + "echo \"Stress installation not found.\";"
                        + "fi";
            } else if (mem_value > 0) {
                command = "if which stress &>/dev/null; then "
                        + "sudo nohup stress --vm " + mem_value + " --timeout " + time_value + "s > /tmp/out.txt &"
                        + "echo 'Memory stress started.';"
                        + "echo 'Stress will stop automatically after timeout';"
                        + " else "
                        + "echo \"Stress installation not found.\";"
                        + "fi";
            }
            if (command != null) {
                try {
                    sendCommand(connectToEC2(), command);
                } catch (JSchException ex) {
                    Helper.display(txtOutput, "JSchException: An error occurred.");
                }
            }
        } else {
            Helper.display(txtOutput, "Stress ignored, as it was unset.");
        }
    }

    //start: start monitor with nothrottle, default, setfixed or random throttling
    public void start(String option, String type, String bandwidth, String bandwidthType, String latency) {
        String command = null;
        String header
                = "if test -f startAgent.sh; then\n"
                + "if lsof -t -i:4444 &>/dev/null;\n"
                + "then echo 'ServerAgent monitor already running on port 4444.\nOther monitors also will also be running if installed.';\n"
                + "else\n"
                + "nohup ./startAgent.sh > /tmp/out.txt &" //nohup for bg processing of long thread
                + "echo 'ServerAgent monitor started.';\n"
                //start CPUMemoryServer monitor
                + "if test -f CPUMemoryAvailServer.class; then\n"
                + "sudo nohup java -cp \".:sigar/sigar.jar\" CPUMemoryAvailServer >> /tmp/out.txt & echo \"cms_pid: $!\" >> /tmp/out.txt;\n"
                + "echo 'CPUMemoryAvailServer monitor started.';\n"
                + "else\n"
                + "echo 'CPUMemoryAvailServer monitor not found.';\n"
                + "fi\n"
                //start BandwidthLatencyServer monitor
                + "if test -f BandwidthLatencyServer.class; then\n"
                + "sudo nohup java BandwidthLatencyServer >> /tmp/out.txt & echo \"bls_pid: $!\" >> /tmp/out.txt;\n"
                + "echo 'BandwidthLatencyServer monitor started.';\n"
                + "else\n"
                + "echo 'BandwidthLatencyServer monitor not found.';\n"
                + "fi\n"
                //Reminder to start offloadable component from display
                + "echo 'Remember to start your offloadable component from the terminal.';\n";
        String defaultThrottle
                = "sudo bash slow;\n"//start default throttler
                + "echo 'Throttler started.';\n";
        String defaultOtherThrottle
                = "sudo bash slow " + type + ";\n"//start other fixed throttler
                + "echo 'Throttler started.';\n";
        String customThrottle
                = "sudo bash slow -b " + bandwidth + bandwidthType + " -l " + latency + "ms;\n"//start custom throttler
                + "echo 'Throttler started.';\n";
        String footer
                = "fi\n"
                + "else\n"
                + "echo 'No ServerAgent monitor found, Setup first.';\n"
                + "fi";

        if (option.equals("default")) {
            if (type == null) {
                command = header + footer;
            } else if (type.equals("default")) {
                command = header + defaultThrottle + footer;
            } else {
                command = header + defaultOtherThrottle + footer;
            }
        } else if (option.equals("custom")) {
            command = header + customThrottle + footer;
        }

        try {
            //Connect to EC2
            Session session = connectToEC2();
            //Check setup -> Check running -> Start
            sendCommand(session, command);
            //Get bandwidth and latency

            //Get server available CPU and Memory
        } catch (JSchException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        }
    }

    public void sendCommand(String command) {
        try {
            //Connect to EC2
            Session session = connectToEC2();
            //Check setup -> Check running -> Start
            sendCommand(session, command);
        } catch (JSchException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        }
    }

    //stop monitor, stop throttler, copy simlog
    public void stop() {
        try {
            //Connect to EC2
            Session session = connectToEC2();
            //Check if port is used -> Then Stop monitor -> Else notify
            sendCommand(session, "if lsof -t -i:4444 &>/dev/null; "
                    + "then lsof -t -i:4444 | xargs kill; echo 'ServerAgent monitor stopped.';"
                    + "sudo bash slow clear; echo 'Throttler stopped.';" //also stop throttler
                    + "sudo pkill java; echo 'Socket monitors stopped';"
                    + " else "
                    + "echo 'Monitor is not running, Start first.';"
                    + "fi");
            // copy simlog
            //getSimLog();
        } catch (JSchException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        }
    }

    private void setUpSocketServers() {
        //1 copy BLS, CMS and sigar.zip to the server
        //2 extract sigar.zip
        //3 compile BLS and CMS
        try {
            //1
            Session session = connectToEC2();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;
            File bls = new File("files/BandwidthLatencyServer.java");
            channelSftp.put(new FileInputStream(bls), bls.getName());
            File cms = new File("files/CPUMemoryAvailServer.java");
            channelSftp.put(new FileInputStream(cms), cms.getName());
            File sigar = new File("files/sigar.zip");
            channelSftp.put(new FileInputStream(sigar), sigar.getName());
            Helper.display(txtOutput, "BandwidthLatencyServer.java, CPUMemoryAvailServer.java and sigar.zip copied to server.");
            //2
            sendCommand("yes | head -n 10 | unzip sigar.zip");
            Helper.display(txtOutput, "SIGAR API setup completed.");
            //3            
            sendCommand("sudo javac BandwidthLatencyServer.java;\n"
                    + "sudo javac -cp \".:sigar/sigar.jar\" CPUMemoryAvailServer.java;");
            Helper.display(txtOutput, "BandwidthLatencyServer and CPUMemoryAvailServer setup completed.\n"
                    + "Remember to set up your offloadable component from the terminal.");
        } catch (JSchException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        } catch (SftpException | FileNotFoundException ex) {
            Helper.display(txtOutput, "Error: some required Socket Servers files setup was not found. To fix this, ensure"
                    + " the required BandwidthLatencyServer.java and CPUMemoryAvailServer.java files are in the files directory, then Cleanup and Setup again.");
        }

    }

    private void setUpServerAgent() {
        try {
            //Connect to EC2
            Session session = connectToEC2();
            //if agent found send error notice else setup: Download ServerAgent -> Install unzip -> Unzip
            sendCommand(session, "if test -f startAgent.sh; then echo 'A ServerAgent installation was found, please cleanup first.';"
                    + " else "
                    + "wget http://jmeter-plugins.org/downloads/file/ServerAgent-2.2.1.zip;"
                    + "sudo apt-get install unzip;"
                    + "unzip ServerAgent-2.2.1.zip;"
                    + "echo 'Monitor setup completed.';"
                    + "fi");
        } catch (JSchException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        }
    }

    private void setUpThrottler() {
        try {
            //Connect to EC2
            Session session = connectToEC2();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;
            //channelSftp.cd(SFTPWORKINGDIR);
            File slow = new File("files/slow");
            channelSftp.put(new FileInputStream(slow), slow.getName());
            //File rand = new File("files/rand");
            //channelSftp.put(new FileInputStream(rand), rand.getName());

            Helper.display(txtOutput, "Network Throttler (slow) setup completed.");
            channel.disconnect();
            session.disconnect();
        } catch (JSchException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        } catch (SftpException | FileNotFoundException ex) {
            Helper.display(txtOutput, "Error: a required file for Throttler setup was not found. To fix this, ensure"
                    + " slow file is in the files directory, then Cleanup and Setup again.");
        }
    }

    private void setUpStress() {
        try {
            //Connect to EC2
            Session session = connectToEC2();
            //check if stress is installed
            sendCommand(session, "if which stress &>/dev/null; then echo 'A Stress installation was found, please cleanup first.';"
                    + " else "
                    + "sudo apt-get install stress;"
                    + "echo 'Stress setup completed.';"
                    + "fi");
        } catch (JSchException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        }
    }

    public void uploadZip(File file) {
        try {
            String zipfile = null;
            Session session = connectToEC2();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;
            //if file is a zip
            int i = file.getName().lastIndexOf('.');
            if (i > 0 && file.getName().substring(i + 1).equals("zip")) {
                channelSftp.put(new FileInputStream(file), file.getName());
                zipfile = file.getName();
            }
            Helper.display(txtOutput, "Components successfully setup.");
            //unzip and replace: auto response yes; 10 times
            if (zipfile != null) {
                sendCommand(session, "yes | head -n 10 | unzip " + zipfile);
            }
            session.disconnect();
        } catch (JSchException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        } catch (SftpException | FileNotFoundException ex) {
            Helper.display(txtOutput, "Error: FileNotFoundException");
        }
    }

    /*private void getSimLog() {
        try {
            //Connect to EC2
            Session session = connectToEC2();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;
            channelSftp.cd(".");

            byte[] buffer = new byte[1024];
            BufferedOutputStream bos;
            try (BufferedInputStream bis = new BufferedInputStream(channelSftp.get("SimLog.dat"))) {
                File f = new File("results/SimLog.dat");
                bos = new BufferedOutputStream(new FileOutputStream(f));
                int readCount;
                while ((readCount = bis.read(buffer)) > 0) {
                    bos.write(buffer, 0, readCount);
                }
            }
            bos.close();

            Helper.display(txtOutput, "SimLog retrieved successfully.");
            channel.disconnect();
            session.disconnect();
        } catch (JSchException | SftpException | IOException ex) {
            Helper.display(txtOutput, "SimLog not found or not applicable.");
            Logger
                    .getLogger(Transfer.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
    }*/
    private Session connectToEC2() throws JSchException {
        JSch jsch = new JSch();
        jsch.addIdentity(pemFile);
        JSch.setConfig("StrictHostKeyChecking", "no");
        Session session = jsch.getSession(user, ip, port);
        session.connect();
        return session;
    }

    private void sendCommand(Session session, String command) {
        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            ((ChannelExec) channel).setErrStream(System.err);
            channel.connect();
            InputStream input = channel.getInputStream();
            //start reading the input from the executed commands on the shell
            byte[] tmp = new byte[1024];
            while (true) {
                while (input.available() > 0) {
                    int i = input.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    //System.out.print(new String(tmp, 0, i));
                    Helper.display(txtOutput, new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    //System.out.println("exit-status: " + channel.getExitStatus());
                    Helper.display(txtOutput, "exit-status: " + channel.getExitStatus());
                    break;
                }
            }
            channel.disconnect();
            session.disconnect();
        } catch (JSchException | IOException ex) {
            Helper.display(txtOutput, "JSchException: An error occurred.");
        }
    }

}
