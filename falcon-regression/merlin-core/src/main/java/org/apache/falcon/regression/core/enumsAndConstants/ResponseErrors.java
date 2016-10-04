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

package org.apache.falcon.regression.core.enumsAndConstants;

/** Class containing response errors of rest requests. */
public enum ResponseErrors {

    PROCESS_NOT_FOUND("(PROCESS) not found"),
    UNPARSEABLE_DATE("Start and End dates cannot be empty for Instance POST apis"),
    START_BEFORE_SCHEDULED("is before the entity was scheduled"),
    PROCESS_INVALID_RANGE("is not in validity range of process"),
    PROCESS_INSTANCE_FAULT("is not a valid instance time on cluster"),
    FEED_INVALID_RANGE("is not in validity range for Feed"),
    FEED_INSTANCE_FAULT("is not a valid instance for the  feed"),
    INVALID_INSTANCE_TIME("not a valid instance");

    private String error;

    ResponseErrors(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

}
