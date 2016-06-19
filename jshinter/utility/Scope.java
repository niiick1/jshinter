package jshinter.utility;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.antlr.v4.runtime.Token;

public class Scope {
	private ScopeType scopeType;
	
	private Hashtable<String, Variable> symbols;
	
	private Hashtable<String, List<Token>> usages;
	
	private Scope parentScope;
	
	public Scope(Scope parent, ScopeType type) {
		parentScope = parent;
		scopeType = type;
		symbols = new Hashtable<>();
		usages = new Hashtable<>();
	}
	
	public boolean addSymbol(Token token, boolean used) {
		Variable var = new Variable(token, used);
		return symbols.put(token.getText(), var) == null;
	}
	
	public boolean isDefined(String label) {
		return symbols.containsKey(label);
	}
	
	public void use(Token token) {
		String label = token.getText();
		List<Token> tokens = usages.get(label);

		if (tokens == null) {
			tokens = new ArrayList<>();
		}

		tokens.add(token);
		
		usages.put(label, tokens);
		
		Variable var = symbols.get(label);
		
		if (var != null) {
			var.setUsed(true);
			
			symbols.put(label, var);
		}
	}
	
	public boolean isUsed(String label) {
		return usages.containsKey(label);
	}
	
	public List<Token> getUnused() {
		List<Token> unused = new ArrayList<>();

		for (Entry<String, Variable> e : symbols.entrySet()) {
			Variable var = e.getValue();
			if (!var.isUsed()) {
				unused.add(var.getToken());
			}
		}
		
		return unused;
	}
	
	public List<Token> getUsedAndNotDefined() {
		List<Token> usedAndNotDefined = new ArrayList<>();
		
		for (String label : usages.keySet()) {
			if (!symbols.containsKey(label)) {
				usedAndNotDefined.add(usages.get(label).get(0));
			}
		}
		
		return usedAndNotDefined;
	}
	
	public ScopeType getType() {
		return scopeType;
	}
	
	public void printCurrentScope() {
		StringBuilder output = new StringBuilder();
		for (String s : symbols.keySet()) {
			output.append(s + ", ");
		}
		
		if (output.length() > 0) {
			output.delete(output.length() - 2, output.length());
			System.out.println(output.toString());
		}
	}
	
	public void printUsage() {
		System.out.println("Usages:");
		for (Entry<String, List<Token>> e : usages.entrySet()) {
			for (Token token : e.getValue()) {
				System.out.printf("'%s' at %d,%d\n", token.getText(), token.getLine(), token.getCharPositionInLine() + 1);
			}
		}
	}
}
