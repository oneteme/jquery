package org.usf.jquery.web.proxy;

import static org.usf.jquery.core.Comparator.basicComparator;
import static org.usf.jquery.core.ComparisonExpression.ge;
import static org.usf.jquery.core.ComparisonExpression.lt;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Operator.function;
import static org.usf.jquery.core.Parameter.required;

import org.usf.jquery.core.ArgTypeRef;
import org.usf.jquery.core.Chainable;
import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;

/**
 * 
 * @author u$f
 *
 */
@Bind("sample") //bind this class to "sample" database
interface SchemaSample extends Resource {
	
	@Expose(identity="v1") //export view_1 as resource name v1, if id is empty, method name will be used as resource name
	@Bind("view_1") //bind this method to "view_1" view of "sample" database
	ViewSample view1();
	
	@Expose(false) //view2 is not exposed, but it is still binded to "v2" view of "sample" database, so it can be used as internal resource
	@Bind("v2")
	ViewSample view2(); 
	
	@Expose(identity="myFn", description="") 
	default TypedOperator myFunction() {
		return new TypedOperator(VARCHAR, function("myFn"), required(VARCHAR));
	}
	
	@Expose(identity="myCmp", description="") 
	default TypedComparator myComparator() {
		return new TypedComparator(basicComparator("<="), required(), required(ArgTypeRef.firstArgJdbcType()));
	}

	@Expose(identity="vitesse") 
    default ComparisonExpression elapsedTimeExpressions(String... values) { //patch ?
		return Chainable.or(values, v-> switch (v) {
            case "fastest" -> lt(1);
            case "fast" -> ge(1).and(lt(3));
            case "medium" -> ge(3).and(lt(5));
            case "slow" -> ge(5).and(lt(10));
            case "slowest" -> ge(10);
            default -> null;
        });
    }
}
