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
package org.apache.ibatis.migration.scripts;

import java.util.Map;

/**
 * @author cbongiorno on 1/3/18.
 */
public class NoOpScript<T> implements Script<T> {

  private static NoOpScript instance = new NoOpScript();

  public static <T> NoOpScript<T> getInstance() {
    return instance;
  }

  @Override
  public T execute(Map<String, Object> bindingMap) {
    return null;
  }
}
