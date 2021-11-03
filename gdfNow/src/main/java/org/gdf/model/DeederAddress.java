/**
 * 
 */
package org.gdf.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


/**
 * @author satindersingh
 *
 */

@Entity
@Table(name = "DEEDER_ADDRESS", uniqueConstraints={@UniqueConstraint(columnNames = {"id"})})
public class DeederAddress implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@Column(name="LINE1")
	private String line1;
	
	@Column(name="LINE2")
	private String line2;
	
	@Column(name="POSTCODE")
	private String postcode;
	
	@Column(name="CITY")
	private String city;
	
	@Column(name="STATE")
	private String state;
	
	
	
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(
	name="COUNTRY_CODE", nullable=false, updatable=true)
	private Country country;
	
	@OneToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(
	name="DEEDER_ID", unique=true, nullable=false, updatable=false)
	private Deeder deeder;
        
        @Column(name = "CREATED_ON")
        private LocalDateTime createdOn;

        @Column(name = "UPDATED_ON")
        private LocalDateTime updatedOn;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getLine2() {
		return line2;
	}

	public void setLine2(String line2) {
		this.line2 = line2;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	
	public Deeder getDeeder() {
		return deeder;
	}

	public void setDeeder(Deeder deeder) {
		this.deeder = deeder;
	}

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
        
        
        
	

	@Override
    public String toString() {
        return "{" + "\"id\":"+ id +","+
        		 "\"line1\":\""+ line1 +"\","+
        		 "\"line2\":\""+ line2 +"\","+
        		 "\"postcode\":\""+ postcode +"\","+
        		 "\"city\":\""+ city +"\","+
        		 "\"state\":\""+ state +"\","+
        		 "\"country\":\""+ country.getName()+"\"}";
    }

	

	
	
	
	

	
}
