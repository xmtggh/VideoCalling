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

package com.ggh.video.rtp;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketOptions;
import java.net.UnknownHostException;

public class SipdroidSocket extends DatagramSocket {

	PlainDatagramSocketImpl impl;
	public static boolean loaded = false;
	
	public SipdroidSocket(int port) throws SocketException, UnknownHostException {
		super(!loaded?port:0);
		if (loaded) {
			impl = new PlainDatagramSocketImpl();
			impl.create();
			impl.bind(port,InetAddress.getByName("0"));
		}
	}
	
	public void close() {
		super.close();
		if (loaded) impl.close();
	}
	
	public void setSoTimeout(int val) throws SocketException {
		if (loaded) impl.setOption(SocketOptions.SO_TIMEOUT, val);
		else super.setSoTimeout(val);
	}
	
	public void receive(DatagramPacket pack) throws IOException {
		if (loaded) impl.receive(pack);
		else super.receive(pack);
	}
	
	public void send(DatagramPacket pack) throws IOException {
		if (loaded) impl.send(pack);
		else super.send(pack);
	}
	
	public boolean isConnected() {
		if (loaded) return true;
		else return super.isConnected();
	}
	
	public void disconnect() {
		if (!loaded) super.disconnect();
	}
	
	public void connect(InetAddress addr,int port) {
		if (!loaded) super.connect(addr,port);
	}

	static {
			try {
		        System.loadLibrary("OSNetworkSystem");
				OSNetworkSystem.getOSNetworkSystem().oneTimeInitialization(true);
				SipdroidSocket.loaded = true;
			} catch (Throwable e) {
			}
	}
}
