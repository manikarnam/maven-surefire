package org.apache.maven.surefire.its;

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

import org.apache.maven.surefire.its.fixture.MavenLauncher;
import org.apache.maven.surefire.its.fixture.SurefireJUnit4IntegrationTestCase;
import org.apache.maven.surefire.its.fixture.SurefireLauncher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.junit.Assume.assumeTrue;

/**
 * Test a directory with an umlaut
 *
 * @author <a href="mailto:dfabulich@apache.org">Dan Fabulich</a>
 */
public class UmlautDirIT
    extends SurefireJUnit4IntegrationTestCase
{
    private String localRepo;

    @Before
    public void backupLocalRepo()
    {
        localRepo = System.getProperty( "maven.repo.local" );
    }

    @After
    public void restoreLocalRepo()
    {
        if ( localRepo == null )
        {
            System.clearProperty( "maven.repo.local" );
        }
        else
        {
            System.setProperty( "maven.repo.local", localRepo );
        }
    }

    @Test
    public void surefire1617()
    {
        assumeTrue( IS_OS_LINUX );
        unpackWithNewLocalRepo()
                .executeTest()
                .verifyErrorFreeLog()
                .assertTestSuiteResults( 1, 0, 0, 0 );
    }

    @Test
    public void testUmlaut()
        throws Exception
    {
        unpackToGermanUmplautDirectory( "1" )
                .executeTest()
                .verifyErrorFreeLog()
                .assertTestSuiteResults( 1, 0, 0, 0 );
    }

    @Test
    public void testUmlautIsolatedClassLoader()
        throws Exception
    {
        unpackToGermanUmplautDirectory( "2" )
                .useSystemClassLoader( false )
                .executeTest()
                .assertTestSuiteResults( 1, 0, 0, 0 );
    }

    private SurefireLauncher unpackToGermanUmplautDirectory( String postfix )
        throws IOException
    {
        SurefireLauncher unpack = unpack( "junit-pathWithUmlaut" );
        MavenLauncher maven = unpack.maven();

        File dest = new File( maven.getUnpackedAt().getParentFile().getPath(), "/junit-pathWith\u00DCmlaut_" + postfix );
        maven.moveUnpackTo( dest );
        return unpack;
    }

    private SurefireLauncher unpackWithNewLocalRepo()
    {
        File target = new File( System.getProperty( "user.dir" ), "target" );
        File newLocalRepo = new File( target, "local repo for : SUREFIRE-1617" );
        //noinspection ResultOfMethodCallIgnored
        newLocalRepo.mkdir();
        System.setProperty( "maven.repo.local", newLocalRepo.getPath() );
        return unpack( "junit-pathWithUmlaut" );
    }
}
