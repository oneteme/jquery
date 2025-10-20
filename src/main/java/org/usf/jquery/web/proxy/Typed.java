package org.usf.jquery.web.proxy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.usf.jquery.core.JDBCType;

@Documented
@Retention(CLASS)
@Target(METHOD)
public @interface Typed {
	
	JDBCType value(); //interface, custom types UUID
}
