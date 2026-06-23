package org.usf.jquery.mvc;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
	
}
