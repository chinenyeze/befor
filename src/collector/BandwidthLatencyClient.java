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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BandwidthLatencyClient {

    // Set a UDP Inbound security rule in EC2 using given port
    public static String start(int port) {
        DatagramSocket socket;
        String toReturn;
        try {
            //Send
            socket = new DatagramSocket();
            String message = "This is a message";
            byte[] buff = message.getBytes();
            //System.out.println(buff.length);
            InetAddress address = InetAddress.getByName("46.137.91.122");
            DatagramPacket pack = new DatagramPacket(buff, buff.length, address, port);
            long start = System.currentTimeMillis();
            socket.send(pack);

            //Receive
            pack = new DatagramPacket(buff, buff.length);
            socket.receive(pack);
            long end = System.currentTimeMillis();
            long latency = end - start; //ms
            double second = (double) latency / 1000;
            double bits = (double) pack.getLength() * 8; //byte to bits
            double bandwidth = bits / second; //bps
            socket.close();

            System.out.println(String.format("%.0f %d", bandwidth, latency));
            toReturn = String.format("%.0f %d", bandwidth, latency);
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
            toReturn = "Error";
        }
        return toReturn;
    }
}
