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
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Optional;
import java.util.stream.IntStream;

public class Change implements Comparable<Change>, Cloneable {

  private BigDecimal id;
  private String description;
  private String appliedTimestamp;
  private String filename;

  public Change() {
  }

  public Change(BigDecimal id) {
    this.id = id;
  }

  /**
   * Used for functional immutability
   * @param id the new id
   * @param other previous properties
   */
  public Change(BigDecimal id, Change other) {
    this(id, other.appliedTimestamp, other.description, other.filename);
  }

  public Change(BigDecimal id, String appliedTimestamp, String description) {
    this.id = id;
    this.appliedTimestamp = appliedTimestamp;
    this.description = description;
  }

  public Change(BigDecimal id, String appliedTimestamp, String description, String filename) {
    this.id = id;
    this.appliedTimestamp = appliedTimestamp;
    this.description = description;
    this.filename = filename;
  }

  public BigDecimal getId() {
    return id;
  }

  public void setId(BigDecimal id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAppliedTimestamp() {
    return appliedTimestamp;
  }

  public void setAppliedTimestamp(String appliedTimestamp) {
    this.appliedTimestamp = appliedTimestamp;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getFileHash() {
    MessageDigest sha256;
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    File f = new File(this.filename);

    byte[] buff = new byte[1024 * 1024];
    Formatter formatter = new Formatter();
    try {
      FileInputStream fis = new FileInputStream(f);
      for (int read = fis.read(buff); read > -1; read = fis.read(buff)) {
        sha256.update(buff, 0, read);
      }
      fis.close();
      byte[] hash = sha256.digest();
      for (byte b : hash) {
        formatter.format("%02x", b);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return formatter.toString();
  }

  @Override
  public String toString() {
    String ts = appliedTimestamp == null ? "   ...pending...   " : appliedTimestamp;
    return String.format("%s %s %s %s", id, ts, description, filename);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Change change = (Change) o;

    return (id.equals(change.getId()));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public int compareTo(Change change) {
    return id.compareTo(change.getId());
  }

  @Override
  public Change clone() {
    try {
      return (Change) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.getMessage());
    }
  }
}
