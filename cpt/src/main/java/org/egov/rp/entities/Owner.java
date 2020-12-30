package org.egov.rp.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Entity
@Table(name = "cs_pt_ownership_v1")
public class Owner extends AuditDetails{

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "property_id")
	private Property property;
	
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "currentowner")
	private PropertyDetails propertyDetails;
 
	@Column(name = "tenantid")
	private String tenantId;

	@Column(name = "allotmen_number")
	private String allotmenNumber;

	@OneToOne(
			cascade = CascadeType.ALL,
			mappedBy = "owner")
	private OwnerDetails ownerDetails;

	@Column(name = "is_primary_owner")
	private Boolean isPrimaryOwner = false;

	/**
	 * This represents currently active owner. During property master, this should
	 * be true.
	 */
	@Column(name = "active_state")
	private Boolean activeState;

	/**
	 * This will indicate the application status.
	 */
	@Column(name = "application_state")
	private String applicationState;

	@Column(name = "application_action")
	private String applicationAction;

}
