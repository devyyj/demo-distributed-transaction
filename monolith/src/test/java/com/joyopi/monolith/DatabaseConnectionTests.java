package com.joyopi.monolith;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 데이터베이스 연결 설정 및 통신을 검증하는 통합 테스트입니다.
 */
@SpringBootTest
class DatabaseConnectionTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("데이터베이스 연결이 정상적으로 수행되어야 한다.")
    void testConnection() throws Exception {
        // Given & When
        try (Connection connection = dataSource.getConnection()) {
            // Then
            assertThat(connection).isNotNull();
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("MariaDB");
        }
    }

    @Test
    @DisplayName("JdbcTemplate을 사용하여 간단한 쿼리가 실행되어야 한다.")
    void testJdbcTemplate() {
        // Given & When
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        // Then
        assertThat(result).isEqualTo(1);
    }
}
