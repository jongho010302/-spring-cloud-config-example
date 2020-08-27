package net.yogurt.config.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {

	@Value("${db.jdbc-url}")
	private String jdbcUrl;

	@Value("${db.username}")
	private String username;

	@Value("${db.password}")
	private String password;

	@Bean
	@ConfigurationProperties(prefix="spring.datasource.hikari")
	public HikariConfig hikariConfig() {
		return new HikariConfig();
	}

	@Bean
	public DataSource dataSource() {
		HikariConfig dataSourceConfig = hikariConfig();
		dataSourceConfig.setJdbcUrl(jdbcUrl);
		dataSourceConfig.setUsername(username);
		dataSourceConfig.setPassword(password);
		return new HikariDataSource(dataSourceConfig);
	}

}