package org.usf.jquery.web.proxy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.usf.jquery.core.JDBCType;

/**
 * 
 * @author u$f
 *
 */
@Documented
@Target({METHOD, PARAMETER})
@Retention(RUNTIME)
public @interface Typed {
	
	JDBCType value();
	
	Class<? extends TypeResolver> resolver() default TypeResolver.class;
	
	interface TypeResolver {
		
		Object extract(int index, ResultSet rs) throws SQLException;

		Object parse(String value);
	}
}
