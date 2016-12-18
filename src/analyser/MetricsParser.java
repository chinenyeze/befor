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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class MetricsParser {

    public static String bandwidth;
    public static String latency;
    public static String availcpu;
    public static String availmemory;

    public static int parse(String fileIn, HashMap timeMap) {
        BufferedReader in;
        BufferedWriter outCpu;
        BufferedWriter outMem;
        try {
            in = new BufferedReader(new FileReader(fileIn));
            outCpu = new BufferedWriter(new FileWriter("results/CPULog.dat"));
            outMem = new BufferedWriter(new FileWriter("results/MemLog.dat"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
            String str;

            int mem, cpu;
            String currKey;

            timeMap.keySet().stream().forEach((key) -> {
                timeMap.put(key, "0,0"); //represents "cpu,mem" defaults
            });

            //read 4 lines for simulation
            bandwidth = in.readLine().split(" ")[1];
            latency = in.readLine().split(" ")[1];
            availcpu = in.readLine().split(" ")[1];
            availmemory = in.readLine().split(" ")[1];

            while ((str = in.readLine()) != null) {
                String[] split = str.split(",");
                //read first line: for memory, then second column of second line: for cpu.
                mem = Integer.parseInt(split[1]) / 1000;
                cpu = Integer.parseInt(in.readLine().split(",")[1]) / 1000;
                currKey = Long.toString(sdf.parse(split[0]).getTime());
                //hashmap: "timstamp", "cpu mem"
                if (timeMap.containsKey(currKey)) {
                    timeMap.remove(currKey);
                }
                timeMap.put(currKey, cpu + "," + mem);
            }
            outCpu.write("# Timestamp   CPU\n");
            outMem.write("# Timestamp   Memory\n");

            SortedSet<String> keys = new TreeSet<>(timeMap.keySet());
            for (String key : keys) {
                outCpu.write(key + " " + timeMap.get(key).toString().split(",")[0] + "\n");
                outMem.write(key + " " + timeMap.get(key).toString().split(",")[1] + "\n");
            }
            timeMap.clear();
            in.close();
            outCpu.flush();
            outMem.flush();
            outCpu.close();
            outMem.close();
            return 1;
        } catch (IOException | NumberFormatException | ParseException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
}
