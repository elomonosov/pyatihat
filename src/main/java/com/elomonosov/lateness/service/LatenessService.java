package com.elomonosov.lateness.service;

import com.elomonosov.lateness.model.Record;
import com.elomonosov.lateness.model.Debtor;
import com.elomonosov.lateness.repository.DebtorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.comparator.ComparableComparator;

import java.util.*;

@Service
@Transactional
public class LatenessService {

    public LatenessService(DebtorRepository debtorRepository, UserService userService) {
        this.debtorRepository = debtorRepository;
        this.userService = userService;
    }

    private final static Logger logger = LoggerFactory.getLogger(LatenessService.class);

    private DebtorRepository debtorRepository;
    private UserService userService;

    public Integer addDebt(String debtorName, int value, String reporter) {
        return getDebtSum(debtorRepository.findByName(debtorName)
                .map(debtor -> {
                    debtor.addRecord(value, Record.Type.DEBT, reporter);
                    return debtor;
                }).orElseGet(() -> {
                    Debtor debtor = new Debtor(debtorName);
                    debtor.addRecord(value, Record.Type.DEBT, reporter);
                    return debtorRepository.save(debtor);
                })
        );
    }

    public Integer getDebt(String debtorName) {
        return debtorRepository.findByName(debtorName)
                .map(this::getDebtSum)
                .orElse(0);
    }

    public Integer payDebt(String debtorName, int value, String reporter) {
        return debtorRepository.findByName(debtorName)
                .map(debtor -> {
                    debtor.addRecord(value, Record.Type.PAYMENT, reporter);
                    return getDebtSum(debtor);
                }).orElseGet(() -> {
                    Debtor debtor = new Debtor(debtorName);
                    debtor.addRecord(value, Record.Type.PAYMENT, reporter);
                    return getDebtSum(debtor);
                });
    }

    public Map<String, Integer> getBalance() {

        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();

        debtorRepository.findAll()
                .forEach(debtor -> {
                    Integer sum = getDebtSum(debtor);
                    if (!sum.equals(0)) {
                        result.put(
                                userService.resolveLogin(debtor.getName()),
                                sum);
                    }
                });

        orderByValue(result, new ComparableComparator<>());

        return result;
    }

    public Integer cancelDebt(String debtorName, int value, String reporter) {
        return debtorRepository.findByName(debtorName)
                .map(debtor -> {
                    debtor.addRecord(value, Record.Type.CANCEL, reporter);
                    return getDebtSum(debtor);
                }).orElseThrow(() -> new IllegalStateException("debtorName " + debtorName + " not found"));
    }

    static <K, V> void orderByValue(
            LinkedHashMap<K, V> m, Comparator<? super V> c) {
        List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());
        m.clear();
        entries.stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, c.reversed()))
                .forEachOrdered(e -> m.put(e.getKey(), e.getValue()));
    }

    private Integer getDebtSum(Debtor debtor) {

        int sum = 0;
        for (Record record : debtor.getRecordSet()) {
            switch (record.getType()) {
                case DEBT: {
                    sum = sum + record.getValue();
                    break;
                }
                case PAYMENT:
                case CANCEL: {
                    sum = sum - record.getValue();
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown type: " + record.getType());
                }
            }
        }
        return sum;
    }
}
