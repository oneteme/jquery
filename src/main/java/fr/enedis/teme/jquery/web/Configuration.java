package fr.enedis.teme.jquery.web;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class Configuration {

	private final String schema;
	private final DataSource dataSource;
	
}
