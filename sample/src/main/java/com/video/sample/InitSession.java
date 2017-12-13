package com.video.sample;

import java.net.DatagramSocket;
import java.net.SocketException;

import ggh.video.com.rtplib.DataFrame;
import ggh.video.com.rtplib.Participant;
import ggh.video.com.rtplib.RTPAppIntf;
import ggh.video.com.rtplib.RTPSession;


public class InitSession implements RTPAppIntf {
	
	public RTPSession rtpSession = null;
	
	public InitSession(int rtpPort, int rtcpPort, int prtpPort, int prtcpPort, String paddress){
		DatagramSocket rtpSocket = null;
		DatagramSocket rtcpSocket = null;
		
		try {
			rtpSocket = new DatagramSocket(rtpPort);
			rtcpSocket = new DatagramSocket(rtcpPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rtpSession = new RTPSession(rtpSocket, rtcpSocket);
//		rtpSession.naivePktReception(true);
		rtpSession.RTPSessionRegister(this, null, null);
		Participant p = new Participant(paddress, prtpPort, prtcpPort);
		rtpSession.addParticipant(p);
	}

	@Override
	public void receiveData(DataFrame frame, Participant participant) {
		// TODO Auto-generated method stub
		String s = new String(frame.getConcatenatedData());
		System.out.println("received:"+s + " from:"+participant.getCNAME()+" ssrc:"+participant.getSSRC());
	}

	@Override
	public void userEvent(int type, Participant[] participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int frameSize(int payloadType) {
		// TODO Auto-generated method stub
		return 1;
	}

}
