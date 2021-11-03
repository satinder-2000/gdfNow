package org.gdf.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="GOVERNMENT_ADDRESS")
public class GovernmentAddress {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private String line1;
	
	private String line2;
	
	private String line3;
	
	private String city;
	
	private String postcode;
	
	private String state;
	
	private String phone1;
	
	private String phone2;
	
	private String phone3;
	
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(
	name="COUNTRY_CODE", nullable=false, updatable=true)
	private Country country;
	
	
	@OneToOne(optional=false, fetch=FetchType.LAZY)
	@JoinColumn(
	name="GOVERNMENT_ID", unique=true, nullable=false, updatable=false)
	private Government government;


	public int getId() {
		return id;
	}


	public void setId(int id) {
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


	public String getLine3() {
		return line3;
	}


	public void setLine3(String line3) {
		this.line3 = line3;
	}


	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}


	public String getPostcode() {
		return postcode;
	}


	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}


	public String getState() {
		return state;
	}


	public void setState(String state) {
		this.state = state;
	}


	public String getPhone1() {
		return phone1;
	}


	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}


	public String getPhone2() {
		return phone2;
	}


	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}


	public String getPhone3() {
		return phone3;
	}


	public void setPhone3(String phone3) {
		this.phone3 = phone3;
	}


	public Country getCountry() {
		return country;
	}


	public void setCountry(Country country) {
		this.country = country;
	}


	public Government getGovernment() {
		return government;
	}


	public void setGovernment(Government government) {
		this.government = government;
	}
	
	
	
	

}
