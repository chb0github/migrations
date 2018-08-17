/**
 *    Copyright 2010-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration;

import org.apache.ibatis.parsing.PropertyParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

/**
 * @author cbongiorno on 8/14/18.
 */
public final class TemplateReader extends Reader {

  private final Properties variables;

  private char[] currBuff = new char[0];

  private int pos = 0;

  private final BufferedReader delegate;

  public TemplateReader(Reader template, Map<String, Object> variables) {
    delegate = new BufferedReader(template);
    this.variables = new Properties();
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      this.variables.put(entry.getKey(), entry.getValue().toString());
    }
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (currBuff == null) {
      return -1;
    }

    if (pos >= currBuff.length) {
      pos = 0;
      String line = delegate.readLine();

      if (line != null) {
        String subLine = PropertyParser.parse(line, variables) + '\n';
        currBuff = subLine.toCharArray();
      } else {
        currBuff = null;
        return -1;
      }
    }

    int toCopy = Math.min(len, currBuff.length - pos);
    System.arraycopy(currBuff, pos, cbuf, off, toCopy);
    pos = pos + toCopy;
    return toCopy;
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }
}
