package multicast;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class MulticastSender {
    public static void main(String[] args) throws Exception {
        final String GROUP = "230.0.0.1";
        final int PORT = 4446;

        NetworkInterface nif = pickBestMulticastInterface();
        if (nif == null) {
            throw new IllegalStateException("Không tìm thấy network interface nào phù hợp để gửi multicast");
        }
        System.out.println("Using interface: " + nif.getDisplayName());

        InetAddress group = InetAddress.getByName(GROUP);
        if (!(group instanceof Inet4Address)) {
            throw new IllegalArgumentException("GROUP phải là IPv4, vd 230.0.0.1");
        }

        try (MulticastSocket socket = new MulticastSocket()) {
            socket.setTimeToLive(1);
            // Chỉ định NIC để gửi
            socket.setNetworkInterface(nif);

            for (int i = 1; i <= 10; i++) {
                String msg = "Hello multicast " + i;
                byte[] data = msg.getBytes(StandardCharsets.UTF_8);
                DatagramPacket pkt = new DatagramPacket(data, data.length, group, PORT);
                socket.send(pkt);
                System.out.println("Sent: " + msg);
                Thread.sleep(1000);
            }
        }
    }

    private static NetworkInterface pickBestMulticastInterface() throws SocketException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface ni = ifaces.nextElement();
            if (!ni.isUp() || ni.isLoopback() || !ni.supportsMulticast()) continue;

            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                if (addrs.nextElement() instanceof Inet4Address) {
                    return ni;
                }
            }
        }
        return null;
    }
}
