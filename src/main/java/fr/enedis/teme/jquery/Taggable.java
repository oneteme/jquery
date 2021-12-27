package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;
import static java.lang.Character.toUpperCase;
import static java.lang.String.join;

import lombok.NonNull;

public interface Taggable<T> {

	String tag(T o);
	
	static String genericTag(String prefix, @NonNull DBColumn column, @NonNull DBTable table) {
		var v = column.tag(table);
		return camelCase(isBlank(prefix) ? v : prefix + "_" + v);
	}

	static String camelCase(@NonNull String columnName) {
		var arr = columnName.toLowerCase().split("_");
		for(var i=1; i<arr.length; i++) {
			arr[i] = toUpperCase(arr[i].charAt(0)) + arr[i].substring(1);
		}
		return join("", arr);
	}
}
