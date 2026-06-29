package com.example.movie.service.impl;

import com.example.movie.entity.User;
import com.example.movie.mapper.UserMapper;
import com.example.movie.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
