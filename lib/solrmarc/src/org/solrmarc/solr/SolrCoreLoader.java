package org.solrmarc.solr;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.xml.sax.InputSource;

public class SolrCoreLoader
{
    
//    public void initialize(String libDir) throws Exception {
//        File dependencyDirectory = new File(libDir);
//        File[] files = dependencyDirectory.listFiles();
//        ArrayList<URL> urls = new ArrayList<URL>();
//        for (int i = 0; i < files.length; i++) {
//            if (files[i].getName().endsWith(".jar")) {
//            urls.add(files[i].toURL());
//            //urls.add(files[i].toURI().toURL());
//            }
//        }
//        classLoader = new JarFileClassLoader("Scheduler CL" + System.currentTimeMillis(), 
//            urls.toArray(new URL[urls.size()]), 
//            GFClassLoader.class.getClassLoader());
//        }
    
    public static SolrCoreProxy loadCore(String solrCoreDir, String solrDataDir, String solrCoreName, Logger logger)
    {
        Object solrCore = null;
        Object genericCoreContainerObject = null;
        SolrCoreProxy solrCoreProxy = null;
            
        try{
            boolean has_1_3_libs = false;
            boolean has_1_2_libs = false;
            boolean has_3_1_libs = false;
            System.setProperty("solr.solr.home", solrCoreDir);
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
                Class<?> indexSchemaClass = Class.forName("org.apache.solr.schema.IndexSchema");
                Class<?> solrConfigClass = Class.forName("org.apache.solr.core.SolrConfig");
                Constructor<?> solrConfigConstructor = solrConfigClass.getConstructor(String.class, String.class, InputStream.class);
                Object solrConfig = solrConfigConstructor.newInstance(solrCoreDir, "solrconfig.xml", null);
                // solrCore = new SolrCore("Solr", solrDataDir, solrConfig, null);
                Class<?> solrCoreClass = Class.forName("org.apache.solr.core.SolrCore");
                Constructor<?> coreConstructor = solrCoreClass.getConstructor(String.class, String.class, solrConfigClass, indexSchemaClass);
                solrCore = coreConstructor.newInstance("Solr", solrDataDir, solrConfig, null);
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
                    genericCoreContainerObject = coreContainerConstructor.newInstance(solrCoreDir, multicoreConfigFile);
                    
                    // cc.getCore(solrCoreName);
                    Method getCoreMethod = coreContainerClass.getMethod("getCore", String.class);
                    solrCore = getCoreMethod.invoke(genericCoreContainerObject, solrCoreName);
                }
                else  // non-multicore Solr 1.3 installation 
                {
                    if (solrDataDir == null) 
                    {
                        solrDataDir = solrCoreDir + "/" + "data";
                    }
                    System.setProperty("solr.data.dir", solrDataDir);
                    
                    Class<?> solrConfigClass = Class.forName("org.apache.solr.core.SolrConfig");
                    Constructor<?> solrConfigConstructor = null;
                    try {
                        solrConfigConstructor = solrConfigClass.getConstructor(String.class, String.class, InputStream.class);
                    }
                    catch (NoSuchMethodException e)
                    {
                        solrConfigConstructor = solrConfigClass.getConstructor(String.class, String.class, InputSource.class);
                    }
                    Object solrConfig = solrConfigConstructor.newInstance(solrCoreDir, "solrconfig.xml", null);
                    //SolrConfig solrConfig = new SolrConfig(solrCoreDir, "solrconfig.xml", null);
                    FileInputStream schemaFile = new FileInputStream(solrCoreDir+"/conf/schema.xml");
                    
                    //cc = new CoreContainer();
                    Class<?> coreContainerClass = Class.forName("org.apache.solr.core.CoreContainer");
                    Constructor<?> coreContainerConstructor = coreContainerClass.getConstructor();
                    Object genericCoreContainerObject1 = coreContainerConstructor.newInstance();
                    
                    //CoreDescriptor desc = new CoreDescriptor(cc, "Solr", solrCoreDir+"/conf");
                    Class<?> coreDescClass = Class.forName("org.apache.solr.core.CoreDescriptor");
                    Constructor<?> coreDescCtor = coreDescClass.getConstructor(coreContainerClass, String.class, String.class);
                    Object genericCoreDesc = coreDescCtor.newInstance(genericCoreContainerObject1, "Solr", solrCoreDir+"/conf");
                    
                    Class<?> indexSchemaClass = Class.forName("org.apache.solr.schema.IndexSchema");
                    Constructor<?> IndexSchemaConstructor = null;
                    try {
                        IndexSchemaConstructor = indexSchemaClass.getConstructor(solrConfigClass, String.class, InputStream.class);
                    }
                    catch (NoSuchMethodException e)
                    {
                        IndexSchemaConstructor = indexSchemaClass.getConstructor(solrConfigClass, String.class, InputSource.class);
                    }
                    Object  solrSchema = IndexSchemaConstructor.newInstance(solrConfig, "schema.xml", null);
                    
                    // solrCore = new SolrCore(solrCoreName, solrDataDir, solrConfig, solrSchema, desc);  
                    Class<?> solrCoreClass = Class.forName("org.apache.solr.core.SolrCore");
                    Constructor<?> coreConstructor = solrCoreClass.getConstructor(String.class, String.class, solrConfigClass, indexSchemaClass, coreDescClass);
                    solrCore = coreConstructor.newInstance(solrCoreName, solrDataDir, solrConfig, solrSchema, genericCoreDesc);
                }
            }
            else // can't find any solr libraries
            {
                System.err.println("Error: SolrMarc cannot find any Solr libraries");
                System.err.println("Error: SolrMarc was built expecting solr libraries to be referenced at runtime, but none were found. ");               
                logger.error("Error: SolrMarc cannot find any Solr libraries");
                logger.error("Error: SolrMarc was built expecting solr libraries to be referenced at runtime, but none were found. ");               
                System.exit(1);               
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: Problem instantiating SolrCore");
            logger.error("Error: Problem instantiating SolrCore");
            e.printStackTrace();
            System.exit(1);
        }
        solrCoreProxy = new SolrCoreProxy(solrCore, genericCoreContainerObject);
        return(solrCoreProxy);
    }

    public static SolrProxy loadEmbeddedCore(String solrCoreDir, String solrDataDir, String solrCoreName, boolean useBinaryRequestHandler, Logger logger)
    {
        Object solrCore = null;
        Object solrServer = null;
        Object genericCoreContainerObject = null;
        SolrCoreProxy solrCoreProxy = null;
        File multicoreConfigFile = new File(solrCoreDir + "/solr.xml");
        
        try{
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
                genericCoreContainerObject = coreContainerConstructor.newInstance(solrCoreDir, multicoreConfigFile);
                
                // cc.getCore(solrCoreName);
                Method getCoreMethod = coreContainerClass.getMethod("getCore", String.class);
                Class<?> solrCoreClass = Class.forName("org.apache.solr.core.SolrCore");
                solrCore = getCoreMethod.invoke(genericCoreContainerObject, solrCoreName);
                
                if (useBinaryRequestHandler)
                { 
                    Class<?> embeddedSolrServerClass = Class.forName("org.solrmarc.solr.embedded.SolrServerEmbeddedImpl");
                    Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(Object.class, Object.class);
                    solrServer = embeddedSolrServerConstructor.newInstance(solrCore, genericCoreContainerObject);
                }
                else
                {
                    try {
                        Class<?> embeddedSolrServerClass = Class.forName("org.apache.solr.client.solrj.embedded.EmbeddedSolrServer");
                        Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(coreContainerClass, String.class);
                        solrServer = embeddedSolrServerConstructor.newInstance(genericCoreContainerObject, solrCoreName);
                    }
                    catch (Exception e)
                    {
                        if (e instanceof ClassNotFoundException || (e instanceof InvocationTargetException && 
                                e.getCause() instanceof java.lang.NoClassDefFoundError) )
                        {
                            logger.error("Error loading class:org.apache.solr.client.solrj.embedded.EmbeddedSolrServer : " + e.getCause());
                            Class<?> embeddedSolrServerClass = Class.forName("org.solrmarc.solr.embedded.SolrServerEmbeddedImpl");
                            Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(Object.class, Object.class);
                            solrServer = embeddedSolrServerConstructor.newInstance(solrCore, genericCoreContainerObject);
                        }
                        else
                        {
                            logger.error("Error loading class:org.apache.solr.client.solrj.embedded.EmbeddedSolrServer : " + e.getCause());
                            e.printStackTrace();
                        }
                    }
                }
                
                
                SolrServerProxy solrServerProxy = new SolrServerProxy((SolrServer)solrServer);
                return(solrServerProxy);
            }
            else  // non-multicore Solr 1.3 installation 
            {
                if (solrDataDir == null) 
                {
                    solrDataDir = solrCoreDir + "/" + "data";
                }
                System.setProperty("solr.data.dir", solrDataDir);
                Class<?> solrConfigClass = Class.forName("org.apache.solr.core.SolrConfig");
                Constructor<?> solrConfigConstructor = null;
                try {
                    solrConfigConstructor = solrConfigClass.getConstructor(String.class, String.class, InputStream.class);
                }
                catch (NoSuchMethodException e)
                {
                    solrConfigConstructor = solrConfigClass.getConstructor(String.class, String.class, InputSource.class);
                }
                Object solrConfig = solrConfigConstructor.newInstance(solrCoreDir, "solrconfig.xml", null);
                //SolrConfig solrConfig = new SolrConfig(solrCoreDir, "solrconfig.xml", null);
      //          FileInputStream schemaFile = new FileInputStream(solrCoreDir+"/conf/schema.xml");
                
                //cc = new CoreContainer();
                Class<?> coreContainerClass = Class.forName("org.apache.solr.core.CoreContainer");
                Constructor<?> coreContainerConstructor = coreContainerClass.getConstructor();
                Object genericCoreContainerObject1 = coreContainerConstructor.newInstance();
                
                //CoreDescriptor desc = new CoreDescriptor(cc, "Solr", solrCoreDir+"/conf");
                Class<?> coreDescClass = Class.forName("org.apache.solr.core.CoreDescriptor");
                Constructor<?> coreDescCtor = coreDescClass.getConstructor(coreContainerClass, String.class, String.class);
                Object genericCoreDesc = coreDescCtor.newInstance(genericCoreContainerObject1, "Solr", solrCoreDir+"/conf");
                
                Class<?> indexSchemaClass = Class.forName("org.apache.solr.schema.IndexSchema");
                Constructor<?> IndexSchemaConstructor = null;
                try {
                    IndexSchemaConstructor = indexSchemaClass.getConstructor(solrConfigClass, String.class, InputStream.class);
                }
                catch (NoSuchMethodException e)
                {
                    IndexSchemaConstructor = indexSchemaClass.getConstructor(solrConfigClass, String.class, InputSource.class);
                }
                Object  solrSchema = IndexSchemaConstructor.newInstance(solrConfig, "schema.xml", null);
                
                // solrCore = new SolrCore(solrCoreName, solrDataDir, solrConfig, solrSchema, desc);  
                Class<?> solrCoreClass = Class.forName("org.apache.solr.core.SolrCore");
                Constructor<?> coreConstructor = solrCoreClass.getConstructor(String.class, String.class, solrConfigClass, indexSchemaClass, coreDescClass);
                solrCore = coreConstructor.newInstance(solrCoreName, solrDataDir, solrConfig, solrSchema, genericCoreDesc);
                
                // Registers a SolrCore descriptor in the container registry using the specified name
                coreContainerClass.getMethod("register", String.class, solrCoreClass, boolean.class).invoke(genericCoreContainerObject1, "Solr", solrCore, false);
                
                if (useBinaryRequestHandler)
                { 
                    Class<?> embeddedSolrServerClass = Class.forName("org.solrmarc.solr.embedded.SolrServerEmbeddedImpl");
                    Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(Object.class, Object.class);
                    solrServer = embeddedSolrServerConstructor.newInstance(solrCore, genericCoreContainerObject);
                }
                else
                {
                    try {
                        Class<?> servletRequestClass = Class.forName("javax.servlet.ServletRequest");
                        Class<?> embeddedSolrServerClass = Class.forName("org.apache.solr.client.solrj.embedded.EmbeddedSolrServer");
                        Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(coreContainerClass, String.class);
                        solrServer = embeddedSolrServerConstructor.newInstance(genericCoreContainerObject1, "Solr");
                    }
                    catch (Exception e)
                    {
                        if (e instanceof ClassNotFoundException || (e instanceof InvocationTargetException && 
                            e.getCause() instanceof java.lang.NoClassDefFoundError) )
                        {
                            logger.error("Error loading class:org.apache.solr.client.solrj.embedded.EmbeddedSolrServer : " + e.getCause());
                            Class<?> embeddedSolrServerClass = Class.forName("org.solrmarc.solr.embedded.SolrServerEmbeddedImpl");
                            Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(Object.class, Object.class);
                            solrServer = embeddedSolrServerConstructor.newInstance(solrCore, genericCoreContainerObject);
                        }
                        else
                        {
                            logger.error("Error loading class:org.apache.solr.client.solrj.embedded.EmbeddedSolrServer : " + e.getCause());
                            e.printStackTrace();
                        }
                    }
                }
                
                SolrServerProxy solrServerProxy = new SolrServerProxy((SolrServer)solrServer);
                return(solrServerProxy);
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: Problem instantiating SolrCore");               
            logger.error("Error: Problem instantiating SolrCore");
            e.printStackTrace();
            System.exit(1);
        }

        // TODO Auto-generated method stub
        return null;
    }


}
