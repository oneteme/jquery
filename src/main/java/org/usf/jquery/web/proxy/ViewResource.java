package org.usf.jquery.web.proxy;

import static org.usf.jquery.core.JDBCType.TIMESTAMP;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewJoin;
import org.usf.jquery.web.proxy.Bind.BindType;

import lombok.NonNull;

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
    
    @Resource(value="toto", description="")
    default @NonNull NamedColumn elapsedtime(){
    	return start().epoch().minus(end().epoch()).as("elps");
    }
    
    @Bind(value="start.epoch.minus(end.epoch)", type = BindType.REQ)
    NamedColumn elapsedtime2();
    
    
    @Resource(value="", description="")
    default DBFilter active(){
    	return null;
    }

    @Resource(value="", description="")
    default ViewJoin[] ratt() {
    	return null;
    }

    //@Resource(value="", description="")
    default Partition latestEdit(){
    	return null;
    }
}
