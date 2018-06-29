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

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.operations.DownOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

import java.sql.Connection;
import java.sql.SQLException;

public final class DownCommand extends BaseCommand {
  public DownCommand(SelectedOptions options) {
    super(options);
  }

  @Override
  public void execute(String... params) {
    int steps = getStepCountParameter(1, params);

    DownOperation op = new DownOperation(steps, options);
    try {
      Connection connection = getConnection();
      try {
        op.operate(connection, getMigrationLoader(), getDatabaseOperationOption(), printStream, createDownHook());
      } finally {
        connection.close();
      }
    } catch (SQLException e) {
      throw new MigrationException(e);
    }
  }
}
