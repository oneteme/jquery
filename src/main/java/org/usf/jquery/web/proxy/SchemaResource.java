package org.usf.jquery.web.proxy;

import static java.util.Objects.nonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * 
 * @author u$f
 *
 */
public interface SchemaResource {
	
	@Bind("table_1")
	ViewResource view1();
}
