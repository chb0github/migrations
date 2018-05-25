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

import static org.junit.Assert.*;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author cbongiorno on 5/25/18.
 */
public class ChangeTest {

  @Test
  public void testGetFileHash() {
    String file = getClass().getResource("/org/apache/ibatis/migration/ChangeTest.sql").getFile();
    Change c = new Change(BigDecimal.ONE, null, null, file);
    assertEquals("6ce46a4101479e3cc5bd3f56bec28ae70bb263b37c350d5fe28f4acd8afd7805", c.getFileHash());
  }
}