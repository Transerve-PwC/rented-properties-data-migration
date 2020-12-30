package org.egov.rp.controller;

import java.io.File;
import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.egov.rp.entities.Property;
import org.egov.rp.models.ExcelSearchCriteria;
import org.egov.rp.service.ReadExcelService;
import org.egov.rp.util.FileStoreUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/v1/excel")
public class ReadExcelController {

	private ReadExcelService readExcelService;
	private FileStoreUtils fileStoreUtils;
	
	@Autowired
	public ReadExcelController(ReadExcelService readExcelService, FileStoreUtils fileStoreUtils) {
		this.readExcelService = readExcelService;
		this.fileStoreUtils = fileStoreUtils;
	}
	@PostMapping("/read")
	public ResponseEntity<List<Property>> readExcel(@Valid @ModelAttribute ExcelSearchCriteria searchCriteria) {
		try {
			log.info("Start controller method readExcel() Request:" + searchCriteria);
			String filePath = fileStoreUtils.fetchFileStoreUrl(searchCriteria);
			if (StringUtils.isBlank(filePath)) {
				throw new Exception("Cannot find property file that is uploaded");
			}
			filePath = filePath.replaceAll(" ", "%20");
			File tempFile = File.createTempFile("File" + System.currentTimeMillis(), ".xlsx");
			FileUtils.copyURLToFile(new URI(filePath).toURL(), tempFile);
			List<Property> propertyList = this.readExcelService.getDataFromExcel(tempFile, 1);
			tempFile.delete();
			log.info("End controller method readExcel property data:" + propertyList.size());
			if (propertyList.size() == 0)
				throw new CustomException("FILE_TEMPLATE_NOT_VALID", "Invalid template uploaded. Please upload a valid property excel file.");

			return new ResponseEntity<>(propertyList, HttpStatus.OK);
		} catch (Exception e) {	
			log.error("Error occurred during readExcel():" + e.getMessage(), e);
			throw new CustomException("FILE_TEMPLATE_NOT_VALID", "Invalid template uploaded. Please upload a valid property excel file.");
		}

	}
	
}
