package org.gl.eir.repository.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class GenericRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenericRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, String>> getListOfUrls() {
        String sql = "select tag, value from sys_param where tag like '%FILE_URL%';";
        return jdbcTemplate.query(sql, new RowMapper<Map<String, String>>() {
            @Override
            public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<String, String> result = new HashMap<>();
                result.put("config_key", rs.getString("tag"));
                result.put("config_value", rs.getString("value"));
                return result;
            }
        });
    }
}
