package jshinter.analyzer;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import jshinter.antlr.ECMAScriptBaseListener;
import jshinter.antlr.ECMAScriptParser;
import jshinter.antlr.ECMAScriptParser.AssignmentExpressionContext;
import jshinter.antlr.ECMAScriptParser.AssignmentOperatorContext;
import jshinter.antlr.ECMAScriptParser.BitAndExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitNotExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitOrExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitShiftExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitXOrExpressionContext;
import jshinter.antlr.ECMAScriptParser.EqualityExpressionContext;
import jshinter.antlr.ECMAScriptParser.MemberDotExpressionContext;
import jshinter.antlr.ECMAScriptParser.SingleExpressionContext;
import jshinter.antlr.ECMAScriptParser.TypeofExpressionContext;

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
	public void exitAssignmentExpression(AssignmentExpressionContext ctx) {
		SingleExpressionContext leftSideAssign = ctx.singleExpression(0);
		
		if (leftSideAssign instanceof MemberDotExpressionContext) {
			checkFreeze(leftSideAssign);
		}
	}

	private void checkFreeze(SingleExpressionContext leftSideAssign) {
		SingleExpressionContext prototypeExpression = getPrototype(leftSideAssign);
		
		if (prototypeExpression == null) {
			return;
		}
		
		List<String> nativeObjects = Arrays.asList(
			"Array", "ArrayBuffer", "Boolean", "Collator", "DataView", "Date",
			"DateTimeFormat", "Error", "EvalError", "Float32Array", "Float64Array",
			"Function", "Infinity", "Intl", "Int16Array", "Int32Array", "Int8Array",
			"Iterator", "Number", "NumberFormat", "Object", "RangeError",
			"ReferenceError", "RegExp", "StopIteration", "String", "SyntaxError",
			"TypeError", "Uint16Array", "Uint32Array", "Uint8Array", "Uint8ClampedArray",
			"URIError");
		
		MemberDotExpressionContext exp = (MemberDotExpressionContext) prototypeExpression;
		SingleExpressionContext object = exp.singleExpression();

		if (nativeObjects.contains(object.getText())) {
			TokenStream ts = parser.getTokenStream();
			Token t = ts.get(leftSideAssign.getChild(0).getSourceInterval().b);
			
			reportError(String.format("Extending prototype of native object: '%s'", object.getText()), t);
		}
	}
	
	private SingleExpressionContext getPrototype(SingleExpressionContext ctx) {
		if (!(ctx instanceof MemberDotExpressionContext)) {
			return null;
		}
		
		MemberDotExpressionContext expression = (MemberDotExpressionContext) ctx;

		String tokenText = expression.identifierName().getText();
		if (tokenText.equals("prototype")) {
			return expression;
		}
		
		return getPrototype(expression.singleExpression());
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
		
		if (shouldBe != null) {
			reportError(String.format("Expected '%s' and instead saw '%s'", shouldBe, operator), t);
		}
		
		if (ctx.getChild(0) instanceof TypeofExpressionContext) {
			processTypeofExpression(ctx);
		}
	}

	private void processTypeofExpression(EqualityExpressionContext ctx) {
		TokenStream ts = parser.getTokenStream();
		Token t = ts.get(ctx.getChild(2).getSourceInterval().a);
		
		List<String> allowedTypeof = Arrays.asList("undefined", "boolean", "number", "string", "function", "object");
		
		if (t.getType() == 99) {
			String literal = t.getText();
			literal = literal.substring(1, literal.length() - 1);
			if (!allowedTypeof.contains(literal)) {
				reportError(String.format("Invalid typeof value '%s'", literal), t);
			}
		}
	}
}
