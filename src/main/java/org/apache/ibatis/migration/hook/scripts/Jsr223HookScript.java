/**
 *    Copyright 2010-2017 the original author or authors.
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
package org.apache.ibatis.migration.hook.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Properties;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.SelectedPaths;
import org.apache.ibatis.migration.scripts.Jsr223Script;
import org.apache.ibatis.migration.utils.Util;

public class Jsr223HookScript extends Jsr223Script<Void> implements HookScript {

  protected final File scriptFile;

  public Jsr223HookScript(String language, File scriptFile, String charset, String[] options, SelectedPaths paths,
      Properties variables, PrintStream printStream) {
    super(language, null, charset, options, paths, variables, printStream);
    this.scriptFile = scriptFile;
    try {
      super.scriptReader = new InputStreamReader(new FileInputStream(scriptFile));
    } catch (FileNotFoundException e) {
      throw new MigrationException("Failed to read JSR-223 hook script file.", e);
    }
  }

  protected void before() {
    printStream.println(Util.horizontalLine("Applying JSR-223 hook : " + scriptFile.getName(), 80));
  }
}
