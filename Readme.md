This project is based on code written by Oliver Obenland, (See https://github.com/oobenland/SolrMarc-Indexer-Tests)  The key design improvement Oliver created is to essentially compile the indexing specification once, and then apply that "compiled" version to each of the records that need indexing.    I have taken his code and added handling of the basic field specification of SolrMarc   (such as:    title_display = 245abnp  )  The code is now to a proof-of-concept phase that can handle many of those sort of specification strings, and can even support some extensions to those specifications.   At present, part of the processing of the indexing specifications is performed via a parser specification (CUP and JFlex) which should make defining and handling more complex specifications simpler.

Included with this project is a Swing-based interactive interface that could eventually be used to develop, modify, extend and debug a set of indexing specifications, but for now it can be used to see how some of the new features will work.


This project contains the implementation of an idea how to improve SolrMarc by improving
performance, extendability and stability.

# Overview

The indexer is divided in a compile time and a runtime. The compile time is for loading configurations and 
translate/compile them to small indexer tasks with minimal functionality. The runtime loads records from input files,
uses the small indexer tasks to extract data and send the data to Solr.

## Compile time

This is mainly made out of factories. Each Factory is for one type of import configuration of the indexer properties 
(e.g marc.properties or marc_local.properties). Such a factory parses the configuration and creates a small indexer task.
A factory is not a singleton but only one instance of this factory will be used, so each factory can build a cache or 
share information between indexer tasks. After the all configurations are compiled to tasks the factories will not 
be needed anymore and will be collected by the Garbage Collector. A task is not allowed to own an instance of its factory.
Every single bit of calculation which can be done by the factory is a good bit of calculation. Everything which can be
preprocessed should be done by the factory, not by the indexer task.


## Runtime

At this point only the indexer task exists. No factories, no properties, no unnecessary processing.
The input file gets read and for each record all indexer tasks will be called to create a new document.

# Indexer task

A task is represented by the AbstractValueIndexer class and is a composition of three parts.

- Extractor: reads data from a record
- Mapping: translates the data by e.g mapping one value to another or by using a regex to extract a value.
- Collector: transforms the data by e.g joining multiple strings to one string or by splitting a string in parts.

Each indexer task will generate the data of one solr field.
