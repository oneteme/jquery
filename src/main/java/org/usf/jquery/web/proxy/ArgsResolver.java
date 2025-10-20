package org.usf.jquery.web.proxy;

import java.lang.reflect.Method;

public interface ArgsResolver {

	Object[] parseArgs(Bind bind, Method method, String[] args) throws Exception;
	
}
