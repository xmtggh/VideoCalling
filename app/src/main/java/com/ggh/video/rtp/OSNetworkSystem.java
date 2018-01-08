/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
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

// BEGIN android-note
// address length was changed from long to int for performance reasons.
// END android-note

package com.ggh.video.rtp;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.nio.channels.Channel;

// BEGIN android-removed
// import java.nio.channels.SelectableChannel;
// END android-removed
/*
 * 
 * This Class is used for native code wrap, the implement class of
 * INetworkSystem.
 * 
 */
public final class OSNetworkSystem {

    // ----------------------------------------------------
    // Class Variables
    // ----------------------------------------------------

    private static final int ERRORCODE_SOCKET_TIMEOUT = -209;

    private static OSNetworkSystem ref = new OSNetworkSystem();
    
    //private static final int INETADDR_REACHABLE = 0;
    
    private static boolean isNetworkInited = false;
    
    // ----------------------------------------------------
    // Class Constructor
    // ----------------------------------------------------

    // can not be instantiated.
    private OSNetworkSystem() {
        super();
    }

    /*
     * @return a static ref of this class
     */
    public static OSNetworkSystem getOSNetworkSystem() {
        return ref;
    }

    // Useing when cache set/get is OK
    // public static native void oneTimeInitializationDatagram(
    // boolean jcl_IPv6_support);
    //
    // public static native void oneTimeInitializationSocket(
    // boolean jcl_IPv6_support);

    // --------------------------------------------------
    // java codes that wrap native codes
    // --------------------------------------------------

    public void createSocket(FileDescriptor fd, boolean preferIPv4Stack)
            throws IOException {
        createSocketImpl(fd, preferIPv4Stack);
    }

    public void createDatagramSocket(FileDescriptor fd, boolean preferIPv4Stack)
            throws SocketException {
        createDatagramSocketImpl(fd, preferIPv4Stack);
    }

    public int read(FileDescriptor aFD, byte[] data, int offset, int count,
            int timeout) throws IOException {
        return readSocketImpl(aFD, data, offset, count, timeout);
    }
    
    public int readDirect(FileDescriptor aFD, int address, int offset, int count,
            int timeout) throws IOException {
        return readSocketDirectImpl(aFD, address, offset, count, timeout);
    }

    public int write(FileDescriptor aFD, byte[] data, int offset, int count)
            throws IOException {
        return writeSocketImpl(aFD, data, offset, count);
    }
    
    public int writeDirect(FileDescriptor aFD, int address, int offset,
            int count) throws IOException {
        return writeSocketDirectImpl(aFD, address, offset, count);
    }

    public void setNonBlocking(FileDescriptor aFD, boolean block)
            throws IOException {
        setNonBlockingImpl(aFD, block);
    }

    public void connectDatagram(FileDescriptor aFD, int port, int trafficClass,
            InetAddress inetAddress) throws SocketException {
        connectDatagramImpl2(aFD, port, trafficClass, inetAddress);
    }

    public int connect(FileDescriptor aFD, int trafficClass,
            InetAddress inetAddress, int port)  throws IOException{
        return connectSocketImpl(aFD, trafficClass, inetAddress, port);
    }

    // BEGIN android-changed
    public int connectWithTimeout(FileDescriptor aFD, int timeout,
            int trafficClass, InetAddress inetAddress, int port, int step,
            byte[] context)  throws IOException{
        return connectWithTimeoutSocketImpl(aFD, timeout, trafficClass,
                inetAddress, port, step, context);
    }
    // END android-changed

    public void connectStreamWithTimeoutSocket(FileDescriptor aFD, int aport,
            int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException {
        connectStreamWithTimeoutSocketImpl(aFD, aport, timeout, trafficClass,
                inetAddress);
    }

    public void bind(FileDescriptor aFD, int port, InetAddress inetAddress)
            throws SocketException {
        socketBindImpl(aFD, port, inetAddress);
    }

    public boolean bind2(FileDescriptor aFD, int port, boolean bindToDevice,
            InetAddress inetAddress) throws SocketException {
        return socketBindImpl2(aFD, port, bindToDevice, inetAddress);
    }

    public void accept(FileDescriptor fdServer, SocketImpl newSocket,
            FileDescriptor fdnewSocket, int timeout) throws IOException {
        acceptSocketImpl(fdServer, newSocket, fdnewSocket, timeout);
    }

    public int sendDatagram(FileDescriptor fd, byte[] data, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException {
        return sendDatagramImpl(fd, data, offset, length, port, bindToDevice,
                trafficClass, inetAddress);
    }
    
    public int sendDatagramDirect(FileDescriptor fd, int address, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException {
        return sendDatagramDirectImpl(fd, address, offset, length, port, bindToDevice,
                trafficClass, inetAddress);
    }

    public int sendDatagram2(FileDescriptor fd, byte[] data, int offset,
            int length, int port, InetAddress inetAddress) throws IOException {
        return sendDatagramImpl2(fd, data, offset, length, port, inetAddress);
    }

    public int receiveDatagram(FileDescriptor aFD, DatagramPacket packet,
            byte[] data, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException {
        return receiveDatagramImpl(aFD, packet, data, offset, length,
                receiveTimeout, peek);
    }
    
    public int receiveDatagramDirect(FileDescriptor aFD, DatagramPacket packet,
            int address, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException {
        return receiveDatagramDirectImpl(aFD, packet, address, offset, length,
                receiveTimeout, peek);
    }

    public int recvConnectedDatagram(FileDescriptor aFD, DatagramPacket packet,
            byte[] data, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException {
        return recvConnectedDatagramImpl(aFD, packet, data, offset, length,
                receiveTimeout, peek);
    }
    
    public int recvConnectedDatagramDirect(FileDescriptor aFD, DatagramPacket packet, int address,
             int offset, int length, int receiveTimeout, boolean peek)
            throws IOException {
        return recvConnectedDatagramDirectImpl(aFD, packet, address, offset, length, receiveTimeout, peek);
    }

    public int peekDatagram(FileDescriptor aFD, InetAddress sender,
            int receiveTimeout) throws IOException {
        return peekDatagramImpl(aFD, sender, receiveTimeout);
    }

    public int sendConnectedDatagram(FileDescriptor fd, byte[] data,
            int offset, int length, boolean bindToDevice) throws IOException {
        return sendConnectedDatagramImpl(fd, data, offset, length, bindToDevice);
    }
    
    public int sendConnectedDatagramDirect(FileDescriptor fd, int address,
            int offset, int length, boolean bindToDevice) throws IOException {
        return sendConnectedDatagramDirectImpl(fd, address, offset, length, bindToDevice);
    }

    public void disconnectDatagram(FileDescriptor aFD) throws SocketException {
        disconnectDatagramImpl(aFD);
    }

    public void createMulticastSocket(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException {
        createMulticastSocketImpl(aFD, preferIPv4Stack);
    }

    public void createServerStreamSocket(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException {
        createServerStreamSocketImpl(aFD, preferIPv4Stack);
    }

    public int receiveStream(FileDescriptor aFD, byte[] data, int offset,
            int count, int timeout) throws IOException {
        return receiveStreamImpl(aFD, data, offset, count, timeout);
    }

    public int sendStream(FileDescriptor fd, byte[] data, int offset, int count)
            throws IOException {
        return sendStreamImpl(fd, data, offset, count);
    }

    public void shutdownInput(FileDescriptor descriptor) throws IOException {
        shutdownInputImpl(descriptor);
    }

    public void shutdownOutput(FileDescriptor descriptor) throws IOException {
        shutdownOutputImpl(descriptor);
    }

    public boolean supportsUrgentData(FileDescriptor fd) {
        return supportsUrgentDataImpl(fd);
    }

    public void sendUrgentData(FileDescriptor fd, byte value) {
        sendUrgentDataImpl(fd, value);
    }

    public int availableStream(FileDescriptor aFD) throws SocketException {
        return availableStreamImpl(aFD);
    }

    // BEGIN android-removed
    // public void acceptStreamSocket(FileDescriptor fdServer,
    //         SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
    //         throws IOException {
    //     acceptStreamSocketImpl(fdServer, newSocket, fdnewSocket, timeout);
    // }
    // 
    // public void createStreamSocket(FileDescriptor aFD, boolean preferIPv4Stack)
    //         throws SocketException {
    //     createStreamSocketImpl(aFD, preferIPv4Stack);
    // }
    // END android-removed

    public void listenStreamSocket(FileDescriptor aFD, int backlog)
            throws SocketException {
        listenStreamSocketImpl(aFD, backlog);
    }
    
    // BEGIN android-removed
    // public boolean isReachableByICMP(final InetAddress dest,
    //         InetAddress source, final int ttl, final int timeout) {
    //     return INETADDR_REACHABLE == isReachableByICMPImpl(dest, source, ttl,
    //             timeout);
    // }
    // END android-removed

    /*
     * 
     * @param 
     *      readChannels all channels interested in read and accept 
     * @param
     *      writeChannels all channels interested in write and connect 
     * @param timeout
     *      timeout in millis @return a set of channels that are ready for operation
     * @throws 
     *      SocketException @return int array, each int approve one of the     * channel if OK
     */

    public int[] select(FileDescriptor[] readFDs,
            FileDescriptor[] writeFDs, long timeout)
            throws SocketException {
        int countRead = readFDs.length;
        int countWrite = writeFDs.length;
        int result = 0;
        if (0 == countRead + countWrite) {
            return (new int[0]);
        }
        int[] flags = new int[countRead + countWrite];

        // handle timeout in native
        result = selectImpl(readFDs, writeFDs, countRead, countWrite, flags,
                timeout);

        if (0 <= result) {
            return flags;
        }
        if (ERRORCODE_SOCKET_TIMEOUT == result) {
            return new int[0];
        }
        throw new SocketException();

    }

    public InetAddress getSocketLocalAddress(FileDescriptor aFD,
            boolean preferIPv6Addresses) {
        return getSocketLocalAddressImpl(aFD, preferIPv6Addresses);
    }

    /*
     * Query the IP stack for the local port to which this socket is bound.
     * 
     * @param aFD the socket descriptor @param preferIPv6Addresses address
     * preference for nodes that support both IPv4 and IPv6 @return int the
     * local port to which the socket is bound
     */
    public int getSocketLocalPort(FileDescriptor aFD,
            boolean preferIPv6Addresses) {
        return getSocketLocalPortImpl(aFD, preferIPv6Addresses);
    }

    /*
     * Query the IP stack for the nominated socket option.
     * 
     * @param aFD the socket descriptor @param opt the socket option type
     * @return the nominated socket option value
     * 
     * @throws SocketException if the option is invalid
     */
    public Object getSocketOption(FileDescriptor aFD, int opt)
            throws SocketException {
        return getSocketOptionImpl(aFD, opt);
    }

    /*
     * Set the nominated socket option in the IP stack.
     * 
     * @param aFD the socket descriptor @param opt the option selector @param
     * optVal the nominated option value
     * 
     * @throws SocketException if the option is invalid or cannot be set
     */
    public void setSocketOption(FileDescriptor aFD, int opt, Object optVal)
            throws SocketException {
        setSocketOptionImpl(aFD, opt, optVal);
    }

    public int getSocketFlags() {
        return getSocketFlagsImpl();
    }

    /*
     * Close the socket in the IP stack.
     * 
     * @param aFD the socket descriptor
     */
    public void socketClose(FileDescriptor aFD) throws IOException {
        socketCloseImpl(aFD);
    }

    public InetAddress getHostByAddr(byte[] addr) throws UnknownHostException {
        return getHostByAddrImpl(addr);
    }

    public InetAddress getHostByName(String addr, boolean preferIPv6Addresses)
            throws UnknownHostException {
        return getHostByNameImpl(addr, preferIPv6Addresses);
    }

    public void setInetAddress(InetAddress sender, byte[] address) {
        setInetAddressImpl(sender, address);
    }

    // ---------------------------------------------------
    // Native Codes
    // ---------------------------------------------------

    static native void createSocketImpl(FileDescriptor fd,
            boolean preferIPv4Stack);

    /*
     * Allocate a datagram socket in the IP stack. The socket is associated with
     * the <code>aFD</code>.
     * 
     * @param aFD the FileDescriptor to associate with the socket @param
     * preferIPv4Stack IP stack preference if underlying platform is V4/V6
     * @exception SocketException upon an allocation error
     */
    static native void createDatagramSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    static native int readSocketImpl(FileDescriptor aFD, byte[] data,
            int offset, int count, int timeout) throws IOException;
    
    static native int readSocketDirectImpl(FileDescriptor aFD, int address,
            int offset, int count, int timeout) throws IOException;

    static native int writeSocketImpl(FileDescriptor fd, byte[] data,
            int offset, int count) throws IOException;
    
    static native int writeSocketDirectImpl(FileDescriptor fd, int address,
            int offset, int count) throws IOException;

    static native void setNonBlockingImpl(FileDescriptor aFD,
            boolean block);

    static native int connectSocketImpl(FileDescriptor aFD,
            int trafficClass, InetAddress inetAddress, int port);

    // BEGIN android-changed
    static native int connectWithTimeoutSocketImpl(
            FileDescriptor aFD, int timeout, int trafficClass,
            InetAddress hostname, int port, int step, byte[] context);
    // END android-changed

    static native void connectStreamWithTimeoutSocketImpl(FileDescriptor aFD,
            int aport, int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException;

    static native void socketBindImpl(FileDescriptor aFD, int port,
            InetAddress inetAddress) throws SocketException;

    static native void listenStreamSocketImpl(FileDescriptor aFD, int backlog)
            throws SocketException;

    static native int availableStreamImpl(FileDescriptor aFD)
            throws SocketException;

    static native void acceptSocketImpl(FileDescriptor fdServer,
            SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
            throws IOException;

    static native boolean supportsUrgentDataImpl(FileDescriptor fd);

    static native void sendUrgentDataImpl(FileDescriptor fd, byte value);

    /*
     * Connect the socket to a port and address
     * 
     * @param aFD the FileDescriptor to associate with the socket @param port
     * the port to connect to @param trafficClass the traffic Class to be used
     * then the connection is made @param inetAddress address to connect to.
     * 
     * @exception SocketException if the connect fails
     */
    static native void connectDatagramImpl2(FileDescriptor aFD,
            int port, int trafficClass, InetAddress inetAddress)
            throws SocketException;

    /*
     * Disconnect the socket to a port and address
     * 
     * @param aFD the FileDescriptor to associate with the socket
     * 
     * @exception SocketException if the disconnect fails
     */
    static native void disconnectDatagramImpl(FileDescriptor aFD)
            throws SocketException;

    /*
     * Allocate a datagram socket in the IP stack. The socket is associated with
     * the <code>aFD</code>.
     * 
     * @param aFD the FileDescriptor to associate with the socket @param
     * preferIPv4Stack IP stack preference if underlying platform is V4/V6
     * @exception SocketException upon an allocation error
     */

    /*
     * Bind the socket to the port/localhost in the IP stack.
     * 
     * @param aFD the socket descriptor @param port the option selector @param
     * bindToDevice bind the socket to the specified interface @param
     * inetAddress address to connect to. @return if bind successful @exception
     * SocketException thrown if bind operation fails
     */
    static native boolean socketBindImpl2(FileDescriptor aFD,
            int port, boolean bindToDevice, InetAddress inetAddress)
            throws SocketException;

    /*
     * Peek on the socket, update <code>sender</code> address and answer the
     * sender port.
     * 
     * @param aFD the socket FileDescriptor @param sender an InetAddress, to be
     * updated with the sender's address @param receiveTimeout the maximum
     * length of time the socket should block, reading @return int the sender
     * port
     * 
     * @exception IOException upon an read error or timeout
     */
    static native int peekDatagramImpl(FileDescriptor aFD,
            InetAddress sender, int receiveTimeout) throws IOException;

    /*
     * Recieve data on the socket into the specified buffer. The packet fields
     * <code>data</code> & <code>length</code> are passed in addition to
     * <code>packet</code> to eliminate the JNI field access calls.
     * 
     * @param aFD the socket FileDescriptor @param packet the DatagramPacket to
     * receive into @param data the data buffer of the packet @param offset the
     * offset in the data buffer @param length the length of the data buffer in
     * the packet @param receiveTimeout the maximum length of time the socket
     * should block, reading @param peek indicates to peek at the data @return
     * number of data received @exception IOException upon an read error or
     * timeout
     */
    static native int receiveDatagramImpl(FileDescriptor aFD,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;
    
    static native int receiveDatagramDirectImpl(FileDescriptor aFD,
            DatagramPacket packet, int address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    /*
     * Recieve data on the connected socket into the specified buffer. The
     * packet fields <code>data</code> & <code>length</code> are passed in
     * addition to <code>packet</code> to eliminate the JNI field access
     * calls.
     * 
     * @param aFD the socket FileDescriptor @param packet the DatagramPacket to
     * receive into @param data the data buffer of the packet @param offset the
     * offset in the data buffer @param length the length of the data buffer in
     * the packet @param receiveTimeout the maximum length of time the socket
     * should block, reading @param peek indicates to peek at the data @return
     * number of data received @exception IOException upon an read error or
     * timeout
     */
    static native int recvConnectedDatagramImpl(FileDescriptor aFD,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;
    
    static native int recvConnectedDatagramDirectImpl(FileDescriptor aFD,
            DatagramPacket packet, int address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    /*
     * Send the <code>data</code> to the nominated target <code>address</code>
     * and <code>port</code>. These values are derived from the
     * DatagramPacket to reduce the field calls within JNI.
     * 
     * @param fd the socket FileDescriptor @param data the data buffer of the
     * packet @param offset the offset in the data buffer @param length the
     * length of the data buffer in the packet @param port the target host port
     * @param bindToDevice if bind to device @param trafficClass the traffic
     * class to be used when the datagram is sent @param inetAddress address to
     * connect to. @return number of data send
     * 
     * @exception IOException upon an read error or timeout
     */
    static native int sendDatagramImpl(FileDescriptor fd,
            byte[] data, int offset, int length, int port,
            boolean bindToDevice, int trafficClass, InetAddress inetAddress)
            throws IOException;
    
    static native int sendDatagramDirectImpl(FileDescriptor fd,
            int address, int offset, int length, int port,
            boolean bindToDevice, int trafficClass, InetAddress inetAddress)
            throws IOException;

    /*
     * Send the <code>data</code> to the address and port to which the was
     * connnected and <code>port</code>.
     * 
     * @param fd the socket FileDescriptor @param data the data buffer of the
     * packet @param offset the offset in the data buffer @param length the
     * length of the data buffer in the packet @param bindToDevice not used,
     * current kept in case needed as was the case for sendDatagramImpl @return
     * number of data send @exception IOException upon an read error or timeout
     */
    static native int sendConnectedDatagramImpl(FileDescriptor fd,
            byte[] data, int offset, int length, boolean bindToDevice)
            throws IOException;
    
    static native int sendConnectedDatagramDirectImpl(FileDescriptor fd,
            int address, int offset, int length, boolean bindToDevice)
            throws IOException;

    /*
     * Answer the result of attempting to create a server stream socket in the
     * IP stack. Any special options required for server sockets will be set by
     * this method.
     * 
     * @param aFD the socket FileDescriptor @param preferIPv4Stack if use IPV4
     * @exception SocketException if an error occurs while creating the socket
     */
    static native void createServerStreamSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    /*
     * Answer the result of attempting to create a multicast socket in the IP
     * stack. Any special options required for server sockets will be set by
     * this method.
     * 
     * @param aFD the socket FileDescriptor @param preferIPv4Stack if use IPV4
     * @exception SocketException if an error occurs while creating the socket
     */
    static native void createMulticastSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    /*
     * Recieve at most <code>count</code> bytes into the buffer <code>data</code>
     * at the <code>offset</code> on the socket.
     * 
     * @param aFD the socket FileDescriptor @param data the receive buffer
     * @param offset the offset into the buffer @param count the max number of
     * bytes to receive @param timeout the max time the read operation should
     * block waiting for data @return int the actual number of bytes read
     * @throws IOException @exception SocketException if an error occurs while
     * reading
     */
    static native int receiveStreamImpl(FileDescriptor aFD, byte[] data,
            int offset, int count, int timeout) throws IOException;

    /*
     * Send <code>count</code> bytes from the buffer <code>data</code> at
     * the <code>offset</code>, on the socket.
     * 
     * @param fd
     * 
     * @param data the send buffer @param offset the offset into the buffer
     * @param count the number of bytes to receive @return int the actual number
     * of bytes sent @throws IOException @exception SocketException if an error
     * occurs while writing
     */
    static native int sendStreamImpl(FileDescriptor fd, byte[] data,
            int offset, int count) throws IOException;

    private native void shutdownInputImpl(FileDescriptor descriptor)
            throws IOException;

    private native void shutdownOutputImpl(FileDescriptor descriptor)
            throws IOException;

    // BEGIN android-removed
    // static native void acceptStreamSocketImpl(FileDescriptor fdServer,
    //         SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
    //         throws IOException;
    // 
    // static native void createStreamSocketImpl(FileDescriptor aFD,
    //         boolean preferIPv4Stack) throws SocketException;
    // END android-removed

    static native int sendDatagramImpl2(FileDescriptor fd, byte[] data,
            int offset, int length, int port, InetAddress inetAddress)
            throws IOException;

    static native int selectImpl(FileDescriptor[] readfd,
            FileDescriptor[] writefd, int cread, int cwirte, int[] flags,
            long timeout);

    static native InetAddress getSocketLocalAddressImpl(FileDescriptor aFD,
            boolean preferIPv6Addresses);

    /*
     * Query the IP stack for the local port to which this socket is bound.
     * 
     * @param aFD the socket descriptor @param preferIPv6Addresses address
     * preference for nodes that support both IPv4 and IPv6 @return int the
     * local port to which the socket is bound
     */
    static native int getSocketLocalPortImpl(FileDescriptor aFD,
            boolean preferIPv6Addresses);

    /*
     * Query the IP stack for the nominated socket option.
     * 
     * @param aFD the socket descriptor @param opt the socket option type
     * @return the nominated socket option value
     * 
     * @throws SocketException if the option is invalid
     */
    static native Object getSocketOptionImpl(FileDescriptor aFD, int opt)
            throws SocketException;

    /*
     * Set the nominated socket option in the IP stack.
     * 
     * @param aFD the socket descriptor @param opt the option selector @param
     * optVal the nominated option value
     * 
     * @throws SocketException if the option is invalid or cannot be set
     */
    static native void setSocketOptionImpl(FileDescriptor aFD, int opt,
            Object optVal) throws SocketException;

    static native int getSocketFlagsImpl();

    /*
     * Close the socket in the IP stack.
     * 
     * @param aFD the socket descriptor
     */
    static native void socketCloseImpl(FileDescriptor aFD);

    static native InetAddress getHostByAddrImpl(byte[] addr)
            throws UnknownHostException;

    static native InetAddress getHostByNameImpl(String addr,
            boolean preferIPv6Addresses) throws UnknownHostException;

    native void setInetAddressImpl(InetAddress sender, byte[] address);

    // BEGIN android-removed
    // native int isReachableByICMPImpl(InetAddress addr, InetAddress local,
    //         int ttl, int timeout);
    // END android-removed
    
    native Channel inheritedChannelImpl();

    public Channel inheritedChannel() {
        return inheritedChannelImpl();
    }
    
    public void oneTimeInitialization(boolean jcl_supports_ipv6){
        if (!isNetworkInited){
            oneTimeInitializationImpl(jcl_supports_ipv6);
            isNetworkInited = true;
        } 
    }
    
    native void oneTimeInitializationImpl (boolean jcl_supports_ipv6);
}