package com.global_id.controller;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.global_id.*;


@RestController
public class controller {

	  @Autowired
	   private dbservice db;

		
		/*1. 신규 생성 GUID,SEQ
		 * METHOD: POST
		 * 기능 : GUID,SEQ 신규 생성
		 * CASE: 타 시스템 생성 요청일때
		 * INPUT: layer[2]
		 */
		@PostMapping("/create/GUID")
		public String createGUID( @RequestParam("layer") String layer) {
					
			guid guid = new guid();
					
			String serial           = db.SQL_getGUID_SEQ();
			String return_guid      = guid.createGUID(layer,serial);
			String return_sysserial = guid.createSYSSERIAL("1");	// seq 초기값 1
			
			
			db.SQL_checkTOTAL_SEQ();
			
			String total_seq        = db.SQL_getTOTAL_SEQ();
			db.SQL_insertGUID(total_seq, return_guid, return_sysserial);
			
			String Final_guid =return_guid+'-'+return_sysserial;
			return Final_guid;
		}

		/*1-1. 신규 생성 GUID,SEQ
		 * METHOD: GET
		 * 기능 : GUID,SEQ 신규 생성
		 * CASE: 자체 시스템에서 GUID,SEQ 생성 필요할 때
		 * INPUT: 없음
		 */
		@GetMapping("/create/SELFGUID")
		public String createSELFGUID() {
			
			guid guid = new guid();
			
			String serial           = db.SQL_getGUID_SEQ();
			String return_guid      = guid.createGUID("",serial);
			String return_sysserial = guid.createSYSSERIAL("1");	// seq 초기값 1
			
			db.SQL_checkTOTAL_SEQ();
			
			String total_seq        = db.SQL_getTOTAL_SEQ();
			db.SQL_insertGUID(total_seq, return_guid, return_sysserial);
			
			String Final_guid =return_guid+'-'+return_sysserial;
			return Final_guid;
		}
		
		
		/*2. sysserial 증분시 사용
		 * METHOD: POST
		 * 기능 : sysserial +1 증분, 시퀀스 획득하여, 업데이트 된 sysserial 리턴
		 * CASE: 피 호출 받는 시스템(타 시스템에서 온 요청 처리)에서 현재 GUID의 sysserial +1하여 시작
		 * INPUT: globalid[30]
		 * OUTPUT : GUID[30], sysserial[10]
		 */
		@PostMapping("/update/SYSSERIAL")
		public String updateSYSSERIAL( @RequestParam("globalid") String globalid) {
			
			String return_guid = globalid;  
			
			//=SQL_updateSYSSERIAL(globalid);
			
			String return_sysserial=db.SQL_updateSYSSERIAL(globalid);
			
			String Final_guid  = return_guid+'-'+return_sysserial;
			return Final_guid;
		}
		
		
		/*3. 현재 시퀀스 조회
		 * METHOD: POST
		 * 기능 : GUID,일자의 시퀀스 값 리턴
		 * CASE: 오늘 또는 입력일의 GUID의 시퀀스가 궁금할때
		 * INPUT: globalid[30], 일자[6]
		 * OUTPUT : GUID[30], seq[10]
		 */
		@GetMapping("/select/TOTALSEQ")
		public String TOTALSEQ() {
			// redis 업데이트 시, 일자+구분자+seq로 구성-> guid key 조회 시, 일자 seq 확인 가능
			// 시스템간 이동하다가 일자가 바뀔경우 seq 1로 초기화, guid일자와 sytem 일자 비교
			// seq 1 초기화 시 , redis key 에 속성값들 모두 del
			// 혹은 last key만 업데이트 
			
			String seq=db.SQL_selectTOTAL_SEQ();
			return seq;
		}
		/*3-1.total seq 초기화*/
		@GetMapping("/reset/TOTALSEQ")
		public String resetTOTALSEQ() { 
			
			db.SQL_resetTOTAL_SEQ();
			return "totalseq reset!";
		}
		
		
		
		
		
		
		
		/*4. 업데이트 SERIAL(deprecated)
		 * METHOD: POST
		 * 기능 : SERIAL +1 증분
		 * CASE: 시스템 내부에서 호출하면서 증분할 경우가 필요할 경우 GUID내 SERIAL +1 하여 사용 
		 * INPUT: globalid[30], seq[10]
		 */
		@PostMapping("/update/SERIAL")
		public String updateSERIAL(@RequestParam("globalid") String globalid, @RequestParam("seq") String sequence) {
			// 시리얼 업데이트 전, GUID길이 30 맞는지 검증
			// 마지막 3자리만 업데이트 string to long , long to string
			// 999에 0으로 초기화
			// 시스템 내부에서 serial 증분, 시스템 간은 sequence 증분
			guid guid = new guid();
			
			String return_seq = sequence;
			String return_guid= guid.updateSERIAL(globalid);
			String Final_guid  = return_guid+'-'+return_seq;
			return Final_guid;
		}
		
		@GetMapping("/test")
		public void index7() { 
			//String seq=db.SQL_getSEQUENCE();
			//Object seq = h2runner.SQL_getSequence2();
			
			//db.SQL_insertGUID("A", "B","C");
			//db.SQL_resetTOTAL_SEQ();
		
		}
		
}