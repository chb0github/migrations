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
package org.apache.ibatis.migration.runtime_migration.scripts_java;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.JavaMigrationLoader;
import org.junit.Test;

public class JavaMigrationLoaderTest {

  @Test
  public void testGetMigrations() throws Exception {
    JavaMigrationLoader loader = new JavaMigrationLoader(this.getClass().getPackage().getName());
    List<Change> migrations = loader.getMigrations();
    assertEquals(3, migrations.size());
  }

  @Test
  public void testGetScriptReader() throws Exception {
    JavaMigrationLoader loader = new JavaMigrationLoader(this.getClass().getPackage().getName());
    Change change = new Change();
    change.setFilename("org.apache.ibatis.migration.runtime_migration.scripts_java.V002_CreateFirstTable");
    Reader reader = loader.getScriptReader(change);
    Writer writer = new StringWriter();
    int c;
    while ((c = reader.read()) != -1) {
      writer.write(c);
    }
    assertTrue(writer.toString().contains("CREATE TABLE first_table (ID INTEGER NOT NULL, NAME VARCHAR(16));"));
  }

  @Test
  public void testGetBootstrapReader() throws Exception {
    JavaMigrationLoader loader = new JavaMigrationLoader(this.getClass().getPackage().getName());
    List<Reader> readers = loader.getBootstrapReaders();
    Writer writer = new StringWriter();
    Reader reader = readers.get(0);
    int c;
    while ((c = reader.read()) != -1) {
      writer.write(c);
    }
    assertTrue(writer.toString().contains("CREATE TABLE bootstrap_table (ID INTEGER NOT NULL, NAME VARCHAR(16));"));
  }

}
