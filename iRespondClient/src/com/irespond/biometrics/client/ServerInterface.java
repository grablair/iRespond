package com.irespond.biometrics.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.UUID;

public class ServerInterface {
	private static final byte HEADER_WSQ_IDENTIFY      = 0x01;
	private static final byte HEADER_WSQ_VERIFY        = 0x02;
	private static final byte HEADER_WSQ_ENROLL        = 0x07;
	private static final byte HEADER_IDENTIFY_SUCCESS  = 0x03;
	private static final byte HEADER_IDENTIFY_FAILURE  = 0x06;
	private static final byte HEADER_VERIFY_SUCCESS    = 0x04;
	private static final byte HEADER_VERIFY_FAILURE    = 0x05;
	private static final byte HEADER_ENROLL_SUCCESS    = 0x08;
	private static final byte HEADER_ERROR             = 0x00;
	
	public static String serverAddress = "localhost";
	public static int serverPort = 8080;
	
	public static void identify(byte[] wsqImage, ServerCallback<UUID> callback) {
		Socket socket = null;
		try {
			socket = new Socket(serverAddress, serverPort);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
			out.write(HEADER_WSQ_IDENTIFY);
			out.write(ByteBuffer.allocate(4).putInt(wsqImage.length).array());
			out.write(wsqImage);
			
			byte retHeader = in.readByte();
			
			switch (retHeader) {
			case HEADER_IDENTIFY_SUCCESS:
				byte[] uuidBytes = new byte[16];
				in.read(uuidBytes);
				UUID uuid = UUID.nameUUIDFromBytes(uuidBytes);
				callback.onSuccess(uuid);
				break;
			case HEADER_IDENTIFY_FAILURE:
				callback.onSuccess(null);
				break;
			case HEADER_ERROR:
				callback.onFailure(getErrorString(in));
				break;
			default:
				callback.onFailure("Invalid response header.");
				break;
			}
		} catch (UnknownHostException e) {
			callback.onFailure("Unable to find host.");
		} catch (IOException e) {
			callback.onFailure("Communication error.");
		} catch (Exception e) {
			callback.onFailure(e.getClass().getCanonicalName());
		} finally {
			if (socket != null && !socket.isClosed())
				try { socket.close(); } catch (IOException e) { }
		}
	}
	
	public static void verify(Collection<UUID> uuids, byte[] wsqImage, ServerCallback<Boolean> callback) {
		Socket socket = null;
		try {
			socket = new Socket(serverAddress, serverPort);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
			out.write(HEADER_WSQ_VERIFY);
			out.write(ByteBuffer.allocate(4).putInt(uuids.size()).array());
			for (UUID uuid : uuids)
				out.write(ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits())
						.putLong(uuid.getLeastSignificantBits()).array());
			
			out.write(ByteBuffer.allocate(4).putInt(wsqImage.length).array());
			out.write(wsqImage);
			
			byte retHeader = in.readByte();
			
			switch (retHeader) {
			case HEADER_VERIFY_SUCCESS:
				callback.onSuccess(true);
				break;
			case HEADER_VERIFY_FAILURE:
				callback.onSuccess(false);
				break;
			case HEADER_ERROR:
				callback.onFailure(getErrorString(in));
				break;
			default:
				callback.onFailure("Invalid response header.");
				break;
			}
		} catch (UnknownHostException e) {
			callback.onFailure("Unable to find host.");
		} catch (IOException e) {
			callback.onFailure("Communication error.");
		} finally {
			if (socket != null && !socket.isClosed())
				try { socket.close(); } catch (IOException e) { }
		}
	}
	
	protected static void enroll(Collection<byte[]> wsqImages, ServerCallback<UUID> callback) {
		Socket socket = null;
		try {
			socket = new Socket(serverAddress, serverPort);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
			out.write(HEADER_WSQ_ENROLL);
			out.write(ByteBuffer.allocate(4).putInt(wsqImages.size()).array());
			
			for (byte[] wsqImage : wsqImages) {
				out.write(ByteBuffer.allocate(4).putInt(wsqImage.length).array());
				out.write(wsqImage);
			}
			
			byte retHeader = in.readByte();
			
			switch (retHeader) {
			case HEADER_ENROLL_SUCCESS:
				byte[] uuidBytes = new byte[16];
				in.read(uuidBytes);
				UUID uuid = UUID.nameUUIDFromBytes(uuidBytes);
				callback.onSuccess(uuid);
				break;
			case HEADER_ERROR:
				callback.onFailure(getErrorString(in));
				break;
			default:
				callback.onFailure("Invalid response header.");
				break;
			}
		} catch (UnknownHostException e) {
			callback.onFailure("Unable to find host.");
		} catch (IOException e) {
			callback.onFailure("Communication error.");
		} finally {
			if (socket != null && !socket.isClosed())
				try { socket.close(); } catch (IOException e) { }
		}
	}
	
	private static String getErrorString(DataInputStream in) throws IOException {
		ByteBuffer wrapped;
		
		byte[] errorLenBytes = new byte[4];
		in.read(errorLenBytes);
		wrapped = ByteBuffer.wrap(errorLenBytes);
		int errorLen = wrapped.getInt();
		
		byte[] errorBytes = new byte[errorLen];
		in.read(errorBytes);
		
		return new String(errorBytes);
	}
}
