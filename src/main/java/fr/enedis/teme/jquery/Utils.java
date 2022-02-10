package fr.enedis.teme.jquery;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {
	
	public static <T> boolean isEmpty(T[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isBlank(String str) {
		return str == null || str.isBlank();
	}
	
}
