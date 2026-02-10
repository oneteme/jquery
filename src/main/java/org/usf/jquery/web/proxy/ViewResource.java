package org.usf.jquery.web.proxy;

import static org.usf.jquery.core.ComparisonExpression.ge;
import static org.usf.jquery.core.ComparisonExpression.lt;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;

import java.time.Instant;
import java.time.LocalDate;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewColumn;
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

    @Entry(value="rattachement", description="", tagname = "rattach")
    @Parameterized(parser = ArgsParser.class)
    default JoinsClause ratt(LocalDate o1, Instant o2) {
    	return null;
    }

    //@Resource(value="", description="")
    default Partition latestEdit(){
    	return null;
    }

    @Parameterized(parser = ArgsParser.class)
    default ComparisonExpression elapsedTimeExpressions(String name) {
        return switch (name) {
            case "fastest" -> lt(1);
            case "fast" -> ge(1).and(lt(3));
            case "medium" -> ge(3).and(lt(5));
            case "slow" -> ge(5).and(lt(10));
            case "slowest" -> ge(10);
            default -> null;
        };
    }
}
