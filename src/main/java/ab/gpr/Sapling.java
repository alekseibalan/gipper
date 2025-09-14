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

package ab.gpr;

import ab.Http;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Sapling.ai grammar checker.
 * We reserve the right to review any User Content https://sapling.ai/tos
 */
public class Sapling implements GrammarCheck {
  protected final Gson gson = new Gson();
  protected String url = "https://api.sapling.ai/api/v1/edits";
  protected Map<String, String> headers = Collections.singletonMap("Content-Type", "application/json");
  protected List<String> ignoreEditTypes = Collections.singletonList("capitalization");
  protected String saplingKey = Objects.requireNonNull(System.getenv("SAPLING_KEY"), "export SAPLING_KEY=");

  @Override
  public String apply(String s) {
    final Map<String, Object> request = new LinkedHashMap<>();
    request.put("key", saplingKey);
    request.put("text", s);
    request.put("session_id", "");
    if (ignoreEditTypes != null) request.put("ignore_edit_types", ignoreEditTypes);
    final String requestString = gson.toJson(request);
    String responseString;
    try {
      responseString = new Http().post(url, headers, requestString);
    } catch (UncheckedIOException e) {
      throw e.getCause().getMessage().startsWith("Server returned HTTP response code: 401 for URL: ")
          ? new UncheckedIOException(new IOException("Invalid API key.")) : e;
    }
    List<Map<String, Object>> edits = (List) gson.fromJson(responseString, Map.class).get("edits");
    if (edits.isEmpty()) return null;
    StringBuilder stringBuilder = new StringBuilder();
    for (Map<String, Object> edit : edits) {
      String errorType = (String) edit.get("general_error_type");
      String description = (String) edit.get("description");
      int start = ((Double) edit.get("start")).intValue();
      int end = ((Double) edit.get("end")).intValue();
      String sentence = (String) edit.get("sentence");
      String replacement = (String) edit.get("replacement");
      String sentenceShort = sentence.substring(start, end);
      String replacementFull = sentence.substring(0, start) + replacement + sentence.substring(end);
      //stringBuilder.append(String.format("%s error: %s, must use: %s\n", description, sentenceShort, replacement));
      //stringBuilder.append(String.format("Error: %s, must use: %s\n", sentence, replacementFull));
      stringBuilder.append(String.format("bad: %s\nuse: %s\n", sentenceShort, replacement));
      stringBuilder.append(String.format("%s %s\n\n", replacementFull, description));
    }
    return stringBuilder.append(responseString).toString();
  }

}
