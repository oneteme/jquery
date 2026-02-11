package org.usf.jquery.web.proxy;

import static org.usf.jquery.core.ComparisonExpression.ge;
import static org.usf.jquery.core.ComparisonExpression.lt;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.web.proxy.Parameterized.ArgsParser;

/**
 * 
 * @author u$f
 *
 */
public interface SchemaResource {
	
	@Bind("table_1")
	ViewResource view1();

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
