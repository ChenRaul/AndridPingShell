package com.example.androidping;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;

import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;



import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class NetAndServerDetection {
	private Context context;
	public NetAndServerDetection(Context context){
		this.context = context;
	}

	/**
	 *检测网络是否连接
	 * @return true，可用；false，不可用
	 */
	public boolean isOpenNetwork(){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager.getActiveNetworkInfo() == null){
			return false;
		}
		return connectivityManager.getActiveNetworkInfo().isAvailable();
	}
	/*获取mac地址*/
	public String getMacAddress(){  
			    String strMacAddr = null;  
			    try {  
			        InetAddress ip = getLocalInetAddress();  
			  
			        byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();  
			        StringBuffer buffer = new StringBuffer();  
			        for (int i = 0; i < b.length; i++) {  
			            if (i != 0) {  
			                buffer.append('-');  
			            }  
			  
			            String str = Integer.toHexString(b[i] & 0xFF);  
			            buffer.append(str.length() == 1 ? 0 + str : str);  
			        }  
			        strMacAddr = buffer.toString().toUpperCase();  
			    } catch (Exception e) {  
			        // TODO Auto-generated catch block  
			        e.printStackTrace();  
			    }  
			  
			    return strMacAddr;  
			} 
		/*getLocalInetAddress方法为getMacAddress方法调用*/
	public  InetAddress getLocalInetAddress() {  
	    InetAddress ip = null;  
	    try {  
	        Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();  
	        while (en_netInterface.hasMoreElements()) {  
	            NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();  
	            Enumeration<InetAddress> en_ip = ni.getInetAddresses();  
	            while (en_ip.hasMoreElements()) {  
	                ip = en_ip.nextElement();  
	                if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)  
	                    break;  
	                else  
	                    ip = null;  
	            }  
	  
	            if (ip != null) {  
	                break;  
	            }  
	        }  
	    } catch (SocketException e) {  
	        // TODO Auto-generated catch block  
	        e.printStackTrace();  
	    }  
	    return ip;  
	}  		
}
