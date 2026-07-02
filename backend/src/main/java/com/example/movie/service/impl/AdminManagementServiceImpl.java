package com.example.movie.service.impl;

import com.example.movie.common.PageResult;
import com.example.movie.dto.admin.AdminMovieRequest;
import com.example.movie.service.AdminManagementService;
import com.example.movie.utils.RegionNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AdminManagementServiceImpl implements AdminManagementService {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<Map<String, Object>> listMovies(String keyword, Integer status, long pageNum, long pageSize) {
        Paging paging = paging(pageNum, pageSize);
        List<Object> args = new ArrayList<>();
        String where = " WHERE m.deleted = 0";
        if (StringUtils.hasText(keyword)) {
            where += " AND (m.title LIKE ? OR m.original_title LIKE ? OR CAST(m.tmdb_id AS CHAR) LIKE ?)";
            String value = "%" + keyword.trim() + "%";
            args.add(value); args.add(value); args.add(value);
        }
        if (status != null) { where += " AND m.status = ?"; args.add(status); }
        Long total = count("SELECT COUNT(*) FROM movie m" + where, args);
        List<Object> queryArgs = new ArrayList<>(args);
        queryArgs.add(paging.size()); queryArgs.add(paging.offset());
        String sql = """
                SELECT m.id, m.tmdb_id AS tmdbId, m.title, m.original_title AS originalTitle,
                       m.poster_url AS posterUrl, m.region, m.release_date AS releaseDate,
                       m.average_rating AS averageRating, m.favorite_count AS favoriteCount,
                       m.view_count AS viewCount, m.status,
                       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ',') AS categories,
                       GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ',') AS directors,
                       GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ',') AS actors
                FROM movie m
                LEFT JOIN movie_category mc ON mc.movie_id=m.id
                LEFT JOIN category c ON c.id=mc.category_id AND c.deleted=0
                LEFT JOIN movie_director md ON md.movie_id=m.id
                LEFT JOIN director d ON d.id=md.director_id AND d.deleted=0
                LEFT JOIN movie_actor ma ON ma.movie_id=m.id
                LEFT JOIN actor a ON a.id=ma.actor_id AND a.deleted=0
                """ + where + " GROUP BY m.id ORDER BY m.update_time DESC, m.id DESC LIMIT ? OFFSET ?";
        return page(total, paging, jdbcTemplate.queryForList(sql, queryArgs.toArray()));
    }

    @Override
    public Map<String, Object> getMovie(Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id, tmdb_id AS tmdbId, title, original_title AS originalTitle, overview,
                       poster_url AS posterUrl, backdrop_url AS backdropUrl, release_date AS releaseDate,
                       runtime, region, language, average_rating AS averageRating, status
                FROM movie WHERE id=? AND deleted=0
                """, id);
        if (rows.isEmpty()) throw new ResponseStatusException(NOT_FOUND, "影片不存在");
        Map<String, Object> movie = new java.util.LinkedHashMap<>(rows.getFirst());
        movie.put("categoryIds", relationIds("movie_category", "category_id", id));
        movie.put("tagIds", relationIds("movie_tag", "tag_id", id));
        movie.put("actorIds", relationIds("movie_actor", "actor_id", id));
        movie.put("directorIds", relationIds("movie_director", "director_id", id));
        return movie;
    }

    @Override @Transactional
    public Long createMovie(AdminMovieRequest r) {
        jdbcTemplate.update("""
                INSERT INTO movie(title,original_title,overview,poster_url,backdrop_url,release_date,runtime,region,language,
                    average_rating,tmdb_rating,favorite_count,view_count,status,deleted,create_time,update_time)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,0,0,?,0,NOW(),NOW())
                """, r.getTitle(), r.getOriginalTitle(), r.getOverview(), r.getPosterUrl(), r.getBackdropUrl(),
                date(r.getReleaseDate()), r.getRuntime(), RegionNormalizer.normalize(r.getRegion()), r.getLanguage(), value(r.getAverageRating(), 0),
                value(r.getAverageRating(), 0), normalizeStatus(r.getStatus()));
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        syncMovieRelations(id, r);
        return id;
    }

    @Override @Transactional
    public void updateMovie(Long id, AdminMovieRequest r) {
        int changed = jdbcTemplate.update("""
                UPDATE movie SET title=?,original_title=?,overview=?,poster_url=?,backdrop_url=?,release_date=?,runtime=?,
                    region=?,language=?,average_rating=?,status=?,update_time=NOW() WHERE id=? AND deleted=0
                """, r.getTitle(), r.getOriginalTitle(), r.getOverview(), r.getPosterUrl(), r.getBackdropUrl(),
                date(r.getReleaseDate()), r.getRuntime(), RegionNormalizer.normalize(r.getRegion()), r.getLanguage(), value(r.getAverageRating(), 0),
                normalizeStatus(r.getStatus()), id);
        ensureChanged(changed, "影片不存在");
        syncMovieRelations(id, r);
    }

    @Override @Transactional
    public void deleteMovies(List<Long> ids) {
        if (ids == null || ids.isEmpty()) throw new ResponseStatusException(BAD_REQUEST, "请选择要删除的影片");
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        jdbcTemplate.update("UPDATE movie SET deleted=1,update_time=NOW() WHERE id IN (" + placeholders + ")", ids.toArray());
    }

    @Override public void updateMovieStatus(Long id, Integer status) {
        ensureChanged(jdbcTemplate.update("UPDATE movie SET status=?,update_time=NOW() WHERE id=? AND deleted=0", normalizeStatus(status), id), "影片不存在");
    }

    @Override
    public PageResult<Map<String, Object>> listUsers(String keyword, Integer status, long pageNum, long pageSize) {
        Paging p = paging(pageNum, pageSize); List<Object> args = new ArrayList<>();
        String where = " WHERE u.deleted=0";
        if (StringUtils.hasText(keyword)) { where += " AND (u.username LIKE ? OR u.nickname LIKE ? OR u.email LIKE ?)"; String v="%"+keyword.trim()+"%"; args.add(v);args.add(v);args.add(v); }
        if (status != null) { where += " AND u.status=?"; args.add(status); }
        Long total=count("SELECT COUNT(*) FROM user u"+where,args); List<Object> qa=new ArrayList<>(args);qa.add(p.size());qa.add(p.offset());
        String sql="""
                SELECT u.id,u.username,u.nickname,u.email,u.avatar,u.role,u.status,u.create_time AS createTime,
                  (SELECT COUNT(*) FROM favorite f WHERE f.user_id=u.id) AS favoriteCount,
                  (SELECT COUNT(*) FROM comment c WHERE c.user_id=u.id AND c.deleted=0) AS commentCount,
                  (SELECT COUNT(*) FROM rating r WHERE r.user_id=u.id) AS ratingCount
                FROM user u
                """+where+" ORDER BY u.create_time DESC,u.id DESC LIMIT ? OFFSET ?";
        return page(total,p,jdbcTemplate.queryForList(sql,qa.toArray()));
    }

    @Override public Map<String, Object> getUser(Long id) {
        List<Map<String,Object>> rows=jdbcTemplate.queryForList("SELECT id,username,nickname,email,avatar,role,status,create_time AS createTime FROM user WHERE id=? AND deleted=0",id);
        if(rows.isEmpty()) throw new ResponseStatusException(NOT_FOUND,"用户不存在");
        Map<String,Object> result=new java.util.LinkedHashMap<>(rows.getFirst());
        result.put("favorites",jdbcTemplate.queryForList("SELECT m.id,m.title,m.poster_url AS posterUrl,f.create_time AS createTime FROM favorite f JOIN movie m ON m.id=f.movie_id WHERE f.user_id=? AND m.deleted=0 ORDER BY f.create_time DESC LIMIT 20",id));
        result.put("ratings",jdbcTemplate.queryForList("SELECT m.id,m.title,r.score,r.create_time AS createTime FROM rating r JOIN movie m ON m.id=r.movie_id WHERE r.user_id=? AND m.deleted=0 ORDER BY r.create_time DESC LIMIT 20",id));
        return result;
    }
    @Override public void updateUserStatus(Long id,Integer status){ ensureChanged(jdbcTemplate.update("UPDATE user SET status=?,update_time=NOW() WHERE id=? AND deleted=0",normalizeStatus(status),id),"用户不存在"); }
    @Override public void resetUserPassword(Long id,String password){ if(!StringUtils.hasText(password)||password.length()<6) throw new ResponseStatusException(BAD_REQUEST,"新密码不能少于 6 位"); ensureChanged(jdbcTemplate.update("UPDATE user SET password=?,update_time=NOW() WHERE id=? AND deleted=0",passwordEncoder.encode(password),id),"用户不存在"); }

    @Override
    public PageResult<Map<String, Object>> listComments(String keyword,Long userId,Long movieId,long pageNum,long pageSize){
        Paging p=paging(pageNum,pageSize);List<Object> args=new ArrayList<>();String where=" WHERE c.deleted=0";
        if(StringUtils.hasText(keyword)){where+=" AND c.content LIKE ?";args.add("%"+keyword.trim()+"%");} if(userId!=null){where+=" AND c.user_id=?";args.add(userId);} if(movieId!=null){where+=" AND c.movie_id=?";args.add(movieId);}
        Long total=count("SELECT COUNT(*) FROM comment c"+where,args);List<Object> qa=new ArrayList<>(args);qa.add(p.size());qa.add(p.offset());
        return page(total,p,jdbcTemplate.queryForList("SELECT c.id,c.content,c.create_time AS createTime,u.id AS userId,u.nickname,m.id AS movieId,m.title FROM comment c JOIN user u ON u.id=c.user_id JOIN movie m ON m.id=c.movie_id"+where+" ORDER BY c.create_time DESC LIMIT ? OFFSET ?",qa.toArray()));
    }
    @Override public void deleteComment(Long id){ensureChanged(jdbcTemplate.update("UPDATE comment SET deleted=1,update_time=NOW() WHERE id=? AND deleted=0",id),"评论不存在");}

    @Override public List<Map<String,Object>> listDictionaries(String type,String keyword){String table=dictionaryTable(type);String where=" WHERE deleted=0";List<Object> args=new ArrayList<>();if(StringUtils.hasText(keyword)){where+=" AND name LIKE ?";args.add("%"+keyword.trim()+"%");}return jdbcTemplate.queryForList("SELECT id,name,description,status,create_time AS createTime FROM "+table+where+" ORDER BY update_time DESC,id DESC",args.toArray());}
    @Override public Long saveDictionary(String type,Long id,Map<String,Object> body){String table=dictionaryTable(type);String name=text(body,"name");if(!StringUtils.hasText(name))throw new ResponseStatusException(BAD_REQUEST,"名称不能为空");String description=text(body,"description");Integer status=intValue(body.get("status"),1);if(id==null){jdbcTemplate.update("INSERT INTO "+table+"(name,description,status,deleted,create_time,update_time) VALUES(?,?,?,0,NOW(),NOW())",name,description,status);return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()",Long.class);}ensureChanged(jdbcTemplate.update("UPDATE "+table+" SET name=?,description=?,status=?,update_time=NOW() WHERE id=? AND deleted=0",name,description,status,id),"数据不存在");return id;}
    @Override public void updateDictionaryStatus(String type,Long id,Integer status){String table=dictionaryTable(type);ensureChanged(jdbcTemplate.update("UPDATE "+table+" SET status=?,update_time=NOW() WHERE id=? AND deleted=0",normalizeStatus(status),id),"数据不存在");}
    @Override public void deleteDictionary(String type,Long id){String table=dictionaryTable(type);String relation=table.equals("category")?"movie_category":"movie_tag";String column=table+"_id";Long used=jdbcTemplate.queryForObject("SELECT COUNT(*) FROM "+relation+" WHERE "+column+"=?",Long.class,id);if(used!=null&&used>0)throw new ResponseStatusException(BAD_REQUEST,"该项已被影片使用，请先禁用或解除绑定");ensureChanged(jdbcTemplate.update("UPDATE "+table+" SET deleted=1,update_time=NOW() WHERE id=? AND deleted=0",id),"数据不存在");}

    @Override public PageResult<Map<String,Object>> listPeople(String type,String keyword,long pageNum,long pageSize){String table=personTable(type);Paging p=paging(pageNum,pageSize);List<Object> args=new ArrayList<>();String where=" WHERE deleted=0";if(StringUtils.hasText(keyword)){where+=" AND (name LIKE ? OR original_name LIKE ?)";String v="%"+keyword.trim()+"%";args.add(v);args.add(v);}Long total=count("SELECT COUNT(*) FROM "+table+where,args);List<Object> qa=new ArrayList<>(args);qa.add(p.size());qa.add(p.offset());return page(total,p,jdbcTemplate.queryForList("SELECT id,name,original_name AS originalName,gender,birthday,nationality,profile_url AS profileUrl,biography FROM "+table+where+" ORDER BY update_time DESC,id DESC LIMIT ? OFFSET ?",qa.toArray()));}
    @Override public Long savePerson(String type,Long id,Map<String,Object> b){String table=personTable(type);String name=text(b,"name");if(!StringUtils.hasText(name))throw new ResponseStatusException(BAD_REQUEST,"姓名不能为空");Object[] values={name,text(b,"originalName"),intValue(b.get("gender"),0),b.get("birthday"),text(b,"nationality"),text(b,"profileUrl"),text(b,"biography")};if(id==null){jdbcTemplate.update("INSERT INTO "+table+"(name,original_name,gender,birthday,nationality,profile_url,biography,deleted,create_time,update_time) VALUES(?,?,?,?,?,?,?,0,NOW(),NOW())",values);return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()",Long.class);}List<Object> args=new ArrayList<>(List.of(values));args.add(id);ensureChanged(jdbcTemplate.update("UPDATE "+table+" SET name=?,original_name=?,gender=?,birthday=?,nationality=?,profile_url=?,biography=?,update_time=NOW() WHERE id=? AND deleted=0",args.toArray()),"人物不存在");return id;}
    @Override public void deletePerson(String type,Long id){String table=personTable(type);ensureChanged(jdbcTemplate.update("UPDATE "+table+" SET deleted=1,update_time=NOW() WHERE id=? AND deleted=0",id),"人物不存在");}

    private void syncMovieRelations(Long id,AdminMovieRequest r){sync("movie_category","category_id",id,r.getCategoryIds());sync("movie_tag","tag_id",id,r.getTagIds());sync("movie_actor","actor_id",id,r.getActorIds());sync("movie_director","director_id",id,r.getDirectorIds());}
    private void sync(String table,String column,Long movieId,List<Long> ids){jdbcTemplate.update("DELETE FROM "+table+" WHERE movie_id=?",movieId);if(ids!=null)ids.stream().distinct().forEach(id->jdbcTemplate.update("INSERT INTO "+table+"(movie_id,"+column+") VALUES(?,?)",movieId,id));}
    private List<Long> relationIds(String table,String column,Long movieId){return jdbcTemplate.query("SELECT "+column+" FROM "+table+" WHERE movie_id=?",(rs,n)->rs.getLong(1),movieId);}
    private String dictionaryTable(String type){return switch(type){case "categories"->"category";case "tags"->"tag";default->throw new ResponseStatusException(BAD_REQUEST,"不支持的数据类型");};}
    private String personTable(String type){return switch(type){case "actors"->"actor";case "directors"->"director";default->throw new ResponseStatusException(BAD_REQUEST,"不支持的人物类型");};}
    private Paging paging(long number,long size){return new Paging(Math.max(number,1),Math.max(1,Math.min(size,100)));}
    private Long count(String sql,List<Object>args){Long result=jdbcTemplate.queryForObject(sql,Long.class,args.toArray());return result==null?0:result;}
    private <T> PageResult<T> page(Long total,Paging p,List<T> records){return new PageResult<>(total,p.number(),p.size(),records);}
    private void ensureChanged(int count,String message){if(count==0)throw new ResponseStatusException(NOT_FOUND,message);}
    private Integer normalizeStatus(Integer status){return status!=null&&status==0?0:1;}
    private Object value(Object value,Object fallback){return value==null?fallback:value;}
    private Date date(java.time.LocalDate value){return value==null?null:Date.valueOf(value);}
    private String text(Map<String,Object> map,String key){Object value=map.get(key);return value==null?null:String.valueOf(value).trim();}
    private Integer intValue(Object value,Integer fallback){if(value==null)return fallback;return value instanceof Number n?n.intValue():Integer.valueOf(String.valueOf(value));}
    private record Paging(long number,long size){long offset(){return (number-1)*size;}}
}
