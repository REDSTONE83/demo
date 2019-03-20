package com.finance.service;

import com.finance.data.Bank;
import com.finance.data.BankRepository;
import com.finance.data.Support;
import com.finance.data.SupportRepository;
import com.finance.exception.NotFoundException;
import com.finance.exception.WrongFormFileException;
import com.finance.model.BankSupports;
import com.mixin.UploadResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

@Service
public class FinanceService {

    private final BankRepository bankRepository;
    private final SupportRepository supportRepository;

    public FinanceService(BankRepository bankRepository, SupportRepository supportRepository) {
        this.bankRepository = bankRepository;
        this.supportRepository = supportRepository;
    }

    //기관별 주택 신용보증 금액 파일 데이터 Upload
    public String saveUploadSupportData(byte[] fileBytes) throws Exception {

        if(fileBytes == null) return UploadResult.NO_FILE;

        ByteArrayInputStream byteIs = new ByteArrayInputStream(fileBytes);
        Reader reader  =  new BufferedReader(new InputStreamReader(byteIs,"EUC-KR"));
        List<CSVRecord> records = CSVFormat.DEFAULT.parse(reader).getRecords();

        //첫줄은 헤더
        CSVRecord headerRecord = records.get(0);

        //첫번째, 두번째 컬럼은 연도와 월 이므로 세번째 컬럼부터 체크
        if(!"연도".equals(headerRecord.get(0)) || !"월".equals(headerRecord.get(1))){
            throw new WrongFormFileException();
        }

        Map<Integer, Bank> banks = new HashMap<>();
        String bankName;

        for(int index = 2; index < headerRecord.size(); index++){
            bankName = headerRecord.get(index);

            //없으면 멈춤
            if(bankName.isEmpty()){
                break;
            }else{
                bankName = bankName.replaceAll("[(억원)]","");
                banks.put(index, new Bank(bankName));
            }
        }

        CSVRecord amountRecord;
        String year;
        String month;

        Set<Support> supports = new HashSet<>();
        Bank supportBank;
        BigDecimal amount;

        for (int index = 1; index < records.size(); index++){
            amountRecord = records.get(index);

            year = amountRecord.get(0);
            month = amountRecord.get(1);

            //금융기관의 숫자만큼
            for (int bankIndex : banks.keySet()){

                supportBank = banks.get(bankIndex);
                amount = BigDecimal.valueOf(
                            Long.valueOf(
                                amountRecord.get(bankIndex)
                                    .replaceAll(",","")
                ));

                supports.add(
                        new Support(
                                supportBank,
                                year,
                                month,
                                amount
                        ));
            }
        }

        for (Bank bank : banks.values()){
            bankRepository.save(bank);
        }

        for (Support support : supports){
            supportRepository.save(support);
        }

        return UploadResult.OK;
    }

    //전체 금융기관 목록 조회
    public List<Bank> findAllBanks() {
        return (List<Bank>) bankRepository.findAll();
    }

    //특정 기관의 지원금액 데이터 통계 조회 (연도별) - 금융기관명으로
    public Map<String, BankSupports> findSupportStaticsByBank(String bankName){
        List<Bank> banks = bankRepository.findByBankName(bankName);

        if(!banks.isEmpty()){
            return findSupportStaticsByBank(banks.get(0));
        }else{
            return new HashMap<>();
        }
    }

    //특정 기관의 지원금액 데이터 통계 조회 (연도별) - 금융기관 객체로
    public Map<String, BankSupports> findSupportStaticsByBank(Bank bank){

        List<Support> supports = supportRepository.findByBank(bank);

        Map<String, BankSupports> bankStatics = new HashMap<>();

        BankSupports bankSupports;
        for(Support support : supports){
            if(bankStatics.containsKey(support.getYear())){
                bankSupports = bankStatics.get(support.getYear());
            }else{
                bankSupports = new BankSupports(bank);
                bankStatics.put(support.getYear(), bankSupports);
            }

            bankSupports.addSupports(support);
        }

        return bankStatics;
    }

    //전체 지원금액 데이터 통계 조회 (연도별, 금융기관별)
    public Map<String, List<BankSupports>> findSupportStatics() {

        List<Bank> banks = findAllBanks();

        Map<String, List<BankSupports>> statics = new HashMap<>();
        List<BankSupports> yearlyStatics;
        Map<String, BankSupports> staticsByBank;

        for(Bank bank : banks){
            staticsByBank = findSupportStaticsByBank(bank);

            for(String year : staticsByBank.keySet()){

                if(!statics.containsKey(year)){
                    yearlyStatics = new ArrayList<>();
                    statics.put(year, yearlyStatics);
                }else{
                    yearlyStatics = statics.get(year);
                }

                yearlyStatics.add(staticsByBank.get(year));
            }
        }

        return statics;
    }

    //특정 은행의 특정 달에 대해서 2018 년도 해당 달에 금융지원 금액을 예측
    public Map<String, Object> calcForcastMontlySupportForBank(String bankName, String month) {

        List<Bank> banks = bankRepository.findByBankName(bankName);
        Bank bank;

        if(banks.isEmpty()) {
            throw new NotFoundException();
        }else{
            bank = banks.get(0);
        }

        List<Support> supports = supportRepository.findByBankAndMonth(bank, month);

        if(supports.isEmpty()) {
            throw new NotFoundException();
        }
        supports.sort(new SupportYearAcending());

        // 다항식 추세선 활용
        final WeightedObservedPoints obs = new WeightedObservedPoints();

        Support support;
        for(int index = 0; index < supports.size(); index++){
            support = supports.get(index);
            obs.add(
                    (double) index + 1L,
                    support.getAmount().doubleValue()
            );
        }

        // 3차 다항식 으로
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);

        // 3차 다항식 계수 확보
        final double[] coeff = fitter.fit(obs.toList());

        // 마지막 데이터의 차년도 예상금액을 계산
        String year = String.valueOf(
                Integer.valueOf(
                        supports.get(supports.size()-1).getYear())+1
        );

        //3차 다항식 계수로 예상금액 계산
        int number = supports.size() + 1;
        double forecastAmount =
                Math.pow(number, 0) * coeff[0] +
                Math.pow(number, 1) * coeff[1] +
                Math.pow(number, 2) * coeff[2] +
                Math.pow(number, 3) * coeff[3];

        Map<String, Object> returnObj = new HashMap<>();
        returnObj.put("bankId", bank.getId());
        returnObj.put("year", year);
        returnObj.put("month", month);
        returnObj.put("amount", BigDecimal.valueOf((long)forecastAmount));

        return returnObj;
    }

    private class SupportYearAcending implements Comparator<Support>{

        @Override
        public int compare(Support o1, Support o2) {
            return (o1.getYear()).compareTo(o2.getYear());
        }
    }
}
