CREATE TABLE   members(
  member_email varchar2(50) ,  --이메일
  member_pass varchar2(100),  --비밀번호
  member_name varchar2(30), --이름
  member_phone char(13),  --전화번호  
  auth_role varchar(10),  --회원구분 
  constraint members_email_pk primary key(member_email)
);


-- 사용자별 Refresh Token을 관리하기 위한 테이블
CREATE TABLE member_refresh_tokens (
    refresh_num NUMBER PRIMARY KEY,               -- 고유 식별자
    member_email VARCHAR2(50),                    -- 외래키 (members.member_email)
    refresh_token CLOB,                           -- 리프레시 토큰
    user_agent VARCHAR2(255),                     -- 브라우저 정보
    ip_address VARCHAR2(50),                      -- IP
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 발급 시간
    CONSTRAINT fk_member_refresh_tokens FOREIGN KEY (member_email) REFERENCES members(member_email)
);

CREATE SEQUENCE member_refresh_tokens_seq 
 START WITH 1 INCREMENT BY 1
 NOCACHE NOCYCLE;

-- MYSQL ===================================================================================
CREATE TABLE members (
  memberEmail VARCHAR(50),
  memberPass VARCHAR(100),
  memberName VARCHAR(30),
  memberPhone CHAR(13),
  memberType INT DEFAULT 1,
  PRIMARY KEY (memberEmail)
);

-- memberType컬럼 삭제
ALTER TABLE members
DROP column memberType;

-- memberType컬럼 추가
ALTER TABLE members
ADD COLUMN  memberType int default 1;

-- default 변경
ALTER TABLE members
ALTER column memberType  SET DEFAULT 1;

DELETE FROM members
WHERE memberType=0;

SELECT * FROM members;



SELECT * FROM board;

DELETE FROM members;

ALTER TABLE board
DROP constraint board_memberEmail;

ALTER TABLE board
ADD CONSTRAINT board_memberEmail FOREIGN KEY(memberEmail) REFERENCES members(memberEmail)
ON DELETE CASCADE;




 






