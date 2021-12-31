package fr.enedis.teme.jquery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class GenericColumn implements TableColumn {

	static final DBColumn c1 = new GenericColumn("someCode");
	static final DBColumn c2 = new GenericColumn("someName");
	static final DBColumn c3 = new GenericColumn("someDesc");
	static final DBColumn c4 = new GenericColumn("someRevi");
	
	private final String tagName;

	@Override
	public String name() {
		return null;
	}

	@Override
	public String getTag() {
		return tagName;
	}

}
