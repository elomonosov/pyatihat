package com.elomonosov.lateness.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "debtors")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Debtor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    @ElementCollection
    @CollectionTable(name = "records",
            joinColumns = {@JoinColumn(name = "debtor_id")})
    private Set<Record> recordSet = new HashSet<>();

    public Debtor(String name) {
        this.name = name;
    }

    public void addRecord(Record record) {
        recordSet.add(record);
    }

    public void addRecord(int value, Record.Type type, String reporter) {
        Record record = new Record();
        record.setType(type);
        record.setValue(value);
        record.setReporter(reporter);
        addRecord(record);
    }
}
