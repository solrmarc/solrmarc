package org.solrmarc.index.extractor.methodcall;


import java.util.Collection;
import java.util.List;

import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.utils.StringReader;

public abstract class AbstractMethodCallFactory extends AbstractValueExtractorFactory
{
    protected final MethodCallManager methodCallManager;
    protected boolean haveShownKnownMethods = false;

    public AbstractMethodCallFactory()
    {
        this(MethodCallManager.instance());
    }

    public AbstractMethodCallFactory(final MethodCallManager methodCallManager)
    {
        this.methodCallManager = methodCallManager;
    }

    public void addMethodsFromClasses(Collection<Class<?>> classes)
    {
        try
        {
            for (final Class<?> aClass : classes)
            {
                methodCallManager.add(aClass.newInstance());
            }
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, MethodCallContext context)
    {
        final AbstractExtractorMethodCall<?> methodCall = methodCallManager.getExtractorMethodCallForContext(context);
        if (methodCall instanceof MultiValueExtractorMethodCall)
        {
            return new MethodCallMultiValueExtractor((MultiValueExtractorMethodCall) methodCall,
                    context.getParameters());
        }
        else if (methodCall instanceof SingleValueExtractorMethodCall)
        {
            return new MethodCallSingleValueExtractor((SingleValueExtractorMethodCall) methodCall,
                    context.getParameters());
        }
        else if (methodCall == null && context.getObjectName() == null)
        {
            List<AbstractExtractorMethodCall<?>> matches = methodCallManager.getLoadedExtractorMixinsMatches(null, context.getMethodName(), context.getParameterTypes().length);
            if (matches.size() > 1)
            {
                throw new IndexerSpecException("Multiple methods with name: " + context.getMethodName() + " you must specify the class of the method you intend to use.  Known methods are: \n"
                        + methodCallManager.loadedExtractorMixinsToString(matches));
            }
            else if (matches.size() == 0)
            {
                List<AbstractExtractorMethodCall<?>> matchesParmWildcard = methodCallManager.getLoadedExtractorMixinsMatches(null, context.getMethodName(), -1);
                if (matchesParmWildcard.size() == 1)
                {
                    int num = (matchesParmWildcard.iterator().next().getNumParameters()-1);
                    throw new IndexerSpecException("Incorrect number of parameters to method: " + context.getMethodName() + " The known method "+methodCallManager.loadedExtractorMixinsToString(matchesParmWildcard)+
                                                   " requires "+ num  + " parameter" +((num == 1) ? "" : "s") + "\n");
                }
                else if (matchesParmWildcard.size() > 1)
                {
                    throw new IndexerSpecException("Multiple methods with name: " + context.getMethodName() + " but none of them require " + context.getParameterTypes().length + " parameters.  Known methods are: \n"
                            + methodCallManager.loadedExtractorMixinsToString(matchesParmWildcard));
                }
            }
            if (!haveShownKnownMethods)
            {
                haveShownKnownMethods = true;
                throw new IndexerSpecException("Unknown extractor method: " + context.toString() + ". Known methods are: \n"
                        + methodCallManager.loadedExtractorMixinsToString());
            }
            else
            {
                throw new IndexerSpecException("Unknown extractor method: " + context.toString());
            }
        }
        else
        {
            List<AbstractExtractorMethodCall<?>> matchesOtherContext = methodCallManager.getLoadedExtractorMixinsMatches(null, context.getMethodName(), context.getParameterTypes().length);
            if (matchesOtherContext.size() == 1)
            {
                String objName = matchesOtherContext.iterator().next().getObjectName();
                throw new IndexerSpecException("Method not found in specified class: " + context.getObjectName() + " A known method does exist in the class : " + objName + "\n");
            }
            List<AbstractExtractorMethodCall<?>> matchesParmWildcard = methodCallManager.getLoadedExtractorMixinsMatches(context.getObjectName(), context.getMethodName(), -1);
            if (matchesParmWildcard.size() == 1)
            {
                int num = (matchesParmWildcard.iterator().next().getNumParameters()-1);
                throw new IndexerSpecException("Incorrect number of parameters to method: " + context.getMethodName() + " The known method "+methodCallManager.loadedExtractorMixinsToString(matchesParmWildcard)+
                        " requires "+ num  + " parameter" +((num == 1) ? "" : "s") + "\n");
            }
            List<AbstractExtractorMethodCall<?>> matchesOtherContextParmWildCard = methodCallManager.getLoadedExtractorMixinsMatches(null, context.getMethodName(), -1);
            if (matchesOtherContextParmWildCard.size() == 1)
            {
                AbstractExtractorMethodCall<?> match = matchesOtherContextParmWildCard.iterator().next();
                throw new IndexerSpecException("Specified method with name: " + context.getMethodName() + " not found.  Closest match is: \n"
                        + methodCallManager.loadedExtractorMixinsToString(matchesOtherContextParmWildCard));
            }
            else if (matchesOtherContextParmWildCard.size() > 1)
            {
                throw new IndexerSpecException("Multiple methods with name: " + context.getMethodName() + " but none of them require " + context.getParameterTypes().length + " parameters.  Known methods are: \n"
                        + methodCallManager.loadedExtractorMixinsToString(matchesOtherContextParmWildCard));
            }
            else if (!haveShownKnownMethods)
            {
                haveShownKnownMethods = true;
                throw new IndexerSpecException("Unknown extractor method: " + context.toString() + ". Known methods are: \n"
                        + methodCallManager.loadedExtractorMixinsToString());
            }
            else
            {
                throw new IndexerSpecException("Unknown extractor method: " + context.toString());
            }

        }
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration)
    {
        MethodCallContext context = MethodCallContext.parseContextFromExtractorSpecification(mappingConfiguration);
        return createExtractor(solrFieldName, context);
    }
    
    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final String[] parts)
    {
        MethodCallContext context = MethodCallContext.parseContextFromExtractorParts(parts);
        return createExtractor(solrFieldName, context);
    }
}
