package com.finance.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupportRepository extends CrudRepository<Support, Long> {

    List<Support> findByBank(@Param("bank") Bank bank);

    List<Support> findByBankAndMonth(@Param("bank") Bank bank, @Param("month") String month);
}
