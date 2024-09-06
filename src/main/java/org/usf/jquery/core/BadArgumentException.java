package org.usf.jquery.core;

import static java.lang.String.format;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Utils.joinAndDelemitArray;
import static org.usf.jquery.core.Utils.joinArray;

import org.usf.jquery.core.JavaType.Typed;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class BadArgumentException extends JQueryException {

	public BadArgumentException(String message) {
		super(message);
	}
	
	public BadArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

	public static BadArgumentException badArgumentCountException(int count, int expect) {
		return new BadArgumentException(format("bad argument count: [%d], expected: %d", count, expect));
	}

	public static BadArgumentException badArgumentTypeException(Object obj, JavaType[] types) {
		var type = obj instanceof Typed t ? t.getType() : JDBCType.typeOf(obj);
		return new BadArgumentException(format("bad argument type: %s[%s], expected: %s", obj, type, joinArray("|", types)));
	}

	public static BadArgumentException badArgumentsException(String type, String id, Object[] args, Exception e) {
		return new BadArgumentException(format("bad %s arguments: ", type) + badArgumentsFormat(id, args), e);
	}
	
	public static String badArgumentsFormat(String id, Object... args) {
		return id + joinAndDelemitArray(SCOMA, "([", "])", args);
	}
}
