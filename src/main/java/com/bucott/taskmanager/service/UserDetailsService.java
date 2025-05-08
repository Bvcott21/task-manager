package com.bucott.taskmanager.service;

import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.bucott.taskmanager.exception.UsernameOrEmailNotFoundException;

public interface UserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService {
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException;
    public UserDetails loadUserByUsernameOrEmail(String identifier) throws UsernameNotFoundException;
    public Map<String, Object> authenticate(String identifier, String password) throws UsernameOrEmailNotFoundException;

}
