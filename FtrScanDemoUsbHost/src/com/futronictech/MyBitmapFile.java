package com.futronictech;


public class MyBitmapFile
{
    private BITMAPFILEHEADER m_fileHeaderBitmap;
    private BITMAPINFO m_infoBitmap;
    private byte[] m_BmpData;

    public MyBitmapFile()
    {
        m_fileHeaderBitmap = new BITMAPFILEHEADER();
        m_infoBitmap = new BITMAPINFO();
    }

    public MyBitmapFile(int nWidth, int nHeight, byte[] pImage)
    {
        m_fileHeaderBitmap = new BITMAPFILEHEADER();
        m_infoBitmap = new BITMAPINFO();

        int length = m_fileHeaderBitmap.sizeof() + m_infoBitmap.sizeof() + nWidth * nHeight;
        m_fileHeaderBitmap.bfSize = length;
        m_fileHeaderBitmap.bfOffBits = m_fileHeaderBitmap.sizeof() + m_infoBitmap.sizeof();
        m_infoBitmap.bmiHeader.biWidth = nWidth;
        m_infoBitmap.bmiHeader.biHeight = nHeight;

        m_BmpData = new byte[length];
        byte[] TempData = m_fileHeaderBitmap.toBytes();
        System.arraycopy(TempData, 0, m_BmpData, 0, TempData.length);
        int offset = TempData.length;
        TempData = m_infoBitmap.bmiHeader.toBytes();
        System.arraycopy(TempData, 0, m_BmpData, offset, TempData.length);
        offset += TempData.length;
        TempData = m_infoBitmap.bmiColors.GetGRBTableByteData();
        System.arraycopy(TempData, 0, m_BmpData, offset, TempData.length);
        offset += TempData.length;
        //rotate image
        byte[] pRotateImage = new byte[nWidth * nHeight];
        int nImgOffset = 0;
        for (int iCyc = 0; iCyc < nHeight; iCyc++)
        {
        	System.arraycopy(pImage, (nHeight - iCyc - 1) * nWidth, pRotateImage, nImgOffset, nWidth);
            nImgOffset += nWidth;
        }       
        System.arraycopy(pRotateImage, 0, m_BmpData, offset, nWidth * nHeight);
        pRotateImage = null;
        TempData = null;
    }

    public byte[] toBytes()
    {
        return m_BmpData;
    }
    
    public class BITMAPFILEHEADER
    {
        public short bfType;
        public int bfSize;
        public short bfReserved1;
        public short bfReserved2;
        public int bfOffBits;

        public BITMAPFILEHEADER()
        {
            bfType = 'B' + 'M' * 0x100;
            bfReserved1 = bfReserved2 = 0;
        }
        
        public int sizeof()
        {
        	return (14);
        }
        
        public byte[] toBytes()
        {
            byte[] m_Data = new byte[14];
            byte[] bytes = convet2bytes.short2bytes(bfType);
            System.arraycopy(bytes, 0, m_Data, 0, 2);
            bytes = convet2bytes.int2bytes(bfSize);
            System.arraycopy(bytes, 0, m_Data, 2, 4);
            bytes = convet2bytes.short2bytes(bfReserved1); 
            System.arraycopy(bytes, 0, m_Data, 6, 2);
            bytes = convet2bytes.short2bytes(bfReserved2); 
            System.arraycopy(bytes, 0, m_Data, 8, 2);
            bytes = convet2bytes.int2bytes(bfOffBits);
            System.arraycopy(bytes, 0, m_Data, 10, 4);
            return m_Data;
        }
    };

    public class RGBQUAD
    {
        public byte rgbBlue;
        public byte rgbGreen;
        public byte rgbRed;
        public byte rgbReserved;

        public RGBQUAD()
        {
            rgbReserved = 0;
        }
 
        public byte[] GetGRBTableByteData()
        {
            byte[] m_Data = new byte[256 * 4];
            int nOffset = 0;
            for (int i = 0; i < 256; i++)
            {
                m_Data[nOffset] = (byte)i;
                m_Data[nOffset + 1] = (byte)i;
                m_Data[nOffset + 2] = (byte)i;
                m_Data[nOffset + 3] = (byte)i;
                nOffset += 4;
            }
            return m_Data;
        }
    };

    public class BITMAPINFOHEADER
    {
    	public int biSize;
    	public int biWidth;
    	public int biHeight;
    	public short biPlanes;
    	public short biBitCount;
    	public int biCompression;
    	public int biSizeImage;
    	public int biXPelsPerMeter;
    	public int biYPelsPerMeter;
    	public int biClrUsed;
        public int biClrImportant;

        public BITMAPINFOHEADER()
        {
            biPlanes = 1;
            biBitCount = 8;
            biCompression = 0;  //BI_RGB; #define BI_RGB        0L
            biSizeImage = 0;
            biClrUsed = biClrImportant = 0;
            biXPelsPerMeter = 0x4CE6;	//500DPI
            biYPelsPerMeter = 0x4CE6;	//500DPI
            biSize = 40;
        }
        
        public byte[] toBytes()
        {
            byte[] m_Data = new byte[40];
            byte[] bytes = convet2bytes.int2bytes(biSize);
            System.arraycopy(bytes, 0, m_Data, 0, 4);
            bytes = convet2bytes.int2bytes(biWidth);
            System.arraycopy(bytes, 0, m_Data, 4, 4);
            bytes = convet2bytes.int2bytes(biHeight); 
            System.arraycopy(bytes, 0, m_Data, 8, 4);
            bytes = convet2bytes.short2bytes(biPlanes); 
            System.arraycopy(bytes, 0, m_Data, 12, 2);
            bytes = convet2bytes.short2bytes(biBitCount);
            System.arraycopy(bytes, 0, m_Data, 14, 2);
            bytes = convet2bytes.int2bytes(biCompression);
            System.arraycopy(bytes, 0, m_Data, 16, 4);
            bytes = convet2bytes.int2bytes(biSizeImage); 
            System.arraycopy(bytes, 0, m_Data, 20, 4);
            bytes = convet2bytes.int2bytes(biXPelsPerMeter);
            System.arraycopy(bytes, 0, m_Data, 24, 4);
            bytes = convet2bytes.int2bytes(biYPelsPerMeter); 
            System.arraycopy(bytes, 0, m_Data, 28, 4);
            bytes = convet2bytes.int2bytes(biClrUsed);
            System.arraycopy(bytes, 0, m_Data, 32, 4);
            bytes = convet2bytes.int2bytes(biClrImportant); 
            System.arraycopy(bytes, 0, m_Data, 36, 4);            
            return m_Data;
        }
    }

    public class BITMAPINFO
    {
        public BITMAPINFOHEADER bmiHeader;
        public RGBQUAD bmiColors;

        public BITMAPINFO()
        {
            bmiHeader = new BITMAPINFOHEADER();
            bmiColors = new RGBQUAD();
        }
        public int sizeof()
        {
            return (bmiHeader.biSize + 4 * 256); 
        }
    }
    
    public static class convet2bytes
    {
    	public static byte[] short2bytes(short s)
    	{
    		byte[] ret = new byte[2];
    		ret[0] = (byte)(s & 0x00ff);
    		ret[1] = (byte)((s & 0xff00) >> 8 );
    		return ret;
    	}
    	public static byte[] int2bytes(int i)
    	{
    		byte[] ret = new byte[4];
    		ret[0] = (byte)(i & 0xff);
    		ret[1] = (byte)((i & 0xff00) >> 8 );
    		ret[2] = (byte)((i & 0xff0000) >> 16 );
    		ret[3] = (byte)((i & 0xff000000) >> 24 );
    		return ret;    		
    	}    
    }
}

