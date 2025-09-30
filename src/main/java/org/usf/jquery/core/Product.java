package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static org.usf.jquery.core.Operator.function;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@Slf4j
public enum Product {

	POSTGRESQL, MYSQL, ORACLE, SQLSERVER, 
	
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
	
	public Operator replace(Operator op) {
		return op;
	}
	
	public static Product fromMetaData(DatabaseMetaData meta) throws SQLException {
		var name = meta.getDatabaseProductName().toUpperCase();
		return stream(values())
				.filter(d-> name.contains(d.name()))
				.findAny()
				.orElseThrow(()-> new UnsupportedOperationException(name));
	}
}
