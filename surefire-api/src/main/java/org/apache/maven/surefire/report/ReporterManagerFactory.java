package org.apache.maven.surefire.report;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.surefire.testset.TestSetFailedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Creates ReporterManager instances for the providers.
 * <p/>
 * A ReporterManager and the underlying reporters are stateful objects. For safe concurrent usage
 * of the reporting infrastructure, each thread needs its own instance.
 * <p/>
 * This factory also ensures that runStarting/runCompleted is called on the FIRST reporterManger that
 * is allocated, but none of the subsequent managers.
 *
 * @author Jason van Zyl
 * @author Kristian Rosenvold (extracted factory)
 */
/** NOTE: The ReporterManagerFactory2 should take over for this once we build surefire with 2.7,
 * just merge it into this class, this is just a hack to be able to build 2.7 with 2.5 */
public class ReporterManagerFactory
{
    protected final List reportDefinitions;

    protected final ClassLoader surefireClassLoader;

    private final RunStatistics globalRunStatistics = new RunStatistics();

    private ReporterManager first;

    private final Object lock = new Object();


    public ReporterManagerFactory( List reportDefinitions, ClassLoader surefireClassLoader )
    {
        this.reportDefinitions = reportDefinitions;
        this.surefireClassLoader = surefireClassLoader;
    }


    public RunStatistics getGlobalRunStatistics()
    {
        return globalRunStatistics;
    }

    public ReporterManager createReporterManager()
        throws TestSetFailedException
    {
        final List reports = instantiateReports( reportDefinitions, surefireClassLoader );
        // Note, if we ever start making >1 reporter Managers, we have to aggregate run statistics
        // i.e. we cannot use a single "globalRunStatistics"
        final ReporterManager reporterManager = new ReporterManager( reports, globalRunStatistics );
        if ( first == null )
        {
            synchronized ( lock )
            {
                if ( first == null )
                {
                    first = reporterManager;
                    reporterManager.runStarting();
                }
            }
        }
        return reporterManager;
    }

    public void close()
    {
        synchronized ( lock )
        {
            if ( first != null )
            {
                first.runCompleted();
            }
        }
    }


    private List instantiateReports( List reportDefinitions, ClassLoader classLoader )
        throws TestSetFailedException
    {
        List reports = new ArrayList();

        for ( Iterator i = reportDefinitions.iterator(); i.hasNext(); )
        {
            Object[] definition = (Object[]) i.next();

            String className = (String) definition[0];
            Object[] params = (Object[]) definition[1];

            Reporter report = instantiateReport( className, params, classLoader );

            reports.add( report );
        }

        return reports;
    }

    private static Reporter instantiateReport( String className, Object[] params, ClassLoader classLoader )
        throws TestSetFailedException
    {
        try
        {
            return (Reporter) instantiateObject( className, params, classLoader );
        }
        catch ( ClassNotFoundException e )
        {
            throw new TestSetFailedException( "Unable to find class to create report '" + className + "'", e );
        }
        catch ( NoSuchMethodException e )
        {
            throw new TestSetFailedException(
                "Unable to find appropriate constructor to create report: " + e.getMessage(), e );
        }
    }

    private static Object instantiateObject( String className, Object[] params, ClassLoader classLoader )
        throws TestSetFailedException, ClassNotFoundException, NoSuchMethodException
    {
        Class clazz = classLoader.loadClass( className );

        Object object;
        try
        {
            if ( params != null )
            {
                Class[] paramTypes = new Class[params.length];

                for ( int j = 0; j < params.length; j++ )
                {
                    if ( params[j] == null )
                    {
                        paramTypes[j] = String.class;
                    }
                    else
                    {
                        paramTypes[j] = params[j].getClass();
                    }
                }

                Constructor constructor = clazz.getConstructor( paramTypes );

                object = constructor.newInstance( params );
            }
            else
            {
                object = clazz.newInstance();
            }
        }
        catch ( IllegalAccessException e )
        {
            throw new TestSetFailedException( "Unable to instantiate object: " + e.getMessage(), e );
        }
        catch ( InvocationTargetException e )
        {
            throw new TestSetFailedException( e.getTargetException().getMessage(), e.getTargetException() );
        }
        catch ( InstantiationException e )
        {
            throw new TestSetFailedException( "Unable to instantiate object: " + e.getMessage(), e );
        }
        return object;
    }
}