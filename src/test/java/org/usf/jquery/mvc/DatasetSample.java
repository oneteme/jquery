package org.usf.jquery.mvc;

import static org.usf.jquery.core.Column.cdate;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;

import java.time.LocalDate;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.JoinGroup;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.mvc.Bind.BindType;

interface DatasetSample extends DatasetCatalog {
    
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
	
	@Typed(INTEGER)
	@Bind("va_size")
    ViewColumn size();
    
    @Expose(identity="toto", description="")
    default Column elapsedtime(){
    	return start().epoch().minus(end().epoch()).as("elps");
    }
    
    @Bind(value="start.epoch.minus(end.epoch)", type = BindType.REQ)
    Column elapsedtime2();
    
    @Expose(description="")
    default Criteria active(LocalDate date, int x) {
    	return size().lt(x).and(cdate().eq(date));
    }

    @Expose(identity="rattachement", description="")
    default JoinGroup ratt(LocalDate o2) {
    	return null;
    }

    //@Resource(value="", description="")
    default Partition latestEdit(){
    	return null;
    }     
}
