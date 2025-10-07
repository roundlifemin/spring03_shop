package com.example.spring03_shop.board.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring03_shop.board.dto.BoardDTO;
import com.example.spring03_shop.board.dto.PageDTO;
import com.example.spring03_shop.board.repository.BoardRepository;
import com.example.spring03_shop.board.service.BoardService;
import com.example.spring03_shop.common.file.FileUpload;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

// @CrossOrigin("*")
@Slf4j
@RestController
public class BoardController {

	private final BoardRepository boardRepository;
	@Autowired
	private BoardService boardService;

	private int currentPage;
	private PageDTO pdto;

	@Value("${spring.servlet.multipart.location}")
	private String tempDir;

	public BoardController(BoardRepository boardRepository) {
		this.boardRepository = boardRepository;

	}

	// http://localhost:8090/board/list/1
	
	@GetMapping(value = "/board/list/{currentPage}")
	public ResponseEntity<Map<String, Object>> listExecute(@PathVariable("currentPage") int currentPage) {
		Map<String, Object> map = new HashMap<>();

		long totalRecord = boardService.countProcess();
		log.info("totalRecord: {}", totalRecord);
		log.info("tempDir: => {  }", tempDir);

		if (totalRecord >= 1) {
			this.currentPage = currentPage;
			this.pdto = new PageDTO(this.currentPage, totalRecord);

			map.put("boardList", boardService.listProcess(pdto));
			map.put("pv", this.pdto);
		}
		return ResponseEntity.ok().body(map);
	}// end
		// listExecute()///////////////////////////////////////////////////////////////

	// 첨부파일이 있을 때 @RequestBody을 선언하면 안된다.
	// 답변글일때 ref, reStep, reLevel 담아서 넘겨야 한다.
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@PostMapping("/board/write")
	public ResponseEntity<String> writeProExecute(BoardDTO dto, HttpServletRequest req) {
		MultipartFile file = dto.getFilename();
		log.info("file => {}", file);

		// 임시 디렉토리 가져오기
		// String tempDir = System.getProperty("java.io.tmpdir");
		log.info("tempDir: {}", tempDir);

		// 파일 첨부가 있으면
		if (file != null && !file.isEmpty()) {
			UUID random = FileUpload.saveCopyFile(file, tempDir);
			dto.setUpload(random + "_" + file.getOriginalFilename());
		}

		dto.setIp(req.getRemoteAddr());
		boardService.insertProcess(dto);
		return ResponseEntity.ok(String.valueOf(1));
	}// end writeProExecute()//////////////////////////////////////////////////////

	@GetMapping(value = "/board/view/{num}")
	public ResponseEntity<BoardDTO> viewExecute(@PathVariable("num") Long num) {
		BoardDTO boardDTO = boardService.contentProcess(num);
		return ResponseEntity.ok(boardDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@PutMapping(value = "/board/update")
	public ResponseEntity<Void> updateExecute(BoardDTO dto, HttpServletRequest req) {
		log.info("update => {}", dto);
		MultipartFile file = dto.getFilename();

		if (file != null && !file.isEmpty()) {
			UUID random = FileUpload.saveCopyFile(file, tempDir);
			dto.setUpload(random + "_" + file.getOriginalFilename());
		}
		boardService.updateProcess(dto, tempDir);
		return ResponseEntity.ok(null);
	}

	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@DeleteMapping(value = "/board/delete/{num}")
	public ResponseEntity<Void> deleteExecute(@PathVariable("num") Long num) {
		boardService.deleteProcess(num, tempDir);
		return ResponseEntity.ok(null);
	}

	// http://localhost:8090/board/contentdownload/167b7f60-e2e6-4bba-917d-860b6df1ce04_자바
	// db연결.txt
	@GetMapping(value = "/board/contentdownload/{filename}")
	public ResponseEntity<byte[]> downloadExecute(@PathVariable("filename") String filename) throws IOException {
		String fileName = filename.substring(filename.indexOf("_") + 1);

		// 파일명이 한글일때 인코딩 작업을 한다.
		String str = URLEncoder.encode(fileName, "UTF-8");
		log.info("str => {}", str);

		// 원본파일명에 공백이 있을 때, "+"표시가 되므로 공백으로 처리해줌
		str = str.replaceAll("\\+", "%20");

		Path path = Paths.get(tempDir + "/" + filename);

//    	Resource  resource = new InputStreamResource(Files.newInputStream(path));
//    	log.info("resource => {}", resource.contentLength());
//    	HttpHeaders  headers= new HttpHeaders();
//    	headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
//    	headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+str+";");
//    	return ResponseEntity.ok().headers(headers).body(resource);

		byte[] fileContent = Files.readAllBytes(path); // 🔸 반드시 byte[] 로 읽기
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // 바이너리 파일
		headers.setContentDisposition(
				ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
		return ResponseEntity.ok().headers(headers).body(fileContent);

	}
}// end class
