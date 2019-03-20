package com.finance.data;

import org.dom4j.tree.AbstractEntity;

import javax.persistence.*;

@Entity
public class Bank extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String bankName;

    protected Bank() {}

    public Long getId() {
        return id;
    }

    public Bank(String bankName) {
        this.bankName = bankName;
    }

    public String getBankName() {
        return bankName;
    }
}