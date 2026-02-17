package org.usf.jquery.web.spec;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.web.proxy.Bind;
import org.usf.jquery.web.proxy.Bind.BindType;
import org.usf.jquery.web.proxy.Expose;
import org.usf.jquery.web.proxy.Parameterized;
import org.usf.jquery.web.proxy.Parameterized.ArgsParser;
import org.usf.jquery.web.proxy.Typed;

public interface ViewResource {
    
	@Bind("va_loc")
    ViewColumn location(); 

	@Bind("va_host") 
    ViewColumn host();

	@Typed(TIMESTAMP)
	@Bind("dh_start")
    ViewColumn start();

	@Typed(TIMESTAMP)
	@Bind("dh_end")
    ViewColumn end();
    
    @Expose(identity="toto", description="")
    default NamedColumn elapsedtime(){
    	return start().epoch().minus(end().epoch()).as("elps");
    }
    
    @Bind(value="start.epoch.minus(end.epoch)", type = BindType.REQ)
    NamedColumn elapsedtime2();
    
    @Expose(description="")
    default DBFilter active(LocalDate date) {
    	return null;
    }

    @Expose(identity="rattachement", description="")
    @Parameterized(parser = ArgsParser.class)
    default JoinsClause ratt(LocalDate o1, Instant o2) {
    	return null;
    }

    //@Resource(value="", description="")
    default Partition latestEdit(){
    	return null;
    }

	Method resourceMethod(String name, Class<?> type);
    
}
