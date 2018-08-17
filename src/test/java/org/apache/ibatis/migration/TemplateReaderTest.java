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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cbongiorno on 8/16/18.
 */
public class TemplateReaderTest {

  private TemplateReader reader;

  @Before
  public void init() {
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("line", "value");
    reader = new TemplateReader(new InputStreamReader(
        this.getClass().getResourceAsStream("/org/apache/ibatis/migration/TemplateReaderTest.sql")), props);
  }

  @Test
  public void readLines() throws Exception {
    BufferedReader br = new CommentStrippingReader(reader);
    assertEquals("value * from beginning;", br.readLine());
    assertEquals("select value from middle;", br.readLine());
    assertEquals("select * from value;", br.readLine());
    assertNull(br.readLine());

  }

  @Test
  public void readBytes() throws Exception {
    int aByte = reader.read();
    assertEquals('v', aByte);

    char[] buf = new char[3];
    reader.read(buf);
    assertArrayEquals(new char[] { 'a', 'l', 'u' }, buf);
    reader.skip(29);
    buf = new char[5];
    reader.read(buf);
    assertArrayEquals(new char[] { 'l', 'u', 'e', ' ', 'f' }, buf);
  }
}