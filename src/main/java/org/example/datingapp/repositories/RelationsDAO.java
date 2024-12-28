package org.example.datingapp.repositories;

import org.example.datingapp.models.Profile;
import org.example.datingapp.models.Relation;
import org.example.datingapp.models.enums.RelationState;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RelationsDAO {
    private final JdbcTemplate jdbcTemplate;
    private final Logger logger;

    @Autowired
    public RelationsDAO(JdbcTemplate jdbcTemplate, Logger logger) {
        this.jdbcTemplate = jdbcTemplate;
        this.logger = logger;
    }

    public Relation findById(Long id) {
        String sql = "SELECT * FROM Relation WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new RelationRowMapper());
    }

    public List<Relation> findByInitiator(Long initiatorId) {
        String sql = "SELECT * FROM Relation WHERE initiator_id = ?";
        return jdbcTemplate.query(sql, new Object[]{initiatorId}, new RelationRowMapper());
    }

    public List<Relation> findByAim(Long aimId) {
        String sql = "SELECT * FROM Relation WHERE aim_id = ?";
        return jdbcTemplate.query(sql, new Object[]{aimId}, new RelationRowMapper());
    }
    
    public Relation findRelationByInitiatorAndAim(Long initiatorId, Long aimId) {
        try {
            String sql = "SELECT * FROM Relation WHERE initiator_id = ? AND aim_id = ?";
            logger.info("1Finding relation by initiator ID: {} and aim ID: {}", initiatorId, aimId);
            Relation r = jdbcTemplate.queryForObject(sql, new Object[]{initiatorId, aimId}, new RelationRowMapper());
            logger.info("Found relation: {}", r.getRelationState());
            return r;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to find relation. Please check the logs for details.", e);
        }

    }

    public boolean existsByInitiatorAndAim(Long initiatorId, Long aimId) {
        try {
            String sql = "SELECT EXISTS (SELECT 1 FROM Relation r WHERE r.initiator_id = ? AND r.aim_id = ?)";
            boolean ee = jdbcTemplate.queryForObject(sql, new Object[]{initiatorId, aimId}, Boolean.class);
            logger.info("Relation exists: {}", ee);
            return ee;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to check relation existence. Please check the logs for details.", e);
        }
    }
    public long save(Relation relation) {
        try {
            String sql = "INSERT INTO Relation (initiator_id, aim_id, relation_state) VALUES (?, ?, ?) RETURNING id";
            return jdbcTemplate.queryForObject(sql, new Object[]{relation.getInitiator().getId(), relation.getAim().getId(),
                    relation.getRelationState().ordinal()},Long.class);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to save relation. Please check the logs for details.", e);
        }

    }

    public void update(Relation relation) {
        String sql = "UPDATE Relation SET initiator_id = ?, aim_id = ?, relation_state = ? WHERE id = ?";
        jdbcTemplate.update(sql, relation.getInitiator().getId(), relation.getAim().getId(),
                relation.getRelationState().ordinal(), relation.getId());
    }

    public void delete(Relation relation) {
        String sql = "DELETE FROM Relation WHERE id = ?";
        jdbcTemplate.update(sql, relation.getId());
    }

    public List<Relation> findAllByAimId(Long aimId) {
        String sql = "SELECT * FROM Relation WHERE aim_id = ?";
        return jdbcTemplate.query(sql, new Object[]{aimId}, new RelationRowMapper());}

    public List<Relation> findAllByInitiatorId(Long initiatorId) {
        String sql = "SELECT * FROM Relation WHERE initiator_id = ?";
        return jdbcTemplate.query(sql, new Object[]{initiatorId}, new RelationRowMapper());
    }

    public List<Relation> findAllByRejection(Long initiatorId) {
        String sql = "SELECT * FROM Relation WHERE relation_state = 2 AND initiator_id = ?";
        return jdbcTemplate.query(sql, new Object[]{initiatorId}, new RelationRowMapper());
    }
    private static class RelationRowMapper implements RowMapper<Relation> {
        @Override
        public Relation mapRow(ResultSet rs, int rowNum) throws SQLException {
            Relation relation = new Relation();
            relation.setId(rs.getLong("id"));
            Profile initiator = new Profile();
            initiator.setId(rs.getLong("initiator_id"));
            relation.setInitiator(initiator);

            Profile aim = new Profile();
            aim.setId(rs.getLong("aim_id"));
            relation.setAim(aim);

            relation.setRelationState(RelationState.values()[rs.getInt("relation_state")]);
            return relation;
        }
    }
}
