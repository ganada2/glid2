package com.global_id;

import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class dbservice implements ApplicationRunner{
	 
	  @Autowired
	    DataSource dataSource;
  
	    @Autowired
	    JdbcTemplate jdbcTemplate;

	    
	    // h2 init setting
	    @Override
	    public void run(ApplicationArguments args) throws Exception {

	        try(Connection connection = dataSource.getConnection()){
	        	
	            System.out.println(connection);
	            
	            String URL = connection.getMetaData().getURL();
	            System.out.println(URL);
	            
	            String User = connection.getMetaData().getUserName();
	            System.out.println(User);

	            // Global ID 용 테이블 생성
	            Statement statement = connection.createStatement();
	            String sql = "CREATE TABLE IF NOT EXISTS global_id ( \r\n"
	            		+ "         total_seq varchar(10) not null,\r\n"
	            		+ "       LOGDAY varchar(6) not null,\r\n"
	            		+ "       GUID varchar(30) primary key,\r\n"
	            		+ "       SYSSERIAL varchar(10) not null\r\n"
	            		+ ")";
	            
	            statement.executeUpdate(sql);
	            
	            // sequence 생성 ( total_seq ) 1~9999999999
	            String sql2 = "CREATE SEQUENCE total_seq\r\n"
	            		+ "START WITH 1\r\n"
	            		+ "INCREMENT BY 1\r\n"
	            		+ "MINVALUE 1\r\n"
	            		+ "MAXVALUE 9999999999\r\n"
	            		+ "CYCLE\r\n"
	            		+ "CACHE 1000";
	            
	            statement.executeUpdate(sql2);
	            
	            // sequence 생성 ( guid_seq ) 1~9999
	            String sql2_1 ="CREATE SEQUENCE guid_seq\r\n"
	            		+ "START WITH 1\r\n"
	            		+ "INCREMENT BY 1\r\n"
	            		+ "MINVALUE 1\r\n"
	            		+ "MAXVALUE 9999\r\n"
	            		+ "CYCLE\r\n"
	            		+ "CACHE 1000";
	            statement.executeUpdate(sql2_1);
	            
	            
	            // 기준일자 테이블 생성( 어제 , 오늘 , 내일)
	            String sql3="create table comm_date(\r\n"
	            		+ "	bfday varchar(6) not null,\r\n"
	            		+ "    curday varchar(6) not null,\r\n"
	            		+ "    afday varchar(6) not null\r\n"
	            		+ ")";
	            statement.executeUpdate(sql3);
	            
	            // 기준일자 테이블( 어제 , 오늘 , 내일)
	            String sql4 ="insert into comm_date" 
	            		+ " select to_char(sysdate-1, 'YYMMdd') as bfday, \r\n"
	            		+ " to_char(sysdate, 'YYMMdd') as curday, \r\n"
	            		+ " to_char(sysdate+1, 'YYMMdd') as afday from dual";	            
	            statement.executeUpdate(sql4);
	            
	        }
 
	        jdbcTemplate.execute("insert into global_id values (total_seq.currval,'init', 'test', guid_seq.currval)");
	    }
	    
	    
	    
	    /* 4자리 시퀀스 획득*/
	    public String SQL_getGUID_SEQ() {
	    	
	    Long l_seq = jdbcTemplate.queryForObject("select GUID_SEQ.nextval as SEQ", Long.class);
	    String seq=Long.toString(l_seq);
	    seq=StringUtils.leftPad(seq, 4, "0");
	    	 
	    return seq;
	    }

	    /* 10자리 시퀀스 획득*/
	    public String SQL_getTOTAL_SEQ() {
	    	
	    	 Long l_seq = jdbcTemplate.queryForObject("select TOTAL_SEQ.nextval as SEQ", Long.class);
	    	 String seq=Long.toString(l_seq);
	    	 seq=StringUtils.leftPad(seq, 10, "0");
	    	 
	    	 return seq;
	    }
	    /* 10자리 시퀀스 조회*/
	    public String SQL_selectTOTAL_SEQ() {
	    	
	    	 Long l_seq = jdbcTemplate.queryForObject("select TOTAL_SEQ.currval as SEQ", Long.class);
	    	 String seq=Long.toString(l_seq);
	    	 seq=StringUtils.leftPad(seq, 10, "0");
	    	 
	    	 return seq;
	    }
	    
	    
	    

	    // 일자체크, sysdate = 테이블 데이트가 다르면, 일자 재 설정(시스템일자로 업데이트), 시퀀스 초기화 .
	    // 테이블은 어제. 시스템은 오늘.
	    public void SQL_checkTOTAL_SEQ() {
	    	
	    	String sql="select curday from comm_date";
	    	String comm_today=jdbcTemplate.queryForObject(sql, String.class);
	    	
	    	if(StringUtils.compare(comm_today,getsysdate()) !=0) {
	    		System.out.println("CHECK COMMDATE \n today="+getsysdate()+" comm_today="+comm_today);

	    		// 일자 변경 내용 업데이트
	    		SQL_updateCOMM_DATE();
	    		// 일자 변경 시, 시퀀스 초기화 
	    		SQL_resetTOTAL_SEQ();
	    		
	    	}
	    }
	    
	    
	    public void SQL_resetTOTAL_SEQ() {
	    	// reset 시퀀스
	    	
			/*- increment
			alter sequence GUID_SEQ increment by ('-'||select GUID_SEQ.currval as SEQ)
			-nextval
			select GUID_SEQ.nextval as SEQ
			- increment
			alter sequence GUID_SEQ increment by 1*/
	    	
	    	String sql1="alter sequence TOTAL_SEQ increment by ('-'||select TOTAL_SEQ.currval as SEQ)";
	    	String sql2="select TOTAL_SEQ.nextval as SEQ";
	    	String sql3="alter sequence TOTAL_SEQ increment by 1";
	    	jdbcTemplate.execute(sql1);
	    	jdbcTemplate.execute(sql2);
	    	jdbcTemplate.execute(sql3);
	    	
	    }
	    // 오늘 일자로 업데이트 
	    public void SQL_updateCOMM_DATE() {
	    	
	        // 기준일자 테이블( 어제 , 오늘 , 내일)
            String sql ="insert into comm_date" 
            		+ " select to_char(sysdate-1, 'YYMMdd') as bfday, \r\n"
            		+ " to_char(sysdate, 'YYMMdd') as curday, \r\n"
            		+ " to_char(sysdate+1, 'YYMMdd') as afday from dual";	            
            jdbcTemplate.execute(sql);
	    	
	    }
	    
	    // GUID 테이블 입력 
	    public void SQL_insertGUID(String total_seq, String guid, String seq) {
	    	
	    	String date=getsysdate();
	    	
	    	String sql="insert into global_id values ('"+total_seq+"','"+date+"','"+guid+"','"+seq+"')";
	    	
	    	jdbcTemplate.execute(sql);
	    }
	    
	    public String getsysdate() {
	    	Date d = new Date();
	    	SimpleDateFormat dayFormat = new SimpleDateFormat("yyMMdd");
	    	String dateday = dayFormat.format(d);
	    	return dateday;
	    }
	    
	    public String SQL_updateSYSSERIAL(String guid) {
	    	
	    	// h2에선 안되는듯..  for update wait 10 
	    	String sql="select sysserial from global_id where guid='"+guid+"' for update";
	    //	jdbcTemplate.execute(sql);
	    	long l_result=jdbcTemplate.queryForObject(sql, Long.class);
	    	
	    	l_result = l_result +1;
		    if(l_result >= 9999999999L) {
		    	System.out.println("sysserial reset!");
		    	l_result=0;
		    }
		    String result=StringUtils.leftPad(Long.toString(l_result), 10 , "0");
		    
		    String sql2="update global_id set sysserial='"+result+"' where guid='"+guid+"'";
		    jdbcTemplate.execute(sql2);
	    	
	    	System.out.println("SQL_updateSYSSERIAL"+result);
	    	return result;
	    }
}
