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

import java.io.Reader;
import java.util.List;

public interface MigrationLoader {

  /**
   * @return A list of migrations (bootstrap should NOT be included).
   */
  List<Change> getMigrations();

  /**
   * @param change identifies the migration to read.
   * @return A {@link Reader} of the specified SQL script.
   */
  Reader getScriptReader(Change change);

  /**
   * @param change identifies the migration requiring rollback
   * @return A {@link Reader} of the specified SQL script.
   */
  Reader getRollbackReader(Change change);

  /**
   * @return A {@link Reader} of the bootstrap SQL script.
   */
  List<Reader> getBootstrapReaders();

  /**
   * @return A {@link Reader} of the onabort SQL script.
   */
  Reader getOnAbortReader(Change change);

}
