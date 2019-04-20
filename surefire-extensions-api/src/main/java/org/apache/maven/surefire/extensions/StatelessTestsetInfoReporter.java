package org.apache.maven.surefire.extensions;

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

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.report.TestSetReportEntry;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Extension listener for stateless file and console reporter of test-set.
 * Signatures can be changed between major, minor versions or milestones.
 *
 * @author <a href="mailto:tibordigana@apache.org">Tibor Digana (tibor17)</a>
 * @since 3.0.0-M4
 * @param <R> report entry type, see <em>WrappedReportEntry</em> from module the <em>maven-surefire-common</em>
 * @param <S> test-set statistics, see <em>TestSetStats</em> from module the <em>maven-surefire-common</em>
 */
public abstract class StatelessTestsetInfoReporter<R extends TestSetReportEntry, S>
{
    private boolean disable;

    public abstract StatelessTestsetInfoConsoleReportEventListener<R, S> createListener( ConsoleLogger logger );

    public abstract StatelessTestsetInfoFileReportEventListener<R, S> createListener( File reportsDirectory,
                                                                                      String reportNameSuffix,
                                                                                      Charset encoding );

    public abstract Object clone( ClassLoader target );

    public boolean isDisable()
    {
        return disable;
    }

    public void setDisable( boolean disable )
    {
        this.disable = disable;
    }

    @Override
    public String toString()
    {
        return "StatelessTestsetInfoReporter{disable=" + disable + '}';
    }
}
