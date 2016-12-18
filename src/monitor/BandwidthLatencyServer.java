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
 * @author Samuel J. Chinenyeze
 * @ Sends packet to mobile app for network computation
 * @ Required: set a UDP Inbound security rule in EC2 with this port
 */
public class BandwidthLatencyServer {

    public static String create(int port) {
        String toReturn;
        File file = new File("files/BandwidthLatencyServer.java");
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
                + "\n"
                + "/**\n"
                + " * @author Samuel Chinenyeze\n"
                + " * @ Sends packet to mobile app for network computation\n"
                + " * @ Required: set a UDP Inbound security rule in EC2 with this port\n"
                + " */\n"
                + "public class BandwidthLatencyServer {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"BandwidthLatencyServer [bls] Started.\");\n"
                + "        DatagramSocket socket;\n"
                + "        try {\n"
                + "            //Server on port " + port + "\n"
                + "            socket = new DatagramSocket(" + port + ");\n"
                + "            byte[] buff = new byte[17]; //request buffer\n"
                + "            while (true) {\n"
                + "                DatagramPacket pack = new DatagramPacket(buff, buff.length);\n"
                + "                socket.receive(pack);\n"
                + "                System.out.println(\"bls_out: x y\");\n"
                + "                byte[] buffer = pack.getData(); //response buffer\n"
                + "                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, pack.getAddress(), pack.getPort());\n"
                + "                socket.send(packet);\n"
                + "            }\n"
                + "        } catch (Exception ex) {\n"
                + "            System.out.println(\"bls_err: \" + ex);\n"
                + "        }\n"
                + "    }\n"
                + "}";
    }
}
