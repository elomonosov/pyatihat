package com.elomonosov.lateness.repository;

import com.elomonosov.lateness.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByLogin(String login);

    List<User> findAll();
}
