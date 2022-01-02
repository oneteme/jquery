package fr.enedis.teme.jquery.reflect;

import static java.lang.Integer.parseInt;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode(of = {"dbType", "length"})
@RequiredArgsConstructor 
class ColumnMetadata {
	
	private final int dbType;
	private final int length;
	
	Object parseValue(String v){
		
		switch(dbType) {
		case 12: return v;
		case  4: return parseInt(v);
		default: throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public String toString() {
		return "{dbType:" + dbType + ", length:" + length +"}";
	}
	
}