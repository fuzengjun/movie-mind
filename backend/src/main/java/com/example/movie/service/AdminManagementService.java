package com.example.movie.service;

import com.example.movie.common.PageResult;
import com.example.movie.dto.admin.AdminMovieRequest;

import java.util.List;
import java.util.Map;

public interface AdminManagementService {
    PageResult<Map<String, Object>> listMovies(String keyword, Integer status, long pageNum, long pageSize);
    Map<String, Object> getMovie(Long id);
    Long createMovie(AdminMovieRequest request);
    void updateMovie(Long id, AdminMovieRequest request);
    void deleteMovies(List<Long> ids);
    void updateMovieStatus(Long id, Integer status);

    PageResult<Map<String, Object>> listUsers(String keyword, Integer status, long pageNum, long pageSize);
    Map<String, Object> getUser(Long id);
    void updateUserStatus(Long id, Integer status);
    void resetUserPassword(Long id, String password);

    PageResult<Map<String, Object>> listComments(String keyword, Long userId, Long movieId, long pageNum, long pageSize);
    void deleteComment(Long id);

    List<Map<String, Object>> listDictionaries(String type, String keyword);
    Long saveDictionary(String type, Long id, Map<String, Object> payload);
    void updateDictionaryStatus(String type, Long id, Integer status);
    void deleteDictionary(String type, Long id);

    PageResult<Map<String, Object>> listPeople(String type, String keyword, long pageNum, long pageSize);
    Long savePerson(String type, Long id, Map<String, Object> payload);
    void deletePerson(String type, Long id);
}
