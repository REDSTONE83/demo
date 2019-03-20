package com.finance.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SupportRepositoryTests {

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private SupportRepository supportRepository;

    @Autowired
    TestEntityManager entityManager;

    //금융기관으로 찾기 테스트
    @Test
    public void findByBank_returnSupportDetails() throws Exception{

        Bank savedBank = entityManager.persistFlushFind(new Bank("TEST"));

        entityManager.persistFlushFind(
            new Support(
                    savedBank,
                    "2000",
                    "1",
                    BigDecimal.ONE));

        List<Support> supports = supportRepository.findByBank(savedBank);

        assertThat(supports.size()).isEqualTo(1);
        assertThat(supports.get(0).getBank().getBankName()).isEqualTo("TEST");
        assertThat(supports.get(0).getYear()).isEqualTo("2000");
        assertThat(supports.get(0).getMonth()).isEqualTo("1");
        assertThat(supports.get(0).getAmount().longValue()).isEqualTo(1L);
    }

    //금융기관과 특정 월에 해당하는 자료목록 찾기 테스트
    @Test
    public void findByBankAndMonth_returnSupportDetails() throws Exception{

        Bank savedBank = entityManager.persistFlushFind(new Bank("TEST"));

        entityManager.persistFlushFind(
            new Support(
                    savedBank,
                    "2000",
                    "1",
                    BigDecimal.ONE));


        List<Support> supports = supportRepository.findByBankAndMonth(savedBank, "1");

        assertThat(supports.size()).isEqualTo(1);
    }
}
