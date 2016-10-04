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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Helper methods for class instantiation through reflection.
 */
public final class ReflectionUtils {

    private ReflectionUtils() {}

    public static <T> T getInstance(String classKey) throws FalconException {
        return ReflectionUtils.<T>getInstanceByClassName(StartupProperties.get().getProperty(classKey));
    }

    public static <T> T getInstance(String classKey, Class<?> argCls, Object arg) throws FalconException {
        return ReflectionUtils.<T>getInstanceByClassName(StartupProperties.get().getProperty(classKey), argCls, arg);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInstanceByClassName(String clazzName) throws FalconException {
        try {
            Class<T> clazz = (Class<T>) ReflectionUtils.class.getClassLoader().loadClass(clazzName);
            try {
                return clazz.newInstance();
            } catch (IllegalAccessException e) {
                Method method = clazz.getMethod("get");
                return (T) method.invoke(null);
            }
        } catch (Exception e) {
            throw new FalconException("Unable to get instance for " + clazzName, e);
        }
    }

    /**
     * Invokes constructor with one argument.
     * @param clazzName - classname
     * @param argCls - Class of the argument
     * @param arg - constructor argument
     * @param <T> - instance type
     * @return Class instance
     * @throws FalconException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstanceByClassName(String clazzName, Class<?> argCls, Object arg) throws
        FalconException {
        try {
            Class<T> clazz = (Class<T>) ReflectionUtils.class.getClassLoader().loadClass(clazzName);
            Constructor<T> constructor = clazz.getConstructor(argCls);
            return constructor.newInstance(arg);
        } catch (Exception e) {
            throw new FalconException("Unable to get instance for " + clazzName, e);
        }
    }
}
