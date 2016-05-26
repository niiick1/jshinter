package jshinter.analyzer;

import jshinter.antlr.ECMAScriptBaseListener;
import jshinter.antlr.ECMAScriptParser;
import jshinter.antlr.ECMAScriptParser.ProgramContext;

public class JSHinterListener extends ECMAScriptBaseListener {

	private ECMAScriptParser parser;
	
	private final Boolean DEBUG;
	
	public JSHinterListener(ECMAScriptParser parser) {
		this.parser = parser;
		this.DEBUG = false;
	}
	
	public JSHinterListener(ECMAScriptParser parser, Boolean debugMode) {
		this.parser = parser;
		this.DEBUG = debugMode;
	}

	@Override
	public void enterProgram(ProgramContext ctx) {
//		super.enterProgram(ctx);
		System.out.println("PROGRAMA JAVASCRIPT");
	}
}
