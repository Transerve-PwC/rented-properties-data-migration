package org.egov.rp.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "cs_pt_propertydetails_v1")
public class PropertyDetails extends AuditDetails{

	/**
	 * Current interest rate per year. This will not change and is constant.
	 * 
	 * Vikas Nagar Mauli Jagran (Sites 1-2765) - 0 Sector 52-53 - 0 Milk Colony
	 * Maloya - 24% Kumhar Colony Maloya - 24%
	 */
	@Column(name = "interest_rate")
	private Double interestRate;

	/**
	 * How much the monthly rent increases once the period ends.
	 * 
	 * Vikas Nagar Mauli Jagran (Sites 1-2765) - 5% Sector 52-53 - 5% Milk Colony
	 * Maloya - 25% Kumhar Colony Maloya - 25%
	 */
	@Column(name = "rent_increment_percentage")
	private Double rentIncrementPercentage;

	/**
	 * How often does the monthly rent amount increase.
	 * 
	 * Vikas Nagar Mauli Jagran (Sites 1-2765) - 1 Sector 52-53 - 1 Milk Colony
	 * Maloya - 5 Kumhar Colony Maloya - 5
	 */
	@Column(name = "rent_increment_period")
	private int rentIncrementPeriod;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "id")
	private String id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "property_id")
	private Property property;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "current_owner")
	private Owner currentowner;
 
	@Column(name = "transit_number")
	private String transitNumber;

	@Column(name = "tenantid")
	private String tenantId;

	@Column(name = "area")
	private String area;

	@Column(name = "rent_per_sqyd")
	private String rentPerSqyd;

	/*
	 * @Column(name = "current_owner") private String currentOwner;
	 */

	@Column(name = "floors")
	private String floors;

	@Column(name = "additional_details")
	private String additionalDetails;

}
