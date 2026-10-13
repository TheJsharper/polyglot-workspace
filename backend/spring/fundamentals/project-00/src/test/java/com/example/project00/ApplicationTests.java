package com.example.project00;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void databaseConnectionIsAvailable() throws SQLException {
		try (var connection = dataSource.getConnection();
				var statement = connection.createStatement();
				var resultSet = statement.executeQuery("SELECT 1")) {
			assertTrue(resultSet.next());
			assertEquals(1, resultSet.getInt(1));
		}
	}

}
