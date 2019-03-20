package com.finance.service;

import com.finance.data.Bank;
import com.finance.data.BankRepository;
import com.finance.data.Support;
import com.finance.data.SupportRepository;
import com.finance.exception.WrongFormFileException;
import com.finance.model.BankSupports;
import com.mixin.UploadResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class FinanceServiceTests {

    @Mock
    private BankRepository bankRepository;

    @Mock
    private SupportRepository supportRepository;

    private FinanceService financeService;

    @Before
    public void setUp() {
        financeService = new FinanceService(bankRepository, supportRepository);
    }

    //1. 기관별 주택 신용보증 금액 파일 데이터 Upload
    //1.1 정상적으로 입력 된 경우
    @Test
    public void saveUploadSupportData_uploadCorrectFile() throws Exception {
        given(bankRepository.save(any()))
                .willReturn(
                        new Bank("NAME")
                );

        given(supportRepository.save(any()))
                .willReturn(
                        new Support(
                                new Bank("NAME"),
                                "2000",
                                "1", BigDecimal.ZERO)
                );


        String fileContents =
                "연도,월,주택도시기금1)(억원),국민은행(억원),우리은행(억원),신한은행(억원),한국시티은행(억원),하나은행(억원),농협은행/수협은행(억원),외환은행(억원),기타은행(억원)\n" +
                "2005,1,1019,846,82,95,30,157,57,80,99";

        String uploadResult = financeService.saveUploadSupportData(fileContents.getBytes("EUC-KR"));

        assertThat(uploadResult).isEqualTo(UploadResult.OK);
    }

    //1.2 파일이 없는 경우
    @Test
    public void saveUploadSupportData_noFile() throws Exception {
        String uploadResult = financeService.saveUploadSupportData(null);

        assertThat(uploadResult).isEqualTo(UploadResult.NO_FILE);
    }

    //1.3 파일이 잘못된 포맷인 경우
    @Test(
            expected = WrongFormFileException.class
    )
    public void saveUploadSupportData_wrongFormFile() throws Exception {
        String fileContents =
                "주택도시기금1)(억원),국민은행(억원),우리은행(억원),신한은행(억원),한국시티은행(억원),하나은행(억원),농협은행/수협은행(억원),외환은행(억원),기타은행(억원)\n" +
                "2005,1,1019,846,82,95,30,157,57,80,99";

        financeService.saveUploadSupportData(fileContents.getBytes("EUC-KR"));
    }

    //2. 전체 금융기관 목록 조회
    //2.1 정상적으로 조회되는 경우
    @Test
    public void findAllBanks_success() {

        //데이터에서 리턴될 은행 목록
        List<Bank> savedBanks = new ArrayList<>();

        savedBanks.add(new Bank("금융기관1"));
        savedBanks.add(new Bank("금융기관2"));
        savedBanks.add(new Bank("금융기관3"));

        given(bankRepository.findAll()).willReturn(savedBanks);

        //전체 금융기관 목록 조회 서비스 테스트
        List<Bank> banks = financeService.findAllBanks();

        assertThat(banks.size()).isEqualTo(3);
        assertThat(banks.get(0).getBankName()).isEqualTo("금융기관1");
        assertThat(banks.get(1).getBankName()).isEqualTo("금융기관2");
        assertThat(banks.get(2).getBankName()).isEqualTo("금융기관3");
    }

    //3. 특정 금융기관의 지원금 통계 조회 (연도별)
    //3.1 정상적으로 조회되는 경우
    @Test
    public void findSupportStaticsByBank_success(){

        //데이터에서 리턴될 지원금 목록
        Bank bank = new Bank("TEST");

        List<Support> savedSupports = new ArrayList<>();

        savedSupports.add(
                new Support(
                        bank,
                        "2000",
                        "1", BigDecimal.ONE)
        );
        savedSupports.add(
                new Support(
                        bank,
                        "2000",
                        "2", BigDecimal.TEN)
        );
        savedSupports.add(
                new Support(
                        bank,
                        "2001",
                        "1", BigDecimal.ZERO)
        );
        savedSupports.add(
                new Support(
                        bank,
                        "2001",
                        "2", BigDecimal.ONE)
        );

        given(supportRepository.findByBank(any(Bank.class))).willReturn(savedSupports);

        //지원금액 데이터 통계 조회 (연도별, 금융기관별) 서비스 테스트
        Map<String, BankSupports> statics = financeService.findSupportStaticsByBank(bank);

        assertThat(statics.size()).isEqualTo(2);
        assertThat(statics.get("2000")).isNotNull();
        assertThat(statics.get("2001")).isNotNull();
        assertThat(statics.get("2000").getSupportsTotal()).isEqualTo(BigDecimal.valueOf(11));
        assertThat(statics.get("2001").getSupportsTotal()).isEqualTo(BigDecimal.ONE);
    }

    //4. 지원금액 데이터 통계 조회 (연도별, 금융기관별)
    //4.1 정상적으로 조회되는 경우
    @Test
    public void findSupportStatics_success(){

        //데이터에서 리턴될 은행 목록
        List<Bank> savedBanks = new ArrayList<>();

        Bank bank1 = new Bank("금융기관1");
        Bank bank2 = new Bank("금융기관2");
        Bank bank3 = new Bank("금융기관3");

        savedBanks.add(bank1);
        savedBanks.add(bank2);
        savedBanks.add(bank3);

        given(bankRepository.findAll()).willReturn(savedBanks);

        //데이터에서 리턴될 지원금 목록
        List<Support> savedSupports = new ArrayList<>();

        savedSupports.add(
                new Support(
                        bank1,
                        "2000",
                        "1", BigDecimal.ONE)
        );
        savedSupports.add(
                new Support(
                        bank1,
                        "2000",
                        "2", BigDecimal.TEN)
        );
        savedSupports.add(
                new Support(
                        bank1,
                        "2001",
                        "1", BigDecimal.ZERO)
        );
        savedSupports.add(
                new Support(
                        bank1,
                        "2001",
                        "2", BigDecimal.ONE)
        );

        given(supportRepository.findByBank(any(Bank.class))).willReturn(savedSupports);

        //지원금액 데이터 통계 조회 (연도별, 금융기관별) 서비스 테스트
        Map<String, List<BankSupports>> statics = financeService.findSupportStatics();

        assertThat(statics.size()).isEqualTo(2);
        assertThat(statics.get("2000").isEmpty()).isFalse();
        assertThat(statics.get("2000").size()).isEqualTo(3);
        assertThat(statics.get("2000").get(0).getSupports().size()).isEqualTo(2);
        assertThat(statics.get("2000").get(1).getSupports().size()).isEqualTo(2);
        assertThat(statics.get("2000").get(2).getSupports().size()).isEqualTo(2);
        assertThat(statics.get("2001").isEmpty()).isFalse();
        assertThat(statics.get("2001").size()).isEqualTo(3);
        assertThat(statics.get("2001").get(0).getSupports().size()).isEqualTo(2);
        assertThat(statics.get("2001").get(1).getSupports().size()).isEqualTo(2);
        assertThat(statics.get("2001").get(2).getSupports().size()).isEqualTo(2);
    }

    //6. 특정 은행의 특정 달에 대해서 2018 년도 해당 달에 금융지원 금액을 예측
    @Test
    public void calcForcastMontlySupportForBank_success(){
        //데이터에서 리턴될 은행 목록
        List<Bank> savedBanks = new ArrayList<>();
        Bank bank = new Bank("금융기관1");
        savedBanks.add(bank);

        //데이터에서 리턴될 지원금 목록
        List<Support> savedSupports = new ArrayList<>();
        savedSupports.add(new Support(bank, "2005","2",BigDecimal.ZERO));
        savedSupports.add(new Support(bank, "2006","2",BigDecimal.ZERO));
        savedSupports.add(new Support(bank, "2007","2",BigDecimal.ZERO));
        savedSupports.add(new Support(bank, "2008","2",BigDecimal.ZERO));
        savedSupports.add(new Support(bank, "2009","2",BigDecimal.ZERO));
        savedSupports.add(new Support(bank, "2010","2",BigDecimal.ZERO));
        savedSupports.add(new Support(bank, "2011","2",BigDecimal.ZERO));
        savedSupports.add(new Support(bank, "2012","2",BigDecimal.ZERO));
        savedSupports.add(new Support(bank, "2013","2",BigDecimal.ZERO));
        savedSupports.add(new Support(bank, "2014","2",BigDecimal.ZERO));

        given(bankRepository.findByBankName(anyString())).willReturn(savedBanks);
        given(supportRepository.findByBankAndMonth(any(Bank.class), anyString())).willReturn(savedSupports);

        Map<String, Object> forecastData = financeService.calcForcastMontlySupportForBank("금융기관1", "2");

        assertThat(forecastData.get("year")).isEqualTo("2015");
        assertThat(forecastData.get("month")).isEqualTo("2");
        assertThat(forecastData.get("amount")).isEqualTo(BigDecimal.valueOf(0L));
    }
}
