import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author Samuel Chinenyeze
 * @ Sends packet to mobile app for network computation
 * @ Required: set a UDP Inbound security rule in EC2 with this port
 */
public class BandwidthLatencyServer {

    public static void main(String[] args) {
        System.out.println("BandwidthLatencyServer [bls] Started.");
        DatagramSocket socket;
        try {
            //Server on port 1
            socket = new DatagramSocket(1);
            byte[] buff = new byte[17]; //request buffer
            while (true) {
                DatagramPacket pack = new DatagramPacket(buff, buff.length);
                socket.receive(pack);
                System.out.println("bls_out: x y");
                byte[] buffer = pack.getData(); //response buffer
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, pack.getAddress(), pack.getPort());
                socket.send(packet);
            }
        } catch (Exception ex) {
            System.out.println("bls_err: " + ex);
        }
    }
}