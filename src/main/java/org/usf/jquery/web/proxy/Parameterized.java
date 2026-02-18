package org.usf.jquery.web.proxy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * 
 * @author u$f
 *
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Parameterized {
	
	//boolean acceptVariables() default true;

	Class<? extends ArgsParser> parser(); 
	
	@FunctionalInterface
	public interface ArgsParser {

		Object[] parse(Method m, Entry[] args, QueryContext ctx);
	}
}