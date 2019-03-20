package com.finance.model;

import com.finance.data.Bank;
import com.finance.data.Support;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BankSupports {

    private Bank bank;
    private List<Support> supports;

    public BankSupports(Bank bank) {
        this.bank = bank;
        this.supports = new ArrayList<>();
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public List<Support> getSupports() {
        return supports;
    }

    public void addSupports(Support support) {
        this.supports.add(support);
    }

    public BigDecimal getSupportsTotal(){
        BigDecimal supportSummary = BigDecimal.ZERO;
        for(Support support : supports){
            supportSummary = supportSummary.add(support.getAmount());
        }

        return supportSummary;
    }

    public BigDecimal getSupportsAverage(){
        return BigDecimal.valueOf(getSupportsTotal().doubleValue() / supports.size());
    }
}
