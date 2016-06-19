package jshinter.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.Token;

public final class GlobalScope extends Scope {
	
	private final List<String> PREDEFINED = new ArrayList<>(Arrays.asList(
			"Array", "Boolean", "Date", "decodeURI", "decodeURIComponent", "encodeURI", "encodeURIComponent", 
			"Error", "eval", "EvalError", "Function", "hasOwnProperty", "isFinite", "isNaN", "Math", "Number",
			"Object", "parseInt", "parseFloat", "RangeError", "ReferenceError", "RegExp", "String", "SyntaxError",
			"TypeError", "URIError",
			
			"JSON"
			));

	public GlobalScope() {
		super(null, ScopeType.GLOBAL);
	}
	
	public List<Token> getUsedAndNotDefined() {
		List<Token> usedAndNotDefined = super.getUsedAndNotDefined();
		
		Iterator<Token> it = usedAndNotDefined.iterator();
		
		while (it.hasNext()) {
			Token token = it.next();

			if (PREDEFINED.contains(token.getText())) {
				it.remove();
			}
		}
		
		return usedAndNotDefined;
	}
}
