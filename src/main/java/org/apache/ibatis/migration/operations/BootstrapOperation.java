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
package org.apache.ibatis.migration.operations;

import static java.lang.System.err;
import static java.lang.System.out;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.apache.ibatis.migration.utils.Util;
import static org.apache.ibatis.migration.utils.Util.horizontalLine;
import sun.font.ScriptRun;

import java.io.PrintStream;
import java.io.Reader;
import java.util.Collection;

public final class BootstrapOperation extends DatabaseOperation {
  private final boolean force;

  public BootstrapOperation() {
    this(false);
  }

  public BootstrapOperation(boolean force) {
    super();
    this.force = force;
  }

  public BootstrapOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
      DatabaseOperationOption option, PrintStream printStream) {
    if (option == null) {
      option = new DatabaseOperationOption();
    }
    if (changelogExists(connectionProvider, option)) {
      if (force) {
        bootstrap(migrationsLoader, getScriptRunner(connectionProvider, option, printStream));
      } else {
        printStream.println("For your safety, the bootstrapping will only run before migrations are applied "
            + "(i.e. before the changelog exists).  If you're certain, you can run it " + "using the --force option.");
      }
    } else {
      bootstrap(migrationsLoader, getScriptRunner(connectionProvider, option, printStream));
    }
    return this;
  }

  private void bootstrap(MigrationLoader migrationLoader, ScriptRunner runner) {

    for (Reader bootstrapReader : migrationLoader.getBootstrapReaders()) {
      out.println(horizontalLine("Bootstrapping: " + bootstrapReader, 80));
      runner.runScript(bootstrapReader);
    }
    runner.closeConnection();
  }
}
