package org.egov.rp.model;

import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PropertyResponse {

	@JsonProperty("Inserted")
	@Valid
	private int generatedCount;

	@JsonProperty("Non-Inserted")
	@Valid
	private AtomicInteger nonGeneratedCount;
}
