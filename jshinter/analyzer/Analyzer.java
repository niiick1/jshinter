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
		Boolean debug = false;
        if (args.length > 0) {
        	if (args[0].equals("-d")) {
        		debug = true;
        	} else {
        		inputFile = args[0];
        		
        		for (int x = 1; x < args.length; x++) {
        			if (args[x].equals("-d")) {
        				debug = true;
        			}
        		}
        	}
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
        JSHinterListener listener = new JSHinterListener(parser, debug);
        walker.walk(listener, tree); // initiate walk of tree with listener
	}
}
