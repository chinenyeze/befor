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

package helper;

import analyser.Plotter;
import analyser.Summariser;
import collector.BandwidthLatencyClient;
import collector.CPUMemoryAvailClient;
import collector.JmxReader;
import collector.PerfmonCollector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import monitor.EC2Server;
import ui.EditTestPlan;
import ui.GraphViewer;

public class Cmd {

    private static String pemfile, ip, port, user, jmeter;
    private static String bandwidth, bandwidthType, latency;
    private static int cpuload, memload, timeout, blport, cmport;
    private static final File PLAN = new File("files/TestPlan.jmx");

    public static void in(String str) {
        try {
            String command = str.split(" ")[0];
            switch (command) {
                case "auto":
                    auto(str);
                    break;
                case "cleanup":
                    cleanup(str);
                    break;
                case "clear":
                    clear(str);
                    break;
                case "collect":
                    collect(str);
                    break;
                case "editplan":
                    editplan(str);
                    break;
                case "exit":
                    break;
                case "extract":
                    extract(str);
                    break;
                case "help":
                    help(str);
                    break;
                case "offload":
                    offload(str);
                    break;
                case "params":
                    params(str);
                    break;
                case "plot":
                    plot(str);
                    break;
                case "setup":
                    setup(str);
                    break;
                case "simulate":
                    simulate(str);
                    break;
                case "start":
                    start(str);
                    break;
                case "stop":
                    stop(str);
                    break;
                default:
                    outerr(null);
                    break;
            }
        } catch (Exception ex) {
            out(ex.getMessage());
        }
    }

    private static void out(String str) {
        System.out.println(str);
    }

    private static void outerr(String str) {
        if (str == null) {
            out("Error with command. To view usage type: help");
        } else {
            out("Error with command. To view usage type: help " + str);
        }
    }

    private static void auto(String str) throws Exception {
        String[] arr = splitQuote(str);
        if (arr.length >= 4 && arr.length < 6) {
            File script = new File(arr[1]);
            File adb = new File(arr[2]); //dir of adb
            int reruns = Integer.parseInt(arr[3]);
            int interleave = 30;
            if (arr.length == 5 && arr[4] != null) {
                interleave = Integer.parseInt(arr[4]);
                if (interleave < 30 || interleave > 180) {
                    interleave = 30;
                }
            }
            if (!script.exists()) {
                out("The auto script file was not found.");
            } else if (!validExtension(script, "auto")) {
                out("The loaded file must be an auto script file.");
            }
            if (!adb.exists()) {
                out("The adb directory was not found.");
            }
            if (reruns <= 0) {
                out("The value of reruns must be greated than 0.");
            }
            if (script.exists() && validExtension(script, "auto") && adb.exists() && reruns > 0) {
                String params = "", offload = "", am = "";
                ArrayList<String> list = new ArrayList<>();
                BufferedReader in = new BufferedReader(new FileReader(script));
                while ((str = in.readLine()) != null) {
                    switch (str.split(" ")[0]) {
                        case "params":
                            params = str;
                            out("params initialised.");
                            break;
                        case "offload":
                            offload = str;
                            out("offload initialised.");
                            break;
                        case "simulate":
                            list.add(str);
                            out("simulate added to list.");
                            break;
                        case "am":
                            am = str;
                            out("am initialised.");
                            break;
                        default:
                            //do nothing
                            break;
                    }
                } //end while
                if (notEmpty(params) && notEmpty(offload) && notEmpty(am) && list.size() > 0) {
                    out("***Automated test started.");
                    int counter = 0;

                    params(params);
                    out("params command completed.");

                    for (int i = 0; i < reruns; i++) {
                        out(">>Test Repeat.........................................................."+ (i + 1));

                        offload(offload);
                        out("offload command completed.");

                        if (list.get(counter).equals("simulate null")) {
                            server().start("default", null, null, null, null);
                            out("skipped simulate.\nstart command completed.");
                        } else {
                            simulate(list.get(counter));
                            out("simulate command completed.");

                            start("start");
                            out("start command completed.");
                        }

                        Thread.sleep(10000);//wait 10s for server start

                        collect("collect");
                        out("collect command completed.");

                        new ProcessBuilder("cmd", "/c", "cd " + adb + " & adb shell " + am).inheritIO().start().waitFor();
                        out("am command completed.");

                        Thread.sleep(10000);//wait 10s then stop monitor

                        stop("stop");
                        out("stop command completed.");

                        Thread.sleep(interleave * 1000);
                        counter++;
                        if (counter >= list.size()) {
                            counter = 0;
                        }
                    }
                    out("***Automated test completed.");
                } else {
                    out("The auto script file was not in correct format.");
                }
            }
        } else {
            outerr("auto");
        }
    }

    private static void cleanup(String str) throws Exception {
        String[] arr = str.split(" ");
        switch (arr.length) {
            case 1:
                server().cleanUp();
                break;
            case 3:
                switch (arr[1]) {
                    case "-d":
                        server().sendCommand("sudo rm -rf " + arr[2]);
                        break;
                    case "-f":
                        server().sendCommand("sudo rm " + arr[2]);
                        break;
                    default:
                        outerr("cleanup");
                        break;
                }
                break;
            default:
                outerr("cleanup");
                break;
        }
    }

    private static void clear(String str) throws Exception {
        if (str.split(" ").length == 1) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            outerr("clear");
        }
    }

    private static void collect(String str) throws Exception {
        if (str.split(" ").length == 1) {
            String bandwidthLatency = BandwidthLatencyClient.start(blport);
            String cpuMemory = CPUMemoryAvailClient.start(cmport);
            if (!cpuMemory.contains("Error") && !bandwidthLatency.contains("Error")) {
                String bandwidth1 = bandwidthLatency.split(" ")[0];
                String latency1 = bandwidthLatency.split(" ")[1];
                String cloudcpu = cpuMemory.split(" ")[0];
                String cloudmem = cpuMemory.split(" ")[1];
                createMetricsFile(bandwidth1, latency1, cloudcpu, cloudmem);
            }
        } else {
            outerr("collect");
        }
    }

    private static void editplan(String str) throws Exception {
        if (str.split(" ").length == 1) {
            EditTestPlan etp = new EditTestPlan();
            etp.setSize(500, 400);
            etp.setVisible(true);
        } else {
            outerr("editplan");
        }
    }

    private static void extract(String str) throws Exception {
        String[] arr = splitQuote(str);
        if (arr.length == 4) {
            File markerLog = new File(arr[1]);
            File powerLog = new File(arr[2]);
            File metricsLog = new File(arr[3]);
            if (markerLog.exists() && powerLog.exists() && metricsLog.exists()) {
                new Summariser(arr[1], arr[2], arr[3], null).extract();
                out("Extraction completed.");
            } else {
                out("Some required files were not found.");
            }
        } else {
            outerr("extract");
        }
    }

    private static void offload(String str) throws Exception {
        String[] arr = splitQuote(str);
        if (arr[1].equals("-u") && arr.length == 3) {
            File zipfile = new File(arr[2]);
            if (!zipfile.exists()) {
                out("The zip file was not found");
            } else {
                server().uploadZip(zipfile);
            }
        } else if (arr[1].equals("-s") && arr.length == 3) {
            server().sendCommand("sudo nohup java " + arr[2] + " >> /tmp/out.txt &");
        } else if (arr[1].equals("-us") && arr.length == 4) {
            File zipfile = new File(arr[2]);
            if (!zipfile.exists()) {
                out("The zip file was not found");
            } else {
                server().uploadZip(zipfile);
                server().sendCommand("sudo nohup java " + arr[2] + " >> /tmp/out.txt &");
            }
        } else {
            outerr("offload");
        }
    }

    private static void params(String str) throws Exception {
        String[] arr = splitQuote(str);
        if (arr.length == 8) {
            pemfile = arr[1];
            ip = arr[2];
            port = arr[3];
            user = arr[4];
            jmeter = arr[5];
            blport = Integer.parseInt(arr[6]);
            cmport = Integer.parseInt(arr[7]);
            //setCompleted("Befor params completed.");
        } else {
            outerr("params");
        }
    }

    private static void plot(String str) throws Exception {
        String[] oldArr = splitQuote(str);
        String[] arr = Arrays.copyOfRange(oldArr, 1, oldArr.length);
        if (arr.length >= 1) {
            ArrayList<String> items = new ArrayList<>();
            String selected = "";
            for (String ar : arr) {
                items.add(ar);
                selected += " " + ar;
            }
            File file = Plotter.plot(items, selected, null);
            GraphViewer gv = new GraphViewer();
            gv.view(file);
            gv.setVisible(true);
        } else {
            outerr("plot");
        }
    }

    private static void setup(String str) throws Exception {
        if (str.split(" ").length == 1) {
            server().setUp();
        } else {
            outerr("setup");
        }
    }

    private static void simulate(String str) throws Exception {
        String[] arr = str.split(" ");
        if (arr.length == 7) {
            bandwidth = arr[1];
            bandwidthType = arr[2];
            latency = arr[3];
            cpuload = Integer.parseInt(arr[4]);
            memload = Integer.parseInt(arr[5]);
            timeout = Integer.parseInt(arr[6]);
            createSimFile(bandwidth, bandwidthType, latency, cpuload, memload);
        } else {
            outerr("simulate");
        }
    }

    private static void start(String str) throws Exception {
        if (str.split(" ").length == 1) {
            server().stress(cpuload, memload, timeout);
            server().start("custom", null, bandwidth, bandwidthType, latency);
        } else {
            outerr("start");
        }
    }

    private static void stop(String str) throws Exception {
        if (str.split(" ").length == 1) {
            server().stop();
        } else {
            outerr("stop");
        }
    }

    private static String[] splitQuote(String str) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(str);
        while (m.find()) {
            //Added .replace("\"", "") to remove surrounding quotes.
            list.add(m.group(1).replace("\"", ""));
        }
        String[] arr = new String[list.size()];
        //System.out.println(list);
        return list.toArray(arr);
    }

    private static boolean notEmpty(String str) {
        return str != null && str.length() > 0;
    }

    private static boolean validExtension(File file, String extension) {
        int i = file.getName().lastIndexOf('.');
        return (i > 0 && file.getName().substring(i + 1).equals(extension));
    }

    private static void createMetricsFile(String bandwidth, String latency, String cloudcpu, String cloudmem) throws Exception {
        BufferedWriter bw;
        String metricsLogFile = "logs/MetricsLog_" + System.currentTimeMillis() + ".log";
        bw = new BufferedWriter(new FileWriter(metricsLogFile));
        bw.write("bandwidth " + bandwidth + "\n" + "latency " + latency + "\n" + "cloudCPU "
                + cloudcpu + "\n" + "cloudMemory " + cloudmem + "\n");
        bw.flush();
        bw.close();
        out("MetricsLog created.");
        String host = new JmxReader(PLAN).getServer();  //get from TestPlan.jmx
        PerfmonCollector.start(jmeter, host, metricsLogFile, PLAN, null);
    }

    private static EC2Server server() {
        return new EC2Server(pemfile, user, ip, Integer.parseInt(port), null);
    }

    private static void createSimFile(String bandwidth, String bandwidthType, String latency, int cpuload, int memload) throws Exception {
        String simLog = "logs/SimLog_" + System.currentTimeMillis() + ".log";
        BufferedWriter bw = new BufferedWriter(new FileWriter(simLog));
        bw.write("bandwidth " + bandwidth + bandwidthType + "\n" + "latency " + latency + "ms\n"
                + "cpuload " + cpuload + "\n" + "memoryload " + memload + "\n");
        bw.flush();
        out("SimLog created.");

    }

    private static void help(String str) throws Exception {
        String[] arr = str.split(" ");
        switch (arr.length) {
            case 1:
                helpAll();
                break;
            case 2:
                switch (arr[1]) {
                    case "auto":
                        helpAuto();
                        break;
                    case "cleanup":
                        helpCleanup();
                        break;
                    case "clear":
                        helpClear();
                        break;
                    case "collect":
                        helpCollect();
                        break;
                    case "editplan":
                        helpEditplan();
                        break;
                    case "exit":
                        helpExit();
                        break;
                    case "extract":
                        helpExtract();
                        break;
                    case "help":
                        helpHelp();
                        break;
                    case "offload":
                        helpOffload();
                        break;
                    case "params":
                        helpParams();
                        break;
                    case "plot":
                        helpPlot();
                        break;
                    case "setup":
                        helpSetup();
                        break;
                    case "simulate":
                        helpSimulate();
                        break;
                    case "start":
                        helpStart();
                        break;
                    case "stop":
                        helpStop();
                        break;
                    default:
                        outerr("help");
                        break;
                }
                break;
            default:
                outerr("help");
                break;
        }
    }

    private static void helpAuto() {
        String help = "Automates the Beftigre full-tier testing of mobile (Band) and cloud (Befor) tiers.";
        help += "\n\nauto auto_script adb_dir reruns\n";
        help += "\nauto_script -> the auto script file (.auto).";
        help += "\nadb_dir     -> the full path to adb.exe (i.e. Android Debug Bridge).";
        help += "\nreruns      -> the number of reruns of the experiment.";
        help += "\ninterleave  -> the interleave (in seconds) between reruns.\n";
        out(help);
    }

    private static void helpCleanup() {
        String help = "Uninstalls all setup files if no argument is supplied, or deletes the third argument from the server based on the second argument -d or -f.";
        help += "\n\ncleanup";
        help += "\ncleanup -d directory";
        help += "\ncleanup -f file\n";
        help += "\ndirectory -> the directory to be deleted from the server.";
        help += "\nfile      -> the file with extension to be deleted from the server, e.g. Sample.java.\n";
        out(help);
    }

    private static void helpClear() {
        String help = "Clears the Befor console.";
        help += "\n\nclear\n";
        out(help);
    }

    private static void helpCollect() {
        String help = "Begins collection of server metrics once the server monitor is launched.";
        help += "\n\ncollect\n";
        out(help);
    }

    private static void helpEditplan() {
        String help = "Provides UI useful to edit the jMeter testplan prior to 'collect' command.";
        help += "\n\neditplan\n";
        out(help);
    }

    private static void helpExit() {
        String help = "Exits the Befor console.";
        help += "\n\nexit\n";
        out(help);
    }

    private static void helpExtract() {
        String help = "Extracts the test results from logs as .dat files.";
        help += "\n\nextract markerLog powerLog metricsLog\n";
        help += "\nmarkerLog  -> the absolute path to markerLog.log, including the file. E.g. \"absolute path\\logs\\markerLog.log\"";
        help += "\npowerLog   -> the absolute path to markerLog.log, including the file. E.g. \"absolute path\\logs\\powerLog.log\"";
        help += "\nmetricsLog -> the absolute path to markerLog.log, including the file. E.g. \"absolute path\\logs\\metricsLog.log\"\n";
        out(help);
    }

    private static void helpHelp() {
        String help = "Provides Help information for all Befor commands, or for a specific Befor command when passed as argument.";
        help += "\n\nhelp";
        help += "\nhelp command\n";
        help += "\ncommand -> a Befor command.\n";
        out(help);
    }

    private static void helpOffload() {
        String help = "Uploads and/or starts offloadable components based on any of three options; -u, -s or -us.";
        help += "\n\noffload -u zipfile";
        help += "\noffload -s mainclass";
        help += "\noffload -us zipfile mainclass\n";
        help += "\nzipfile   -> the zip file to upload, which gets extracted at the server. E.g. \"absolute path\\zipfile.zip\"";
        help += "\nmainclass -> the class name used to start an offloaded component by java interpreter.\n";
        out(help);
    }

    private static void helpParams() {
        String help = "Sets up parameters for prior connection to the server. It is a required command, and takes 7 arguments in the specified order.";
        help += "\n\nparams pemfile ip port user jmeter blport cmport\n";
        help += "\npemfile -> the .pem file from EC2 server setup.";
        help += "\nip      -> the ip address of the server.";
        help += "\nport    -> the port number for the server connection.";
        help += "\nuser    -> the server registered user.";
        help += "\njmeter  -> the absolute path to Apache jmeter home directory. E.g. \"absolute path\\jmeter\"";
        help += "\nblport  -> the port number for BandwidthLatencyServer and Client.";
        help += "\ncmport  -> the port number for CPUMemoryServer and Client.\n";
        out(help);
    }

    private static void helpPlot() {
        String help = "Plots graph using the extracted .dat files, it takes one to three logs as arguments in any order.";
        help += "\n\nplot PowerLog";
        help += "\nplot PowerLog AppLog";
        help += "\nplot PowerLog AppLog CPULog MemLog\n";
        help += "\nPowerLog -> the absolute path to PowerLog.dat, including the file. E.g. \"absolute path\\results\\PowerLog.log\"";
        help += "\nAppLog   -> the absolute path to AppLog.dat, including the file. E.g. \"absolute path\\results\\AppLog\"";
        help += "\nCPULog   -> the absolute path to CPULog.dat, including the file. E.g. \"absolute path\\results\\CPULog\"";
        help += "\nMemLog   -> the absolute path to MemLog.dat, including the file. E.g. \"absolute path\\results\\MemLog\"\n";
        out(help);
    }

    private static void helpSetup() {
        String help = "Installs and copies all necessary files for the test unto the server.";
        help += "\n\nsetup\n";
        out(help);
    }

    private static void helpSimulate() {
        String help = "Sets up parameters for the simulation of resource stress and newtrork throttle.";
        help += "\n\nsimulate bandwidth bandwidthType latency cpuload memload timeout\n";
        help += "\nbandwidth     -> an integer representing the bandwidth.";
        help += "\nbandwidthType -> the bandwidth unit, e.g. bps, kbps or mbps.";
        help += "\nlatency       -> an integer representing the latency in ms.";
        help += "\ncpuload       -> an integer representing the cpu load.";
        help += "\nmemload       -> an integer representing the memory load.";
        help += "\ntimeout       -> an integer representing the timeout in s for the cpu and memory load.\n";
        out(help);
    }

    private static void helpStart() {
        String help = "Starts the server monitors.";
        help += "\n\nstart\n";
        out(help);
    }

    private static void helpStop() {
        String help = "Stops the server monitors.";
        help += "\n\nstop\n";
        out(help);
    }

    private static void helpAll() {
        String help = "To view details of a command enter; 'help' followed by the 'command', e.g. help params";
        help += "\ncommand   : function of command.";
        help += "\n          : list of arguments. nil, if none applies. / to denote alternative command. [] to group commands.";
        help += "\n--------------------------------------------------------------------------";
        help += "\nauto      : Automates the Beftigre full-tier testing of mobile (Band) and cloud (Befor) tiers.";
        help += "\n          : [auto_script adb_dir reruns interleave]";
        help += "\ncleanup   : Uninstalls all setup files if no argument is supplied, or deletes the third argument from the server based on the second argument d or f.";
        help += "\n          : [nil]/[-d directory]/[-f file]";
        help += "\nclear     : Clears the Befor console.";
        help += "\n          : [nil]";
        help += "\ncollect   : Begins collection of server metrics once the server monitor is launched.";
        help += "\n          : [nil]";
        help += "\neditplan  : Provides UI useful to edit the jMeter testplan prior to 'collect' command.";
        help += "\n          : [nil]";
        help += "\nexit      : Exits the Befor console.";
        help += "\n          : [nil]";
        help += "\nextract   : Extracts the test results from logs as .dat files.";
        help += "\n          : [markerLog powerLog metricsLog]";
        help += "\nhelp      : Provides Help information for all Befor commands, or for a specific Befor command when passed as argument.";
        help += "\n          : [nil]/[command]";
        help += "\noffload   : Uploads and/or starts offloadable components based on any of three options; -u, -s or -us.";
        help += "\n          : [-u/-s/-us zipDirectory startCommand]";
        help += "\nparams    : Sets up parameters for prior connection to the server, it is a required command.";
        help += "\n          : [pemfile ip port user jmeter blport cmport]";
        help += "\nplot      : Plots graph using the extracted .dat files, it takes one to three logs as arguments in any order.";
        help += "\n          : [PowerLog AppLog CPULog MemLog]";
        help += "\nsetup     : Installs and copies all necessary files for the test unto the server.";
        help += "\n          : [nil]";
        help += "\nsimulate  : Sets up parameters for the simulation of resource stress and newtrork throttle.";
        help += "\n          : [bandwidth bandwidthType latency cpuload memload timeout]";
        help += "\nstart     : Starts the server monitors.";
        help += "\n          : [nil]";
        help += "\nstop      : Stops the server monitors.";
        help += "\n          : [nil]";
        out(help);
    }

}
