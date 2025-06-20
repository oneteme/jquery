package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
import static java.lang.String.format;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageUtils {
	
	public static String resourceAlreadyExistsMessage(String type, String name) {
		return format("an other %s with name='%s' is already exists", type, name);
	}

}
