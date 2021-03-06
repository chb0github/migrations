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
package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.operations.DatabaseOperation;
import org.apache.ibatis.migration.operations.StatusOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public final class ScriptCommand extends BaseCommand {

  public ScriptCommand(SelectedOptions options) {
    super(options);
  }

  @Override
  public void execute(String... args) {
    try {

      if (args == null || args.length < 1 || args[0] == null) {
        throw new MigrationException("The script command requires a range of versions from v1 - v2.");
      }

      boolean scriptPending = false;
      boolean scriptPendingUndo = false;

      if (args[0].equals("pending")) {
        scriptPending = true;
      } else if (args[0].equals("pending_undo")) {
        scriptPendingUndo = true;
      }

      BigDecimal v1 = null;
      BigDecimal v2 = null;
      boolean undo = scriptPendingUndo;

      if (!scriptPending && !scriptPendingUndo) {

        if (args.length < 2 || args[1] == null) {
          throw new MigrationException("The script command requires a range of versions from v1 - v2.");
        }
        v1 = new BigDecimal(args[0]);
        v2 = new BigDecimal(args[1]);
        int comparison = v1.compareTo(v2);
        if (comparison == 0) {
          throw new MigrationException(
              "The script command requires two different versions. Use 0 to include the first version.");
        }
        undo = comparison > 0;
      }

      MigrationLoader loader = getMigrationLoader();

      List<Change> migrations = null;
      try {
        Connection connection = getConnection();
        try {
          migrations = (scriptPending || scriptPendingUndo)
              ? new StatusOperation().operate(connection, loader, getDatabaseOperationOption(), null).getCurrentStatus()
              : loader.getMigrations();
        } finally {
          connection.close();
        }
      } catch (SQLException e) {
        throw new MigrationException(e);
      }
      Collections.sort(migrations);
      if (undo) {
        Collections.reverse(migrations);
      }
      for (Change change : migrations) {
        if (shouldRun(change, v1, v2, scriptPending || scriptPendingUndo)) {
          printStream.println("-- " + change.getFilename());

          Reader migrationReader = undo ? loader.getRollbackReader(change) : loader.getScriptReader(change);
          char[] cbuf = new char[1024];
          int l;
          while ((l = migrationReader.read(cbuf)) == cbuf.length) {
            printStream.print(new String(cbuf, 0, l));
          }

          if (l > 0) {
            printStream.print(new String(cbuf, 0, l - 1));
          }
          printStream.println();
          printStream.println();
          printStream.println(undo ? generateVersionDelete(change) : generateVersionInsert(change));
          printStream.println();
        }
      }
    } catch (IOException e) {
      throw new MigrationException("Error generating script. Cause: " + e, e);
    }
  }

  private String generateVersionInsert(Change change) {
    return "INSERT INTO " + changelogTable() + " (ID, APPLIED_AT, DESCRIPTION) " + "VALUES (" + change.getId() + ", '"
        + DatabaseOperation.generateAppliedTimeStampAsString() + "', '" + change.getDescription().replace('\'', ' ')
        + "')" + getDelimiter();
  }

  private String generateVersionDelete(Change change) {
    return "DELETE FROM " + changelogTable() + " WHERE ID = " + change.getId() + getDelimiter();
  }

  private boolean shouldRun(Change change, BigDecimal v1, BigDecimal v2, boolean pendingOnly) {
    if (!pendingOnly) {
      BigDecimal id = change.getId();
      if (v1.compareTo(v2) > 0) {
        return (id.compareTo(v2) > 0 && id.compareTo(v1) <= 0);
      } else {
        return (id.compareTo(v1) > 0 && id.compareTo(v2) <= 0);
      }
    } else {
      return change.getAppliedTimestamp() == null;
    }
  }

  // Issue 699
  private String getDelimiter() {
    StringBuilder delimiter = new StringBuilder();
    if (environment().isFullLineDelimiter()) {
      delimiter.append("\n");
    }
    delimiter.append(environment().getDelimiter());
    return delimiter.toString();
  }

}
