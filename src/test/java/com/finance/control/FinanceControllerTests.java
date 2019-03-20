package com.finance.control;

import com.finance.data.Bank;
import com.finance.data.Support;
import com.finance.model.BankSupports;
import com.finance.exception.WrongFormFileException;
import com.mixin.UploadResult;
import com.finance.service.FinanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(FinanceController.class)
public class FinanceControllerTests {

    private static final Logger log = LoggerFactory.getLogger(FinanceControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinanceService financeService;

    //1.데이터 파일에서 각 레코드를 데이터베이스에 저장하는 API
    //1.1 정상적인 데이터 파일을 전송, 성공
    @Test
    public void postUploadSupportData_shouldSuccessUpload() throws Exception{

        //데이터 파일 저장 API가 정상적으로 수행됨
        given(financeService.saveUploadSupportData(any(byte[].class))).willReturn(UploadResult.OK);

        mockMvc.perform(MockMvcRequestBuilders
                            .multipart("/finance/uploadSupportData")
                .file("file", "TEST".getBytes()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").value(UploadResult.OK));
    }

    //1.2 파일을 전송하지 않는 경우, 실패
    @Test
    public void postUploadSupportData_noFileUpload() throws Exception{
        //파일이 없는 오류가 발생함
        given(financeService.saveUploadSupportData(null)).willReturn(UploadResult.OK);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/finance/uploadSupportData"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("result").value(UploadResult.NO_FILE));
    }

    //1.3 잘못된 형식의 파일을 전송하는 경우
    @Test
    public void postUploadSupportData_wrongFileUpload() throws Exception{
        //파일 포맷이 맞지않는 오류가 발생함
        given(financeService.saveUploadSupportData(any(byte[].class))).willThrow(new WrongFormFileException());

        mockMvc.perform(MockMvcRequestBuilders
                            .multipart("/finance/uploadSupportData")
                            .file("file", "TEST".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("result").value(UploadResult.WRONG_FORM));
    }

    /*********************************************/
    //2. 주택 금융 공급 금융기관 목록을 출력하는 API
    //2.1 목록 출력이 정상적으로 되는 경우
    @Test
    public void getFindAllBanks_shouldSuccessGet() throws Exception{

        //서비스에서 리턴될 은행 목록
        List<Bank> banks = new ArrayList<>();

        banks.add(new Bank("금융기관1"));
        banks.add(new Bank("금융기관2"));
        banks.add(new Bank("금융기관3"));

        //금융기관 목록조회 API가 정상적으로 수행
        given(financeService.findAllBanks()).willReturn(banks);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/finance/findAllBanks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("banks").isArray())
                    .andExpect(jsonPath("banks[0].bankName").value("금융기관1"))
                    .andExpect(jsonPath("banks[1].bankName").value("금융기관2"))
                    .andExpect(jsonPath("banks[2].bankName").value("금융기관3"));

    }

    //2.2 데이터가 한 건도 없는 경우
    @Test
    public void getFindAllBanks_noDataFound() throws Exception{

        //서비스에서 리턴될 은행 목록
        List<Bank> banks = new ArrayList<>();

        //금융기관 목록조회 API가 정상적으로 수행
        given(financeService.findAllBanks()).willReturn(banks);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/finance/findAllBanks"))
                .andExpect(status().isNotFound());

    }

    /*********************************************/
    //3. 연도별 각 금융기관의 지원금액 합계를 출력하는 API
    //3.1 정상적으로 데이터가 출력 되는 경우
    @Test
    public void getFindSupportsStatics_shouldSuccessGet() throws Exception{

        //서비스에서 리턴될 집계자료 목록
        Map<String, List<BankSupports>> supportStatics = new HashMap<>();
        List<BankSupports> banks = new ArrayList<>();
        Bank bank;
        BankSupports bankSupports;

        for(int i = 1; i < 10; i++){
            bank = new Bank(String.format("금융기관%d", i));
            bankSupports = new BankSupports(bank);

            for(int j = 1; j <= 12; j++) {
                bankSupports.addSupports(
                        new Support(
                                bank,
                                "2000",
                                String.valueOf(j),
                                BigDecimal.TEN.multiply(BigDecimal.valueOf(i))
                        )
                );
            }

            banks.add(bankSupports);
        }

        supportStatics.put("2010", banks);
        supportStatics.put("2009", banks);
        supportStatics.put("2004", banks);
        supportStatics.put("2006", banks);
        supportStatics.put("2000", banks);

        //지원금액 데이터 통계자료 조회
        given(financeService.findSupportStatics()).willReturn(supportStatics);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/finance/findSupportStatics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("statics").isArray())
                .andExpect(jsonPath("statics[0].year").value("2000"))
                .andExpect(jsonPath("statics[0].totalAmount").value(5400))
                .andExpect(jsonPath("statics[0].detailAmount").isArray())
                .andExpect(jsonPath("statics[4].year").value("2010"))
                .andExpect(jsonPath("statics[4].totalAmount").value(5400))
                .andExpect(jsonPath("statics[4].detailAmount").isArray());

    }

    //3.2 데이터가 한 건도 없는 경우
    @Test
    public void getFindSupportsStatics_noDataFound() throws Exception{

        //서비스에서 리턴될 집계자료 목록
        Map<String, List<BankSupports>> supportStatics = new HashMap<>();

        //지원금액 데이터 통계자료 조회
        given(financeService.findSupportStatics()).willReturn(supportStatics);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/finance/findSupportStatics"))
                .andExpect(status().isNotFound());

    }

    /*********************************************/
    //4. 각 연도별 각 기관의 전체 지원금액 중에서 가장 큰 금액의 기관명을 출력하는 API
    //4.1 정상적으로 데이터가 출력 되는 경우
    @Test
    public void getFindLargestSupportBank_shouldSuccessGet() throws Exception{

        //서비스에서 리턴될 집계자료 목록
        Map<String, List<BankSupports>> supportStatics = new HashMap<>();
        List<BankSupports> banks;
        Bank bank;
        BankSupports bankSupports;

        for(int y = 0; y < 5; y++) {
            banks = new ArrayList<>();

            for (int i = 1; i < 10; i++) {
                bank = new Bank(String.format("금융기관%d", i));
                bankSupports = new BankSupports(bank);

                for (int j = 1; j <= 12; j++) {
                    bankSupports.addSupports(
                            new Support(
                                    bank,
                                    String.valueOf(2000+y),
                                    String.valueOf(j),
                                    BigDecimal.TEN
                                            .multiply(BigDecimal.valueOf(i+y))
                            )
                    );
                }
                banks.add(bankSupports);
            }
            supportStatics.put(String.valueOf(2000+y), banks);
        }

        //지원금액 데이터 통계자료 조회
        given(financeService.findSupportStatics()).willReturn(supportStatics);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/finance/findLargestSupportBank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("year").value("2004"))
                .andExpect(jsonPath("bank").value("금융기관9"));

    }

    //4.2 데이터가 한 건도 없는 경우
    @Test
    public void getFindLargestSupportBank_noDataFound() throws Exception{

        //서비스에서 리턴될 집계자료 목록
        Map<String, List<BankSupports>> supportStatics = new HashMap<>();

        //지원금액 데이터 통계자료 조회
        given(financeService.findSupportStatics()).willReturn(supportStatics);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/finance/findLargestSupportBank"))
                .andExpect(status().isNotFound());

    }

    /**********************************************************************/
    //5. 전체 년도에서 외환은행의 지원금액 평균 중에서 가장 작은 금액과 큰 금액을 출력하는 API
    //5.1 정상적으로 데이터가 출력되는 경우
    @Test
    public void getBankSupportLargestAndSmallestAverage_shouldSuccessGet() throws Exception {

        //서비스에서 리턴될 집계자료 목록
        Map<String, BankSupports> supportStatics = new HashMap<>();

        Bank bank = new Bank("TEST");
        BankSupports bankSupports;


        for(int y = 0; y < 5; y++) {
            bankSupports = new BankSupports(bank);

            for (int j = 1; j <= 12; j++) {
                bankSupports.addSupports(
                        new Support(
                                bank,
                                String.valueOf(2000+y),
                                String.valueOf(j),
                                BigDecimal.TEN
                                        .multiply(BigDecimal.valueOf(y+1))
                        )
                );
            }

            supportStatics.put(String.valueOf(2000+y), bankSupports);
        }

        //특정 은행의 지원금액 데이터 통계자료 조회
        given(financeService.findSupportStaticsByBank(anyString())).willReturn(supportStatics);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/finance/findBankSupportLargestAndSmallestAverage/KEB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("bank").value("외환은행"))
                .andExpect(jsonPath("largestYear").value("2004"))
                .andExpect(jsonPath("largestAmount").value(BigDecimal.valueOf(50.0)))
                .andExpect(jsonPath("smallestYear").value("2000"))
                .andExpect(jsonPath("smallestAmount").value(BigDecimal.valueOf(10.0)));
    }

    //5.2 데이터가 한 건도 없는 경우
    @Test
    public void getBankSupportLargestAndSmallestAverage_noDataFound() throws Exception{

        //서비스에서 리턴될 집계자료 목록
        Map<String, BankSupports> supportStatics = new HashMap<>();

        //특정 은행의 지원금액 데이터 통계자료 조회
        given(financeService.findSupportStaticsByBank(anyString())).willReturn(supportStatics);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/finance/findBankSupportLargestAndSmallestAverage/TEST"))
                .andExpect(status().isNotFound());

    }

    /**********************************************************************/
    //6. 특정 은행의 특정 달에 대해서 2018 년도 해당 달에 금융지원 금액을 예측하는 API
    //6.1 정상적으로 예측될 경우
    @Test
    public void getForcastMontlySupportForBank_shouldSuccessGet() throws Exception {
        //서비스에서 리턴될 예상 지원금액과 년도
        Map<String, Object> forecastAmountAndYear = new HashMap<>();
        forecastAmountAndYear.put("bankId", "1");
        forecastAmountAndYear.put("year", "2018");
        forecastAmountAndYear.put("month", "2");
        forecastAmountAndYear.put("amount", BigDecimal.valueOf(0.0));

        //특정 은행의 특정 달 차년도 예상 지원금액 서비스
        given(financeService.calcForcastMontlySupportForBank(anyString(), anyString())).willReturn(forecastAmountAndYear);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/finance/calcForecastMonthlySupportForBank")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"bankName\":\"국민은행\",\"month\":\"2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("bankId").value("1"))
                .andExpect(jsonPath("year").value("2018"))
                .andExpect(jsonPath("month").value("2"))
                .andExpect(jsonPath("amount").value(BigDecimal.valueOf(0.0)));
    }

    //6.2 데이터가 한 건도 없는 경우
    @Test
    public void getForcastMontlySupportForBank_noDataFound() throws Exception{

        //서비스에서 리턴될 집계자료 목록
        Map<String, Object> supportStatics = new HashMap<>();

        //특정 은행의 지원금액 데이터 통계자료 조회
        given(financeService.calcForcastMontlySupportForBank(anyString(), anyString())).willReturn(supportStatics);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/finance/calcForecastMonthlySupportForBank")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"bankName\":\"국민은행\",\"month\":\"2\"}"))
                .andExpect(status().isNotFound());
    }
}
