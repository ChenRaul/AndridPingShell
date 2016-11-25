package com.example.androidping;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import com.android.internal.telephony.TelephonyIntents;
import com.example.androidping.ShellUtils.CommandResult;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Telephony;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Button bt;
	private EditText et;
	private TextView tv;
	Handler handler;
	private CommandResult commandReslut;//ִ��shell����ķ��ؽ��
	private boolean isPing = true;
	private int pingCount= 0;//ping�Ĵ���
	private int pingSuccessCount=0;//ping�ɹ��Ĵ���
	private long tempTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		bt = (Button) findViewById(R.id.bt);
		tv = (TextView) findViewById(R.id.tv);
		et = (EditText) findViewById(R.id.srcIp);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.obj.equals("btfalse")){
					bt.setEnabled(false);
				}else  if(msg.obj.equals("bttrue")){
					bt.setEnabled(true);
				}else{
					tv.setText((CharSequence) msg.obj);
				}
			}
		};
		bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String srcIp = et.getText().toString().trim();
				new Thread() {
					public void run() {
						String pingIp = srcIp;
						NetAndServerDetection nasd = new NetAndServerDetection(MainActivity.this);
						Process process = null;
						InputStream instream = null;
						BufferedReader bufferReader = null;
						Message msg1 = new Message();
						msg1.obj = "btfalse";
						handler.sendMessage(msg1);
						long startPingTime= System.currentTimeMillis();
						while (isPing) {
							try {
								/*��Ҳ��һ��ִ��shell����ķ��������Ե������*/
								process = Runtime.getRuntime().exec("ping -c 1 -w 1 " + pingIp);
								instream = process.getInputStream();
								bufferReader = new BufferedReader(new InputStreamReader(instream, "GBK"));
								StringBuilder pingString = new StringBuilder("");
								String str3;
								while ((str3 = bufferReader.readLine()) != null) {
									pingString.append(str3 + "\n");
									Message msg = new Message();
									msg.obj = pingString.toString();
									handler.sendMessage(msg);
								}
								/*ִ��������shell����ķ���״̬��0�������������಻����;
								 * ������������б䣺��˴���ִ��ping����3�Σ������һ��pingͨ�ˣ�
								 * ��������ûͨ�᷵��1��pingͨ���λ᷵��2*/
								int status = process.waitFor();
								pingCount++;
								System.out.println("status = " + status);
								if (status == 0) {
									long endPingTime = System.currentTimeMillis();
									System.out.println("pingͨʱ�䣺"+(endPingTime-startPingTime));
									pingSuccessCount++;
									String ip = nasd.getLocalInetAddress().toString();
									Message msg = new Message();
									msg.obj = "PING SUCCESS������IP��ַ��:"+ ip.substring(1, ip.length())+"\n"+
											"ʱ��:"+(endPingTime-startPingTime)/1000+" s"+
											"\n"+"��PING����:"+pingCount+"\n"+"��PING�ɹ�����:"+pingSuccessCount;
									handler.sendMessage(msg);
									break;
								} else {
									//turnOnAirplaneMode();//��������ģʽ
									//setMobileData(MainActivity.this, false);//�ر��ƶ���������
									openLTE(false);
									Thread.sleep(1000);
									openLTE(true);
									//setMobileData(MainActivity.this, true);//�����ƶ���������
									//turnOffAirplaneMode();//�رշ���ģʽ
									Thread.sleep(1000);
									while (true) {
										if(nasd.isOpenNetwork()){
											String ip;
											while ((ip = nasd.getLocalInetAddress().toString()) != null) {
												Message msg = new Message();
												msg.obj = "�����������ӣ������»�ȡ��IP��"+ip.substring(1, ip.length());
												handler.sendMessage(msg);
												break;
											}
											break;
										}
									}
									
								}
							} catch (IOException e1) {
								System.out.println("e1.printStackTrace()");
							} catch (InterruptedException e) {
								e.printStackTrace();
							} 
							process.destroy();
						
							try {
								bufferReader.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						pingCount =0;
						pingSuccessCount = 0;
						Message msg2 = new Message();
						msg2.obj = "bttrue";
						handler.sendMessage(msg2);
					};
				}.start();
			}
		});
	}
	/** 
	 * �����ֻ����ƶ����� ���磬ͨ��������ʵ�ֵ�
	 */  
	public static void setMobileData(Context pContext, boolean pBoolean) {  
	    try {  
	        ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
	        Class ownerClass = mConnectivityManager.getClass();  
	        Class[] argsClass = new Class[1];  
	        argsClass[0] = boolean.class;  
	        Method method = ownerClass.getMethod("setMobileDataEnabled", argsClass);  
	        method.invoke(mConnectivityManager, pBoolean);  
	    } catch (Exception e) {  
	        // TODO Auto-generated catch block  
	        e.printStackTrace();  
	        System.out.println("�ƶ��������ô���: " + e.toString());  
	    }  
	}  
	  /*����4G, ���ֻ�ʺ���ͨ�Ĺ��̻�*/
	private void openLTE(boolean isOpen){
		System.out.println("isOpen:"+isOpen);
		Intent intent = new Intent(TelephonyIntents.ACTION_LTE_ENABLED);
		intent.putExtra("enabled",isOpen);
		sendBroadcast(intent);
	}
	/** 
	 * �����ֻ��ƶ����ݵ�״̬ ��ͨ������ʵ��
	 * 
	 * @param pContext 
	 * @param arg    Ĭ����null 
	 * @return true ���� false δ���� 
	 */  
	public static boolean getMobileDataState(Context pContext, Object[] arg) {  
	    try {  
	        ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
	        Class ownerClass = mConnectivityManager.getClass();  
	        Class[] argsClass = null;  
	        if (arg != null) {  
	            argsClass = new Class[1];  
	            argsClass[0] = arg.getClass();  
	        }  
	        Method method = ownerClass.getMethod("getMobileDataEnabled", argsClass);  
	        Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);  
	        return isOpen;  
	  
	    } catch (Exception e) {  
	        // TODO: handle exception  
	        System.out.println("�õ��ƶ�����״̬����");  
	        return false;  
	    }  
	  
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isPing =false;
		//android.os.Process.killProcess(android.os.Process.myPid());
		
	}
	/*�رշ���ģʽ,��ҪrootȨ��*/
	private int turnOffAirplaneMode() {
		// /*4.2�汾��ǰ��*/
		// Settings.System.putInt(getContentResolver(),
		// Settings.System.AIRPLANE_MODE_ON, 0);
		// Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		// intent.putExtra("state", false);
		// sendBroadcast(intent);
		 
		ShellUtils.execCommand("settings put global airplane_mode_on 0", true,true);
		commandReslut = ShellUtils.execCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false",true, true);
		return commandReslut.result;
	}
	@SuppressLint("NewApi")
	/*��������ģʽ,��ҪrootȨ��*/
	private int turnOnAirplaneMode() {
		/* 4.2�汾��ǰ�� */
		// Settings.System.putInt(getContentResolver(),
		// Settings.System.AIRPLANE_MODE_ON, 1);
		// Intent localIntent1 = new
		// Intent("android.intent.action.AIRPLANE_MODE").putExtra("state",
		// true);
		// sendBroadcast(localIntent1);
		// 
		ShellUtils.execCommand("settings put global airplane_mode_on 1", true,true);
		commandReslut = ShellUtils.execCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true",true, true);
		return commandReslut.result;
	}
	/**
	 * ��ȡ�ֻ�ip��ַ
	 * 
	 * @return
	 */ 
	public static String getPhoneIp() { 
		try { 
			    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
			      NetworkInterface intf = en.nextElement(); 
			      int i = 0;
			      for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) { 
			         InetAddress inetAddress = enumIpAddr.nextElement(); 
			         System.out.println((i++)+":"+inetAddress.getHostAddress().toString());
			         if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) { 
			         return inetAddress.getHostAddress().toString(); 
			      } 
			    } 
			  } 
			  } catch (SocketException ex) { 
			     // Log.e(LOG_TAG, ex.toString()); 
			  } 
			  return null;
	}
	/**
	 * �õ���ǰ���ֻ���������
	 * 
	 * @param context
	 * @return
	 */ 
	public static String getCurrentNetType(Context context) { 
	    String type = ""; 
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
	    NetworkInfo info = cm.getActiveNetworkInfo(); 
	    if (info == null) { 
	        type = "null"; 
	    } else if (info.getType() == ConnectivityManager.TYPE_WIFI) { 
	        type = "wifi"; 
	    } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) { 
	        int subType = info.getSubtype(); 
	        if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS 
	                || subType == TelephonyManager.NETWORK_TYPE_EDGE) { 
	            type = "2g"; 
	        } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA 
	                || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0 
	                || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) { 
	            type = "3g"; 
	        } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE��3g��4g�Ĺ��ɣ���3.9G��ȫ���׼ 
	            type = "4g"; 
	        } 
	    } 
	    return type; 
	}
}
