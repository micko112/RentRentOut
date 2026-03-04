package org.landm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.landm.entity.Enums.Currency;

@Entity
@Table(name = "user")
public class User {

    // ovde bi trebalo username da se vidi umesto first i last name
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name="email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name="firstname", nullable = false)
    private String firstname;
    @Column(name="lastname", nullable = false)
    private String lastname;
    @Column(name="money")
    private BigDecimal money = BigDecimal.ZERO;
//    @OneToMany(mappedBy = "roles")
//    private List<String> roles;
    
    @Enumerated(EnumType.STRING)
    @Column(name="currency", nullable=false)
    private Currency currency;
    
    @Version
    @Column(name="version", nullable = false)
    private long version;
    
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="role_id", nullable=false)
    private Role role;
    
    @Column(name="enabled")
    @NotNull
    private boolean enabled = false;

    @Column(name = "positive_reviews")
    @NotNull
    private int positiveReviews =0;

    @Column(name = "negative_reviews")
    @NotNull
    private int negativeReviews =0;

    public User(){
    }

    public User(String email, String password, String firstname, String lastname, Role role, int positiveReviews, int negativeReviews) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
        this.positiveReviews = positiveReviews;
        this.negativeReviews = negativeReviews;

    }


    public long getId() {
        return id;
    }

    public void setId(long userId) {
        this.id = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }
    
	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public List<String> getStringRoles() {
		List<String> roles = Arrays.stream(role.getName().split(" "))
				.collect(Collectors.toList());
		
		return roles;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public int getPositiveReviews() {
        return positiveReviews;
    }

    public void setPositiveReviews(int positiveReviews) {
        this.positiveReviews = positiveReviews;
    }

    public int getNegativeReviews() {
        return negativeReviews;
    }

    public void setNegativeReviews(int negativeReviews) {
        this.negativeReviews = negativeReviews;
    }
}
