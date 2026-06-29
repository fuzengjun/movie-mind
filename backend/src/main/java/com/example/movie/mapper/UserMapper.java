package com.example.movie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.movie.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
