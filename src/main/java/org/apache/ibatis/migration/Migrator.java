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

import org.apache.ibatis.migration.io.ExternalResources;

import java.io.FileNotFoundException;

public class Migrator {
  private static final String MIGRATIONS_HOME = "MIGRATIONS_HOME";
  /* TODO: remove in the next major release */
  private static final String MIGRATIONS_HOME_PROPERTY_DEPRECATED = "migrationHome";
  private static final String MIGRATIONS_HOME_PROPERTY = "migrationsHome";
  private static final String MIGRATIONS_PROPERTIES = "migration.properties";

  public static void main(String[] args) {
    new CommandLine(args).execute();
  }

  public static String migrationsHome() {
    String migrationsHome = System.getenv(MIGRATIONS_HOME);
    // Check if there is a system property
    if (migrationsHome == null) {
      migrationsHome = System.getProperty(MIGRATIONS_HOME_PROPERTY);
      if (migrationsHome == null) {
        migrationsHome = System.getProperty(MIGRATIONS_HOME_PROPERTY_DEPRECATED);
      }
    }
    return migrationsHome;
  }

  public static String getPropertyOption(String key) throws FileNotFoundException {
    String migrationsHome = migrationsHome();
    if (migrationsHome == null || migrationsHome.isEmpty()) {
      return null;
    }
    return ExternalResources.getConfiguredTemplate(migrationsHome + "/" + MIGRATIONS_PROPERTIES, key);
  }
}
