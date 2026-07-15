package com.enterprise.smarttask.repository;

import com.enterprise.smarttask.model.Task;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public TaskRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
    }

    private final RowMapper<Task> rowMapper = (rs, rowNum) -> Task.builder()
            .id(rs.getLong("id"))
            .title(rs.getString("title"))
            .description(rs.getString("description"))
            .priority(Task.Priority.valueOf(rs.getString("priority")))
            .status(Task.Status.valueOf(rs.getString("status")))
            .assignee(rs.getString("assignee"))
            .aiSuggestion(rs.getString("ai_suggestion"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    public List<Task> findAll() {
        return jdbc.query("SELECT * FROM tasks ORDER BY created_at DESC", rowMapper);
    }

    public Optional<Task> findById(Long id) {
        List<Task> results = jdbc.query(
                "SELECT * FROM tasks WHERE id = ?", rowMapper, id);
        return results.stream().findFirst();
    }

    public List<Task> findByStatus(Task.Status status) {
        return jdbc.query(
                "SELECT * FROM tasks WHERE status = ? ORDER BY priority DESC",
                rowMapper, status.name());
    }

    public List<Task> findByPriority(Task.Priority priority) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("priority", priority.name());
        return namedJdbc.query(
                "SELECT * FROM tasks WHERE priority = :priority ORDER BY created_at DESC",
                params, rowMapper);
    }

    public Task save(Task task) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO tasks (title, description, priority, status, assignee, ai_suggestion, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority() != null ? task.getPriority().name() : Task.Priority.MEDIUM.name());
            ps.setString(4, Task.Status.OPEN.name());
            ps.setString(5, task.getAssignee());
            ps.setString(6, task.getAiSuggestion());
            ps.setTimestamp(7, Timestamp.valueOf(now));
            ps.setTimestamp(8, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        task.setId(keyHolder.getKey().longValue());
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setStatus(Task.Status.OPEN);
        return task;
    }

    public int update(Task task) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", task.getId())
                .addValue("title", task.getTitle())
                .addValue("description", task.getDescription())
                .addValue("priority", task.getPriority().name())
                .addValue("status", task.getStatus().name())
                .addValue("assignee", task.getAssignee())
                .addValue("aiSuggestion", task.getAiSuggestion())
                .addValue("updatedAt", Timestamp.valueOf(LocalDateTime.now()));

        return namedJdbc.update(
                "UPDATE tasks SET title=:title, description=:description, priority=:priority, " +
                "status=:status, assignee=:assignee, ai_suggestion=:aiSuggestion, updated_at=:updatedAt " +
                "WHERE id=:id",
                params);
    }

    public int deleteById(Long id) {
        return jdbc.update("DELETE FROM tasks WHERE id = ?", id);
    }

    /** Calls a database stored procedure to retrieve task summary counts by status. */
    public List<Map<String, Object>> getTaskSummary() {
        return jdbc.queryForList("CALL sp_task_summary()");
    }
}
