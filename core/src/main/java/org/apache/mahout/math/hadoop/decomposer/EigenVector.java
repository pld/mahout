/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.math.hadoop.decomposer;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

import java.util.regex.Pattern;

/**
 * TODO this is a horrible hack.  Make a proper writable subclass also.
 */
public class EigenVector extends DenseVector {

  private static final Pattern EQUAL_PATTERN = Pattern.compile(" = ");
  private static final Pattern PIPE_PATTERN = Pattern.compile("|");

  private final String name;

  public EigenVector(Vector v, double eigenValue, double cosAngleError, int order) {
    super(v instanceof DenseVector ? (DenseVector) v : new DenseVector(v), false);
    name = "e|" + order + "| = |" + eigenValue + "|, err = " + cosAngleError;
  }

  public double getEigenValue() {
    return parseMetaData()[1];
  }

  public double getCosAngleError() {
    return parseMetaData()[2];
  }

  public int getIndex() {
    return (int)parseMetaData()[0];
  }

  protected double[] parseMetaData() {
    double[] m = new double[3];
    String[] s = EQUAL_PATTERN.split(name);
    m[0] = Double.parseDouble(PIPE_PATTERN.split(s[0])[1]);
    m[1] = Double.parseDouble(PIPE_PATTERN.split(s[1])[1]);
    m[2] = Double.parseDouble(s[2].substring(1));
    return m;
  }

}
