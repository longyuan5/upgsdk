package com.asiainfo.util;


import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class ApkSignUtil {
	private static char[] nonceChars={'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9'};

	public static String  randomString(int len){
		StringBuilder sb=new StringBuilder();
		Random ran=new Random();
		for(int i=0;i<len;i++){
			sb.append(nonceChars[ran.nextInt(nonceChars.length)]);
		}
		return sb.toString();
	}
	
	public static boolean verify(Map<String,String> map,String key) throws Exception{
		SortedMap<String, String> sMap=paraFilter(map);
		String text=sortString(sMap);
		return verify(text, map.get("sign"), map.get("sign_type"), key, map.get("input_charset"));
	}
	
	public static boolean verify(String text, String sign,String sign_type, String key, String input_charset) throws Exception {
		if("md5".equals(sign_type)||"Md5".equals(sign_type)||"MD5".equals(sign_type)){
			return key.equals(CryptTool.md5Digest(text+key, input_charset));
		}else{
			return false;
		}
	}
	public static Map<String,String> sign(Map<String,String> map,String sign_type,String key, String input_charset) throws Exception{
		Map<String,String> result=map;
		SortedMap<String, String> sMap=paraFilter(map);
		String text=sortString(sMap);
		System.out.println(text+key);
		System.out.println(CryptTool.md5Digest(text+key, input_charset));
		result.put("sign", CryptTool.md5Digest(text+key, input_charset));
		return result;
	}
	private static SortedMap<String, String> paraFilter(Map<String, String> sArray) {

    	SortedMap<String, String> result = new TreeMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }
    private static String sortString(SortedMap<String,String> sMap){
    	StringBuilder sb=new StringBuilder();
    	for(String key:sMap.keySet()){
    		sb.append(sMap.get(key));
    	} 
    	return sb.toString();
    }
}
