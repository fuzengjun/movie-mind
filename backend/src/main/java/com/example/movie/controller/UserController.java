package com.example.movie.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.movie.common.Result;
import com.example.movie.entity.User;
import com.example.movie.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/user") @RequiredArgsConstructor
public class UserController {
    private final UserMapper users; private final PasswordEncoder encoder;
    @GetMapping("/profile") public Result<Map<String,Object>> profile(Authentication a){return Result.success(profile(user(a)));}
    @PutMapping("/profile") @Transactional public Result<Map<String,Object>> update(Authentication a,@RequestBody Map<String,String>b){
        User u=user(a);String n=b.get("nickname"),e=b.get("email");if(!StringUtils.hasText(n))throw new IllegalArgumentException("昵称不能为空");
        e=StringUtils.hasText(e)?e.trim().toLowerCase():null;if(e!=null&&!e.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new IllegalArgumentException("邮箱格式不正确");
        if(e!=null&&users.selectCount(Wrappers.<User>lambdaQuery().eq(User::getEmail,e).ne(User::getId,u.getId()))>0)throw new IllegalArgumentException("邮箱已被使用");
        u.setNickname(n.trim());u.setEmail(e);users.updateById(u);return Result.success(profile(u));
    }
    @PutMapping("/password") @Transactional public Result<Void> password(Authentication a,@RequestBody Map<String,String>b){
        User u=user(a);String old=b.get("oldPassword"),next=b.get("newPassword");if(!StringUtils.hasText(old)||!encoder.matches(old,u.getPassword()))throw new IllegalArgumentException("原密码不正确");
        if(next==null||next.length()<6)throw new IllegalArgumentException("新密码不能少于 6 位");if(!next.equals(b.get("confirmPassword")))throw new IllegalArgumentException("两次输入的新密码不一致");
        u.setPassword(encoder.encode(next));users.updateById(u);return Result.success(null);
    }
    private User user(Authentication a){User u=users.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,a.getName()).eq(User::getStatus,1));if(u==null)throw new IllegalArgumentException("用户不存在");return u;}
    private Map<String,Object> profile(User u){Map<String,Object>r=new LinkedHashMap<>();r.put("id",u.getId());r.put("username",u.getUsername());r.put("nickname",u.getNickname());r.put("email",u.getEmail());r.put("avatar",u.getAvatar());r.put("role",u.getRole());return r;}
}