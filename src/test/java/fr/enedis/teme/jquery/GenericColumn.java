package fr.enedis.teme.jquery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GenericColumn implements DBColumn {
	
	private final String mappedName;

}
