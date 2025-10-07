package com.example.spring03_shop.members.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.spring03_shop.config.auth.PrincipalDetails;
import com.example.spring03_shop.config.jwt.JwtTokenProvider;
import com.example.spring03_shop.members.dto.AuthInfo;
import com.example.spring03_shop.members.dto.MembersDTO;
import com.example.spring03_shop.members.entity.MembersEntity;
import com.example.spring03_shop.members.repository.MembersRepository;
import com.example.spring03_shop.members.service.AuthService;
import com.example.spring03_shop.members.service.MembersService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@CrossOrigin(origins ={"http://localhost:3000"})
//@CrossOrigin("*")

@RestController
public class MembersController {

	private final MembersRepository membersRepository;

	@Autowired
	private MembersService membersService;

	@Autowired
	private AuthService authService;

	@Autowired
	private BCryptPasswordEncoder encodePassword;


	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	public MembersController(MembersRepository membersRepository) {
		this.membersRepository = membersRepository;

	}

	// 회원가입
	@PostMapping(value = "/member/signup")
	public ResponseEntity<AuthInfo> addMember(@RequestBody MembersDTO membersDTO) {
		membersDTO.setMemberPass(encodePassword.encode(membersDTO.getMemberPass()));
		AuthInfo authInfo = membersService.addMemberProcess(membersDTO);
		return ResponseEntity.ok(authInfo);
	}

	// http://localhost:8090/member/editinfo/dong@google.com
	// 회원정보 가져오기
	@PreAuthorize("isAuthenticated()")
	// @PreAuthorize("hasRole('USER')")
	// @PreAuthorize("hasRole('ADMIN')")
	// @PreAuthorize("hasAnyRole('ADMIN','USER')")
	// @PreAuthorize("principal.username == #memberEmail")
	// @PreAuthorize("hasAnyRole('ADMIN','USER') and principal.username ==
	// #memberEmail")
	@GetMapping(value = "/member/editinfo/{memberEmail}")
	public ResponseEntity<MembersDTO> getMember(@PathVariable("memberEmail") String memberEmail,
			@AuthenticationPrincipal PrincipalDetails principal) {

		// public ResponseEntity<MembersDTO> getMember(@PathVariable("memberEmail")
		// @org.springframework.data.repository.query.Param("memberEmail") String
		// memberEmail, @AuthenticationPrincipal PrincipalDetails principal ){

		log.info("path memberEmail => {}", memberEmail);
		log.info("principal={}", principal.getUsername());

		PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		String authenticatedUsername = principalDetails.getUsername();
		log.info("authenticatedUsername=> {}", authenticatedUsername);

		MembersDTO memDTO = membersService.getByMemberProcess(memberEmail);
		return ResponseEntity.ok(memDTO);
	}

	// 회원정보 수정
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@PutMapping(value = "/member/update")
	public ResponseEntity<AuthInfo> updateMember(@RequestBody MembersDTO membersDTO) {
		membersDTO.setMemberPass(encodePassword.encode(membersDTO.getMemberPass()));
		return ResponseEntity.ok(membersService.updateMemberProcess(membersDTO));
	}

	// 회원탈퇴
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@DeleteMapping(value = "/member/delete/{memberEmail}")
	public ResponseEntity<Void> deleteMember(@PathVariable("memberEmail") String memberEmail) {
		membersService.deleteMemberProcess(memberEmail);
		return ResponseEntity.ok(null);
	}
	

	
	
	
	// 로그아웃	
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@DeleteMapping(value = "/member/logout")
	public ResponseEntity<?> logout(@RequestHeader("Authorization-refresh") String refreshToken){
		log.info("refreshToken: {}", refreshToken);		
		 
		//DecodedJWT decoded = JWT.require(Algorithm.HMAC512("mySecurityCos")).build().verify(refreshToken);
		//System.out.println("Claims: " + decoded.getClaims()); // 👈 실제로 있는지 확인
		
		String email = JWT.require(Algorithm.HMAC512("mySecurityCos"))
			.build().verify(refreshToken).getClaim("memberEmail").asString();
		
	    //String email = jwtTokenProvider.getEmailFromToken(refreshToken);
	    
		log.info("email======================>{}", email);
		authService.deleteRefreshToken(email);
		return ResponseEntity.ok(Map.of("message", "로그아웃완료"));
	}

//	@PreAuthorize("hasAnyRole('ADMIN','USER')")
//	@DeleteMapping(value = "/member/logout/{email}")
//	public ResponseEntity<?> logout(@PathVariable("email") String email) {
//		log.info("email======================>", email);
//		authService.deleteRefreshToken(email);
//		return ResponseEntity.ok(Map.of("message", "로그아웃완료"));
//	}

// 리플레쉬 토큰
	@PostMapping("/auth/refresh")
	public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = request.getHeader("Authorization-refresh");

		if (refreshToken == null || refreshToken.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "리프레시 토큰이 없습니다."));
		}

		try {
			String email = jwtTokenProvider.getEmailFromToken(refreshToken);

			boolean isValid = authService.validateRefreshToken(email, refreshToken);
			if (!isValid) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "유효하지 않은 리프레시 토큰입니다."));
			}

			MembersEntity membersEntity = membersService.findByEmail(email).orElseThrow(() -> new RuntimeException("사용자 없음"));
			AuthInfo authInfo = AuthInfo.builder().memberEmail(membersEntity.getMemberEmail())
					.memberPass(membersEntity.getMemberPass())
					.memberName(membersEntity.getMemberName())
					.authRole(membersEntity.getAuthRole()).build();
			String newAccessToken = jwtTokenProvider.createAccessToken(authInfo);
			
			   // ✅ 브라우저가 이 헤더를 읽을 수 있도록 노출 설정
	        response.setHeader("Access-Control-Expose-Headers", "Authorization");

	        // ✅ 응답 헤더로 accessToken 전달
	        response.setHeader("Authorization", "Bearer " + newAccessToken);
			return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "토큰 검증 실패", "message", e.getMessage()));
		}
	}
	
}// end class
