package jshinter.analyzer;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import jshinter.antlr.ECMAScriptBaseListener;
import jshinter.antlr.ECMAScriptParser;
import jshinter.antlr.ECMAScriptParser.AssignmentExpressionContext;
import jshinter.antlr.ECMAScriptParser.AssignmentOperatorContext;
import jshinter.antlr.ECMAScriptParser.BitAndExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitNotExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitOrExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitShiftExpressionContext;
import jshinter.antlr.ECMAScriptParser.BitXOrExpressionContext;
import jshinter.antlr.ECMAScriptParser.BlockContext;
import jshinter.antlr.ECMAScriptParser.BreakStatementContext;
import jshinter.antlr.ECMAScriptParser.ContinueStatementContext;
import jshinter.antlr.ECMAScriptParser.DebuggerStatementContext;
import jshinter.antlr.ECMAScriptParser.DoStatementContext;
import jshinter.antlr.ECMAScriptParser.EosContext;
import jshinter.antlr.ECMAScriptParser.EqualityExpressionContext;
import jshinter.antlr.ECMAScriptParser.ExpressionStatementContext;
import jshinter.antlr.ECMAScriptParser.FormalParameterListContext;
import jshinter.antlr.ECMAScriptParser.FunctionBodyContext;
import jshinter.antlr.ECMAScriptParser.FunctionDeclarationContext;
import jshinter.antlr.ECMAScriptParser.FunctionExpressionContext;
import jshinter.antlr.ECMAScriptParser.IdentifierExpressionContext;
import jshinter.antlr.ECMAScriptParser.IfStatementContext;
import jshinter.antlr.ECMAScriptParser.InitialiserContext;
import jshinter.antlr.ECMAScriptParser.IterationStatementContext;
import jshinter.antlr.ECMAScriptParser.MemberDotExpressionContext;
import jshinter.antlr.ECMAScriptParser.NewExpressionContext;
import jshinter.antlr.ECMAScriptParser.ProgramContext;
import jshinter.antlr.ECMAScriptParser.ReturnStatementContext;
import jshinter.antlr.ECMAScriptParser.SingleExpressionContext;
import jshinter.antlr.ECMAScriptParser.SourceElementsContext;
import jshinter.antlr.ECMAScriptParser.StatementContext;
import jshinter.antlr.ECMAScriptParser.ThrowStatementContext;
import jshinter.antlr.ECMAScriptParser.TypeofExpressionContext;
import jshinter.antlr.ECMAScriptParser.VariableDeclarationContext;
import jshinter.antlr.ECMAScriptParser.VariableStatementContext;
import jshinter.antlr.ECMAScriptParser.WithStatementContext;
import jshinter.utility.ScopeManager;
import jshinter.utility.ScopeType;

public class JSHinterListener extends ECMAScriptBaseListener {

	private ECMAScriptParser parser;
	
	private final Boolean DEBUG;
	
	private ScopeManager scopeManager;
	
	public JSHinterListener(ECMAScriptParser parser) {
		this.parser = parser;
		this.DEBUG = false;
		scopeManager = new ScopeManager();
	}
	
	public JSHinterListener(ECMAScriptParser parser, Boolean debugMode) {
		this.parser = parser;
		this.DEBUG = debugMode;
		scopeManager = new ScopeManager();
	}
	
	private void reportError(String msg, Token t) {
		System.out.printf("%d,%d: %s.\n", t.getLine(), t.getCharPositionInLine() + 1, msg);
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
		
		if (type >= 45 && type <= 50) {
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

	@Override
	public void enterSourceElements(SourceElementsContext ctx) {
		if (ctx.getParent() instanceof ProgramContext) {
			return;
		}
	}

	@Override
	public void enterVariableDeclaration(VariableDeclarationContext ctx) {
		scopeManager.defineVariable(ctx.Identifier().getSymbol());
	}

	@Override
	public void exitProgram(ProgramContext ctx) {
		scopeManager.unstack();
	}

	@Override
	public void exitFunctionBody(FunctionBodyContext ctx) {
		scopeManager.unstack();
	}

	@Override
	public void enterFunctionDeclaration(FunctionDeclarationContext ctx) {
		scopeManager.defineVariable(ctx.Identifier().getSymbol());
		scopeManager.stack(ScopeType.FUNCTION);
	}

	@Override
	public void enterFunctionExpression(FunctionExpressionContext ctx) {
		scopeManager.stack(ScopeType.FUNCTION);
	}

	@Override
	public void enterFormalParameterList(FormalParameterListContext ctx) {
		for (TerminalNode id : ctx.Identifier()) {
			scopeManager.defineVariable(id.getSymbol());
		}
	}

	@Override
	public void enterIdentifierExpression(IdentifierExpressionContext ctx) {
		TerminalNode token = ctx.Identifier();
		
		scopeManager.registerUsage(token.getSymbol());
		
		if (token.getText().equals("eval")) {
			reportError("eval can be harmful", token.getSymbol());
		}
	}

	@Override
	public void enterStatement(StatementContext ctx) {
		ParserRuleContext parentContext = ctx.getParent();
		if (parentContext instanceof IfStatementContext || parentContext instanceof IterationStatementContext
				|| parentContext instanceof WithStatementContext) {
			ParseTree childContext = ctx.getChild(0);
			if (!(childContext instanceof BlockContext)) {
				TokenStream ts = parser.getTokenStream();
				Token t = ts.get(childContext.getSourceInterval().a);
				
				reportError(String.format("Expected '{' and instead saw '%s'", t.getText()), t);
			}
		}
	}
	
	private void checkForSemicolon(ParserRuleContext ctx) {
		ParseTree lastChild = ctx.getChild(ctx.getChildCount() - 1);
		if (ctx.getChild(ctx.getChildCount() - 1) instanceof EosContext) {
			EosContext eos = (EosContext) lastChild;
			
			TerminalNode semicolon = eos.SemiColon();
			
			if (semicolon == null) {
				reportError("Missing semicolon", eos.getStop());
			}
		}
	}

	@Override
	public void enterExpressionStatement(ExpressionStatementContext ctx) {
		checkForSemicolon(ctx);
	}

	@Override
	public void enterVariableStatement(VariableStatementContext ctx) {
		checkForSemicolon(ctx);
	}

	@Override
	public void enterDoStatement(DoStatementContext ctx) {
		checkForSemicolon(ctx);
	}

	@Override
	public void enterContinueStatement(ContinueStatementContext ctx) {
		checkForSemicolon(ctx);
	}

	@Override
	public void enterBreakStatement(BreakStatementContext ctx) {
		checkForSemicolon(ctx);
	}

	@Override
	public void enterReturnStatement(ReturnStatementContext ctx) {
		checkForSemicolon(ctx);
	}

	@Override
	public void enterThrowStatement(ThrowStatementContext ctx) {
		checkForSemicolon(ctx);
	}

	@Override
	public void enterDebuggerStatement(DebuggerStatementContext ctx) {
		reportError("Forgotten 'debugger' statement?", ctx.getStart());
		checkForSemicolon(ctx);
	}

	@Override
	public void enterNewExpression(NewExpressionContext ctx) {
		if (!(ctx.getParent() instanceof InitialiserContext)) {
			reportError("Do not use 'new' for side effects", ctx.getStart());
		}
	}
}
