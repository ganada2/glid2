package com.global_id;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.global_id.dbservice;


public class guid {
	
	   @Autowired
	    private dbservice db2;
	
	  	private String layer;
	  	private String dateday;
	    private String datetime;
	    private String datetime2;
	    private String hostname;
	    private String pid;
	    private String serial;
	    private String guid; 		/* GUID */
	    
	    private String sequence;
	    
	    /* GUID 포멧 30자리
	    Layer         // layer      	[2] AN,IO
	    일자(YYMMDD)   // dateday    		[6] 8
	    시간(HHmmSS)   // datetime  		[6] 14
	    host_id       // hostname  		[5] 19
	    pid           // pid      		[7] 26
	    serial(일련번호,시퀀스)// serial    [4] 30
	    
	    증분일련번호    //   sysserial     [10] 1~9,999,999,999
		*/
	    
	    public String createGUID(String layer, String serial){
	    	
	    	/*Layer ( 요청시스템 구분, 혹은 처리업무 구분으로 맵핑 )
	    	 * layer에 입력이 있을 경우, 해당 입력으로 맵핑
	    	 * 없을경우 (자체생성)  자체 생성 layer입력 , 자체 KK
	    	 */
	    	//-- [layer]
	    	if(StringUtils.isEmpty(layer)){
	    		layer="KK";
	    	}
	    	else {
	    		layer=layer;
	    	}
	    	
	    	//-- [dateday]
	    	Date d = new Date();
	    	SimpleDateFormat dayFormat = new SimpleDateFormat("yyMMdd");
	    	dateday=dayFormat.format(d);
	    	
	    	//-- [datetime]
	    	SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
	    	//SimpleDateFormat timeFormat2 = new SimpleDateFormat("SS");
	    	//datetime+Millisec (+ 0 leftpadding)
	    	//datetime2=timeFormat2.format(d);
	    	//Long datetime3 = Long.parseLong(datetime2);
	    	//datetime2=String.format("%03d",datetime3 );
	    	
	    	
	    	datetime=timeFormat.format(d);
	    	
	    	/* [host_id] */
	    	try {
				hostname=getHostName();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	/* [pid] */
	    	// String.format("%07d",12345678); -- pid는 최대 7자리 2^22
	    	pid = StringUtils.leftPad(Long.toString(ProcessHandle.current().pid()) , 7, "0");
	    	
	    	/* [serial] */
	    	//serial[4] GUID_SEQ 시퀀스에서 획득
	    	if(StringUtils.isEmpty(serial)){
	    		serial="0001";
	    	}
	    	else {
	    		serial=serial;
	    	}
	    	
	    	// 4자리 변환
	    	serial = StringUtils.leftPad(serial,4, "0");
	    	
	    	
	    	/* [GUID] */
	    	//guid=layer+'/'+dateday+'/'+datetime+'/'+hostname+'/'+pid+'/'+serial;
	    	guid=layer+dateday+datetime+hostname+pid+serial;
	    	return guid;
	    }
	    
	    public String getHostName () throws Exception{
	    	String host;
	    	InetAddress iaddr = InetAddress.getLocalHost();
	    	
	    	try {
	    		host = iaddr.getHostName();
	    	}
	    	catch (RuntimeException e){
	    		host="kakao";
	    	}
	    	finally {
	    		//host="kakao";
	    	}
	    	
	    	// host name 길이 조정
	    	if(host.length()==5) {
	    	    return host;
	    		}
	    	else if( host.length()<5 || host.length()<0 ){
	    		host=StringUtils.leftPad(host, 5, "0");	  
	    		
	    		}
	    	else if(5<host.length()) {
	    		//host=StringUtils.substring(host, 0, 4);
	    		host=StringUtils.substring(host, 0, 5);
	    		return host;
	    		}
	    	else {
	    		host="kakao";
	    		}
	    	
	    	return host;
	    }
	    
	    
	    
	    public String createSYSSERIAL(String sysserial){
	    	
	    	if(StringUtils.isEmpty(sysserial)) {
	    		sysserial="1";
	    	}
	    	return StringUtils.leftPad(sysserial, 10, "0");
	    	
	    }
	    
	    //@Deprecated
	    public String updateSYSSERIAL(String globalid, String sysserial){
	    	
	    	/* seq 빈값일때
	    	 * */
	    	if(StringUtils.isEmpty(sysserial)) {
	    		sysserial="1";
	    	}
	    	/* 최대값 체크 로직
	    	 * 9,999,999,999 와 같을 시 강제 초기화
	    	 */
		    Long maxsysserial = Long.parseLong(sysserial);	
		    if(maxsysserial >= 9999999999L) {
		    	sysserial="0";
		    }	
		    
	    	/* 일자 체크 로직, guid일자, 오늘날짜, 어제날짜 함께 비교*/
	    	// guid 일자[6] 포지션 2-8
	    	String guid_date=StringUtils.substring(globalid, 2, 8);
	    	// 오늘일자 
	    	Date d = new Date();
	    	SimpleDateFormat dayFormat = new SimpleDateFormat("yyMMdd");
	    	String sys_date=dayFormat.format(d);
	    	// 어제날짜
	    	Date d2 = new Date();
	    	d2 = new Date(d2.getTime()+(1000*60*60*24*-1));
	    	SimpleDateFormat dayFormat2 = new SimpleDateFormat("yyMMdd");
	    	String ys_date=dayFormat2.format(d2);
	    	System.out.println("ys_date="+ys_date);
	    	
	    	/* guid 와 로직 수행 시점 간 일자 비교
	    	 * 조건 :1. guid와 시스템 일자가 같지않으면 초기화 대상
	    	 *      2. 어제일자와 guid일자가 같으면 1로 초기화
	    	 */
	    	if(StringUtils.compare(guid_date, sys_date)!=0 ) {
				/*
				 * System.out.println("guid_date="+guid_date);
				 * System.out.println("sys_date="+sys_date);
				 * System.out.println("ys_date="+ys_date);
				 */
	    		if(StringUtils.compare(ys_date, guid_date) == 0 ) {
	    			// 0 으로 초기화하면 아래 로직에서 +1
	    			System.out.println("b");
	    			sequence="0";
	    		}
	    	}
	    	
	    	// 계산 전 str -> long로 형 변환 후 계산
	    	Long bufsysserial=Long.parseLong(sysserial);
	    	bufsysserial = bufsysserial + 1;
	    	//System.out.println("bufseq="+bufsysserial);
	    	//long -> str 형 변환
	    	sysserial=Long.toString(bufsysserial);
	    	return StringUtils.leftPad(sysserial, 10, "0");
	    	
	    }
	    
	    
	    //@Deprecated
	    public String updateSERIAL(String globalid){
			/*
			 * 시리얼 업데이트 전, 마지막 3자리만 업데이트 string to long , long to string
			 * 999에 0으로 초기화 시스템 내부에서 serial 증분, 시스템 간은 sequence 증분
			 */
	    		    	
	    	Long bufserial=Long.parseLong(StringUtils.substring(globalid, 27, 30));
	    	if(bufserial>=999) {
	    			bufserial=(long)0;
	    		}
	    	
	    	// SERIAL +1증분
	    	bufserial = bufserial + 1;
	    	String serial=StringUtils.leftPad(Long.toString(bufserial), 3, "0");
	    	
	    	String bufglobalid=StringUtils.substring(globalid, 0, 27);
	    	
	    	globalid=bufglobalid+serial;
	    	
	    	return globalid;
	    }
	    	
	    
	    
	    
	    
	    
	    public String getGuid() {
	        return guid;
	    }

	    public void setGuid(String guid) {
	        this.guid = guid;
	    }

	    
	    public String getSequence() {
	        return createSYSSERIAL("");
	    }
	    public void setSequence(String sequence) {
	        this.sequence = sequence;
	    }
		
}
