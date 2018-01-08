/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.ggh.video.rtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;


/**
 * RtpSocket implements a RTP socket for receiving and sending RTP packets.
 * <p>
 * RtpSocket is associated to a DatagramSocket that is used to send and/or
 * receive RtpPackets.
 */
public class RtpSocket {
    /**
     * UDP socket
     */
    SipdroidSocket socket;
    DatagramPacket datagram;

    /**
     * Remote address
     */
    InetAddress r_addr;

    /**
     * Remote port
     */
    int r_port;

    /**
     * Creates a new RTP socket (only receiver)
     */
    public RtpSocket(SipdroidSocket datagram_socket) {
        socket = datagram_socket;
        r_addr = null;
        r_port = 0;
        datagram = new DatagramPacket(new byte[1], 1);
    }

    /**
     * Creates a new RTP socket (sender and receiver)
     */
    public RtpSocket(SipdroidSocket datagram_socket,
                     InetAddress remote_address, int remote_port) {
        socket = datagram_socket;
        r_addr = remote_address;
        r_port = remote_port;
        datagram = new DatagramPacket(new byte[1], 1);
    }

    /**
     * Returns the RTP SipdroidSocket
     */
    public SipdroidSocket getDatagramSocket() {
        return socket;
    }

    /**
     * Receives a RTP packet from this socket
     */
    public void receive(RtpPacket rtpp) throws IOException {
        datagram.setData(rtpp.packet);
        datagram.setLength(rtpp.packet.length);
        socket.receive(datagram);
        if (!socket.isConnected())
            socket.connect(datagram.getAddress(), datagram.getPort());
        rtpp.packet_len = datagram.getLength();
    }

    /**
     * Sends a RTP packet from this socket
     */
    public void send(RtpPacket rtpp) throws IOException {
        datagram.setData(rtpp.packet);
        datagram.setLength(rtpp.packet_len);
        datagram.setAddress(r_addr);
        datagram.setPort(r_port);
        socket.send(datagram);
    }

    /**
     * Closes this socket
     */
    public void close() { // socket.close();
        socket.close();
    }

}
