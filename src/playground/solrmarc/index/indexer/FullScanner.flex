package playground.solrmarc.index.indexer;

import java_cup.runtime.SymbolFactory;
import java.util.List;
import java.util.ArrayList;
//import java.io.StringReader;
import playground.solrmarc.index.utils.StringReader;
import java_cup.runtime.ComplexSymbolFactory;

%%
%public
%class FullConditionalScanner
%cup
%extends playground.solrmarc.index.indexer.FullSym
%{
    int save_zzLexicalState;
    
    List<String> scanner_errors = null;
    StringBuilder string = new StringBuilder();
    public FullConditionalScanner(ComplexSymbolFactory sf)
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
     return sf.newSymbol("EOF", FullSym.EOF);
%eofval}

IntLiteral = 0 | [1-9][0-9]*
new_line = \r|\n|\r\n
white_space = {new_line} | [ \t\f]
identifier = [A-Za-z0-9][A-Z_a-z0-9.]*[A-Za-z0-9]
fullrecord = "xml"|"raw"|"json"|"json2"|"text"|"FullRecordAs"[A-Za-z0-9]*
datespec = "date"|"dateOfPublication"|"dateRecordIndexed"|"index_date"
%state STARTSPEC STRING CONDITIONAL SUBFIELDSPEC CUSTOMSPEC CUSTOMMETHOD CUSTOMPARAM MAPSPEC CONSTANT 

%%
<YYINITIAL>{
[A-Za-z][A-Za-z_0-9]*   { return sf.newSymbol("FIELDNAME", FullSym.FIELDNAME, yytext()); }
","                     { return sf.newSymbol(",", FullSym.COMMA); }
{white_space}           { /* ignore */ }
"="                     { yybegin(STARTSPEC); return sf.newSymbol("EQU", FullSym.EQU ); }
^"#".*                  { /* ignore as comment */ }
}

<STARTSPEC>{
[{]						{ return sf.newSymbol("{",FullSym.LBRACE); }
[0-9][0-9][0-9]   		{ yybegin(SUBFIELDSPEC); return sf.newSymbol("FIELDSPEC",FullSym.FIELDSPEC, yytext());  }
"LNK"[0-9][0-9][0-9]    { yybegin(SUBFIELDSPEC); return sf.newSymbol("FIELDSPEC",FullSym.FIELDSPEC, yytext());  }
[A-Z][A-Z][A-Z]   		{ yybegin(SUBFIELDSPEC); return sf.newSymbol("FIELDSPEC",FullSym.FIELDSPEC, yytext()); }
":"						{ yybegin(STARTSPEC);    return sf.newSymbol(":",FullSym.COLON);  }
"?"                     { yybegin(CONDITIONAL);  return sf.newSymbol("?",FullSym.QUESTION); }
","                     { yybegin(MAPSPEC);      return sf.newSymbol(",", FullSym.COMMA); }
{white_space}           { /* ignore */ }
"script"                { yybegin(CUSTOMSPEC);   return sf.newSymbol("SCRIPT", FullSym.SCRIPT ); }
"custom"                { yybegin(CUSTOMSPEC);   return sf.newSymbol("CUSTOM", FullSym.CUSTOM ); }
"java"                  { yybegin(CUSTOMSPEC);   return sf.newSymbol("JAVA", FullSym.JAVA ); }
{fullrecord}            { yybegin(MAPSPEC);      return sf.newSymbol("FULLRECORD", FullSym.FULLRECORD, yytext()); }
{datespec}              { yybegin(MAPSPEC);      return sf.newSymbol("DATE", FullSym.DATE, yytext()); }
\"                      { save_zzLexicalState = CONSTANT; string.setLength(0); yybegin(STRING); }
}

<CONSTANT>{
  \"                    { save_zzLexicalState = CONSTANT; string.setLength(0); yybegin(STRING); }
"||"|"|"                { return sf.newSymbol("OR",FullSym.OR); }
","                     { yybegin(MAPSPEC);  return sf.newSymbol(",", FullSym.COMMA); }
}

<CUSTOMSPEC>{
"("                     { return sf.newSymbol("(",FullSym.LPAREN); }
")"                     { return sf.newSymbol(")",FullSym.RPAREN); }
","                     { yybegin(CUSTOMMETHOD); return sf.newSymbol(",", FullSym.COMMA); }
{identifier}			{ return sf.newSymbol("IDENTIFIER", FullSym.IDENTIFIER, yytext()); }
{white_space}           { /* ignore */ }
}

<CUSTOMMETHOD>{
{identifier}			{ return sf.newSymbol("IDENTIFIER", FullSym.IDENTIFIER, yytext()); }
"("                     { yybegin(CUSTOMPARAM); return sf.newSymbol("(",FullSym.LPAREN); }
","                     { yybegin(MAPSPEC); return sf.newSymbol(",", FullSym.COMMA); }
}

<CUSTOMPARAM>{
\"                      { save_zzLexicalState = CUSTOMPARAM; string.setLength(0); yybegin(STRING); }
"("                     { return sf.newSymbol("(",FullSym.LPAREN); }
","                     { return sf.newSymbol(",", FullSym.COMMA); }
{identifier}  			{ return sf.newSymbol("IDENTIFIER", FullSym.IDENTIFIER, yytext()); }
")"                     { yybegin(MAPSPEC); return sf.newSymbol(")",FullSym.RPAREN); }
{white_space}           { /* ignore */ }
}

<MAPSPEC>{
","                     { return sf.newSymbol(",", FullSym.COMMA); }
{identifier}			{ return sf.newSymbol("IDENTIFIER", FullSym.IDENTIFIER, yytext()); }
"("                     { yybegin(CUSTOMPARAM); return sf.newSymbol("(",FullSym.LPAREN); }
")"                     { return sf.newSymbol(")",FullSym.RPAREN); }
}

<SUBFIELDSPEC>{
"[""^"?[a-z][-a-z0-9]*"]"   { return sf.newSymbol("SUBFIELDSPEC",FullSym.SUBFIELDSPEC, yytext()); }
[a-z][a-z0-9]*          { return sf.newSymbol("SUBFIELDSPEC",FullSym.SUBFIELDSPEC, yytext()); }
"["[0-9]+(-[0-9]+)?"]"  { return sf.newSymbol("POSITION", FullSym.POSITION, yytext()); }
{white_space}           { /* ignore */ }
":"						{ yybegin(STARTSPEC);   return sf.newSymbol(":",FullSym.COLON);  }
","				        { yybegin(MAPSPEC);  return sf.newSymbol(",", FullSym.COMMA);  }
[}]				        { yybegin(STARTSPEC);   return sf.newSymbol("}",FullSym.RBRACE);  }
[?]				  		{ yybegin(CONDITIONAL); return sf.newSymbol("?",FullSym.QUESTION);  }
}

<CONDITIONAL>{
/* keywords */
[0][0][0-9]				  { return sf.newSymbol("FIELDSPEC", FullSym.FIELDSPEC, yytext()); }
"$"[a-z0-9A-Z]            { return sf.newSymbol("SUBFIELD",FullSym.SUBFIELD, yytext().substring(1,2)); }
"ind"[12]                 { return sf.newSymbol("IND",FullSym.IND, yytext().substring(3,4)); }
"ind"[03-9]               { error("Illegal indicator specification <"+ yytext()+">"); }
"["[0-9]+(-[0-9]+)?"]"    { return sf.newSymbol("POSITION", FullSym.POSITION, yytext()); }

/* literals */
{IntLiteral}              { return sf.newSymbol("NUMBER",FullSym.NUMBER, yytext()); }

/* separators */
  \"              { string.setLength(0); save_zzLexicalState = CONDITIONAL; yybegin(STRING); }

":"	              { yybegin(STARTSPEC);  return sf.newSymbol(":", FullSym.COLON);  }
[}]				  { yybegin(STARTSPEC);  return sf.newSymbol("}", FullSym.RBRACE);  }
"("               { return sf.newSymbol("(",FullSym.LPAREN); }
")"               { return sf.newSymbol(")",FullSym.RPAREN); }
"=="|"="          { return sf.newSymbol("EQU",FullSym.EQU ); }
"!="              { return sf.newSymbol("NEQ",FullSym.NEQ); }
"~"|"matches"     { return sf.newSymbol("MATCH",FullSym.MATCH); }
"<"               { return sf.newSymbol("LE",FullSym.LT); }
">"               { return sf.newSymbol("GT",FullSym.GT); }
"&&"|"&"          { return sf.newSymbol("AND",FullSym.AND); }
"||"|"|"          { return sf.newSymbol("OR",FullSym.OR); }
"!"               { return sf.newSymbol("NOT",FullSym.NOT); }
'[^\\]'           { return sf.newSymbol("CHAR",FullSym.CHAR, yytext().substring(1, 2)); } 
'\\.'             { return sf.newSymbol("CHAR",FullSym.CHAR, yytext().substring(1, 3)); } 
{white_space}     { /* ignore */ }

}

<STRING> {
  \"                             { yybegin(save_zzLexicalState); 
                                   return sf.newSymbol("QUOTEDSTR",FullSym.QUOTEDSTR,string.toString()); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
  <<EOF>>						 { yybegin(save_zzLexicalState); error("Error: End of input encountered in quoted string: "+ string.toString()); }
}


/* error fallback */
[^]              {  /* throw new Error("Illegal character <"+ yytext()+">");*/
		    		String scannerStateStr = "initial";
		    		switch (yystate() ) {
		    			case YYINITIAL:    scannerStateStr = "initial";      break;
		    			case STARTSPEC:    scannerStateStr = "startspec";    break;
		    			case STRING:       scannerStateStr = "string";       break;
		    			case CONDITIONAL:  scannerStateStr = "conditional";  break; 
		    			case SUBFIELDSPEC: scannerStateStr = "subfield";     break;
		    			case CUSTOMSPEC:   scannerStateStr = "customspec";   break;
		    			case CUSTOMMETHOD: scannerStateStr = "custommethod"; break;
		    			case CUSTOMPARAM:  scannerStateStr = "customparam";  break;
		    			case MAPSPEC:      scannerStateStr = "mapspec";      break;
		    			default:           scannerStateStr = "unknown";      break; 
		    		}
		    		error("Error: Illegal character <"+ yytext()+">  found in scanner state "+ scannerStateStr);
                  }
