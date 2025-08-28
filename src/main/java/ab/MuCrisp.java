/*
 * Copyright (C) 2025 Aleksei Balan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ab;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Syntax wrapper for Mustache
 */
public class MuCrisp {
  protected static final MustacheFactory FACTORY = new DefaultMustacheFactory();
  protected static final Map<String, Mustache> compiled = new HashMap<>();
  protected final String key;
  protected final String value;
  protected final MuCrisp[] muCrisps;

  public MuCrisp(String key, String value) {
    this.key = key;
    this.value = value;
    this.muCrisps = null;
  }

  public MuCrisp(String key, MuCrisp[] muCrisps) {
    this.key = key;
    this.value = null;
    this.muCrisps = muCrisps;
  }

  public static MuCrisp m(String key, String value) {
    return new MuCrisp(key, value);
  }
  
  public static MuCrisp m(String key, MuCrisp... muCrisps) {
    return new MuCrisp(key, muCrisps);
  }

  @Override
  public String toString() {
    if (value != null) return value;
    Mustache mustache = compiled.computeIfAbsent(key, k -> FACTORY.compile(k + ".mustache"));
    HashMap<String, Object> map = new HashMap<>();
    for (MuCrisp muCrisp : muCrisps) {
      String value = muCrisp.toString();
      int lastChar = value.length() - 1;
      if (value.charAt(lastChar) == '\n') value = value.substring(0, lastChar);
      map.put(muCrisp.key, value);
    }
    StringWriter writer = new StringWriter();
    try {
      mustache.execute(writer, map).flush();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return writer.toString();
  }

}
