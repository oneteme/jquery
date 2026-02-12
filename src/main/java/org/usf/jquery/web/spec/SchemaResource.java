package org.usf.jquery.web.spec;

import static org.usf.jquery.core.Comparator.basicComparator;
import static org.usf.jquery.core.ComparisonExpression.ge;
import static org.usf.jquery.core.ComparisonExpression.lt;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Operator.function;
import static org.usf.jquery.core.Parameter.required;

import org.usf.jquery.core.ArgTypeRef;
import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.web.proxy.Bind;
import org.usf.jquery.web.proxy.Expose;
import org.usf.jquery.web.proxy.Parameterized;
import org.usf.jquery.web.proxy.Parameterized.ArgsParser;

/**
 * 
 * @author u$f
 *
 */
@Bind("sample") //bind this class to "sample" database
public interface SchemaResource {
	
	@Expose(identity="v1") //export view_1 as resource name v1, if id is empty, method name will be used as resource name
	@Bind("view_1") //bind this method to "view_1" view of "sample" database
	ViewResource view1();
	
	@Expose(false) //view2 is not exposed, but it is still binded to "v2" view of "sample" database, so it can be used as internal resource
	@Bind("v2")
	ViewResource view2(); 
	
	@Expose(identity="myFn", description="") 
	default TypedOperator myFunction() {
		return new TypedOperator(VARCHAR, function("myFn"), required(VARCHAR));
	}
	
	@Expose(identity="myCmp", description="") 
	default TypedComparator myComparator() {
		return new TypedComparator(basicComparator("<="), required(), required(ArgTypeRef.firstArgJdbcType()));
	}

	@Expose(identity="vitesse") 
    @Parameterized(parser = ArgsParser.class) 
    default ComparisonExpression elapsedTimeExpressions(String name) { //can bind ?
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
