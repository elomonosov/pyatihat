package com.elomonosov.lateness.service;

import com.elomonosov.lateness.model.User;
import com.elomonosov.lateness.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;

    public String resolveLogin(String login) {
        return userRepository.findByLogin(login)
                .orElse(new User() {{
                    setName(login);
                }})
                .getName();
    }

    public User createUser(String login, String name) {
        User user = new User();
        user.setName(name);
        user.setLogin(login);
        return userRepository.save(user);
    }
}
