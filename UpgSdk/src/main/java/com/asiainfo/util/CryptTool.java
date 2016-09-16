package com.asiainfo.util;



/**
 * Title:
 * Description:
 * @author yangyh
 */
public class CryptTool
{
    
    /**
     * MD5 摘要计算(byte[]).
     *
     * @param src byte[]
     * @throws java.lang.Exception
     * @return byte[] 16 bit digest
     */
    public static byte[] md5Digest(byte[] src) throws Exception
    {
        java.security.MessageDigest alg =
                java.security.MessageDigest.getInstance("MD5");
        return alg.digest(src);


    }

    /**
     * MD5 摘要计算(String).
     *
     * @param src String
     * @throws java.lang.Exception
     * @return String
     */
    public static String md5Digest(String src) throws Exception
    {
        return byteToHex(md5Digest(src.getBytes()));
        //return new String(md5Digest(src.getBytes()));
    }
    
    public static String md5Digest(String src,String charset) throws Exception
    {
        return byteToHex(md5Digest(src.getBytes(charset)));
    }
    
    public static String byteToHex(byte[] ibArray)
    {
        String idStr="";
        for(int i=0;i<ibArray.length;i++)
        {
            idStr += byteHEX(ibArray[i]);
        }
        return idStr;
    }
    
    public static String byteHEX(byte ib)
    {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
                'B', 'C', 'D', 'E', 'F' };
        char[] ob = new char[2];
        ob[0] = Digit[(ib >>> 4) & 0X0F];
        ob[1] = Digit[ib & 0X0F];
        String s = new String(ob);
        return s;
    }
    
    /**
     * 特殊检验算法
     * @param src
     * @return
     */
    public static String getCheckData(String src)
    {
        byte[] byteArray=src.getBytes();
        int j=byteArray.length/8+1;
        byte[] macArray=new byte[8];
        for(int i=0;i<macArray.length;i++)
            macArray[i]=(byte)("0".charAt(0));
        byte[] dataValue=new byte[j*8];
        for(int i=0;i<dataValue.length;i++)
            dataValue[i]=(byte)("0".charAt(0));
        System.arraycopy(byteArray,0,dataValue,0,byteArray.length);
        for(int i=0;i<j;i++)
        {
            byte[] tempValue=new byte[8];
            for(int m=0;m<8;m++)
            {
                tempValue[m]=dataValue[i*8+m];
            }
            getXorValue(macArray,tempValue);
        }
        return byteToHex(macArray);
//        StringBuffer buf=new StringBuffer();
//        for(int i=0;i<macArray.length;i++)
//        {
//            char cha=(char)macArray[i];
//            buf.append(cha);
//        }
//        return buf.toString();
    }
    
    private static void getXorValue(byte[] macArray,byte[] tempValue)
    {
        int i;
		for ( i = 0; i < 8; i ++ ) 		
		    macArray[i] ^= tempValue[i];
    }
    
    /**
     * 左加补零算法
     * @param str
     * @param length
     * @return
     */
    public static  String AddLeft0(String str,int length)
    {
        if (str==null)
            str="";
        boolean haveSign=false;
        if (!str.equalsIgnoreCase("") && str.substring(0,1).equals("-"))
        {
            length=length-1;
            str=str.substring(1);
            haveSign=true;
        }
        str=str.trim();
        while (str.length()<length)
    		str="0"+str;
        if (haveSign)
            str="-"+str;
        return str;
    }
    
    public static void main(String[] args) throws Exception
    {
        String str = "谢俊12343253456436jhgvjmh";
        System.out.println(CryptTool.md5Digest(str));
    }
}
