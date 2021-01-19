package org.egov.rp.model;

import java.util.HashSet;
import java.util.Set;

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

	@JsonProperty("Skipped Properties Transit Numbers")
	@Valid
	private Set<String> skippedTransitNo = new HashSet<>();
}
