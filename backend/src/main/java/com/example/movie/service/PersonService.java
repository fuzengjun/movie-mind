package com.example.movie.service;

import com.example.movie.vo.PersonDetailVO;

public interface PersonService {

    PersonDetailVO getPersonDetail(String type, Long id);
}