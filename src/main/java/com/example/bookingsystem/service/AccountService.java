package com.example.bookingsystem.service;

import com.example.bookingsystem.entity.Account;
import com.example.bookingsystem.repository.AccountRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found"));

        return User.builder()
                .username(account.getUsername())
                .password(account.getPasswordHash())
                .roles(account.getAccessLevel().name())
                .disabled(!account.isEnabled())
                .build();
    }
}