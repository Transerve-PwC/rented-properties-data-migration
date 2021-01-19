package org.egov.rp.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.egov.rp.entities.Address;
import org.egov.rp.entities.Document;
import org.egov.rp.entities.Owner;
import org.egov.rp.entities.OwnerDetails;
import org.egov.rp.entities.Property;
import org.egov.rp.entities.PropertyDetails;
import org.egov.rp.model.PropertyResponse;
import org.egov.rp.repository.PropertyRepository;
import org.egov.rp.service.StreamingSheetContentsHandler.StreamingRowProcessor;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReadExcelServiceImpl implements ReadExcelService {

	@Autowired
	private PropertyRepository propertyRepository;

	@Autowired
	private org.egov.rp.util.FileStoreUtils fileStoreUtils;

	@Value("${file.location}")
	private String fileLocation;

	private static final String SYSTEM = "system";
	private static final String TENANTID = "ch.chandigarh";
	private static final String APPROVE = "APPROVE";
	private static final String PM_APPROVED = "PM_APPROVED";
	private static final String MASTERENTRY = "MasterEntry";

	@Override
	public PropertyResponse getDataFromExcel(File file, int sheetIndex) {
		try {
			OPCPackage opcPackage = OPCPackage.open(file);
			return this.process(opcPackage, sheetIndex);
		} catch (IOException | OpenXML4JException | SAXException e) {
			log.error("Error while parsing Excel", e);
			throw new CustomException("PARSE_ERROR", "Could not parse excel. Error is " + e.getMessage());
		}

	}

	@Override
	public PropertyResponse getDocFromExcel(File file, int sheetIndex) {
		try {
			OPCPackage opcPackage = OPCPackage.open(file);
			return this.processDoc(opcPackage, sheetIndex);
		} catch (IOException | OpenXML4JException | SAXException e) {
			log.error("Error while parsing Excel", e);
			throw new CustomException("PARSE_ERROR", "Could not parse excel. Error is " + e.getMessage());
		}

	}

	private void processSheet(Styles styles, SharedStrings strings, SheetContentsHandler sheetHandler,
			InputStream sheetInputStream) throws IOException, SAXException {
		DataFormatter formatter = new DataFormatter();
		InputSource sheetSource = new InputSource(sheetInputStream);
		try {
			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			saxFactory.setNamespaceAware(false);
			SAXParser saxParser = saxFactory.newSAXParser();
			XMLReader sheetParser = saxParser.getXMLReader();
			ContentHandler handler = new MyXSSFSheetXMLHandler(styles, null, strings, sheetHandler, formatter, false);
			sheetParser.setContentHandler(handler);
			sheetParser.parse(sheetSource);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
		}
	}

	private PropertyResponse process(OPCPackage xlsxPackage, int sheetNo)
			throws IOException, OpenXML4JException, SAXException, CustomException {
		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(xlsxPackage);
		XSSFReader xssfReader = new XSSFReader(xlsxPackage);
		StylesTable styles = xssfReader.getStylesTable();
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		int index = 0;
		while (iter.hasNext()) {
			try (InputStream stream = iter.next()) {

				if (index == sheetNo) {
					SheetContentsProcessor processor = new SheetContentsProcessor();
					processSheet(styles, strings, new StreamingSheetContentsHandler(processor), stream);
					if (!processor.propertyList.isEmpty()) {
						return saveProperties(processor.propertyList, processor.skippedTransitNo);
					}
				}
				index++;
			}
		}
		throw new CustomException("PARSE_ERROR", "Could not process sheet no " + sheetNo);
	}

	private PropertyResponse processDoc(OPCPackage xlsxPackage, int sheetNo)
			throws IOException, OpenXML4JException, SAXException, CustomException {
		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(xlsxPackage);
		XSSFReader xssfReader = new XSSFReader(xlsxPackage);
		StylesTable styles = xssfReader.getStylesTable();
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		int index = 0;
		while (iter.hasNext()) {
			try (InputStream stream = iter.next()) {
				// String sheetName = iter.getSheetName();
				if (index == sheetNo) {
					SheetContentsProcessorDoc sheetContentsProcessorDoc = new SheetContentsProcessorDoc();
					processSheet(styles, strings, new StreamingSheetContentsHandler(sheetContentsProcessorDoc), stream);
					if (!sheetContentsProcessorDoc.propertywithdoc.isEmpty()) {
						PropertyResponse propertyResponse = PropertyResponse.builder()
								.generatedCount(sheetContentsProcessorDoc.propertywithdoc.size())
								.skippedTransitNo(sheetContentsProcessorDoc.skippedTransitNo).build();
						return propertyResponse;
					}
				}
				index++;
			}
		}
		throw new CustomException("PARSE_ERROR", "Could not process sheet no " + sheetNo);
	}

	protected Object getValueFromCell(Row row, int cellNo, Row.MissingCellPolicy cellPolicy) {
		Cell cell1 = row.getCell(cellNo, cellPolicy);
		Object objValue = "";
		switch (cell1.getCellType()) {
		case BLANK:
			objValue = "";
			break;
		case STRING:
			objValue = cell1.getStringCellValue();
			break;
		case NUMERIC:
			try {
				if (DateUtil.isCellDateFormatted(cell1)) {
					objValue = cell1.getDateCellValue().getTime();
				} else {
					throw new InvalidFormatException();
				}
			} catch (Exception ex1) {
				try {
					objValue = cell1.getNumericCellValue();
				} catch (Exception ex2) {
					objValue = 0.0;
				}
			}

			break;
		case FORMULA:
			objValue = cell1.getNumericCellValue();
			break;

		default:
			objValue = "";
		}
		return objValue;
	}

	protected long convertStrDatetoLong(String dateStr) {
		try {
			SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
			Date d = f.parse(dateStr);
			return d.getTime();
		} catch (Exception e) {
			log.error("Date parsing issue occur :" + e.getMessage());
		}
		return 0;
	}

	private class SheetContentsProcessorDoc implements StreamingRowProcessor {

		List<Property> propertywithdoc = new ArrayList<>();
		String transitNo = "";
		Set<String> skippedTransitNo = new HashSet<>();

		@Override
		public void processRow(Row row) {
			File folder = new File(fileLocation);
			String[] listOfFiles = folder.list();
			List<String> filesList = Arrays.asList(listOfFiles);

			if (!filesList.isEmpty()) {
				if (row.getRowNum() >= 2) {
					String documentType = String
							.valueOf(getValueFromCell(row, 2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
					if (!documentType.isEmpty()) {
						String transitSiteNo = String
								.valueOf(getValueFromCell(row, 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();

						Property property;
						String documentName = String
								.valueOf(getValueFromCell(row, 5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
						if (filesList.contains(documentName)) {

							if (!transitSiteNo.isEmpty()) {
								transitNo = transitSiteNo;

								property = propertyRepository.getPropertyByTransitNumber(
										transitSiteNo.substring(0, transitSiteNo.length() - 2));
							} else {
								property = propertyRepository
										.getPropertyByTransitNumber(transitNo.substring(0, transitNo.length() - 2));
							}
							if (property != null) {
							byte[] bytes = null;
							List<HashMap<String, String>> response = null;
							try {
								bytes = Files.readAllBytes(Paths.get(folder + "/" + documentName));
								ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
								outputStream.write(bytes);
								String [] tenantId = property.getTenantId().split("\\.");
								response = fileStoreUtils.uploadStreamToFileStore(outputStream, tenantId[0],
										documentName);
								outputStream.close();
							} catch (IOException e) {
								log.error("error while converting file into byte output stream");
							}
								String docType = "";

								if (documentType.contains("documents")) {
									docType = "TRANSIT_SITE_DOCUMENTS";
								} else if (documentType.contains("Transit site Images")) {
									docType = "TRANSIT_SITE_IMAGES";
								} else if (documentType.contains("Allotee Image")) {
									docType = "ALLOTEE_IMAGE";
								} else if (documentType.contains("Aadhar")) {
									docType = "ALLOTEE_AADHAR";
								} else if (documentType.contains("Building Plan")) {
									docType = "APPROVED_BUILDING_PLAN";
								}
								Document document = Document.builder().referenceId(property.getId())
										.tenantId(property.getTenantId()).active(true).documentType(docType)
										.fileStoreId(response.get(0).get("fileStoreId")).build();
								document.setCreatedBy(SYSTEM);
								document.setProperty(property);
								property.setDocuments(document);
								propertyRepository.save(property);
								propertywithdoc.add(property);
							} else {
								skippedTransitNo.add(transitNo.substring(0, transitNo.length() - 2));
								log.error("We are skipping uploading document as property for transit number: "+ transitNo.substring(0, transitNo.length() - 2) + " as it does not exists.");
							}
						}
					}
				}

			}

		}

	}

	private class SheetContentsProcessor implements StreamingRowProcessor {

		List<Property> propertyList = new ArrayList<>();
		Set<String> skippedTransitNo = new HashSet<>();

		@Override
		public void processRow(Row currentRow) {

			if (currentRow.getRowNum() >= 7) {
				if (currentRow.getCell(2) != null) {

					String secondCell = String
							.valueOf(getValueFromCell(currentRow, 2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					Property propertyDb = propertyRepository
							.getPropertyByTransitNumber(secondCell.substring(0, secondCell.length() - 2));
					if (propertyDb == null) {
					String firstCell = String
							.valueOf(getValueFromCell(currentRow, 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String thirdCell = String
							.valueOf(getValueFromCell(currentRow, 3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String fourthCell = String
							.valueOf(getValueFromCell(currentRow, 4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String fifthCell = String
							.valueOf(getValueFromCell(currentRow, 5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String sixthCell = String
							.valueOf(getValueFromCell(currentRow, 6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String seventhCell = String
							.valueOf(getValueFromCell(currentRow, 7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String eirthCell = String
							.valueOf(getValueFromCell(currentRow, 8, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String ninthCell = String
							.valueOf(getValueFromCell(currentRow, 9, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String tenthCell = String
							.valueOf(getValueFromCell(currentRow, 10, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String eleventhCell = String
							.valueOf(getValueFromCell(currentRow, 11, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					// twelve is date
					String twelveCell = String
							.valueOf(getValueFromCell(currentRow, 12, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String thirteenCell = String
							.valueOf(getValueFromCell(currentRow, 13, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					// fourteen is date
					String fourteenCell = String
							.valueOf(getValueFromCell(currentRow, 14, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String fifteenCell = String
							.valueOf(getValueFromCell(currentRow, 15, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String sixteenCell = String
							.valueOf(getValueFromCell(currentRow, 16, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();
					String seventeenCell = String
							.valueOf(getValueFromCell(currentRow, 17, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
							.trim();

					if (isNumeric(secondCell)) {
						if (isNumeric(thirdCell) && isNumeric(fifthCell)
								&& isNumeric(seventhCell.substring(1, seventhCell.length() - 1))) {

							Float fvalue = 0f;
							if (!sixteenCell.isEmpty()) {
								fvalue = Float.valueOf(sixteenCell);
							}
							Double dvalue = 0d;
							if (!fifteenCell.isEmpty()) {
								dvalue = Double.valueOf(fifteenCell);
							}
							String thirdValue = "";
							if (isNumeric(thirdCell)) {
								thirdValue = thirdCell;
							}
							PropertyDetails propertyDetails = PropertyDetails.builder().area(thirdValue)
									.interestRate(dvalue).rentIncrementPeriod((int) Math.round(fvalue))
									.rentIncrementPercentage(Double.valueOf(seventeenCell))
									.transitNumber(secondCell.substring(0, secondCell.length() - 2)).tenantId(TENANTID)
									.build();
							Address address = Address.builder().area(fourthCell)
									.pincode(fifthCell.substring(0, fifthCell.length() - 2)).tenantId(TENANTID)
									.transitNumber(secondCell.substring(0, secondCell.length() - 2)).build();
							OwnerDetails ownerDetails = OwnerDetails.builder().name(sixthCell)
									.phone(seventhCell.substring(1, seventhCell.length() - 1)).relation(eirthCell)
									.fatherOrHusband(ninthCell).allotmentStartdate(convertStrDatetoLong(twelveCell))
									.tenantId(TENANTID).applicationType(MASTERENTRY).permanent(true).build();
							if (tenthCell.equalsIgnoreCase("na")) {
								ownerDetails.setEmail(null);
							} else {
								ownerDetails.setEmail(tenthCell);
							}
							if (eleventhCell.equalsIgnoreCase("na")) {
								ownerDetails.setAadhaarNumber(null);
							} else {
								ownerDetails.setAadhaarNumber(eleventhCell.substring(1, eleventhCell.length() - 1));
							}
							if (convertStrDatetoLong(fourteenCell) == 0) {
								ownerDetails.setPosessionStartdate(null);
							} else {
								ownerDetails.setPosessionStartdate(convertStrDatetoLong(fourteenCell));
							}
							Owner owner = Owner.builder().allotmenNumber(thirteenCell).ownerDetails(ownerDetails)
									.tenantId(TENANTID).isPrimaryOwner(true).activeState(true).build();

							String colonyCode = "";
							if (firstCell.contains("Milk")) {
								colonyCode = "COLONY_MILK";
							} else if (firstCell.contains("Kumhar")) {
								colonyCode = "COLONY_KUMHAR";
							} else if (firstCell.contains("Sector 52-53")) {
								colonyCode = "COLONY_SECTOR_52_53";
							} else if (firstCell.contains("Vikas Nagar")) {
								colonyCode = "COLONY_VIKAS_NAGAR";
							}
							Property property = Property.builder().colony(colonyCode)
									.transitNumber(secondCell.substring(0, secondCell.length() - 2))
									.propertyDetails(propertyDetails).address(address)
									.owners(Collections.singleton(owner))
									.ownerDetails(Collections.singleton(ownerDetails)).tenantId(TENANTID)
									.masterDataState(PM_APPROVED).masterDataAction(APPROVE).build();

							property.setCreatedBy(SYSTEM);
							owner.setCreatedBy(SYSTEM);
							ownerDetails.setCreatedBy(SYSTEM);
							propertyDetails.setCreatedBy(SYSTEM);
							address.setCreatedBy(SYSTEM);
							propertyDetails.setProperty(property);
							address.setProperty(property);
							owner.setProperty(property);
							ownerDetails.setProperty(property);
							ownerDetails.setOwner(owner);

							propertyDetails.setCurrentowner(owner);
							owner.setPropertyDetails(propertyDetails);
							propertyList.add(property);
						} else {
							skippedTransitNo.add(secondCell.substring(0, secondCell.length() - 2));
							log.error("We are skipping uploading property for transit number: "
									+ secondCell.substring(0, secondCell.length() - 2) + " because of incorrect data.");
						}
					} else {
						skippedTransitNo.add(secondCell);
						log.error("We are skipping uploading property for transit number: " + secondCell
								+ " because of incorrect transit number.");
					}
				} else {
					skippedTransitNo.add(secondCell.substring(0, secondCell.length() - 2));
					log.error("We are skipping uploading property for transit number: "
							+ secondCell.substring(0, secondCell.length() - 2) + " as it already exists.");
				}
			}
		}
	}
}

	private PropertyResponse saveProperties(List<Property> properties, Set<String> skippedTransitNo) {
		properties.forEach(property -> {
			propertyRepository.save(property);
		});
		PropertyResponse propertyResponse = PropertyResponse.builder().generatedCount(properties.size()).skippedTransitNo(skippedTransitNo).build();
		return propertyResponse;
	}

	private Boolean isNumeric(String value) {
		if (value != null && !value.matches("[1-9][0-9]*(\\.[0])?")) {
			return false;
		}
		return true;
	}
}
