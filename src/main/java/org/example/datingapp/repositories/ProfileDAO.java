package org.example.datingapp.repositories;

import org.example.datingapp.models.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProfileDAO{
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ProfileDAO.class);

    @Autowired
    public ProfileDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Profile findById(Long id) {
        if (id == null) {
            return null;
        }

        try {
            logger.info("Finding profile with ID: {}", id);
            String sql = "SELECT * FROM Profile WHERE id = ?";
            Profile profile = jdbcTemplate.queryForObject(sql, new Object[]{id}, new ProfileRowMapper());
            logger.info("Found profile: {}", profile);
            return profile;
        } catch (EmptyResultDataAccessException e) {
            logger.warn("No profile found with ID: {}", id);
            return null;
        } catch (Exception e) {
            logger.error("Error finding profile with ID: {}", id, e);
            throw e;
        }
    }

    public Profile findByEmail(String email) {
        logger.info("Finding profile with email: {}", email);
        String sql = "SELECT * FROM Profile WHERE email = ?";
        try {
            Profile test = jdbcTemplate.queryForObject(sql, new Object[]{email}, new ProfileRowMapper());
            logger.info("Found profile: {}", test);
            return test;
        }
        catch (EmptyResultDataAccessException e) {
            logger.warn("No profile found with email: {}", email);
            return null;
        }
        catch (Exception e) {
            logger.error("Error finding profile with email: {}", email, e);
            throw e;
        }
    }

    public long save(Profile profile) {
        String sql = "INSERT INTO Profile (name, email, password, open_info, closed_info) VALUES (?, ?, ?, ?, ?) RETURNING id";
        return jdbcTemplate.queryForObject(sql, new Object[]{
                profile.getName(),
                profile.getEmail(),
                profile.getPassword(),
                profile.getOpenInfo(),
                profile.getClosedInfo()
        }, Long.class);

    }

    public Profile update(Profile profile) {
        try {
            String updateSql = "UPDATE Profile SET name = ?, email = ?, password = ?, open_info = ?, closed_info = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, profile.getName(), profile.getEmail(), profile.getPassword(),
                    profile.getOpenInfo(), profile.getClosedInfo(), profile.getId());

            String fetchSql = "SELECT * FROM Profile WHERE id = ?";
            return jdbcTemplate.queryForObject(fetchSql, new Object[]{profile.getId()}, new ProfileRowMapper());
        } catch (DataAccessException e) {
            logger.error("Error updating profile with ID: {}", profile.getId(), e);
            throw new RuntimeException("Failed to update profile. Please check the logs for details.", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM Profile WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public List<Profile> findAll() {
        String sql = "SELECT * FROM Profile";
        return jdbcTemplate.query(sql, new ProfileRowMapper());
    }

    public List<Profile> findByOpenInfoContainingIgnoreCase(String keyword){
        String sql = "SELECT * FROM profile WHERE LOWER(open_info) LIKE LOWER('%keyword%')";
        return jdbcTemplate.query(sql, new Object[]{keyword}, new ProfileRowMapper());
    }

    private static class ProfileRowMapper implements RowMapper<Profile> {
        @Override
        public Profile mapRow(ResultSet rs, int rowNum) throws SQLException {
            Profile profile = new Profile();
            profile.setId(rs.getLong("id"));
            profile.setName(rs.getString("name"));
            profile.setEmail(rs.getString("email"));
            profile.setPassword(rs.getString("password"));
            profile.setOpenInfo(rs.getString("open_info"));
            profile.setClosedInfo(rs.getString("closed_info"));
            return profile;
        }
    }
}
