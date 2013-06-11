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

/**
 * The <code>ServerInterface</code> deals with all the network
 * interface with the iRespond biometric server.
 * 
 * @author grahamb5
 * @author angela18
 */
public class ServerInterface {
	// All the header values.
	private static final byte HEADER_WSQ_IDENTIFY      = 0x01;
	private static final byte HEADER_WSQ_VERIFY        = 0x02;
	private static final byte HEADER_WSQ_ENROLL        = 0x07;
	private static final byte HEADER_IDENTIFY_SUCCESS  = 0x03;
	private static final byte HEADER_IDENTIFY_FAILURE  = 0x06;
	private static final byte HEADER_VERIFY_SUCCESS    = 0x04;
	private static final byte HEADER_VERIFY_FAILURE    = 0x05;
	private static final byte HEADER_ENROLL_SUCCESS    = 0x08;
	private static final byte HEADER_ERROR             = 0x00;
	
	// The address and listening port of the server.
	// TODO: This needs to be updated. The current IP/port is that of the dev's laptop.
	public static String serverAddress = "173.250.188.41";
	public static int serverPort = 8080;
	
	/**
	 * Perform identification of the given WSQ image. On success, the <code>callback</code>'s
	 * <code>onSuccess</code> function is called with the parameter of the matching UUID.
	 * On failure, the <code>callback</code>'s <code>onSuccess</code> is called with the
	 * parameter <code>null</code>.
	 * On error, the <code>callback</code>'s <code>onFailure</code> is called with the error
	 * message as the parameter.
	 * 
	 * @param wsqImage The WSQ image data.
	 * @param callback The callback to call after RPC completion.
	 */
	public static void identify(final byte[] wsqImage, final ServerCallback<UUID> callback) {
		new AsyncTask<Void, Void, Object>() {
			@Override
			protected Object doInBackground(Void... arg0) {
				Socket socket = null;
				try {
					// Open the socket, get the inputs/outputs.
					socket = new Socket(serverAddress, serverPort);
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					DataInputStream in = new DataInputStream(socket.getInputStream());
					
					// Write the identify header.
					out.write(HEADER_WSQ_IDENTIFY);
					// Write the WSQ image length.
					out.write(ByteBuffer.allocate(4).putInt(wsqImage.length).array());
					// Write the WSQ image data.
					out.write(wsqImage);
					
					// Read the response header.
					byte retHeader = in.readByte();
					
					switch (retHeader) {
					case HEADER_IDENTIFY_SUCCESS:
						// Identification success.
						// Read the UUID from network.
						byte[] uuidBytes = new byte[16];
						in.read(uuidBytes);
						ByteBuffer buf = ByteBuffer.wrap(uuidBytes);
						
						// Create UUID.
						long mostSig = buf.getLong();
						long leastSig = buf.getLong();
						UUID uuid = new UUID(mostSig, leastSig);
						
						// Return the UUID.
						return uuid;
					case HEADER_IDENTIFY_FAILURE:
						// Identification failure. Return null.
						return null;
					case HEADER_ERROR:
						// Error. Return the error message.
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
	
	/**
	 * Perform verification of the given WSQ image against the given UUIDs. On success,
	 * the <code>callback</code>'s <code>onSuccess</code> function is called with the
	 * parameter <code>true</code>.
	 * On failure, the <code>callback</code>'s <code>onSuccess</code> is called with the
	 * parameter <code>false</code>.
	 * On error, the <code>callback</code>'s <code>onFailure</code> is called with the error
	 * message as the parameter.
	 * 
	 * @param uuids The UUIDs to match the given WSQ fingerprint image against.
	 * @param wsqImage The WSQ image data.
	 * @param callback The callback to call after RPC completion.
	 */
	public static void verify(final Collection<UUID> uuids, final byte[] wsqImage, final ServerCallback<Boolean> callback) {
		new AsyncTask<Void, Void, Object>() {
			@Override
			protected Object doInBackground(Void... arg0) {
				Socket socket = null;
				try {
					// Open the socket, get the inputs/outputs.
					socket = new Socket(serverAddress, serverPort);
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					DataInputStream in = new DataInputStream(socket.getInputStream());
					
					// Write the verify header.
					out.write(HEADER_WSQ_VERIFY);
					// Write the amount of UUIDs to match against.
					out.write(ByteBuffer.allocate(4).putInt(uuids.size()).array());
					// Write the UUID(s).
					for (UUID uuid : uuids)
						out.write(ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits())
								.putLong(uuid.getLeastSignificantBits()).array());
					
					// Write the length of the WSQ file.
					out.write(ByteBuffer.allocate(4).putInt(wsqImage.length).array());
					// Write the WSQ file.
					out.write(wsqImage);
					
					// Read the response header.
					byte retHeader = in.readByte();
					
					switch (retHeader) {
					case HEADER_VERIFY_SUCCESS:
						// Verification success. Return true.
						return true;
					case HEADER_VERIFY_FAILURE:
						// Verification failure. Return false.
						return false;
					case HEADER_ERROR:
						// Error. Return the error message.
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
	
	/**
	 * Perform enrollment of the given WSQ images. On success, the <code>callback</code>'s
	 * <code>onSuccess</code> function is called with the parameter being the new UUID.
	 * On error, the <code>callback</code>'s <code>onFailure</code> is called with the error
	 * message as the parameter.
	 * 
	 * @param wsqImages The collection of WSQ images to enroll with.
	 * @param callback The callback to call after RPC completion.
	 */
	protected static void enroll(final Collection<byte[]> wsqImages, final ServerCallback<UUID> callback) {
		new AsyncTask<Void, Void, Object>() {
			@Override
			protected Object doInBackground(Void... arg0) {
				Socket socket = null;
				try {
					// Open the socket, get the inputs/outputs.
					socket = new Socket(serverAddress, serverPort);
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					DataInputStream in = new DataInputStream(socket.getInputStream());
					
					// Write the enrollment header.
					out.write(HEADER_WSQ_ENROLL);
					// Write the amount of WSQ images to be sent.
					out.write(ByteBuffer.allocate(4).putInt(wsqImages.size()).array());
					
					// Write the WSQ images to network.
					for (byte[] wsqImage : wsqImages) {
						out.write(ByteBuffer.allocate(4).putInt(wsqImage.length).array());
						out.write(wsqImage);
					}
					
					// Read the response header.
					byte retHeader = in.readByte();
					
					switch (retHeader) {
					case HEADER_ENROLL_SUCCESS:
						// Enrollment success.
						// Read the new UUID.
						byte[] uuidBytes = new byte[16];
						in.read(uuidBytes);
						ByteBuffer buf = ByteBuffer.wrap(uuidBytes);
						
						// Create the new UUID.
						long mostSig = buf.getLong();
						long leastSig = buf.getLong();
						UUID uuid = new UUID(mostSig, leastSig);
						// Return the UUID.
						return uuid;
					case HEADER_ERROR:
						// Error. Return the error message.
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
	
	/**
	 * Reads the error string from <code>in</code> and returns it.
	 * 
	 * @param in The stream to read the error from.
	 * @return the error string.
	 * @throws IOException 
	 */
	private static String getErrorString(DataInputStream in) throws IOException {
		ByteBuffer wrapped;
		
		// Read the error length.
		byte[] errorLenBytes = new byte[4];
		in.read(errorLenBytes);
		wrapped = ByteBuffer.wrap(errorLenBytes);
		int errorLen = wrapped.getInt();
		
		// Read the error message.
		byte[] errorBytes = new byte[errorLen];
		in.read(errorBytes);
		
		// Return the error message.
		return new String(errorBytes);
	}
}
