package org.usf.jquery;

import static java.util.stream.Collectors.toList;
import static org.usf.jquery.SqlStringBuilder.COMA_SEPARATOR;
import static org.usf.jquery.Utils.isEmpty;

import java.util.stream.Stream;

import lombok.NonNull;

public interface DBFunction extends DBCallable {
		
	default boolean isAggregate() {
		return false;
	}
	
	default FunctionColumn of(@NonNull Object column, Object... args) {
		return new FunctionColumn(this, column, args);
	}
	
	static DBFunction definedFunction(final String name) {
		
		return (b, op, args)-> {
			var sb = new SqlStringBuilder(args.length * 10)
					.append(name)
					.append("(")
					.append(b.appendParameter(op))
					.append(",");
			if(!isEmpty(args)) {
				sb.appendEach(Stream.of(args).map(b::appendParameter).collect(toList()), COMA_SEPARATOR);
			}
			return sb.append(")")
					.toString();
		};
	}
	
}
