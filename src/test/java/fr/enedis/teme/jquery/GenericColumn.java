package fr.enedis.teme.jquery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class GenericColumn implements TableColumn {

	static final TableColumn c1 = new GenericColumn("someCode");
	static final TableColumn c2 = new GenericColumn("someName");
	static final TableColumn c3 = new GenericColumn("someDesc");
	static final TableColumn c4 = new GenericColumn("someRevi");
	
	private final String tagName;

	@Override
	public String name() {
		return null;
	}

	@Override
	public String tagname() {
		return tagName;
	}

}
