package fr.enedis.teme.jquery;

@FunctionalInterface
public interface DBCallable {

	String sql(QueryParameterBuilder builder, Object operand, Object... args);
	
	static String sql(DBCallable call, QueryParameterBuilder builder, Object operand, Object args) {

		if(args == null) {
			return call.sql(builder, operand); //avoid [null]
		}
		return args instanceof Object[] 
				? call.sql(builder, operand, (Object[]) args)
				: call.sql(builder, operand, args);
	}

}
