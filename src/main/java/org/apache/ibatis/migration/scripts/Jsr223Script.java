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

import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.SelectedPaths;
import org.apache.ibatis.migration.utils.Util;

public class Jsr223Script<T> implements Script<T> {

  private static final String MIGRATION_PATHS = "migrationPaths";

  private static final String KEY_FUNCTION = "_function";
  private static final String KEY_OBJECT = "_object";
  private static final String KEY_METHOD = "_method";
  private static final String KEY_ARG = "_arg";

  protected final String language;
  protected final String charset;
  protected final Properties variables;
  protected final SelectedPaths paths;
  protected final PrintStream printStream;

  protected Reader scriptReader;

  protected String functionName;
  protected String objectName;
  protected String methodName;
  protected List<String> args = new ArrayList<String>();
  protected Map<String, String> localVars = new HashMap<String, String>();

  public Jsr223Script(String language, Reader scriptReader, String charset, List<String> options, SelectedPaths paths,
      Properties variables) {
    this(language, scriptReader, charset, options.toArray(new String[0]), paths, variables, System.out);
  }

  public Jsr223Script(String language, Reader scriptReader, String charset, String[] options, SelectedPaths paths,
      Properties variables, PrintStream printStream) {
    super();
    this.language = language;
    this.charset = charset;
    this.paths = paths;
    this.variables = variables;
    this.printStream = printStream;
    this.scriptReader = scriptReader;
    for (String option : options) {
      int sep = option.indexOf('=');
      if (sep > -1) {
        String key = option.substring(0, sep);
        String value = option.substring(sep + 1);
        if (KEY_FUNCTION.equals(key)) {
          functionName = value;
        } else if (KEY_METHOD.equals(key)) {
          methodName = value;
        } else if (KEY_OBJECT.equals(key)) {
          objectName = value;
        } else if (KEY_ARG.equals(key)) {
          args.add(value);
        } else {
          localVars.put(key, value);
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public T execute(Map<String, Object> bindingMap) {
    T result = null;
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName(language);
    if (engine == null)
      throw new MigrationException("Unsupported language: " + language);

    // bind global/local variables defined in the environment file
    Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
    bindVariables(bindingMap, variables.entrySet());
    bindVariables(bindingMap, localVars.entrySet());
    bindings.put(MIGRATION_PATHS, paths);
    bindings.putAll(bindingMap);
    try {
      before();
      result = (T) engine.eval(scriptReader, bindings);
      if (functionName != null || (objectName != null && methodName != null)) {
        Invocable invocable = (Invocable) engine;
        if (functionName != null) {
          printStream.println(Util.horizontalLine("Invoking function : " + functionName, 80));
          result = (T) invocable.invokeFunction(functionName, args.toArray());
        } else {
          printStream.println(Util.horizontalLine("Invoking method : " + methodName, 80));
          Object targetObject = engine.get(objectName);
          result = (T) invocable.invokeMethod(targetObject, methodName, args.toArray());
        }
      }
      // store vars in bindings to the per-operation map
      bindVariables(bindingMap, bindings.entrySet());
    } catch (ClassCastException e) {
      throw new MigrationException(
          "Script engine '" + engine.getClass().getName() + "' does not support function/method invocation.", e);

    } catch (ScriptException e) {
      printStream.println(e.getMessage());
      throw new MigrationException("Failed to execute JSR-223 script.", e);
    } catch (NoSuchMethodException e) {
      throw new MigrationException("Method or function not found in JSR-223 hook script: " + functionName, e);
    }
    return result;
  }

  private <S, T> void bindVariables(Map<String, Object> bindingMap, Set<Entry<S, T>> vars) {
    for (Entry<S, T> entry : vars) {
      bindingMap.put((String) entry.getKey(), entry.getValue());
    }
  }

  protected void before() {

  }
}
