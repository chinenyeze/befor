import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * @author Samuel Chinenyeze
 * @ Sends %cloud cpu and memory availability to mobile app
 * @ Required: set a UDP Inbound security rule in EC2 with this port
 * @ Required: SIGAR library files, both .jar and .so files
 */
public class CPUMemoryAvailServer {

    public static void main(String[] args) {
        System.out.println("CPUMemoryAvailServer [cms] Started.");
        DatagramSocket socket;
        Sigar sigar = new Sigar();
        try {
            //Server on port 2
            socket = new DatagramSocket(2);
            byte[] buff = new byte[2]; //request buffer
            while (true) {
                DatagramPacket pack = new DatagramPacket(buff, buff.length);
                socket.receive(pack); //receive but ignore
                int usedcpu = 0, usedmem = 0;
                try {
                    usedcpu = (int) Math.round(sigar.getCpuPerc().getCombined() * 100);
                    usedmem = (int) Math.round(sigar.getMem().getUsedPercent());
                } catch (SigarException se) {
                    System.out.println("cms_err: " + se);
                }
                int availcpu = 100 - usedcpu;
                int availmem = 100 - usedmem;
                String result = availcpu + " " + availmem;
                byte[] buffer = result.getBytes(); //response buffer
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, pack.getAddress(), pack.getPort());
                socket.send(packet);
                System.out.println("cms_out: " + result);
            }
        } catch (Exception ex) {
            System.out.println("cms_err: " + ex);
        }
    }
}