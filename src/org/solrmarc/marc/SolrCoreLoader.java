package org.solrmarc.marc;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.IndexSchema;

public class SolrCoreLoader
{
    static SolrCore loadCore(String solrCoreDir, String solrDataDir, String solrCoreName, Logger logger)
    {
        SolrCore solrCore = null;
            
        try{
            boolean has_1_3_libs = false;
            boolean has_1_2_libs = false;
            try
            {
                Class<?> areLibraries_1_3 = Class.forName("org.apache.solr.core.CoreContainer");
                has_1_3_libs = true;
/*
                String className = "org.apache.solr.core.CoreContainer".replace('.', '/');
                String classJar = areLibraries_1_3.getResource( "/" + className + ".class").toString();
                System.out.println("className is " + className);
                System.out.println("classJar is " + classJar);
*/
            }
            catch (ClassNotFoundException e1)
            {
                //  catch and release
            }           
            try
            {
                Class<?> areLibraries_1_2 = Class.forName("org.apache.solr.core.MultiCore");
                has_1_2_libs = true;
/*
                String className = "org.apache.solr.core.MultiCore".replace('.', '/');
                String classJar = areLibraries_1_2.getResource( "/" + className + ".class").toString();
                System.out.println("className is " + className);
                System.out.println("classJar is " + classJar);
*/
            }
            catch (ClassNotFoundException e1)
            {
                //  catch and release
            }           
            if (has_1_2_libs && has_1_3_libs)
            {
                System.err.println("Error: Program has access to both Solr 1.2 libraries and Solr 1.3 libraries");               
                logger.error("Error: Program has access to both Solr 1.2 libraries and Solr 1.3 libraries");               
                System.exit(1);               
            }
            if (has_1_2_libs)
            {
                if (solrDataDir == null) solrDataDir = solrCoreDir + "/data";
                System.setProperty("solr.data.dir", solrDataDir);
                SolrConfig solrConfig = new SolrConfig(solrCoreDir, "solrconfig.xml", null);
                // solrCore = new SolrCore("Solr", solrDataDir, solrConfig, null);
                Class<?> solrCoreClass = Class.forName("org.apache.solr.core.SolrCore");
                Constructor<?> coreConstructor = solrCoreClass.getConstructor(String.class, String.class, SolrConfig.class, IndexSchema.class);
                solrCore = (SolrCore)coreConstructor.newInstance("Solr", solrDataDir, solrConfig, null);
            }
            else if (has_1_3_libs) 
            {   
                File multicoreConfigFile = new File(solrCoreDir + "/solr.xml");
                
                if (multicoreConfigFile.exists())
                {
                    // multicore Solr 1.3 installation 
                    if (solrDataDir == null) 
                    {
                        solrDataDir = solrCoreDir + "/" + solrCoreName;
                    }
                    System.setProperty("solr.data.dir", solrDataDir);
                    logger.info("Using the data directory of: " + solrDataDir);
        
                    logger.info("Using the multicore schema file at : " + multicoreConfigFile.getAbsolutePath());
                    logger.info("Using the " + solrCoreName + " core");
                    
                    // cc = new org.apache.solr.core.CoreContainer(solrCoreDir, multicoreConfigFile);
                    Class<?> coreContainerClass = Class.forName("org.apache.solr.core.CoreContainer");
                    Constructor<?> coreContainerConstructor = coreContainerClass.getConstructor(String.class, File.class);
                    Object genericCoreContainerObject = coreContainerConstructor.newInstance(solrCoreDir, multicoreConfigFile);
                    
                    // cc.getCore(solrCoreName);
                    Method getCoreMethod = coreContainerClass.getMethod("getCore", String.class);
                    solrCore = (SolrCore)getCoreMethod.invoke(genericCoreContainerObject, solrCoreName);
                }
                else  // non-multicore Solr 1.3 installation 
                {
                    if (solrDataDir == null) 
                    {
                        solrDataDir = solrCoreDir + "/" + "data";
                    }
                    System.setProperty("solr.data.dir", solrDataDir);
                    SolrConfig solrConfig = new SolrConfig(solrCoreDir, "solrconfig.xml", null);
                    FileInputStream schemaFile = new FileInputStream(solrCoreDir+"/conf/schema.xml");
                    
                    //cc = new CoreContainer();
                    Class<?> coreContainerClass = Class.forName("org.apache.solr.core.CoreContainer");
                    Constructor<?> coreContainerConstructor = coreContainerClass.getConstructor();
                    Object genericCoreContainerObject = coreContainerConstructor.newInstance();
                    
                    //CoreDescriptor desc = new CoreDescriptor(cc, "Solr", solrCoreDir+"/conf");
                    Class<?> coreDescClass = Class.forName("org.apache.solr.core.CoreDescriptor");
                    Constructor<?> coreDescCtor = coreDescClass.getConstructor(coreContainerClass, String.class, String.class);
                    Object genericCoreDesc = coreDescCtor.newInstance(genericCoreContainerObject, "Solr", solrCoreDir+"/conf");
                    
                    IndexSchema solrSchema = new IndexSchema(solrConfig, "Solr", schemaFile);
                    
                    // solrCore = new SolrCore(solrCoreName, solrDataDir, solrConfig, solrSchema, desc);  
                    Class<?> solrCoreClass = Class.forName("org.apache.solr.core.SolrCore");
                    Constructor<?> coreConstructor = solrCoreClass.getConstructor(String.class, String.class, SolrConfig.class, IndexSchema.class, coreDescClass);
                    solrCore = (SolrCore)coreConstructor.newInstance(solrCoreName, solrDataDir, solrConfig, solrSchema, genericCoreDesc);
                }
            }   
        }
        catch (Exception e)
        {
            System.err.println("Error: Problem instantiating SolrCore");               
            logger.error("Error: Problem instantiating SolrCore");
            e.printStackTrace();
            System.exit(1);
        }
        return(solrCore);
    }

}
