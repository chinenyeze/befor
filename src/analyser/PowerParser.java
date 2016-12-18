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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * Adapted from http://stephendnicholas.com/assets/files/PowerTutorParser.zip
 */
public class PowerParser {

    private static final String _3G = "3G-";
    private static final String AUDIO = "Audio-";
    private static final String CPU = "CPU-";
    private static final String GPS = "GPS-";
    private static final String OLED = "OLED-";
    private static final String WIFI = "Wifi-";
    private static final String[] VARS = new String[]{_3G, AUDIO, CPU, GPS,
        OLED, WIFI};
    private static final String ASSOCIATE = "associate";
    private static final String BEGIN = "begin";
    private static final String TIME = "time";

    public static void parse(String fileIn, String processIdOfInterest, HashMap timeMap) {
        double batteryVoltage = 3.7;
        int avgOverXPeriods = 1;
        String fileOut = "results/PowerLog.dat"; //produces "power.dat" when fileIn is "power"
        Application appOfInterest = null;
        int currentPeriodNum = 0;
        BufferedReader in;

        System.out.println("Started...");
        try {
            in = new BufferedReader(new FileReader(fileIn));
            String str;
            while ((str = in.readLine()) != null) {
                int i = 0;
                boolean foundMatch = false;
                while (!foundMatch && i < VARS.length) {
                    if (str.startsWith(VARS[i])) {
                        String[] split = str.split(" ");
                        String processId = split[0].substring(VARS[i].length());
                        if (processId.equals(processIdOfInterest)) {
                            appOfInterest.addPowerUsage(VARS[i],
                                    currentPeriodNum,
                                    Integer.parseInt(split[1]));
                        }
                    }
                    i++;
                }
                if (!foundMatch) {
                    if (str.startsWith(BEGIN)) {
                        String[] split = str.split(" ");
                        currentPeriodNum = Integer.parseInt(split[1]);
                        appOfInterest.timeList.add(Long.parseLong(split[2]));

                    } else if (str.startsWith(ASSOCIATE)) {
                        String[] split = str.split(" ");
                        if (split[1].equals(processIdOfInterest)) {
                            appOfInterest = new Application(split[2], split[1]);
                        }
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
        //Output
        BufferedWriter out;
        String currKey;
        try {
            out = new BufferedWriter(new FileWriter(fileOut));

            out.write("# Timestamp   Power\n");
            HashMap<String, HashMap<Integer, Integer>> powerUsageMap = appOfInterest
                    .getPowerUsageMap();
            int numOfAveragePeriods = (int) Math.floor(currentPeriodNum
                    / avgOverXPeriods);
            // For every average period row
            for (int i = 0; i <= numOfAveragePeriods; i++) {
                currKey = appOfInterest.timeList.get(i).toString();
                //out.write(appOfInterest.timeList.get(i).toString()); //using timestamp
                double total = 0;
                for (String var : VARS) {
                    double varTotal = 0;
                    int startRow = i * avgOverXPeriods;
                    int endRow = startRow + avgOverXPeriods;
                    for (int j = startRow; j < endRow; j++) {
                        HashMap<Integer, Integer> varMap = powerUsageMap
                                .get(var);
                        if (varMap != null && varMap.containsKey(j)) {
                            varTotal += varMap.get(j);
                        }
                    }
                    varTotal = varTotal / (double) avgOverXPeriods;
                    total += varTotal;
                }

                double totalMa = total / batteryVoltage;
                //out.write("," + totalMa + "\n"); //total power
                //System.out.println(currKey + " " + Double.toString(totalMa));

                if (timeMap.containsKey(currKey)) {
                    timeMap.remove(currKey);
                }
                timeMap.put(currKey, Double.toString(totalMa));
            }

            SortedSet<String> keys = new TreeSet<>(timeMap.keySet());
            for (String key : keys) {
                out.write(key + " " + timeMap.get(key) + "\n");
            }
            timeMap.clear();
            powerUsageMap.clear();
            Application.timeList.clear();
            out.flush();
            out.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
        System.out.println("...finished.");
    }
}

//=====Application class
class Application {

    private final String name;
    private final String processId;
    private final HashMap<String, HashMap<Integer, Integer>> powerUsageMap;
    private final HashMap<String, Integer> powerTotalMap;
    public static ArrayList<Long> timeList = new ArrayList<>();

    public Application(String name, String processId) {
        this.name = name;
        this.processId = processId;
        this.powerUsageMap = new HashMap<>();
        this.powerTotalMap = new HashMap<>();
    }

    public void addPowerUsage(String powerTypeName, int period, int value) {
        if (!powerUsageMap.containsKey(powerTypeName)) {
            powerUsageMap.put(powerTypeName, new HashMap<>());
            powerTotalMap.put(powerTypeName, 0);
        }
        powerUsageMap.get(powerTypeName).put(period, value);
        powerTotalMap.put(powerTypeName, powerTotalMap.get(powerTypeName) + value);
    }

    public String getName() {
        return name;
    }

    public String getProcessId() {
        return processId;
    }

    public HashMap<String, Integer> getPowerTotalMap() {
        return powerTotalMap;
    }

    public HashMap<String, HashMap<Integer, Integer>> getPowerUsageMap() {
        return powerUsageMap;
    }
}
