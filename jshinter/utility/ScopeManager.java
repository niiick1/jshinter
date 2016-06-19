package jshinter.utility;

import java.util.Stack;

import org.antlr.v4.runtime.Token;

public class ScopeManager {
	
	private Stack<Scope> scopes;
	
	private Scope current;
	
	public ScopeManager() {
		scopes = new Stack<>();

		GlobalScope global = new GlobalScope();
		scopes.push(global);
		current = global;
	}
	
	public Scope stack(ScopeType type) {
		Scope newScope = new Scope(current, type);
		scopes.push(newScope);
		current = newScope;
		
		return newScope;
	}
	
	public Scope unstack() {
		for (Token token : current.getUnused()) {
			reportError(String.format("'%s' is defined but never used", token.getText()), token);
		}

		if (scopes.size() > 1) {
			Scope oldScope = scopes.pop();
			current = scopes.peek();
			
			for (Token token : oldScope.getUsedAndNotDefined()) {
				current.use(token);
			}
			
			return oldScope;
		}
		
		for (Token token : current.getUsedAndNotDefined()) {
			reportError(String.format("'%s' is not defined", token.getText()), token);
		}
		
		return current;
	}
	
	public void defineVariable(Token token) {
		String label = token.getText();
		if (current.isDefined(label)) {
			reportError(String.format("'%s' is already defined", token.getText()), token);
			return;
		}

		boolean isUsed = current.isUsed(label);
		if (isUsed) {
			reportError(String.format("'%s' was used before it was defined", token.getText()), token);
		}

		current.addSymbol(token, isUsed);
	}
	
	public void registerUsage(Token token) {
		current.use(token);
	}
	
	private void reportError(String msg, Token t) {
		System.out.printf("%d,%d: %s.\n", t.getLine(), t.getCharPositionInLine() + 1, msg);
	}
}
