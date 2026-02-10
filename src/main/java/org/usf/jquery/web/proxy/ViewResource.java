package org.usf.jquery.web.proxy;

import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;

import java.time.Instant;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewJoin;
import org.usf.jquery.web.proxy.Bind.BindType;
import org.usf.jquery.web.proxy.Parameterized.ArgsParser;

public interface ViewResource {
    
    DBView toView();

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
    
    @Entry(value="toto", description="")
    default NamedColumn elapsedtime(){
    	return start().epoch().minus(end().epoch()).as("elps");
    }
    
    @Bind(value="start.epoch.minus(end.epoch)", type = BindType.REQ)
    NamedColumn elapsedtime2();
    
    @Entry(value="", description="")
    default DBFilter active(){
    	return null;
    }

    @Entry(value="", description="")
    @Parameterized(parser = ArgsParser.class)
    default ViewJoin[] ratt(Object o1, Instant o2) {
    	return null;
    }

    //@Resource(value="", description="")
    default Partition latestEdit(){
    	return null;
    }
    
    
}
