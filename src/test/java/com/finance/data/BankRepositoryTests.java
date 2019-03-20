package com.finance.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class BankRepositoryTests {

    @Autowired
    private BankRepository repository;

    @Autowired
    TestEntityManager entityManager;

    //기관명으로 찾기 테스트
    @Test
    public void findBankByBankName_returnBankDetails() throws Exception{
        Bank savedBank = entityManager.persistFlushFind(new Bank("TEST"));
        List<Bank> banks = repository.findByBankName("TEST");

        assertThat(banks.size()).isEqualTo(1);
        assertThat(banks.get(0).getBankName()).isEqualTo("TEST");
    }
}
