package org.egov.rp.controller;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.egov.rp.model.PropertyResponse;
import org.egov.rp.service.ReadExcelService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/v1/excel")
public class ReadExcelController {

	private ReadExcelService readExcelService;

	@Value("${file.path}")
	private String filePath;

	@Autowired
	public ReadExcelController(ReadExcelService readExcelService) {
		this.readExcelService = readExcelService;
	}
	@PostMapping("/read")
	public ResponseEntity<?> readExcel() {
		try {
			log.info("Start controller method readExcel() Request:" + filePath);
			if (StringUtils.isBlank(filePath)) {
				throw new Exception("Cannot find property file that is uploaded");
			}
			File tempFile = new File(filePath);
			if(!tempFile.exists()) {
				throw new CustomException("FILE_NOT_FOUND", "File not found in resource folder");
			}
			PropertyResponse propertyResponse = this.readExcelService.getDataFromExcel(tempFile, 1);
			log.info("End controller method readExcel property inserted:" + propertyResponse.getGeneratedCount());
			if (propertyResponse.getGeneratedCount() == 0)
				throw new CustomException("FILE_TEMPLATE_NOT_VALID", "Invalid template uploaded. Please upload a valid property excel file.");

			return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
		} catch (Exception e) {	
			log.error("Error occurred during readExcel():" + e.getMessage(), e);
			throw new CustomException("FILE_TEMPLATE_NOT_VALID", "Invalid template uploaded. Please upload a valid property excel file.");
		}

	}
	
	@PostMapping("/read_doc")
	public ResponseEntity<?> readExcelforDoc() {
		try {
			log.info("Start controller method readExcelforDoc() Request:" + filePath);
			if (StringUtils.isBlank(filePath)) {
				throw new Exception("Cannot find property file that is uploaded");
			}
			File tempFile = new File(filePath);
			if(!tempFile.exists()) {
				throw new CustomException("FILE_NOT_FOUND", "File not found in resource folder");
			}
			PropertyResponse propertyResponse = this.readExcelService.getDocFromExcel(tempFile, 2);
			log.info("End controller method readExcelforDoc property document inserted:" + propertyResponse.getGeneratedCount());
			if (propertyResponse.getGeneratedCount() == 0)
				throw new CustomException("FILE_TEMPLATE_NOT_VALID", "Invalid template uploaded. Please upload a valid property excel file.");

			return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
		} catch (Exception e) {	
			log.error("Error occurred during readExcel():" + e.getMessage(), e);
			throw new CustomException("FILE_TEMPLATE_NOT_VALID", "Invalid template uploaded. Please upload a valid property excel file.");
		}

	}
}
