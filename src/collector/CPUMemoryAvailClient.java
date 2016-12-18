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

package collector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CPUMemoryAvailClient {

    // Set a UDP Inbound security rule in EC2 using given port
    public static String start(int port) {
        DatagramSocket socket;
        String toReturn = "";
        try {
            //Send
            socket = new DatagramSocket();
            String message = "cm"; //length is 2 in server
            byte[] buff = message.getBytes();
            //System.out.println(buff.length);
            InetAddress address = InetAddress.getByName("46.137.91.122");
            DatagramPacket pack = new DatagramPacket(buff, buff.length, address, port);
            socket.send(pack);

            //Receive
            byte[] buffer = new byte[7];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String[] result = new String(packet.getData()).trim().split(" ");
            int cpu = Integer.parseInt(result[0]);
            int mem = Integer.parseInt(result[1]);
            socket.close();
            System.out.println("ServerCPU:" + cpu + " ServerMem:" + mem);
            toReturn = String.format("%d %d", cpu, mem);
        } catch (IOException | NumberFormatException ex) {
            toReturn = "Error";
        }
        return toReturn;
    }
}
