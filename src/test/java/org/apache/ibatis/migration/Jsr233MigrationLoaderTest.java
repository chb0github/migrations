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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.migration.options.SelectedPaths;
import org.junit.Before;
import org.junit.Test;

/**
 * @author cbongiorno on 1/12/18.
 * these tests verify that the Environment and SelectedPath objects are passed in and accessible
 * and that the results from the script are honored
 */
public class Jsr233MigrationLoaderTest {

  private double id;
  private Change change;

  @Before
  public void setUp() throws Exception {
    id = Math.floor(Math.random() * 10000);
    change = new Change(new BigDecimal(id), null, "Jsr233MigrationLoaderTest", "foopath");
  }

  private Jsr233MigrationLoader mkloader(String propsFile) {

    String resourceRoot = this.getClass().getName().replaceAll("\\.", "/");
    URL url = this.getClass().getClassLoader().getResource(resourceRoot);
    File dir = new File(url.getFile());

    String propsPath = String.format("%s%c%s.properties", url.getFile(), File.separatorChar, propsFile);
    File props = new File(propsPath);

    Environment env = new Environment(props);
    SelectedPaths sp = new SelectedPaths(dir);
    env.getVariables().put("testid", id);
    return new Jsr233MigrationLoader(sp, env);
  }

  @Test
  public void getMigrationsFromCp() throws Exception {
    Jsr233MigrationLoader loader = mkloader("classpath");
    List<Change> changes = loader.getMigrations();
    assertEquals(1, changes.size());
    assertEquals(change, changes.get(0));
  }

  @Test
  public void getScriptReaderFromCp() throws Exception {
    Jsr233MigrationLoader loader = mkloader("classpath");

    Reader reader = loader.getScriptReader(change);
    String actual = new BufferedReader(reader).readLine();
    String expected = String.format("select '%s' as id, '%s' as change", this.change.getId(),
        loader.getPaths().getBasePath().getCanonicalPath());
    assertEquals(expected, actual);
  }

  @Test
  public void getBootstrapReaderFromCp() throws Exception {
    Jsr233MigrationLoader loader = mkloader("classpath");
    List<Reader> reader = loader.getBootstrapReaders();
    String actual = new BufferedReader(reader.get(0)).readLine();
    String expected = String.format("select '%s' as id, '%s' as bootstrap", this.change.getId(),
        loader.getPaths().getBasePath().getCanonicalPath());
    assertEquals(expected, actual);
  }

  @Test
  public void getOnAbortReaderFromCp() throws Exception {
    Jsr233MigrationLoader loader = mkloader("classpath");

    Reader reader = loader.getOnAbortReader(this.change);
    String actual = new BufferedReader(reader).readLine();
    String expected = String.format("select '%s' as id, '%s' as abort", this.change.getId(),
        loader.getPaths().getBasePath().getCanonicalPath());
    assertEquals(expected, actual);
  }

  @Test
  public void getMigrationsFromFile() throws Exception {
    Jsr233MigrationLoader loader = mkloader("file");

    List<Change> changes = loader.getMigrations();
    assertEquals(Collections.singletonList(change), changes);
  }

  @Test
  public void getScriptReaderFromFile() throws Exception {
    Jsr233MigrationLoader loader = mkloader("file");
    Reader reader = loader.getScriptReader(change);
    String actual = new BufferedReader(reader).readLine();
    String expected = String.format("select '%s' as id, '%s' as change", this.change.getId(),
        loader.getPaths().getBasePath().getCanonicalPath());
    assertEquals(expected, actual);
  }

  @Test
  public void getBootstrapReaderFromFile() throws Exception {
    Jsr233MigrationLoader loader = mkloader("file");

    List<Reader> reader = loader.getBootstrapReaders();
    String actual = new BufferedReader(reader.get(0)).readLine();
    String expected = String.format("select '%s' as id, '%s' as bootstrap", this.change.getId(),
        loader.getPaths().getBasePath().getCanonicalPath());
    assertEquals(expected, actual);
  }

  @Test
  public void getOnAbortReaderFromFile() throws Exception {
    Jsr233MigrationLoader loader = mkloader("file");
    Reader reader = loader.getOnAbortReader(this.change);
    String actual = new BufferedReader(reader).readLine();
    String expected = String.format("select '%s' as id, '%s' as abort", this.change.getId(),
        loader.getPaths().getBasePath().getCanonicalPath());
    assertEquals(expected, actual);
  }

  @Test
  public void getMigrationsNoScript() throws Exception {
    Jsr233MigrationLoader loader = mkloader("default");

    List<Change> changes = loader.getMigrations();
    Change expected = new Change(BigDecimal.ONE, null, "change", "00001_change.sql");
    assertEquals(Collections.singletonList(expected), changes);
  }

  @Test
  public void getScriptReaderNoScript() throws Exception {
    Jsr233MigrationLoader loader = mkloader("default");
    Change c = new Change(BigDecimal.ONE, null, "change", "00001_change.sql");
    Reader reader = loader.getScriptReader(c);
    String actual = new CommentStrippingReader(reader).readLine();
    String expected = String.format("select '%s' as id;", c.getId());
    assertEquals(expected, actual);
  }

  @Test
  public void getBootstrapReaderNoScript() throws Exception {
    Jsr233MigrationLoader loader = mkloader("default");

    List<Reader> readers = loader.getBootstrapReaders();
    String actual = new CommentStrippingReader(readers.get(0)).readLine();
    String expected = "select 'bootstrap' as id from bootstrap;";
    assertEquals(expected, actual);
  }

  @Test
  public void getOnAbortReaderNoScript() throws Exception {
    Jsr233MigrationLoader loader = mkloader("default");
    Reader reader = loader.getOnAbortReader(this.change);
    String actual = new CommentStrippingReader(reader).readLine();
    String expected = "select 'onabort' as id from onabort;";
    assertEquals(expected, actual);
  }

  // This little trick is necessary as a maven plugin sticks headers on all of our files.
  private static class CommentStrippingReader extends BufferedReader {

    public CommentStrippingReader(Reader in) {
      super(in);
    }

    @Override
    public String readLine() throws IOException {
      String line = null;
      do {
        line = super.readLine();
      } while (line != null && (line.startsWith("--") || line.isEmpty()));
      return line;
    }
  }
}