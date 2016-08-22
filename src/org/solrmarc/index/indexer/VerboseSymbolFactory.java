package org.solrmarc.index.indexer;

import org.apache.log4j.Logger;

//import org.apache.log4j.Logger;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class VerboseSymbolFactory extends ComplexSymbolFactory
{

    private final static Logger logger = Logger.getLogger(VerboseSymbolFactory.class);

    @Override
    public Symbol newSymbol(String name, int id, Object value)
    {
        Symbol sym = super.newSymbol(name, id, value);
        logger.debug("Returning symbol: " + name + " value is : " + value);
        return (sym);
    }

    @Override
    public Symbol newSymbol(String name, int id)
    {
        Symbol sym = super.newSymbol(name, id);
        logger.debug("Returning symbol: " + name);
        return (sym);
    }

    /**
     * newSymbol
     * creates a complex symbol with Location objects for left and right boundaries;
     * this is used for terminals with values!
     */
    public Symbol newSymbol(String name, int id, Location left, Location right, Object value)
    {
        Symbol sym = super.newSymbol(name, id, left, right, value);
        logger.debug("Returning symbol: " + name + " value is : " + value);
        return (sym);
    }

    /**
     * newSymbol
     * creates a complex symbol with Location objects for left and right boundaries;
     * this is used for terminals without values!
     */
    public Symbol newSymbol(String name, int id, Location left, Location right)
    {
        Symbol sym = super.newSymbol(name, id, left, right);
        logger.debug("Returning symbol: " + name);
        return (sym);
    }

//    public Symbol newSymbol(String name, int id, Symbol left, Object value)
//    {
//        Symbol sym = super.newSymbol(name, id, left, value);
//        System.err.println("Returning symbol: " + name);
//        return (sym);
//    }

    public Symbol newSymbol(String name, int id, Symbol left, Symbol right, Object value)
    {
        Symbol sym = super.newSymbol(name, id, left, right, value);
        logger.debug("Returning symbol: " + name + " value is : " + value);
        return (sym);
    }

    public Symbol newSymbol(String name, int id, Symbol left, Symbol right)
    {
        Symbol sym = super.newSymbol(name, id, left, right);
        logger.debug("Returning symbol: " + name);
        return (sym);
    }
}
