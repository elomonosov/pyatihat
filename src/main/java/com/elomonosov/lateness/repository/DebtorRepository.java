package com.elomonosov.lateness.repository;

import com.elomonosov.lateness.model.Debtor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DebtorRepository extends CrudRepository<Debtor, Integer> {

    Optional<Debtor> findByName(String name);

    List<Debtor> findAll();
}
