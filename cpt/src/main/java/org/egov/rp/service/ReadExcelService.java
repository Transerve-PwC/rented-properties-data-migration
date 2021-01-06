package org.egov.rp.service;

import java.io.File;
import java.util.List;

import org.egov.rp.entities.Property;

public interface ReadExcelService {

	public List<Property> getDataFromExcel(File file, int sheetIndex);
	public List<Property> getDocFromExcel(File file, int sheetIndex);
}
