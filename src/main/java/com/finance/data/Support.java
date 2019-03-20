package com.finance.data;

import org.dom4j.tree.AbstractEntity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class Support extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private Bank bank;

    @Column(nullable = false)
    private String year;

    @Column(nullable = false)
    private String month;

    @Column(nullable = false)
    private BigDecimal amount;

    public Support(Bank bank, String year, String month, BigDecimal amount){
        this.bank = bank;
        this.year = year;
        this.month = month;
        this.amount = amount;
    }

    Support(){}

    public Bank getBank() {
        return bank;
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
