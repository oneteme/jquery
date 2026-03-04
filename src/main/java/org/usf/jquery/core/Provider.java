package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.Operators.function;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@Slf4j
public enum Provider {

	POSTGRESQL, MYSQL, ORACLE, SQLSERVER, DEFAULT, 
	
	TERADATA {
		@Override
		public Operator replace(Operator op) {
			return switch (op.id()){
			case "&"-> function("BITAND");
			case "|"-> function("BITOR");
			case "^"-> function("BITXOR");
			case "~"-> function("BITNOT");
			case "<<"-> function("SHIFTLEFT");
			case ">>"-> function("SHIFTRIGHT");
			case "WEEK"-> function("TD_WEEK_OF_YEAR");
			case "DOW"-> function("TD_DAY_OF_WEEK");
			case "DOY"-> function("TD_DAY_OF_YEAR");
			case "REPLACE"-> function("OREPLACE");
			default-> super.replace(op);
			};
		}
	},
	
	H2 {
		@Override
		public Operator replace(Operator op) {
			return switch (op.id()){
			case "&"-> function("BITAND");
			case "|"-> function("BITOR");
			case "^"-> function("BITXOR");
			case "~"-> function("BITNOT");
			case "<<"-> function("LSHIFT");
			case ">>"-> function("RSHIFT");
			default-> super.replace(op);
			};
		}
	};
	
	@Deprecated
	public Operator replace(Operator op) {
		return op;
	}
	
	public static Provider parseName(String name) {
		if(isNull(name)) {
			return DEFAULT;
		}
		var v = name.toUpperCase();
		return stream(values())
				.filter(d-> d.name().contains(v))
				.findAny()
				.orElse(DEFAULT);
	}
}
