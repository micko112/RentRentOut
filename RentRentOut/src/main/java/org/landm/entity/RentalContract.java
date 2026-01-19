package org.landm.entity;

import jakarta.persistence.*;
import org.landm.entity.Enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "rental_contract")
public class RentalContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ad_id")
    private Ad ad;

    @ManyToOne
    @JoinColumn(name = "lessee_id", nullable = false)
    private User lessee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "agreed_price", nullable = false)
    private BigDecimal agreedPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status")
    private ContractStatus contractStatus;

    @ManyToOne
    @JoinColumn(name="offer_sender_id")
    private User offerSender;
    
    public RentalContract() {
    }

    public RentalContract(Ad ad, User lessee, LocalDate startDate, LocalDate endDate, 
    		BigDecimal agreedPrice, ContractStatus contractStatus, User offerSender) {
        this.ad = ad;
        this.lessee = lessee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.agreedPrice = agreedPrice;
        this.contractStatus = contractStatus;
        this.offerSender = offerSender;
    }

    public User getLessee() {
        return lessee;
    }

    public void setLessee(User lesee) {
        this.lessee = lesee;
    }

    public Ad getAd() {
        return ad;
    }

    public void setAd(Ad ad) {
        this.ad = ad;
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

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public User getOfferSender() {
		return offerSender;
	}

	public void setOfferSender(User offerSender) {
		this.offerSender = offerSender;
	}

	@Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RentalContract that = (RentalContract) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RentalContract{" +
                "id=" + id +
                ", ad=" + ad +
                ", lesee=" + lessee +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", agreedPrice=" + agreedPrice +
                ", contractStatus=" + contractStatus +
                '}';
    }
}
