package org.egov.rp.service;

import java.io.File;

import org.egov.rp.model.PropertyResponse;

public interface ReadExcelService {

	public PropertyResponse getDataFromExcel(File file, int sheetIndex);
	public PropertyResponse getDocFromExcel(File file, int sheetIndex);
}
