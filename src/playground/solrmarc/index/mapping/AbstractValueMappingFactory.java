package playground.solrmarc.index.mapping;


public abstract class AbstractValueMappingFactory {
    public abstract boolean canHandle(String mappingConfiguration);

    public abstract AbstractSingleValueMapping createSingleValueMapping(String mappingConfiguration);

    public abstract AbstractMultiValueMapping createMultiValueMapping(String mappingConfiguration);
}
