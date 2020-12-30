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
@Table(name = "cs_pt_address_v1")
public class Address extends AuditDetails{

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	private String id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "property_id")
	private Property property;

	@Column(name = "transit_number")
	private String transitNumber;

	@Column(name = "tenantid")
	private String tenantId;

	@Column(name = "colony")
	private String colony;

	@Column(name = "area")
	private String area;

	@Column(name = "district")
	private String district;

	@Column(name = "state")
	private String state;

	@Column(name ="country")
	private String country;

	@Column(name ="pincode")
	private String pincode;

	@Column(name="landmark")
	private String landmark;

}
