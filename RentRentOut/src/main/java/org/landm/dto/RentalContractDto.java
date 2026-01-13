package org.landm.dto;

import org.landm.entity.Enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RentalContractDto {
    private long id;

    private AdDto adDto;
    private UserDto lesseeDto;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal agreedPrice;
    private ContractStatus contractStatus;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public AdDto getAdDto() {
        return adDto;
    }

    public void setAdDto(AdDto adDto) {
        this.adDto = adDto;
    }

    public UserDto getLesseeDto() {
        return lesseeDto;
    }

    public void setLesseeDto(UserDto lesseeDto) {
        this.lesseeDto = lesseeDto;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public ContractStatus getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(ContractStatus contractStatus) {
        this.contractStatus = contractStatus;
    }
}
