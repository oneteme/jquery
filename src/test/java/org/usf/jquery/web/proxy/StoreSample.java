package org.usf.jquery.web.proxy;

import static org.usf.jquery.core.Comparators.basicComparator;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Operators.function;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Predicate.ge;
import static org.usf.jquery.core.Predicate.lt;

import org.usf.jquery.core.Chainable;
import org.usf.jquery.core.ComparatorDefinition;
import org.usf.jquery.core.OperatorDefinition;
import org.usf.jquery.core.Predicate;
import org.usf.jquery.core.TypeResolver;

/**
 * 
 * @author u$f
 *
 */
@Bind("sample") //bind this class to "sample" database
interface StoreSample extends StoreResource {
	
	@Expose(identity="v1") //export view_1 as resource name v1, if id is empty, method name will be used as resource name
	@Bind("view_1") //bind this method to "view_1" view of "sample" database
	DatasetSample view1();
	
	@Expose(false) //view2 is not exposed, but it is still binded to "v2" view of "sample" database, so it can be used as internal resource
	@Bind("v2")
	DatasetSample view2(); 
	
	@Expose(identity="myFn", description="") 
	default OperatorDefinition myFunction() {
		return function(VARCHAR, "myFn", required(VARCHAR));
	}
	
	@Expose(identity="myCmp", description="") 
	default ComparatorDefinition myComparator() {
		return basicComparator("<=", "");
	}

	@Expose(identity="vitesse") 
    default Predicate elapsedTimeExpressions(String... values) { //patch ?
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
