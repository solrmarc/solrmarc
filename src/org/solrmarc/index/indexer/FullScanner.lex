package org.solrmarc.index.indexer;

import java.util.List;
import java.util.ArrayList;
import org.solrmarc.index.utils.StringReader;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory.Location;

%%
%public
%class FullConditionalScanner
%cup
%extends org.solrmarc.index.indexer.FullSym
%char
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
    
    ComplexSymbolFactory sf;
    
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
    
    private Symbol symbol(String name, int sym) 
    {
        return sf.newSymbol(name, sym, new Location(yyline+1, yycolumn+1, yychar), new Location(yyline+1, yycolumn+yylength(), yychar+yylength()));
    }

    private Symbol symbol(String name, int sym, Object val)
    {
        Location left = new Location(yyline+1,yycolumn+1,yychar);
        Location right= new Location(yyline+1,yycolumn+yylength(), yychar+yylength());
        return sf.newSymbol(name, sym, left, right,val);
    }
      
%}

%eofval{
     return sf.newSymbol("EOF", FullSym.EOF);
%eofval}

IntLiteral = 0 | [1-9][0-9]*
new_line = \r|\n|\r\n
white_space = {new_line} | [ \t\f]
identifier = [A-Za-z0-9][A-Z_a-z0-9./\\]*[A-Za-z0-9]
fullrecord = "xml"|"raw"|"json"|"json2"|"text"|"FullRecordAs"[A-Za-z0-9]*
datespec = "date"|[Dd]"ateOfPublication"|[Dd]"ateRecordIndexed"|"index_date"
%state STRING CONDITIONAL SUBFIELDSPEC CUSTOMSPEC CUSTOMMETHOD CUSTOMPARAM MAPSPEC CONSTANT 

%%
<YYINITIAL>{
[{]						{ return symbol("{",FullSym.LBRACE); }
[0-9][0-9][0-9]   		{ yybegin(SUBFIELDSPEC); return symbol("FIELDSPEC",FullSym.FIELDSPEC, yytext());  }
"LNK"[0-9][0-9][0-9]    { yybegin(SUBFIELDSPEC); return symbol("FIELDSPEC",FullSym.FIELDSPEC, yytext());  }
[A-Z][A-Z][A-Z]   		{ yybegin(SUBFIELDSPEC); return symbol("FIELDSPEC",FullSym.FIELDSPEC, yytext()); }
":"						{ yybegin(YYINITIAL);    return symbol(":",FullSym.COLON);  }
"?"                     { yybegin(CONDITIONAL);  return symbol("?",FullSym.QUESTION); }
","                     { yybegin(MAPSPEC);      return symbol(",", FullSym.COMMA); }
{white_space}           { /* ignore */ }
"script"                { yybegin(CUSTOMSPEC);   return symbol("SCRIPT", FullSym.SCRIPT, yytext() ); }
"custom"                { yybegin(CUSTOMSPEC);   return symbol("CUSTOM", FullSym.CUSTOM, yytext() ); }
"java"                  { yybegin(CUSTOMSPEC);   return symbol("JAVA", FullSym.JAVA, yytext() ); }
{fullrecord}            { yybegin(MAPSPEC);      return symbol("FULLRECORD", FullSym.FULLRECORD, yytext()); }
{datespec}              { yybegin(MAPSPEC);      return symbol("DATE", FullSym.DATE, yytext()); }
\"                      { save_zzLexicalState = CONSTANT; string.setLength(0); yybegin(STRING); }
}

<CONSTANT>{
  \"                    { save_zzLexicalState = CONSTANT; string.setLength(0); yybegin(STRING); }
"||"|"|"                { return symbol("OR",FullSym.OR); }
","                     { yybegin(MAPSPEC);  return symbol(",", FullSym.COMMA); }
{white_space}           { /* ignore */ }
}

<CUSTOMSPEC>{
"("                     { return symbol("(",FullSym.LPAREN); }
")"                     { return symbol(")",FullSym.RPAREN); }
","                     { yybegin(CUSTOMMETHOD); return symbol(",", FullSym.COMMA); }
{identifier}			{ return symbol("IDENTIFIER", FullSym.IDENTIFIER, yytext()); }
{white_space}           { /* ignore */ }
}

<CUSTOMMETHOD>{
{identifier}			{ return symbol("IDENTIFIER", FullSym.IDENTIFIER, yytext()); }
"("                     { yybegin(CUSTOMPARAM); return symbol("(",FullSym.LPAREN); }
","                     { yybegin(MAPSPEC); return symbol(",", FullSym.COMMA); }
{white_space}           { /* ignore */ }
}

<CUSTOMPARAM>{
\"                      { save_zzLexicalState = CUSTOMPARAM; string.setLength(0); yybegin(STRING); }
"("                     { return symbol("(",FullSym.LPAREN); }
","                     { return symbol(",", FullSym.COMMA); }
{identifier}  			{ return symbol("IDENTIFIER", FullSym.IDENTIFIER, yytext()); }
{IntLiteral}			{ return symbol("NUMBER",FullSym.NUMBER, yytext()); }
")"                     { yybegin(MAPSPEC); return symbol(")",FullSym.RPAREN); }
{white_space}           { /* ignore */ }
}

<MAPSPEC>{
","                     { return symbol(",", FullSym.COMMA); }
"custom_map"            { return symbol("CUSTOM_MAP", FullSym.CUSTOM_MAP, yytext()); }
{identifier}            { return symbol("IDENTIFIER", FullSym.IDENTIFIER, yytext()); }
"("                     { yybegin(CUSTOMPARAM); return symbol("(",FullSym.LPAREN); }
")"                     { return symbol(")",FullSym.RPAREN); }
{white_space}           { /* ignore */ }
}

<SUBFIELDSPEC>{
"[""^"?[a-z][-a-z0-9]*"]"   { return symbol("SUBFIELDSPEC",FullSym.SUBFIELDSPEC, yytext()); }
[a-z][a-z0-9]*              { return symbol("SUBFIELDSPEC",FullSym.SUBFIELDSPEC, yytext()); }
"["[0-9]+(-[0-9]+)?"]"      { return symbol("POSITION", FullSym.POSITION, yytext()); }
{white_space}               { /* ignore */ }
":"						    { yybegin(YYINITIAL);   return symbol(":",FullSym.COLON);  }
","				            { yybegin(MAPSPEC);  return symbol(",", FullSym.COMMA);  }
[}]				            { yybegin(YYINITIAL);   return symbol("}",FullSym.RBRACE);  }
[?]				  		    { yybegin(CONDITIONAL); return symbol("?",FullSym.QUESTION);  }
}

<CONDITIONAL>{
/* keywords */
[0][0][0-9]				  { return symbol("FIELDSPEC", FullSym.FIELDSPEC, yytext()); }
"$"[a-z0-9A-Z]            { return symbol("SUBFIELD",FullSym.SUBFIELD, yytext().substring(1,2)); }
"ind"[12]                 { return symbol("IND",FullSym.IND, yytext().substring(3,4)); }
"ind"[03-9]               { error("Illegal indicator specification <"+ yytext()+">"); }
"["[0-9]+(-[0-9]+)?"]"    { return symbol("POSITION", FullSym.POSITION, yytext()); }

/* literals */
{IntLiteral}              { return symbol("NUMBER",FullSym.NUMBER, yytext()); }

/* separators */
  \"              { string.setLength(0); save_zzLexicalState = CONDITIONAL; yybegin(STRING); }

":"	              { yybegin(YYINITIAL);  return symbol(":", FullSym.COLON);  }
[}]				  { yybegin(YYINITIAL);  return symbol("}", FullSym.RBRACE);  }
","               { yybegin(MAPSPEC);  return symbol(",", FullSym.COMMA);  }
"("               { return symbol("(",FullSym.LPAREN); }
")"               { return symbol(")",FullSym.RPAREN); }
"=="|"="          { return symbol("EQU",FullSym.EQU ); }
"!="              { return symbol("NEQ",FullSym.NEQ); }
"~"|"matches"     { return symbol("MATCH",FullSym.MATCH); }
"contains"        { return symbol("CONTAINS",FullSym.CONTAINS); }
"<"|"startsWith"  { return symbol("LE",FullSym.LT); }
">"|"endsWith"    { return symbol("GT",FullSym.GT); }
"&&"|"&"          { return symbol("AND",FullSym.AND); }
"||"|"|"          { return symbol("OR",FullSym.OR); }
"!"               { return symbol("NOT",FullSym.NOT); }
'[^\\]'           { return symbol("CHAR",FullSym.CHAR, yytext().substring(1, 2)); } 
'\\.'             { return symbol("CHAR",FullSym.CHAR, yytext().substring(1, 3)); } 
{white_space}     { /* ignore */ }

}

<STRING> {
  \"                             { yybegin(save_zzLexicalState); 
                                   return symbol("QUOTEDSTR",FullSym.QUOTEDSTR,string.toString()); }
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
		    			case YYINITIAL:    scannerStateStr = "startspec";    break;
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
