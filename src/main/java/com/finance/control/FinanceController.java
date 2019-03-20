package com.finance.control;

import com.finance.data.Bank;
import com.finance.exception.NoUploadFileException;
import com.finance.exception.NotFoundException;
import com.finance.exception.WrongFormFileException;
import com.finance.model.BankSupports;
import com.mixin.UploadResult;
import com.finance.service.FinanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.web.JsonPath;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/finance")
public class FinanceController {

    private static final Logger log = LoggerFactory.getLogger(FinanceController.class);

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    //데이터가 없는 경우 NOT FOUND
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private Map<String, Object> dataNotFoundHandler(NotFoundException ex){
        return new HashMap<>();
    }

    //1. 데이터 파일에서 각 레코드를 데이터베이스에 저장하는 API
    @PostMapping("/uploadSupportData")
    private Map<String, Object> uploadSupportData (MultipartRequest request) throws Exception {

        //받아온 파일 업로드 수행
        MultipartFile supportDataFile = request.getFile("file");
        if(supportDataFile == null){
            throw new NoUploadFileException();
        }

        String resultSave = financeService.saveUploadSupportData(supportDataFile.getBytes());

        //응답전문 작성
        Map<String, Object> responseBody = new HashMap();
        responseBody.put("result", resultSave);

        return responseBody;
    }

    //업로드된 파일이 없는 경우 예외
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private Map<String, Object> noUploadFileHandler(NoUploadFileException ex){
        //응답전문 작성
        Map<String, Object> responseBody = new HashMap();
        responseBody.put("result", UploadResult.NO_FILE);

        return responseBody;
    }

    //업로드된 파일이 잘못된 포맷인 경우 예외
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private Map<String, Object> noUploadFileHandler(WrongFormFileException ex){
        //응답전문 작성
        Map<String, Object> responseBody = new HashMap();
        responseBody.put("result", UploadResult.WRONG_FORM);

        return responseBody;
    }

    //2. 전체 금융기관 목록을 조회
    @GetMapping("/findAllBanks")
    private Map<String, Object> findAllBanks(){

        List<Bank> banks = financeService.findAllBanks();

        if(banks.size() == 0) {
            throw new NotFoundException();
        }

        List<Map<String, String>> responseBanks = new ArrayList<>();
        Map<String, String> responseBank;
        for(Bank bank : banks){
            responseBank = new HashMap<>();
            responseBank.put("bankName", bank.getBankName());
            responseBanks.add(responseBank);
        }

        //응답전문 작성
        Map<String, Object> responseBody = new HashMap();
        responseBody.put("banks", responseBanks);

        return responseBody;
    }

    //3. 연도별 각 금융기관의 지원금액 합계를 출력하는 API
    @GetMapping("/findSupportStatics")
    private Map<String, Object> findSupportStatics(){

        Map<String, List<BankSupports>> supportStatics = financeService.findSupportStatics();

        if(supportStatics.size() == 0) {
            throw new NotFoundException();
        }

        List<Map<String, Object>> statics = new ArrayList<>();
        Map<String, Object> yearlyStatics;

        List<Map<String, Object>> yearlyStaticDetails;
        Map<String, Object> yearlyStaticDetail;

        BigDecimal yearlyTotal;

        List<String> years = new ArrayList<>(supportStatics.keySet());
        years.sort(Comparator.naturalOrder());

        for(String year : years){
            yearlyStatics = new HashMap<>();
            yearlyTotal = BigDecimal.ZERO;
            yearlyStaticDetails = new ArrayList<>();

            for(BankSupports bankSupport : supportStatics.get(year)){

                yearlyTotal = yearlyTotal.add(bankSupport.getSupportsTotal());

                yearlyStaticDetail = new HashMap<>();
                yearlyStaticDetail.put(
                        bankSupport.getBank().getBankName(),
                        bankSupport.getSupportsTotal()
                );
                yearlyStaticDetails.add(yearlyStaticDetail);
            }

            yearlyStatics.put("year", year);
            yearlyStatics.put("totalAmount", yearlyTotal.longValue());
            yearlyStatics.put("detailAmount", yearlyStaticDetails);

            statics.add(yearlyStatics);
        }

        //응답전문 작성
        Map<String, Object> responseBody = new HashMap();
        responseBody.put("statics", statics);

        return responseBody;
    }

    //4. 각 연도별 각 기관의 전체 지원금액 중에서 가장 큰 금액의 기관명을 출력하는 API
    @GetMapping("/findLargestSupportBank")
    private Map<String, Object> findLargestSupportBank(){

        Map<String, List<BankSupports>> supportStatics = financeService.findSupportStatics();

        if(supportStatics.size() == 0) {
            throw new NotFoundException();
        }

        String largestYear = null;
        String largestBank = null;
        BigDecimal largestAmount = BigDecimal.ZERO;

        for(String year : supportStatics.keySet()){
            for(BankSupports bankSupport : supportStatics.get(year)){

                if(largestAmount.compareTo(bankSupport.getSupportsTotal()) < 0){
                    largestYear = year;
                    largestBank = bankSupport.getBank().getBankName();
                    largestAmount = bankSupport.getSupportsTotal();
                }
            }
        }

        //응답전문 작성
        Map<String, Object> responseBody = new HashMap();
        responseBody.put("year", largestYear);
        responseBody.put("bank", largestBank);

        return responseBody;
    }

    //5. 전체 년도에서 외환은행의 지원금액 평균 중에서 가장 작은 금액과 큰 금액을 출력하는 API
    @GetMapping("/findBankSupportLargestAndSmallestAverage/KEB")
    private Map<String, Object> findBankSupportLargestAndSmallestAverage(){

        String bankName = "외환은행";
        Map<String, BankSupports> bankSupportStatics = financeService.findSupportStaticsByBank(bankName);

        if(bankSupportStatics.isEmpty()) {
            throw new NotFoundException();
        }

        String largestYear = null;
        BigDecimal largestAmount = BigDecimal.valueOf(Long.MIN_VALUE);

        String smallestYear = null;
        BigDecimal smallestAmount = BigDecimal.valueOf(Long.MAX_VALUE);

        BankSupports bankSupport;

        for(String year : bankSupportStatics.keySet()){

            bankSupport = bankSupportStatics.get(year);

            if(largestAmount.compareTo(bankSupport.getSupportsAverage()) < 0){
                largestYear = year;
                largestAmount = bankSupport.getSupportsAverage();
            }

            if(smallestAmount.compareTo(bankSupport.getSupportsAverage()) > 0){
                smallestYear = year;
                smallestAmount = bankSupport.getSupportsAverage();
            }
        }

        //응답전문 작성
        Map<String, Object> responseBody = new HashMap();

        responseBody.put("largestYear", largestYear);
        responseBody.put("largestAmount", largestAmount);
        responseBody.put("smallestYear", smallestYear);
        responseBody.put("smallestAmount", smallestAmount);
        responseBody.put("bank", bankName);

        return responseBody;
    }

    //6. 특정 은행의 특정 달에 대해서 2018 년도 해당 달에 금융지원 금액을 예측하는 API
    @PostMapping("/calcForecastMonthlySupportForBank")
    private Map<String, Object> calcForecastMonthlySupportForBank(@RequestBody Map<String, Object> jsonParam){

        String bankName = (String) jsonParam.get("bankName");
        String month = (String) jsonParam.get("month");

        Map<String, Object> forecastData = financeService.calcForcastMontlySupportForBank(bankName, month);

        if(forecastData.get("amount") == null){
            throw new NotFoundException();
        }

        //응답전문 작성
        Map<String, Object> responseBody = new HashMap();

        responseBody.put("bankId", forecastData.get("bankId"));
        responseBody.put("year", forecastData.get("year"));
        responseBody.put("month", forecastData.get("month"));
        responseBody.put("amount", forecastData.get("amount"));

        return responseBody;
    }
}