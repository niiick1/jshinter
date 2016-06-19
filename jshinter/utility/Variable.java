package jshinter.utility;

import org.antlr.v4.runtime.Token;

public class Variable {
	private Token token;
	
	private boolean used;

	public Variable(Token token, boolean used) {
		this.token = token;
		this.used = used;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}
}
