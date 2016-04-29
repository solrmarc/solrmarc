package playground.solrmarc.index.indexer;

//import org.apache.log4j.Logger;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;

public class VerboseSymbolFactory extends ComplexSymbolFactory
{

//    private final static Logger logger = Logger.getLogger(ValueIndexerFactory.class);

    @Override
    public Symbol newSymbol(String name, int id, Object value)
    {
        Symbol sym = super.newSymbol(name, id, value);
        System.err.println("Returning symbol: " + name + " value is : " + value);
        return (sym);
    }

    @Override
    public Symbol newSymbol(String name, int id)
    {
        Symbol sym = super.newSymbol(name, id);
        System.err.println("Returning symbol: " + name);
        return (sym);
    }

}
