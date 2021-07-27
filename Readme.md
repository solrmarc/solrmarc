# Overview   ![Build Status](https://github.com//solrmarc/solrmarc/actions/workflows/ant.yml/badge.svg?branch=master)

SolrMarc is designed to read MARC records and to extract data from those records to build an Apache Solr index. It relies on the library [Marc4j](https://github.com/marc4j/marc4j) for reading MARC records and then uses a user-provided indexing specification to determine what fields are to be created for the Solr input document, and where that data should be extracted from, lastly it uses the SolrJ library for sending the Solr input documents to the Solr index. 

As of version 3.0 the program has been completely re-written, based on code written by Oliver Obenland, (See https://github.com/oobenland/SolrMarc-Indexer-Tests)  
The key design improvement Oliver created is to essentially compile the indexing specification once, and then apply 
that "compiled" version to each of the records that need indexing.    I have taken his code and added handling of 
the basic field specification of SolrMarc   (such as:    `title_display = 245abnp`  )  via a parser specification
(CUP and JFlex) which makes defining and handling more complex specifications simpler.   

The goal of the design is a program which operates much the same as the earlier versions of SolrMarc, including being able to process index specifications that worked with previous versions and produce substantially the same Solr records. But with the further goals of operating much faster and supporting a richer superset of features in the index specification language.

Included with this project is a Swing-based interactive interface that could eventually be used to develop, modify, extend
and debug a set of indexing specifications, but for now it can be used to see how some of the new features will work.

A more in-depth description of the differences in this new version can be found in the [Wiki](https://github.com/solrmarc/solrmarc/wiki), as well as information on how to install the program, how to create an index specification, how to run the program with that specification.

Additionally there is some information there about the code and design of the program for those that might be interested in contributing to the project. 
