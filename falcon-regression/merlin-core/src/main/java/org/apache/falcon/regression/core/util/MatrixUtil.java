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

package org.apache.falcon.regression.core.util;

import org.apache.commons.lang.ArrayUtils;
import org.testng.Assert;

import java.util.Arrays;

/**
 * Util class for matrix operations.
 */
public final class MatrixUtil {
    private MatrixUtil() {
        throw new AssertionError("Instantiating utility class...");
    }

    /**
     * Cross product many arrays.
     * @param firstArray first array that you want to cross product
     * @param otherArrays other arrays that you want to cross product
     * @return cross product
     */
    public static Object[][] crossProduct(Object[] firstArray, Object[]... otherArrays) {
        if (otherArrays == null || otherArrays.length == 0) {
            Object[][] result = new Object[firstArray.length][1];
            for (int i = 0; i < firstArray.length; ++i) {
                result[i][0] = firstArray[i];
            }
            return result;
        }
        // computing cross product for the rest of the arrays
        Object[][] restArray = new Object[otherArrays.length-1][];
        System.arraycopy(otherArrays, 1, restArray, 0, otherArrays.length - 1);
        Object[][] restCrossProduct = crossProduct(otherArrays[0], restArray);
        //creating and initializing result array
        Object[][] result = new Object[firstArray.length * restCrossProduct.length][];
        for(int i = 0; i < result.length; ++i) {
            result[i] = new Object[otherArrays.length + 1];
        }
        //doing the final cross product
        for (int i = 0; i < firstArray.length; ++i) {
            for (int j = 0; j < restCrossProduct.length; ++j) {
                //computing one row of result
                final int rowIdx = i * restCrossProduct.length + j;
                result[rowIdx][0] = firstArray[i];
                System.arraycopy(restCrossProduct[j], 0, result[rowIdx], 1, otherArrays.length);
            }
        }
        return result;
    }

    public static Object[][] append(Object[][] arr1, Object[][] arr2) {
        Assert.assertFalse(ArrayUtils.isEmpty(arr1), "arr1 can't be empty:"
            + Arrays.deepToString(arr1));
        Assert.assertFalse(ArrayUtils.isEmpty(arr2), "arr2 can't be empty:"
            + Arrays.deepToString(arr2));
        Assert.assertEquals(arr1[0].length, arr2[0].length, "Array rows are not compatible. "
            + "row of first array: " + Arrays.deepToString(arr1[0])
            + "row of second array: " + Arrays.deepToString(arr2[0]));
        return (Object[][]) ArrayUtils.addAll(arr1, arr2);
    }

    /**
     * Cross product many arrays.
     * @param firstArray first array that you want to cross product
     * @param otherArrays other arrays that you want to cross product
     * @return cross product
     */
    public static Object[][] crossProductNew(Object[] firstArray, Object[][]... otherArrays) {
        return crossProduct(firstArray, otherArrays);
    }
}
