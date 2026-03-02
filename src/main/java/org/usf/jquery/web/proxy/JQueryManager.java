package org.usf.jquery.web.proxy;

import static org.usf.jquery.web.proxy.SchemaProxy.createSchema;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.usf.jquery.web.NoSuchResourceException;

import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class JQueryManager {
	
	private static final Map<Class<?>, Store> schemas = new LinkedHashMap<>();
	
	public static void register(Class<? extends Store> clazz, DataSource ds) {
		schemas.put(clazz, createSchema(clazz, ds));
	}
	
	public static <T extends Store> T getSchema(Class<T> clazz){
		return clazz.cast(schemas.get(clazz));
	}

	public static <T extends Store> T getDefaultSchema(){
		if(schemas.size() == 1) {
			return (T) schemas.values().iterator().next();
		}
		throw new NoSuchResourceException("multiple schemas registered, specify schema class");
	}
}
