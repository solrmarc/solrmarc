package org.solrmarc.index.specification.conditional;

import java_cup.runtime.SymbolFactory;
import java.util.List;
import java.util.ArrayList;
import org.solrmarc.index.utils.StringReader;
import java_cup.runtime.ComplexSymbolFactory;

%%
%public
%class ConditionalScanner
%cup
%extends org.solrmarc.index.specification.conditional.sym
%{
    List<String> scanner_errors = null;
    StringBuilder string = new StringBuilder();
    public ConditionalScanner(ComplexSymbolFactory sf)
    {
		this(new StringReader(""));
		this.sf = sf;
		scanner_errors = new ArrayList<String>();
    }
    
    SymbolFactory sf;
    
    public void startParse(String strToParse)
    {
        yyreset(new StringReader(strToParse));
        scanner_errors = new ArrayList<String>();
    }
    
    private void error(String message) 
    {
        scanner_errors.add(message);
    }
    
    public List<String> getScannerErrors()
    {
    	return(scanner_errors);
    }
%}

%eofval{
     return sf.newSymbol("EOF", sym.EOF);
%eofval}

IntLiteral = 0 | [1-9][0-9]*
new_line = \r|\n|\r\n;
white_space = {new_line} | [ \t\f]
%state STRING CONDITIONAL SUBFIELDSPEC

%%
<YYINITIAL>{
[{]						{ return sf.newSymbol("{",sym.LBRACE); }
[0-9][0-9][0-9]   		{ yybegin(SUBFIELDSPEC); return sf.newSymbol("FIELDSPEC",sym.FIELDSPEC, yytext());  }
"LNK"[0-9][0-9][0-9]    { yybegin(SUBFIELDSPEC); return sf.newSymbol("FIELDSPEC",sym.FIELDSPEC, yytext());  }
[A-Z][A-Z][A-Z]   		{ yybegin(SUBFIELDSPEC); return sf.newSymbol("FIELDSPEC",sym.FIELDSPEC, yytext()); }
":"						{ yybegin(YYINITIAL);    return sf.newSymbol(":",sym.COLON);  }
"?"                     { yybegin(CONDITIONAL);  return sf.newSymbol("?",sym.QUESTION); }
{white_space}           { /* ignore */ }
}

<SUBFIELDSPEC>{
"[""^"?[a-z][-a-z0-9]*"]"   { return sf.newSymbol("SUBFIELDSPEC",sym.SUBFIELDSPEC, yytext()); }
[a-z][a-z0-9]*          { return sf.newSymbol("SUBFIELDSPEC",sym.SUBFIELDSPEC, yytext()); }
"["[0-9]+(-[0-9]+)?"]"  { return sf.newSymbol("POSITION", sym.POSITION, yytext()); }
{white_space}           { /* ignore */ }
":"						{ yybegin(YYINITIAL);   return sf.newSymbol(":",sym.COLON);  }
[}]				        { yybegin(YYINITIAL);   return sf.newSymbol("}",sym.RBRACE);  }
[?]				  		{ yybegin(CONDITIONAL); return sf.newSymbol("?",sym.QUESTION);  }
}

<CONDITIONAL>{
/* keywords */
[0][0][0-9]				  { return sf.newSymbol("FIELDSPEC", sym.FIELDSPEC, yytext()); }
"$"[a-z0-9A-Z]            { return sf.newSymbol("SUBFIELD",sym.SUBFIELD, yytext().substring(1,2)); }
"ind"[12]                 { return sf.newSymbol("IND",sym.IND, yytext().substring(3,4)); }
"ind"[03-9]               { error("Illegal indicator specification <"+ yytext()+">"); }
"["[0-9]+(-[0-9]+)?"]"    { return sf.newSymbol("POSITION", sym.POSITION, yytext()); }

/* literals */
{IntLiteral}              { return sf.newSymbol("NUMBER",sym.NUMBER, yytext()); }

/* separators */
  \"              { string.setLength(0); yybegin(STRING); }

":"	              { yybegin(YYINITIAL);  return sf.newSymbol(":",sym.COLON);  }
[}]				  { yybegin(YYINITIAL);  return sf.newSymbol("}",sym.RBRACE);  }
"("               { return sf.newSymbol("(",sym.LPAREN); }
")"               { return sf.newSymbol(")",sym.RPAREN); }
"=="|"="          { return sf.newSymbol("EQU",sym.EQU ); }
"!="              { return sf.newSymbol("NEQ",sym.NEQ); }
"~"|"matches"     { return sf.newSymbol("MATCH",sym.MATCH); }
"<"|"startsWith"  { return sf.newSymbol("LE",sym.LT); }
">"|"endsWith"    { return sf.newSymbol("GT",sym.GT); }
"&&"|"&"          { return sf.newSymbol("AND",sym.AND); }
"||"|"|"          { return sf.newSymbol("OR",sym.OR); }
"!"               { return sf.newSymbol("NOT",sym.NOT); }
'[^\\]'           { return sf.newSymbol("CHAR",sym.CHAR, yytext().substring(1, 2)); } 
'\\.'             { return sf.newSymbol("CHAR",sym.CHAR, yytext().substring(1, 3)); } 
{white_space}     { /* ignore */ }

}

<STRING> {
  \"                             { yybegin(CONDITIONAL); 
      return sf.newSymbol("QUOTEDSTR",sym.QUOTEDSTR,string.toString()); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
  <<EOF>>						 { yybegin(CONDITIONAL); error("Error: End of input encountered in quoted string: "+ string.toString()); }
}


/* error fallback */
[^]              {  /* throw new Error("Illegal character <"+ yytext()+">");*/
		    		String scannerStateStr = "initial";
		    		switch (yystate() ) {
		    			case YYINITIAL:    scannerStateStr = "initial";     break;
		    			case STRING:       scannerStateStr = "string";      break;
		    			case CONDITIONAL:  scannerStateStr = "conditional"; break; 
		    			case SUBFIELDSPEC: scannerStateStr = "subfield";    break;
		    			default:           scannerStateStr = "unknown";     break; 
		    		}
		    		error("Error: Illegal character <"+ yytext()+">  found in scanner state "+ scannerStateStr);
                  }
