package org.egov.rp.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cs_pt_ownershipdetails_v1")
public class OwnerDetails extends AuditDetails{

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "id")
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "property_id")
	private Property property;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private Owner owner;

	@Column(name = "tenantid")
	private String tenantId;

	@Column(name = "name")
	private String name;

	@Column(name = "email")
	private String email;

	@Column(name = "phone")
	private String phone;

	@Column(name="gender")
	private String gender;

	@Column(name = "date_of_birth")
	private Long dateOfBirth;

	@Column(name = "aadhaar_number")
	private String aadhaarNumber;

	@Column(name = "allotment_startdate")
	private Long allotmentStartdate;

	@Column(name = "allotment_enddate")
	private Long allotmentEnddate;

	@Column(name = "posession_startdate")
	private Long posessionStartdate;

	@Column(name = "posession_enddate")
	private Long posessionEnddate;

	@Column(name = "monthly_rent")
	private String monthlyRent;

	@Column(name = "revision_period")
	private String revisionPeriod;

	@Column(name = "revision_percentage")
	private String revisionPercentage;

	@Column(name = "father_or_husband")
	private String fatherOrHusband;

	@Column(name = "relation")
	private String relation;

	/**
	 * This value will tell us if this got added as part of property masters or via
	 * ownership transfer application. This should be either MasterEntry or
	 * CitizenApplication. This
	 */
	@Column(name = "application_type")
	private String applicationType;

	/**
	 * After approval of application this owner becomes permanent.
	 */
	@Builder.Default
	@Column(name = "permanent")
	private Boolean permanent = false;

	@Column(name = "relation_with_deceased_allottee")
	private String relationWithDeceasedAllottee;

	@Column(name="date_of_death_allottee")
	private Long dateOfDeathAllottee;

	@Column(name = "application_number")
	private String applicationNumber;

	@Column(name = "due_amount")
	private BigDecimal dueAmount;

	@Column(name = "apro_Charge")
	private BigDecimal aproCharge;

}
