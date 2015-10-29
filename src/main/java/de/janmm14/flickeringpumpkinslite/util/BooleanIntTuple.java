package de.janmm14.flickeringpumpkinslite.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class BooleanIntTuple {
	@Getter(AccessLevel.NONE)
	private boolean bool;
	private int integer;

	public boolean getBool() {
		return bool;
	}
}
