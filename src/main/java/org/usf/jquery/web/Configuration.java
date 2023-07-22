package org.usf.jquery.web;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public final class Configuration {

	private final String schema;
	private final DataSource dataSource;
	
}
