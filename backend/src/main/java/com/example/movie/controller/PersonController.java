package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.service.PersonService;
import com.example.movie.vo.PersonDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/people")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping("/{type}/{id}")
    public Result<PersonDetailVO> detail(@PathVariable String type, @PathVariable Long id) {
        PersonDetailVO detail = personService.getPersonDetail(type, id);
        if (detail == null) {
            return Result.fail("人物不存在");
        }
        return Result.success(detail);
    }
}