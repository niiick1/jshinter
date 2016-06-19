package jshinter.analyzer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import jshinter.antlr.ECMAScriptLexer;
import jshinter.antlr.ECMAScriptParser;

public class Analyzer {

	public static void main(String[] args) throws Exception {
		String inputFile = null;
		int maxDepth = 0,
			maxParams = 0,
			maxStatements = 0;

        if (args.length > 0) {
        	for (int x = 0; x < args.length; x++) {
        		String arg = args[x];
        		
        		if (arg.startsWith("-")) {
        			Pattern p = Pattern.compile("^-(maxdepth|maxparams|maxstatements)=([0-9]+)");
        			Matcher m = p.matcher(arg);
        			
        			if (!m.matches()) {
        				System.out.printf("Invalid parameter %s\n", arg);
        				continue;
        			}
        			
        			String param = m.group(1);
        			Integer value = Integer.parseInt(m.group(2));

        			switch (param) {
        				case "maxdepth":
        					maxDepth = value;
        					break;
        					
        				case "maxparams":
        					maxParams = value;
        					break;
        					
        				case "maxstatements":
        					maxStatements = value;
        					break;
        					
        				default: break;
        			}
        		} else {
        			inputFile = args[x];
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
        JSHinterListener listener = new JSHinterListener(parser);

        listener.verifyMaxDepth(maxDepth);
        listener.verifyMaxParams(maxParams);
        listener.verifyMaxStatements(maxStatements);

        walker.walk(listener, tree); // initiate walk of tree with listener
	}
}
