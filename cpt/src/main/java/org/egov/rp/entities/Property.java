package org.egov.rp.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
@Table(name = "cs_pt_property_v1")
public class Property extends AuditDetails{

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "id")
	private String id;

	@Column(name = "transit_number")
	private String transitNumber;

	@Column(name = "tenantid")
	private String tenantId;

	@Column(name = "colony")
	private String colony;

	@Column(name = "master_data_State")
	private String masterDataState;

	@Column(name = "master_data_action")
	private String masterDataAction;
	
	@OneToOne(
            cascade =  CascadeType.ALL,
            mappedBy = "property")
	private PropertyDetails propertyDetails;

	@OneToMany(
			cascade = CascadeType.ALL,
			mappedBy = "property")
	private Set<Owner> owners = new HashSet<>();
	
	@OneToMany(
			cascade = CascadeType.ALL,
			mappedBy = "property"
			)
	private Set<OwnerDetails> ownerDetails= new HashSet<>();

	@OneToOne(
			cascade = CascadeType.ALL,
			mappedBy = "property"
			)
	private Address address;

	@Column(name = "rent_payment_consumer_code")
	private String rentPaymentConsumerCode;
	
}