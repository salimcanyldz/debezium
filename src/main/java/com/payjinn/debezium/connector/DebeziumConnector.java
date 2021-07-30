package com.payjinn.debezium.connector;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DebeziumConnector {

	@Value("${mysql.datasource.database}")
	private String database;
	
	@Value("${mysql.datasource.host}")
	private String host;
	
	@Value("${mysql.datasource.port}")
	private String port;
	
	@Value("${mysql.datasource.username}")
	private String user;
	
	@Value("${mysql.datasource.password}")
	private String password;
	
	
	@Bean
	public io.debezium.config.Configuration connector() throws IOException{
		File offsetStorageFile = File.createTempFile("offset_", ".dat");
		File dbHistoryFile = File.createTempFile("dbhistory_", ".dat");
		
		return io.debezium.config.Configuration.create()
				.with("name", "debezium-connector")
				.with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
				.with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
				.with("offset.storage.file.filename", offsetStorageFile.getAbsolutePath())
				.with("offset.flush.interval.ms", "60000")
				.with("database.hostname", host)
				.with("database.port", port)
				.with("database.user", user)
				.with("database.password", password)
				.with("database.dbname", database)
				.with("database.include.list", database)
				.with("include.schema.changes", "false")
				.with("database.server.id", "85744")
				.with("database.server.name", "mysql-app-connector")
				.with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
				.with("database.history.file.filename", dbHistoryFile.getAbsolutePath())
				.build();
	}
}
