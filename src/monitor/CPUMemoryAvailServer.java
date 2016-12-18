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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Sends %cloud cpu and memory availability to mobile app
 * Requires a UDP Inbound security rule in EC2 with this port
 * Requires SIGAR library files, both .jar and .so files
 */
public class CPUMemoryAvailServer {

    public static String create(int port) {
        String toReturn;
        File file = new File("files/CPUMemoryAvailServer.java");
        try {
            if (file.createNewFile()) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.write(file(port));
                    bw.flush();
                }
                toReturn = file + " created. ";
            } else {
                toReturn = file + " already exist. ";
            }
        } catch (IOException ex) {
            toReturn = "IOException " + ex.getMessage();
        }
        return toReturn;
    }

    public static String file(int port) {
        return "import java.net.DatagramPacket;\n"
                + "import java.net.DatagramSocket;\n"
                + "import org.hyperic.sigar.Sigar;\n"
                + "import org.hyperic.sigar.SigarException;\n"
                + "\n"
                + "/**\n"
                + " * @author Samuel Chinenyeze\n"
                + " * @ Sends %cloud cpu and memory availability to mobile app\n"
                + " * @ Required: set a UDP Inbound security rule in EC2 with this port\n"
                + " * @ Required: SIGAR library files, both .jar and .so files\n"
                + " */\n"
                + "public class CPUMemoryAvailServer {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"CPUMemoryAvailServer [cms] Started.\");\n"
                + "        DatagramSocket socket;\n"
                + "        Sigar sigar = new Sigar();\n"
                + "        try {\n"
                + "            //Server on port " + port + "\n"
                + "            socket = new DatagramSocket(" + port + ");\n"
                + "            byte[] buff = new byte[2]; //request buffer\n"
                + "            while (true) {\n"
                + "                DatagramPacket pack = new DatagramPacket(buff, buff.length);\n"
                + "                socket.receive(pack); //receive but ignore\n"
                + "                int usedcpu = 0, usedmem = 0;\n"
                + "                try {\n"
                + "                    usedcpu = (int) Math.round(sigar.getCpuPerc().getCombined() * 100);\n"
                + "                    usedmem = (int) Math.round(sigar.getMem().getUsedPercent());\n"
                + "                } catch (SigarException se) {\n"
                + "                    System.out.println(\"cms_err: \" + se);\n"
                + "                }\n"
                + "                int availcpu = 100 - usedcpu;\n"
                + "                int availmem = 100 - usedmem;\n"
                + "                String result = availcpu + \" \" + availmem;\n"
                + "                byte[] buffer = result.getBytes(); //response buffer\n"
                + "                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, pack.getAddress(), pack.getPort());\n"
                + "                socket.send(packet);\n"
                + "                System.out.println(\"cms_out: \" + result);\n"
                + "            }\n"
                + "        } catch (Exception ex) {\n"
                + "            System.out.println(\"cms_err: \" + ex);\n"
                + "        }\n"
                + "    }\n"
                + "}";
    }
}
