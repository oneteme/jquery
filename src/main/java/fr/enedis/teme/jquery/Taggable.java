package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;
import static java.lang.Character.toUpperCase;
import static java.lang.String.join;

import lombok.NonNull;

public interface Taggable {

	String getTag();
	
	static String prefix(String prefix, @NonNull Taggable column) {
		var v = column.getTag();
		return snakeToCamelCase(isBlank(prefix) ? v : prefix + "_" + v);
	}

	static String snakeToCamelCase(String columnName) {
		var arr = columnName.toLowerCase().split("_");
		arr[0] = arr[0].toLowerCase();
		for(var i=1; i<arr.length; i++) {
			arr[i] = toUpperCase(arr[i].charAt(0)) + arr[i].substring(1).toLowerCase();
		}
		return join("", arr);
	}
}
