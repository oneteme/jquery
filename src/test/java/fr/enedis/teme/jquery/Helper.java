package fr.enedis.teme.jquery;

import static org.junit.jupiter.api.Assertions.fail;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class Helper {

	static Object fieldValue(@NonNull String name, @NonNull Object o) {
		try {
			var field = o.getClass().getDeclaredField(name);
			if(field.canAccess(o)) {
				return field.get(o);
			}
			field.setAccessible(true);
			var v = field.get(o);
			field.setAccessible(false);
			return v;
		}
		catch(Exception e) {
			fail("cannot get ", e);
			return null; //unreachable
		}
	}

	static String[] array(String... arr) {
		return arr;
	}
	
	
}
