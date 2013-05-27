package com.irespond.biometrics.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.UUID;

import android.os.AsyncTask;

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
	
	public static String serverAddress = "192.168.1.112";
	public static int serverPort = 8080;
	
	public static void identify(final byte[] wsqImage, final ServerCallback<UUID> callback) {
		new AsyncTask<Void, Void, Object>() {
			@Override
			protected Object doInBackground(Void... arg0) {
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
						ByteBuffer buf = ByteBuffer.wrap(uuidBytes);
						
						long mostSig = buf.getLong();
						long leastSig = buf.getLong();
						UUID uuid = new UUID(mostSig, leastSig);
						return uuid;
					case HEADER_IDENTIFY_FAILURE:
						return null;
					case HEADER_ERROR:
						return getErrorString(in);
					default:
						return "Invalid response header.";
					}
				} catch (UnknownHostException e) {
					return "Unable to find host.";
				} catch (IOException e) {
					return "Communication error.";
				} catch (Exception e) {
					return e.getClass().getCanonicalName();
				} finally {
					if (socket != null && !socket.isClosed())
						try { socket.close(); } catch (IOException e) { }
				}
			}
			
			@Override
			protected void onPostExecute(Object result) {
				if (result instanceof String)
					callback.onFailure((String) result);
				else
					callback.onSuccess((UUID) result);
			}
		}.execute();
		
	}
	
	public static void verify(final Collection<UUID> uuids, final byte[] wsqImage, final ServerCallback<Boolean> callback) {
		new AsyncTask<Void, Void, Object>() {
			@Override
			protected Object doInBackground(Void... arg0) {
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
						return true;
					case HEADER_VERIFY_FAILURE:
						return false;
					case HEADER_ERROR:
						return getErrorString(in);
					default:
						return "Invalid response header.";
					}
				} catch (UnknownHostException e) {
					return "Unable to find host.";
				} catch (IOException e) {
					return "Communication error.";
				} finally {
					if (socket != null && !socket.isClosed())
						try { socket.close(); } catch (IOException e) { }
				}
			}
			
			@Override
			protected void onPostExecute(Object result) {
				if (result instanceof String)
					callback.onFailure((String) result);
				else
					callback.onSuccess((Boolean) result);
			}
		}.execute();
	}
	
	protected static void enroll(final Collection<byte[]> wsqImages, final ServerCallback<UUID> callback) {
		new AsyncTask<Void, Void, Object>() {
			@Override
			protected Object doInBackground(Void... arg0) {
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
						ByteBuffer buf = ByteBuffer.wrap(uuidBytes);
						
						long mostSig = buf.getLong();
						long leastSig = buf.getLong();
						UUID uuid = new UUID(mostSig, leastSig);
						return uuid;
					case HEADER_ERROR:
						return getErrorString(in);
					default:
						return "Invalid response header.";
					}
				} catch (UnknownHostException e) {
					return "Unable to find host.";
				} catch (IOException e) {
					return "Communication error.";
				} catch (Exception e) {
					return e.getClass().getCanonicalName();
				} finally {
					if (socket != null && !socket.isClosed())
						try { socket.close(); } catch (IOException e) { }
				}
			}
			
			@Override
			protected void onPostExecute(Object result) {
				if (result instanceof String)
					callback.onFailure((String) result);
				else
					callback.onSuccess((UUID) result);
			}
		}.execute();
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
