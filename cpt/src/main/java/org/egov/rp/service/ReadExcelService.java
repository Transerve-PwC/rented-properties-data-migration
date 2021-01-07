package org.egov.rp.service;

import java.io.File;

public interface ReadExcelService {

	public int getDataFromExcel(File file, int sheetIndex);
	public int getDocFromExcel(File file, int sheetIndex);
}
