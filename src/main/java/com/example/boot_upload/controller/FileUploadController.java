package com.example.boot_upload.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {

	// 파일 저장 경로
	private final Path fileStorageLocation;

	// app.pro 내에 file.upload-dir= 키에 설정된 값을
	// 불러와서 파일 저장 경로를 설정
	public FileUploadController(@Value("${file.upload-dir}") String uploadDir) {
		fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize(); // 절대경로로 변환 + 정규화
		System.out.println(fileStorageLocation);

		try {
			Files.createDirectories(fileStorageLocation);
		} catch (IOException e) {
			throw new RuntimeException("파일을 저장할 디렉토리 생성 불가");
		}
	}
	
	@PostMapping("/upload")
	public String handleFileUpload(
			@RequestParam MultipartFile file, RedirectAttributes redirectAttribues) {
		
		if(file.isEmpty()) {
			System.out.println("빈 파일");
			redirectAttribues.addFlashAttribute("message", "파일을 선택해주세요.");
		} else {
			System.out.println(file.getOriginalFilename());
			System.out.println(file.getSize());
			
			//저장 처리
			String filename = storeFile(file);
			redirectAttribues.addFlashAttribute("message", "파일 업로드 성공 : " + filename);
		}
		
		return "redirect:/";
	}
	
	private String storeFile(MultipartFile file) {
		//저장을 위해 사용할 path에 맞게, 특수문자 등을 지움
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		Path targetLocation = fileStorageLocation.resolve(fileName);
		try {
		Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING); //중복 제거 등등
		return fileName;
		} catch (IOException e){
			throw new RuntimeException("파일 저장 실패" + fileName, e);
		}
	}
	
	@GetMapping
	public String fileUploadForm(Model model) throws IOException {
		List<String> files = Files.list(fileStorageLocation)
								.map(path -> path //uploads에 저장된 파일 목록
								.getFileName() //문자열로 전환시킴
								.toString())
								.collect(Collectors.toList()); 
		model.addAttribute("files", files);
		return "file";
	}
	
	
}
