package com.example.movie.controller.admin;

import com.example.movie.common.PageResult;
import com.example.movie.common.Result;
import com.example.movie.dto.admin.AdminMovieRequest;
import com.example.movie.service.AdminManagementService;
import com.example.movie.service.MovieCacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminManagementController {
    private final AdminManagementService service;
    private final MovieCacheService movieCache;

    @GetMapping("/movies/page")
    public Result<PageResult<Map<String, Object>>> movies(@RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(service.listMovies(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/movies/{id}")
    public Result<Map<String, Object>> movie(@PathVariable Long id) {
        return Result.success(service.getMovie(id));
    }

    @PostMapping("/movies")
    public Result<Long> createMovie(@Valid @RequestBody AdminMovieRequest body) {
        Long id = service.createMovie(body);
        movieCache.evictMovieCaches();
        return Result.success(id);
    }

    @PutMapping("/movies/{id}")
    public Result<Void> updateMovie(@PathVariable Long id, @Valid @RequestBody AdminMovieRequest body) {
        service.updateMovie(id, body);
        movieCache.evictMovieCaches();
        return Result.success(null);
    }

    @PutMapping("/movies/{id}/status")
    public Result<Void> movieStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        service.updateMovieStatus(id, body.get("status"));
        movieCache.evictMovieCaches();
        return Result.success(null);
    }

    @DeleteMapping("/movies/{id}")
    public Result<Void> deleteMovie(@PathVariable Long id) {
        service.deleteMovies(List.of(id));
        movieCache.evictMovieCaches();
        return Result.success(null);
    }

    @DeleteMapping("/movies/batch")
    public Result<Void> deleteMovies(@RequestBody List<Long> ids) {
        service.deleteMovies(ids);
        movieCache.evictMovieCaches();
        return Result.success(null);
    }

    @GetMapping("/users/page")
    public Result<PageResult<Map<String, Object>>> users(@RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(service.listUsers(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/users/{id}")
    public Result<Map<String, Object>> user(@PathVariable Long id) {
        return Result.success(service.getUser(id));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> userStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        service.updateUserStatus(id, body.get("status"));
        return Result.success(null);
    }

    @PutMapping("/users/{id}/password")
    public Result<Void> userPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        service.resetUserPassword(id, body.get("password"));
        return Result.success(null);
    }

    @GetMapping("/comments")
    public Result<PageResult<Map<String, Object>>> comments(@RequestParam(required = false) String keyword, @RequestParam(required = false) Long userId, @RequestParam(required = false) Long movieId, @RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(service.listComments(keyword, userId, movieId, pageNum, pageSize));
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        service.deleteComment(id);
        return Result.success(null);
    }

    @GetMapping("/{type:categories|tags}")
    public Result<List<Map<String, Object>>> dictionaries(@PathVariable String type, @RequestParam(required = false) String keyword) {
        return Result.success(service.listDictionaries(type, keyword));
    }

    @PostMapping("/{type:categories|tags}")
    public Result<Long> createDictionary(@PathVariable String type, @RequestBody Map<String, Object> body) {
        Long id = service.saveDictionary(type, null, body);
        movieCache.evictMovieCaches();
        return Result.success(id);
    }

    @PutMapping("/{type:categories|tags}/{id}")
    public Result<Long> updateDictionary(@PathVariable String type, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long result = service.saveDictionary(type, id, body);
        movieCache.evictMovieCaches();
        return Result.success(result);
    }

    @PutMapping("/{type:categories|tags}/{id}/status")
    public Result<Void> dictionaryStatus(@PathVariable String type, @PathVariable Long id, @RequestBody Map<String, Integer> body) {
        service.updateDictionaryStatus(type, id, body.get("status"));
        movieCache.evictMovieCaches();
        return Result.success(null);
    }

    @DeleteMapping("/{type:categories|tags}/{id}")
    public Result<Void> deleteDictionary(@PathVariable String type, @PathVariable Long id) {
        service.deleteDictionary(type, id);
        movieCache.evictMovieCaches();
        return Result.success(null);
    }

    @GetMapping("/{type:actors|directors}")
    public Result<PageResult<Map<String, Object>>> people(@PathVariable String type, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(service.listPeople(type, keyword, pageNum, pageSize));
    }

    @PostMapping("/{type:actors|directors}")
    public Result<Long> createPerson(@PathVariable String type, @RequestBody Map<String, Object> body) {
        return Result.success(service.savePerson(type, null, body));
    }

    @PutMapping("/{type:actors|directors}/{id}")
    public Result<Long> updatePerson(@PathVariable String type, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return Result.success(service.savePerson(type, id, body));
    }

    @DeleteMapping("/{type:actors|directors}/{id}")
    public Result<Void> deletePerson(@PathVariable String type, @PathVariable Long id) {
        service.deletePerson(type, id);
        return Result.success(null);
    }
}
