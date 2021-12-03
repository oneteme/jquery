package fr.enedis.teme.jquery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StdFunction implements DBFunction {

	ABS, TRIM, LENGTH, UPPER, LOWER;
	
	@Override
	public String getFunctionName() {
		return name();
	}
	
}