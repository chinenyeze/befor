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

package analyser;

import helper.Helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JTextArea;

public class Summariser {

    private static final HashMap<String, String> TIMEMAP = new HashMap<>(); //for summaryMap

    //app timestamps for power and resource logs
    HashMap<String, String> powerTimeMap = new HashMap<>();
    HashMap<String, String> metricsTimeMap = new HashMap<>();

    private static String appPackage;
    private final JTextArea txtOutput;
    private final String markerLog;    //path to markerLog
    private final String powerLog;  //path to powerLog
    private final String metricsLog;  //path to metricsLog
    private static String baseCPU, baseMemory;
    private int markerSize = 0;

    public Summariser(String markerLog, String powerLog, String metricsLog, JTextArea txtOutput) {
        this.markerLog = markerLog;
        this.powerLog = powerLog;
        this.metricsLog = metricsLog;
        this.txtOutput = txtOutput;
    }

    //extract all
    public void extract() {
        if (extractMarker() == 1
                && extractPower(appPackage, powerTimeMap) == 1
                && MetricsParser.parse(metricsLog, metricsTimeMap) == 1
                && extractSummary() == 1) {
            Helper.display(txtOutput,
                    "Data extracted successfully from logs (MarkerLog, PowerLog, MemLog, CPULog & Summary).");
        } else {
            Helper.display(txtOutput, "An error occurred with data extraction.");
        }
    }

    //extractMarker: also sets appPackage for extractPower
    private int extractMarker() {
        BufferedReader br;
        BufferedWriter out;
        try {
            br = new BufferedReader(new FileReader(markerLog));
            out = new BufferedWriter(new FileWriter("results/MarkerLog.dat"));
            String strLine;
            //first load all in hashmap
            while ((strLine = br.readLine()) != null) {
                String[] split = strLine.split(" ");
                TIMEMAP.put(split[0], strLine.substring(split[0].length() + 1, strLine.length())); // +1 for space
                System.out.println(split[0] + " : " + strLine.substring(split[0].length() + 1, strLine.length()));
            }
            out.write("# Label   Start         Finish\n");
            appPackage = TIMEMAP.get("app");
            if (TIMEMAP.containsKey("mobileCPU")) {
                markerSize = (TIMEMAP.size() - 4) / 3; //-4 removes testpackage, M1_anno, Base_CPU and Base_Memory for 0 indexed hashmap
                baseCPU = TIMEMAP.get("mobileCPU");
                baseMemory = TIMEMAP.get("mobileMemory");
            } else {
                markerSize = (TIMEMAP.size() - 2) / 3; //-2 removes testpackage and M1_anno for 0 indexed hashmap
            }
            for (int i = 1; i <= markerSize; i++) {
                String label = TIMEMAP.get("M" + i + "_label");
                String start = TIMEMAP.get("M" + i + "_start");
                String finish = TIMEMAP.get("M" + i + "_finish");
                //populate appTimestamps (for merging with power)
                powerTimeMap.put(start, "0.0");
                powerTimeMap.put(finish, "0.0");
                metricsTimeMap.put(start, "0.0");
                metricsTimeMap.put(finish, "0.0");
                //write MetricsLog.dat for plot
                out.write(label + " " + start + " " + finish + "\n");
            }
            br.close();
            out.flush();
            out.close();
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    //extractPower
    private int extractPower(String appPackage, HashMap timeMap) {
        Scanner scanner;
        try {
            scanner = new Scanner(new FileReader(powerLog));
            while (scanner.hasNextLine()) {
                final String currentLine = scanner.nextLine();
                if (currentLine.contains(appPackage)) {
                    //associate 10043 com.google.android.apps.docs@52833135
                    String[] split = currentLine.split(" ");
                    String processID = split[1]; //eg. gets 10043
                    //extract csv
                    PowerParser.parse(powerLog, processID, timeMap);
                    //delete powerLog
                    scanner.close();
                    return 1;
                }
            }  //end while
            scanner.close();
            return 0;
        } catch (Exception e) {
            return -1;
        }
    }

    //extractSummary
    private int extractSummary() {
        try {
            String fileOut = "results/Summary.csv";
            HashMap<String, String> summaryMap = new HashMap<>();

            if (TIMEMAP.containsKey("Base_CPU")) {
                markerSize = (TIMEMAP.size() - 4) / 3; //-4 removes testpackage, M1_anno, Base_CPU and Base_Memory for 0 indexed hashmap
            } else {
                markerSize = (TIMEMAP.size() - 2) / 3; //-2 removes testpackage and M1_anno for 0 indexed hashmap
            }

            Scanner scanPow = null;
            Scanner scanCPU = null;
            Scanner scanMem = null;

            for (int i = 1; i <= markerSize; i++) {
                String start = TIMEMAP.get("M" + i + "_start");
                String finish = TIMEMAP.get("M" + i + "_finish");
                String label = TIMEMAP.get("M" + i + "_label");
                long time = Long.parseLong(finish) - Long.parseLong(start);

                // get from POWER dat from start to finish. N.B. dat is space separated not csv
                scanPow = new Scanner(new FileReader("results/PowerLog.dat")); //the newly generated dat
                int notify = 0;
                double totalPower = 0.0;
                int divider = 0;
                while (scanPow.hasNextLine()) {
                    final String currentLine = scanPow.nextLine();
                    if (currentLine.contains(start)) {
                        notify = 1;
                    }
                    if (notify == 1) {
                        totalPower += Double.parseDouble(currentLine.split(" ")[1]);
                        divider++;
                    }
                    if (currentLine.contains(finish)) {
                        notify = 0;
                        break;
                    }
                }

                // get from CPU & MEM dat from start to finish. N.B. dat is space separated not csv
                scanCPU = new Scanner(new FileReader("results/CPULog.dat")); //the newly generated dat
                int count = 0;
                int usedCPU = 0, /*average used CPU*/ cpuTotal = 0;
                /*total used CPU*/
                while (scanCPU.hasNextLine()) {
                    final String currentLine = scanCPU.nextLine();
                    if (currentLine.contains(start)) {
                        notify = 1;
                    }
                    if (notify == 1) {
                        cpuTotal += Integer.parseInt(currentLine.split(" ")[1]);
                        count++;
                    }
                    if (currentLine.contains(finish)) {
                        notify = 0;
                        break;
                    }
                }
                try {
                    usedCPU = cpuTotal / (count - 2);//-2 removes the values of 0 added for marker start and finish
                } catch (ArithmeticException ae) {
                    //catch divide by zero
                    System.err.println("Divide by zero error at ServerCPU calculation. Maybe due to wrong dataset.");
                }

                scanMem = new Scanner(new FileReader("results/MemLog.dat")); //the newly generated dat
                count = 0;
                int usedMemory = 0, /*average used memory*/ memTotal = 0;
                /*total used memory*/
                while (scanMem.hasNextLine()) {
                    final String currentLine = scanMem.nextLine();
                    if (currentLine.contains(start)) {
                        notify = 1;
                    }
                    if (notify == 1) {
                        memTotal += Integer.parseInt(currentLine.split(" ")[1]);
                        count++;
                    }
                    if (currentLine.contains(finish)) {
                        notify = 0;
                        break;
                    }
                }
                try {
                    usedMemory = memTotal / (count - 2); //-2 removes the values of 0 added for marker start and finish
                } catch (ArithmeticException ae) {
                    //catch divide by zero
                    System.err.println("Divide by zero error at ServerMem calculation. Maybe due to wrong dataset.");
                }

                double energy = (totalPower / (divider - 2)) * (time / 1000); //divider-2 for start and finish of marker

                //Energy = PowerParser x Sec :: averagePower*milliseconds/1000
                //marker,label,baseCPU,baseMemory, bandwidth,latency,cpuload,memoryload,  time,energy,cloudCPU,cloudMEMORY [with marker as key]
                //if this has anno
                if (TIMEMAP.containsKey("M" + i + "_anno") && !TIMEMAP.get("M" + i + "_anno").equals("na") && baseCPU != null && baseMemory != null) {
                    //--set expected from annotation attributes
                    String[] anno = TIMEMAP.get("M" + i + "_anno").split(" ");
                    summaryMap.put("S" + i + "-F" + i + "_expected,",
                            label + ","
                            + anno[0] + "," //basecpu
                            + anno[1] + "," //basememory
                            + anno[2] + "," //bandwidth
                            + anno[3] + "," //latency
                            + anno[4] + "," //cpuload
                            + anno[5] + "," //memoryload
                            + anno[6] + "," //elapsedtime
                            + anno[7] + "," //usedenergy
                            + anno[8] + "," //cloudcpu
                            + anno[9]);     //cloudmemory
                    //--set actual from test results
                    summaryMap.put("S" + i + "-F" + i + "_actual,",
                            label + ","
                            + String.format("%.0f", Double.parseDouble(baseCPU)) + ","
                            + String.format("%.0f", Double.parseDouble(baseMemory)) + ","
                            + MetricsParser.bandwidth + ","
                            + MetricsParser.latency + ","
                            + MetricsParser.availcpu + ","
                            + MetricsParser.availmemory + ","
                            + time + ","
                            + String.format("%.2f", energy) + "," /*divider-2 for start and finish of marker*/
                            + usedCPU + ","
                            + usedMemory);
                    //--assert
                    if (Helper.inRange(Double.parseDouble(baseCPU), Double.parseDouble(anno[0]))
                            && Helper.inRange(Double.parseDouble(baseMemory), Double.parseDouble(anno[1]))
                            && Helper.inRange(Integer.parseInt(MetricsParser.bandwidth), Integer.parseInt(anno[2]))
                            && Helper.inRange(Integer.parseInt(MetricsParser.latency), Integer.parseInt(anno[3]))
                            && Helper.inRange(Integer.parseInt(MetricsParser.availcpu), Integer.parseInt(anno[4]))
                            && Helper.inRange(Integer.parseInt(MetricsParser.availmemory), Integer.parseInt(anno[5]))) {
                        double assertTime = Helper.percentageIncrease(Long.parseLong(anno[6]), time);
                        double assertEnergy = Helper.percentageIncrease(Double.parseDouble(anno[7]), energy);
                        double assertCPU = Helper.percentageIncrease(Integer.parseInt(anno[8]), usedCPU);
                        double assertMem = Helper.percentageIncrease(Integer.parseInt(anno[9]), usedMemory);

                        String assertResult = Helper.isCongruent(assertTime, assertEnergy, assertCPU, assertMem);
                        //assert passed
                        summaryMap.put("S" + i + "-F" + i + "_assert_passed,", //first assert
                                label + ",,,,,,,"
                                + Helper.lessOrMore(assertTime) + ","
                                + Helper.lessOrMore(assertEnergy) + ","
                                + Helper.lessOrMore(assertCPU) + ","
                                + Helper.lessOrMore(assertMem) + ","
                                + assertResult);                                //final assert
                    } else {
                        //assert failed
                        summaryMap.put("S" + i + "-F" + i + "_assert_failed,", //first assert
                                label + ",,,,,,,-,-,-,-,-");                    //no final assert
                    }
                } else if (baseCPU == null || baseMemory == null) {
                    summaryMap.put("S" + i + "-F" + i + ",",
                            label + ","
                            + "na,"
                            + "na,"
                            + MetricsParser.bandwidth + ","
                            + MetricsParser.latency + ","
                            + MetricsParser.availcpu + ","
                            + MetricsParser.availmemory + ","
                            + time + ","
                            + String.format("%.2f", energy) + ","
                            + usedCPU + ","
                            + usedMemory + ","
                            + "-");
                } else {
                    summaryMap.put("S" + i + "-F" + i + ",",
                            label + ","
                            + String.format("%.0f", Double.parseDouble(baseCPU)) + ","
                            + String.format("%.0f", Double.parseDouble(baseMemory)) + ","
                            + MetricsParser.bandwidth + ","
                            + MetricsParser.latency + ","
                            + MetricsParser.availcpu + ","
                            + MetricsParser.availmemory + ","
                            + time + ","
                            + String.format("%.2f", energy) + ","
                            + usedCPU + ","
                            + usedMemory + ","
                            + "-");
                }

            }

            closeScan(scanPow);
            closeScan(scanCPU);
            closeScan(scanMem);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut))) {
                int count = 1;
                bw.write("Marker,Label,MobileCPU,MobileMemory,Bandwidth,Latency,CloudCPU,CloudMemory,mElapsedTime(ms),mUsedEnergy(mJ),cUsedCPU(%),cUsedMemory(%),FinalAssert\n");
                for (Map.Entry<String, String> entry : summaryMap.entrySet()) {
                    bw.write(entry.getKey() + "" + entry.getValue());
                    if (count != summaryMap.size()) {
                        bw.write("\n");
                        count++;
                    }
                }
            }
            return 1;
        } catch (NumberFormatException | IOException e) {
            System.err.println(e.getMessage());
            return 0;
        }
    }

    private static void closeScan(Scanner scanner) {
        if (scanner != null) {
            scanner.close();
        }
    }

}
