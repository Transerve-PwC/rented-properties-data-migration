package org.egov.rp.util;

import java.util.HashMap;
import java.util.Map;

import org.egov.rp.models.ExcelSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class FileStoreUtils {

	@Value("${egov.filestore.host}${egov.file.url.path}")
	private String fileStoreUrl;

	private RestTemplate restTemplate;

	public FileStoreUtils(RestTemplate restTemplate) {
		super();
		this.restTemplate = restTemplate;
	}

	@Cacheable(value = "fileUrl", sync = true)
	@SuppressWarnings("unchecked")
	public String fetchFileStoreUrl(ExcelSearchCriteria searchCriteria) {
		StringBuilder uri = new StringBuilder(fileStoreUrl);
		String stateLevelTenantId = this.getStateLevelTenantId(searchCriteria.getTenantId());
		uri.append("?tenantId=" + stateLevelTenantId + "&fileStoreIds=" + searchCriteria.getFileStoreId());
		Map<String, Object> response = (Map<String, Object>) (restTemplate.getForObject(uri.toString(), HashMap.class));
		if (!response.containsKey(searchCriteria.getFileStoreId())) {
			throw new CustomException("FILE_NOT_FOUND", String.format("File store id %s not found with tenant id %s",
					searchCriteria.getFileStoreId(), stateLevelTenantId));
		}
		return String.valueOf(response.get(searchCriteria.getFileStoreId()));
	}

		private String getStateLevelTenantId(String tenantId) {
		String[] components = tenantId.split(".");
		if (components.length == 0) {
			return "ch";
		}
		return components[0];
	}

}
