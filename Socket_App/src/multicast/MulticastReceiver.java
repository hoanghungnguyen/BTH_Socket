package multicast;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class MulticastReceiver {
    public static void main(String[] args) throws Exception {
        final String GROUP = "230.0.0.1";
        final int PORT = 4446;

        // 1) Chọn NIC tốt nhất: up, non-loopback, hỗ trợ multicast, có IPv4
        NetworkInterface nif = pickBestMulticastInterface();
        if (nif == null) {
            throw new IllegalStateException("Không tìm thấy network interface nào phù hợp để join multicast");
        }
        System.out.println("Using interface: " + nif.getDisplayName());

        InetAddress group = InetAddress.getByName(GROUP); // IPv4 multicast
        if (!(group instanceof Inet4Address)) {
            throw new IllegalArgumentException("GROUP phải là IPv4, vd 230.0.0.1");
        }

        // 2) Tạo socket và join trên đúng interface
        MulticastSocket socket = new MulticastSocket(PORT);
        socket.setReuseAddress(true);
        socket.setTimeToLive(1); // giới hạn trong LAN
        socket.joinGroup(new InetSocketAddress(group, PORT), nif);
        System.out.println("Joined group " + GROUP + ":" + PORT);

        // 3) Nhận dữ liệu
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (true) {
            socket.receive(packet);
            String msg = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            System.out.printf("From %s:%d -> %s%n",
                    packet.getAddress().getHostAddress(), packet.getPort(), msg);
        }
    }

    private static NetworkInterface pickBestMulticastInterface() throws SocketException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface ni = ifaces.nextElement();
            if (!ni.isUp() || ni.isLoopback() || !ni.supportsMulticast()) continue;

            // Ưu tiên interface có IPv4
            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress a = addrs.nextElement();
                if (a instanceof Inet4Address) {
                    return ni;
                }
            }
        }
        return null;
    }
}
