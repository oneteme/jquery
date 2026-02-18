package org.usf.jquery.web.proxy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author u$f
 *
 */
@Documented
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface Bind {

	String value();
	
	BindType type() default BindType.REF;
	
	//scope !?
	
	enum BindType {
		REF, //bind to physical resource reference 
		REQ, //bind to request expression
		SQL; //bind to sql expression
	}
}
