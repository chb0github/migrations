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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.ibatis.migration.options.SelectedPaths;
import org.apache.ibatis.migration.scripts.Jsr223Script;
import org.apache.ibatis.migration.scripts.Script;

/**
 * @author cbongiorno on 1/3/18.
 */
public class Jsr233MigrationLoader implements MigrationLoader {

  private final Script<List<Change>> migrationScript;
  private final Script<Reader> changeReaderScript;
  private final Script<Reader> bootstrap;
  private final Script<Reader> abortScript;
  private final FileMigrationLoader original;
  private final Environment env;
  private final SelectedPaths paths;
  private final Map<String, Object> defParams = new HashMap<String, Object>();

  public Jsr233MigrationLoader(SelectedPaths paths, Environment env) {
    this.env = env;
    this.paths = paths;
    defParams.put("env", env);
    defParams.put("paths", paths);

    this.original = new FileMigrationLoader(paths, env);

    this.migrationScript = toScript("migration_script");
    this.changeReaderScript = toScript("change_script");
    this.bootstrap = toScript("bootstrap_script");
    this.abortScript = toScript("abort_script");

  }

  <T> Script<T> toScript(String kind) {
    String charset = this.env.getScriptCharset();
    Properties props = this.env.getVariables();
    String settings = props.getProperty(kind);
    Script<T> result = null;

    if (settings != null) {
      List<String> segments = Arrays.asList(settings.split(":"));
      if (segments.size() < 3) {
        throw new MigrationException(
            "Error creating a HookScript. Hook setting must contain 'language' and 'file name' separated by ':' (e.g. SQL:post-up.sql).");
      }
      String scriptLang = segments.get(0);
      String streamType = segments.get(1); // classpath: or file:
      String arg = segments.get(2);

      List<String> options = segments.subList(2, segments.size() - 1);
      result = new Jsr223MigrationScript<T>(scriptLang, charset, options, props, streamType, arg);
    }
    return result;
  }

  public Environment getEnvironment() {
    return env;
  }

  public SelectedPaths getPaths() {
    return new SelectedPaths(paths);
  }

  @Override
  public List<Change> getMigrations() {
    return migrationScript != null ? migrationScript.execute(defParams) : original.getMigrations();
  }

  @Override
  public Reader getScriptReader(Change change, boolean undo) {

    Reader result;
    if (changeReaderScript != null) {
      Map<String, Object> params = new HashMap<String, Object>(defParams);
      params.put("change", change);
      params.put("undo", undo);
      result = changeReaderScript.execute(params);
    } else {
      result = original.getScriptReader(change, undo);
    }

    return result;
  }

  @Override
  public Reader getBootstrapReader() {
    return bootstrap != null ? bootstrap.execute(defParams) : original.getBootstrapReader();
  }

  @Override
  public Reader getOnAbortReader() {
    return abortScript != null ? abortScript.execute(defParams) : original.getOnAbortReader();
  }

  private class Jsr223MigrationScript<T> extends Jsr223Script<T> {

    private final String streamType;
    private final String arg;
    private final File f;

    public Jsr223MigrationScript(String scriptLang, String charset, List<String> options, Properties props,
        String streamType, String arg) {
      super(scriptLang, charset, options, Jsr233MigrationLoader.this.paths, props);

      this.f = toFile(streamType, arg);
      this.streamType = streamType;
      this.arg = arg;
    }

    private File toFile(String streamType, String arg) {
      File result = null;
      if (!streamType.equals("classpath") && !streamType.equals("file")) {
        throw new MigrationException("Can only load 'file:' or 'classpath:' got " + streamType);
      }
      if ("classpath".equals(streamType)) {

        URL resource = ClassLoader.getSystemResource(arg);
        if (resource == null) {
          throw new MigrationException("Couldn't find classpath resource " + arg);
        }
        result = new File(resource.getFile());
      }
      if ("file".equals(streamType)) {
        result = new File(arg);
      }
      if (result != null && !result.exists())
        throw new MigrationException("Unable to load script file " + arg);

      return result;
    }

    @Override
    protected Reader getReader() {
      try {
        return new InputStreamReader(new FileInputStream(f));
      } catch (FileNotFoundException e) {
        throw new MigrationException("Unable to load script file " + f);
      }
    }
  }
}
