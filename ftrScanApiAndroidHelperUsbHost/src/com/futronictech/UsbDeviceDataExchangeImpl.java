package com.futronictech;

import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.os.Handler;
import android.util.Log;

public class UsbDeviceDataExchangeImpl
{
	private UsbManager mDevManager;
	private FTR_USB_DEVICE_INTERNAL usb_ctx = null;
	private Context context = null;
	private Handler handler = null;
	
	private boolean pending_open = false;

	static final int transfer_buffer_size = 1024 * 64;
	static final int transfer_buffer_size_2 = 1024 * 16;
	static final String log_tag = "FUTRONICFTR_J";
	static final String ACTION_USB_PERMISSION =
		    "android.permission.USB_PERMISSION";
	
	public static final int MESSAGE_ALLOW_DEVICE = 255;
	public static final int MESSAGE_DENY_DEVICE = 256;
	
	private PendingIntent mPermissionIntent = null;
			
	private byte[] max_transfer_buffer = new byte[transfer_buffer_size];
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

	    public void onReceive(Context context, Intent intent)
	    {
	    	Log.i( log_tag , String.format( "Intent receive for: %s", intent.toString() ) );
	    	String action = intent.getAction();
	        if (ACTION_USB_PERMISSION.equals(action))
	        {
              	synchronized (mPermissionIntent)
	   	        {
              		UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) 
                    {
                    	if(device != null)
                        {
                    		Log.i( log_tag , String.format( "Permission intent receive for %s", device.toString() ) );
                    		usb_ctx = OpenDevice(device);
                        }
                    	else
                    	{
                    		Log.i( log_tag , String.format( "Permission intent receive but device NULL" ) );
                    	}
	
                        handler.obtainMessage(MESSAGE_ALLOW_DEVICE).sendToTarget();
                    } 
                    else
                    {
                    	handler.obtainMessage(MESSAGE_DENY_DEVICE).sendToTarget();
                    }
    	        }
	        }
	    }
	};
       
	public UsbDeviceDataExchangeImpl( Context ctx, Handler trg_handler )
    {
		context = ctx;
		handler = trg_handler;
    	    
		mDevManager = (UsbManager)ctx.getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_USB_PERMISSION), 0);
 	
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		context.registerReceiver(mUsbReceiver, filter);
	}
	
	public void Destroy()
	{
		context.unregisterReceiver(mUsbReceiver);
	}
			
	public boolean OpenDevice( int instance, boolean is_activity_thread )
	{
		synchronized(this)
		{
			if(usb_ctx == null)
			{
				pending_open = false;		
				
				HashMap<String, UsbDevice> usb_devs =  mDevManager.getDeviceList();
		    	
		    	Iterator<UsbDevice> deviceIterator = usb_devs.values().iterator();
		    	
		    	int index = 0;
		       
		    	while(deviceIterator.hasNext())
		    	{
		    	    UsbDevice device = deviceIterator.next();
		    	    
		    	    if( IsFutronicDevice(device.getVendorId(), device.getProductId()))
		    	    {
			   	       	if(index < instance)
		    	    	{
		    	    		index++;
		    	    		continue;
		    	    	}    	
			   	        
			   	       	if(!mDevManager.hasPermission(device))
			   	       	{
    			   	       	mDevManager.requestPermission(device, mPermissionIntent);
    			   	            			   	       		   	        
				   	        if( !is_activity_thread )
				   	        {
					   	       	pending_open = false;
					   	       	synchronized (mPermissionIntent)
					   	        {
					   	        	try 
					   	        	{
										mPermissionIntent.wait();
									} 
					   	        	catch (InterruptedException e) 
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
				   	        }
				   	        else
				   	        {				   	        	
				   	        	pending_open = true;
				   	        	return false;
				   	        }
			   	       	}
			   	       	
			   	        		   	       	
			   	       	if(mDevManager.hasPermission(device))
			   	        {
			   	        	usb_ctx = OpenDevice(device);
			   	        }
			   	        else
			   	        {
			   	        	Log.e(log_tag, "device not allow");
			   	        }
		   	   	    }
		    	}
		    	
		    	
			}
	    	
	    	return usb_ctx != null;
		}
	}
	
	public void CloseDevice()
	{
    	synchronized(this)
		{
			if( usb_ctx != null )
			{
				if( usb_ctx.mDevConnetion_ != null )
				{
					usb_ctx.mDevConnetion_.close();
				}
			}
			
			usb_ctx = null;
		}
	}
			
	public boolean IsPendingOpen() { return pending_open; }
				
	public boolean DataExchange(byte[] out_data, byte[] in_data, int in_time_out, int out_time_out, boolean keep_open, boolean use_max_end_point_size, int trace_level )
	{
		synchronized(this)
		{
			
		//Log.i( log_tag , String.format( "Java DataExchange S: %d R: %d", out_data.length, in_data.length ) );	
		
		boolean res = false;
				
		try
		{
						
			boolean check_res = false;
			
			if( usb_ctx != null )
			{
				check_res = 
						usb_ctx.mIntf_ != null && 
						usb_ctx.mDevConnetion_ != null && 
						usb_ctx.mReadPoint_ != null &&
						usb_ctx.mWritePoint_ != null;
				
			}
				
			if( !check_res )
			{
				return res;
			}
			
		
			/*if( (trace_level & (int)0x20) != 0 ? true : false )
			{
				Log.i( log_tag , String.format( "Java DataExchange S: %d R: %d", out_data.length, in_data.length ) );
			}*/
							
			if( !usb_ctx.mHandleClaimed_ )
			{
		    	usb_ctx.mDevConnetion_.claimInterface(usb_ctx.mIntf_, false);
				usb_ctx.mHandleClaimed_ = true;
			}
			
				
			int transfer_bytes = usb_ctx.mDevConnetion_.bulkTransfer(usb_ctx.mWritePoint_, out_data, out_data.length, out_time_out );
			
			if( transfer_bytes == -1 )
			{
				Log.e( log_tag , String.format( "Send %d bytes failed", out_data.length ) );
				return res;
			}
								
			int to_read_size = in_data.length;
			int copy_pos = 0;
			
			while( to_read_size >= getTransferBuffer().length )
			{
				transfer_bytes = usb_ctx.mDevConnetion_.bulkTransfer(usb_ctx.mReadPoint_,  getTransferBuffer(), getTransferBuffer().length, in_time_out );
											
				if( transfer_bytes == -1 )
				{
					Log.e( log_tag , String.format( "Receive %d bytes failed", getTransferBuffer().length ) );
					return res;
				}
				
				if( copy_pos + transfer_bytes > in_data.length )
				{
					Log.e( log_tag , String.format( "Small receive buffer. Need %d bytes", copy_pos + transfer_bytes - in_data.length ) );
					return res;
				}
				
				System.arraycopy(getTransferBuffer(), 0, in_data, copy_pos, transfer_bytes);
								
				to_read_size -= transfer_bytes;
				copy_pos += transfer_bytes;
			}
			
			while( to_read_size >=  transfer_buffer_size_2 )
			{
				transfer_bytes = usb_ctx.mDevConnetion_.bulkTransfer(usb_ctx.mReadPoint_,  getTransferBuffer(),  transfer_buffer_size_2, in_time_out );
											
				if( transfer_bytes == -1 )
				{
					Log.e( log_tag , String.format( "Receive %d bytes failed", getTransferBuffer().length ) );
					return res;
				}
				
				if( copy_pos + transfer_bytes > in_data.length )
				{
					Log.e( log_tag , String.format( "Small receive buffer. Need %d bytes", copy_pos + transfer_bytes - in_data.length ) );
					return res;
				}
				
				System.arraycopy(getTransferBuffer(), 0, in_data, copy_pos, transfer_bytes);
								
				to_read_size -= transfer_bytes;
				copy_pos += transfer_bytes;
			}
				
			if( to_read_size > usb_ctx.mReadPoint_.getMaxPacketSize() )
			{
				int data_left = to_read_size - ( to_read_size % usb_ctx.mReadPoint_.getMaxPacketSize() );
							
				if( data_left > 0 )
				{
					transfer_bytes = usb_ctx.mDevConnetion_.bulkTransfer(usb_ctx.mReadPoint_, getTransferBuffer(), data_left, in_time_out );
					
					if( transfer_bytes == -1 )
					{
						Log.e( log_tag , String.format( "Receive(1) %d bytes failed", data_left ) );
						return res;
					}
					
					if( copy_pos + transfer_bytes > in_data.length )
					{
						Log.e( log_tag , String.format( "Small receive buffer. Need %d bytes", copy_pos + transfer_bytes - in_data.length ) );
						return res;
					}
												
					System.arraycopy(getTransferBuffer(), 0, in_data, copy_pos, transfer_bytes);
					
					to_read_size -= transfer_bytes;
					copy_pos += transfer_bytes;
				}
			}
			
			while( to_read_size > 0 )
			{
				transfer_bytes = usb_ctx.mDevConnetion_.bulkTransfer(usb_ctx.mReadPoint_, getTransferBuffer(), use_max_end_point_size ? usb_ctx.mReadPoint_.getMaxPacketSize() : to_read_size, in_time_out );
				
				if( transfer_bytes == -1 )
				{
					Log.e( log_tag , String.format( "Receive(1) %d bytes failed", to_read_size ) );
					return res;
				}
										
				int real_read = to_read_size > usb_ctx.mReadPoint_.getMaxPacketSize() ? usb_ctx.mReadPoint_.getMaxPacketSize() : to_read_size;
				
				if( copy_pos + real_read > in_data.length )
				{
					Log.e( log_tag , String.format( "Small receive buffer. Need %d bytes", copy_pos + real_read - in_data.length ) );
					return res;
				}
				
				System.arraycopy(getTransferBuffer(), 0, in_data, copy_pos, real_read);
				
				to_read_size -= real_read;
				copy_pos += real_read;
			}
						
			if( !keep_open )
			{
				usb_ctx.mDevConnetion_.releaseInterface(usb_ctx.mIntf_);
				usb_ctx.mHandleClaimed_ = false;
			}
							
			res = true;
		}
		catch( Exception e )
		{
			Log.e( log_tag , String.format( "Data exchange fail %s", e.toString() ) );
		}
		
		return res;
		}
	}
	
	public boolean ValidateContext()
	{
		synchronized(this)
		{
			//Log.i( log_tag , String.format( "Java ValidateContext " ) );
			
			boolean res = false;
			
			if( usb_ctx != null )
			{
				res = 
						usb_ctx.mIntf_ != null && 
						usb_ctx.mDevConnetion_ != null && 
						usb_ctx.mReadPoint_ != null &&
						usb_ctx.mWritePoint_ != null;
				
			}
					
			return res;
		}
	}
	
	public void DataExchangeEnd()
	{
		synchronized(this)
		{
			//Log.i( log_tag , String.format( "Java DataExchangeEnd" ) );
			
			if( usb_ctx != null )
			{
				if( usb_ctx.mHandleClaimed_ )
				{
					usb_ctx.mDevConnetion_.releaseInterface(usb_ctx.mIntf_);
					usb_ctx.mHandleClaimed_ = false;
				}
			}
		}
	}
	
	
	public static boolean IsFutronicDevice(int idVendor, int idProduct)
    {
    	boolean res = false;
    	
    	if( 
    		    (idVendor == 0x0834 && idProduct == 0x0020 ) || 
    		    (idVendor == 0x0958 && idProduct == 0x0307 ) || 
    		    (idVendor == 0x1491 &&   
    		    ( idProduct == 0x0020 || 
    		      idProduct == 0x0025 || 
    		      idProduct == 0x0088 || 
    		      idProduct == 0x0090 || 
    		      idProduct == 0x0050 || 
    		      idProduct == 0x0060 || 
    		      idProduct == 0x0098 || 
    		      idProduct == 0x8098 || 
    		      idProduct == 0x9860 ) ) || 
    		    ( idVendor == 0x1FBA && ( idProduct == 0x0013 || idProduct == 0x0012 ) ) 
    		)
    	{
    		res = true;
    	}
    	
    	return res;
    }
	
	private FTR_USB_DEVICE_INTERNAL OpenDevice(UsbDevice device)
	{
		FTR_USB_DEVICE_INTERNAL res = null;
		
		UsbInterface intf = device.getInterface(0);
		
		if( intf != null )
		{
			UsbEndpoint readpoint = null;
			UsbEndpoint writepoint = null;
			
			for(int i = 0; i < intf.getEndpointCount(); i++)
			{					
				if( intf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT )
				{
					writepoint = intf.getEndpoint(i);
				}
				
				if( intf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN )
				{
					readpoint = intf.getEndpoint(i);
				}
			}
			
			if( readpoint != null && writepoint != null )
			{
				
				UsbDeviceConnection connection = mDevManager.openDevice(device);
			
				if( connection != null )
				{
					res = new FTR_USB_DEVICE_INTERNAL(intf, readpoint, writepoint, connection);
				}
				else
				{
					Log.e(log_tag, "open device failed: " + device);
				}
			}
			else
			{
				Log.e(log_tag, "End points not found in device: " + device );
			}
		}
		else
		{
			Log.e(log_tag, "Get interface failed failed in device: " + device );
		}
						
		return res;
	}
	
	public byte[] getTransferBuffer() {
		return max_transfer_buffer;
	}

	public void setTransferBuffer(byte[] max_transfer_buffer) {
		this.max_transfer_buffer = max_transfer_buffer;
	}
	
	public static void GetInterfaces(Context ctx, byte[] pInterfaceList)
	{
		UsbManager DevManager = (UsbManager)ctx.getSystemService(Context.USB_SERVICE);
		
		HashMap<String, UsbDevice> usb_devs =  DevManager.getDeviceList();
    	
    	Iterator<UsbDevice> deviceIterator = usb_devs.values().iterator();
    	
    	int index = 0;
    	
    	for( index = 0; index < Scanner.FTR_MAX_INTERFACE_NUMBER; index++ )
    	{
    		pInterfaceList[index] = Scanner.FTRSCAN_INTERFACE_STATUS_DISCONNECTED;
    	}
    	
    	index = 0;
       
    	while(deviceIterator.hasNext())
    	{
    	    UsbDevice device = deviceIterator.next();
    	    
    	    if( IsFutronicDevice(device.getVendorId(), device.getProductId()))
    	    {
    	    	pInterfaceList[index] = Scanner.FTRSCAN_INTERFACE_STATUS_CONNECTED;
    	    }
    	}
 	
	}
	
	public class FTR_USB_DEVICE_INTERNAL
	{
		public UsbInterface mIntf_;
		public UsbEndpoint mReadPoint_;
		public UsbEndpoint mWritePoint_;
		public UsbDeviceConnection mDevConnetion_;
		public boolean mHandleClaimed_;
		
		public FTR_USB_DEVICE_INTERNAL( 
				UsbInterface mIntf,
				UsbEndpoint mReadPoint,
				UsbEndpoint mWritePoint,
				UsbDeviceConnection mDevConnetion)
		{
			mIntf_ = mIntf;
			mReadPoint_ = mReadPoint;
			mWritePoint_ = mWritePoint;
			mDevConnetion_ = mDevConnetion;
			mHandleClaimed_ = false;
		}
	}
	
	
}
