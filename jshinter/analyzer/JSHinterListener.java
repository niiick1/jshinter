package jshinter.analyzer;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import jshinter.antlr.ECMAScriptBaseListener;
import jshinter.antlr.ECMAScriptParser;
import jshinter.antlr.ECMAScriptParser.AssignmentOperatorContext;
import jshinter.antlr.ECMAScriptParser.BitAndExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitNotExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitOrExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitShiftExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitXOrExpressionContext;
import jshinter.antlr.ECMAScriptParser.EqualityExpressionContext;

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
	
	private void reportError(String msg, Token t) {
		System.out.printf("%d,%d: %s.\n", t.getLine(), t.getCharPositionInLine() + t.getText().length() + 1, msg);
	}

	@Override
	public void enterBitXOrExpression(BitXOrExpressionContext ctx) {
		TokenStream ts = parser.getTokenStream();

		reportError(String.format("Unexpected use of '%s'", ctx.getChild(1).getText()), ts.get(ctx.getChild(1).getSourceInterval().a));
	}

	@Override
	public void enterBitShiftExpression(BitShiftExpressionContext ctx) {
		TokenStream ts = parser.getTokenStream();

		reportError(String.format("Unexpected use of '%s'", ctx.getChild(1).getText()), ts.get(ctx.getChild(1).getSourceInterval().a));
	}

	@Override
	public void enterBitNotExpression(BitNotExpressionContext ctx) {
		TokenStream ts = parser.getTokenStream();

		reportError(String.format("Unexpected use of '%s'", ctx.getChild(1).getText()), ts.get(ctx.getChild(1).getSourceInterval().a));
	}

	@Override
	public void enterBitAndExpression(BitAndExpressionContext ctx) {
		TokenStream ts = parser.getTokenStream();

		reportError(String.format("Unexpected use of '%s'", ctx.getChild(1).getText()), ts.get(ctx.getChild(1).getSourceInterval().a));
	}

	@Override
	public void enterBitOrExpression(BitOrExpressionContext ctx) {
		TokenStream ts = parser.getTokenStream();

		reportError(String.format("Unexpected use of '%s'", ctx.getChild(1).getText()), ts.get(ctx.getChild(1).getSourceInterval().a));
	}

	@Override
	public void enterAssignmentOperator(AssignmentOperatorContext ctx) {
		Token t = ctx.getStart();
		int type = t.getType();
		
		if (type >= 45 || type <= 50) {
			reportError(String.format("Unexpected use of '%s'", t.getText()), t);
		}
	}

	@Override
	public void enterEqualityExpression(EqualityExpressionContext ctx) {
		TokenStream ts = parser.getTokenStream();
		Token t = ts.get(ctx.getChild(1).getSourceInterval().b);
		String operator = t.getText();
		
		String shouldBe = null;
		
		if (operator.equals("==")) {
			shouldBe = "===";
		} else if (operator.equals("!=")) {
			shouldBe = "!==";
		}
		
		if (!shouldBe.equals(null)) {
			reportError(String.format("Expected '%s' and instead saw '%s'", shouldBe, operator), t);
		}
	}
}
