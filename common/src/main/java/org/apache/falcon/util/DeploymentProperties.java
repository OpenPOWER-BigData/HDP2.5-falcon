/**
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.falcon.util;

import org.apache.falcon.FalconException;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Application deployment properties. particularly relating to
 * whether the server is in embedded mode or distributed mode.
 */
public final class DeploymentProperties extends ApplicationProperties {
    private static final String PROPERTY_FILE = "deploy.properties";

    private static final AtomicReference<DeploymentProperties> INSTANCE =
            new AtomicReference<>();

    private DeploymentProperties() throws FalconException {
        super();
    }

    @Override
    protected String getPropertyFile() {
        return PROPERTY_FILE;
    }

    public static Properties get() {
        try {
            if (INSTANCE.get() == null) {
                INSTANCE.compareAndSet(null, new DeploymentProperties());
            }
            return INSTANCE.get();
        } catch (FalconException e) {
            throw new RuntimeException("Unable to read application " + "startup properties", e);
        }
    }
}
