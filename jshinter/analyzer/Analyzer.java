package jshinter.analyzer;

import java.io.FileInputStream;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import jshinter.antlr.ECMAScriptLexer;
import jshinter.antlr.ECMAScriptParser;

public class Analyzer {

	public static void main(String[] args) throws Exception {
		String inputFile = null;

        if (args.length > 0) {
    		inputFile = args[0];
        }
        
        InputStream is = System.in;
        
        if (inputFile != null) {
            is = new FileInputStream(inputFile);
        }

        ANTLRInputStream input = new ANTLRInputStream(is);

        ECMAScriptLexer lexer = new ECMAScriptLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ECMAScriptParser parser = new ECMAScriptParser(tokens);
        ParseTree tree = parser.program(); // parse

        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        JSHinterListener listener = new JSHinterListener(parser);
        walker.walk(listener, tree); // initiate walk of tree with listener
	}
}
