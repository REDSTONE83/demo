package com.finance.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BankRepository extends CrudRepository<Bank, Long> {

    List<Bank> findByBankName(@Param("bankName") String bankName);
}
