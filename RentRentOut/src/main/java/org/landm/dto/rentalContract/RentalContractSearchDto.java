package org.landm.dto.rentalContract;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.landm.entity.Enums.ContractStatus;

public class RentalContractSearchDto {

	//Search
	private String term;
	
	//Filter
	private ContractStatus status;
	
	private BigDecimal priceFrom;
	private BigDecimal priceTo;
	
	private LocalDate startDateFrom;
	private LocalDate startDateTo;
	
	private LocalDate endDateFrom;
	private LocalDate endDateTo;
	
	//Pageable
	private int page = 0;
	private int size = 10;
	
	//Sort
	private String sortBy = "startDate";
	private boolean descending = false;
	
	
	public ContractStatus getStatus() {
		return status;
	}
	public void setStatus(ContractStatus status) {
		this.status = status;
	}
	public BigDecimal getPriceFrom() {
		return priceFrom;
	}
	public void setPriceFrom(BigDecimal priceFrom) {
		this.priceFrom = priceFrom;
	}
	public BigDecimal getPriceTo() {
		return priceTo;
	}
	public void setPriceTo(BigDecimal priceTo) {
		this.priceTo = priceTo;
	}
	public LocalDate getStartDateFrom() {
		return startDateFrom;
	}
	public void setStartDateFrom(LocalDate startDateFrom) {
		this.startDateFrom = startDateFrom;
	}
	public LocalDate getStartDateTo() {
		return startDateTo;
	}
	public void setStartDateTo(LocalDate startDateTo) {
		this.startDateTo = startDateTo;
	}
	public LocalDate getEndDateFrom() {
		return endDateFrom;
	}
	public void setEndDateFrom(LocalDate endDateFrom) {
		this.endDateFrom = endDateFrom;
	}
	public LocalDate getEndDateTo() {
		return endDateTo;
	}
	public void setEndDateTo(LocalDate endDateTo) {
		this.endDateTo = endDateTo;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getSortBy() {
		return sortBy;
	}
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	public boolean isDescending() {
		return descending;
	}
	public void setDescending(boolean descending) {
		this.descending = descending;
	}
	
	
	
}
