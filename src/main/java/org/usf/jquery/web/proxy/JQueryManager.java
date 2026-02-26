package org.usf.jquery.web.proxy;

import static org.usf.jquery.web.proxy.SchemaProxy.createSchema;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class JQueryManager {
	
	private static final Map<Class<?>, Resource> schemas = new LinkedHashMap<>();
	
	public static void register(Class<? extends Resource> clazz, DataSource ds) {
		schemas.put(clazz, createSchema(clazz, ds));
	}
	
	public static <T extends Resource> T getSchema(Class<T> clazz){
		return clazz.cast(schemas.get(clazz));
	}
}
