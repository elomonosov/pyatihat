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

    public LatenessService(DebtorRepository debtorRepository) {
        this.debtorRepository = debtorRepository;
    }

    private final static Logger logger = LoggerFactory.getLogger(LatenessService.class);

    private DebtorRepository debtorRepository;

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

    private Integer getDebtSum(Debtor debtor) {

        int sum = 0;
        for (Record record : debtor.getRecordSet()) {
            switch (record.getType()) {
                case DEBT: {
                    sum = sum + record.getValue();
                    break;
                }
                case PAYMENT: {
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
                .forEach(debtor -> result.put(debtor.getName(), getDebtSum(debtor)));

        orderByValue(result, new ComparableComparator<>());

        return result;
    }

    static <K, V> void orderByValue(
            LinkedHashMap<K, V> m, Comparator<? super V> c) {
        List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());
        m.clear();
        entries.stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, c.reversed()))
                .forEachOrdered(e -> m.put(e.getKey(), e.getValue()));
    }
}
