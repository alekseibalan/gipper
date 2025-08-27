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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * LanguageTool spell checker. Does not check grammar. Yesterday we see cow.
 */
public class LanguageTool implements GrammarCheck {

  protected String url = "https://api.languagetool.org/v2/check";
  protected final Map<String, String> headers = Collections.singletonMap(
      "Content-Type", "application/x-www-form-urlencoded");

  @Override
  public String apply(String s) {
//    String responseString = "";
    String responseString = new Http().post(url, headers,
        "language=en-US&text=" + URLEncoder.encode(s, StandardCharsets.UTF_8));
    JsonArray matches = JsonParser.parseString(responseString).getAsJsonObject().get("matches").getAsJsonArray();
    boolean errors = false;
    StringBuilder stringBuilder = new StringBuilder();
    for (JsonElement match : matches) {
      String shortMessage = match.getAsJsonObject().get("shortMessage").getAsString();
      if ("Capitalization".equals(shortMessage)) continue;
      String sentence = match.getAsJsonObject().get("sentence").getAsString();
      String message = match.getAsJsonObject().get("message").getAsString();
      int offset = match.getAsJsonObject().get("offset").getAsInt();
      int length = match.getAsJsonObject().get("length").getAsInt();
      JsonArray replacements = match.getAsJsonObject().get("replacements").getAsJsonArray();
      int indexOfSentence = s.lastIndexOf(sentence, offset);
      if (indexOfSentence >= 0 && !replacements.isEmpty()) {
        String replacement = replacements.get(0).getAsJsonObject().get("value").getAsString();
        int sentenceOffset = offset - indexOfSentence;
        message = sentence.substring(0, sentenceOffset) + replacement + sentence.substring(sentenceOffset + length);
      }
      stringBuilder.append(String.format("Error: %s, must use: %s\n", sentence, message));
      errors = true;
    }
    return errors ? stringBuilder.append(responseString).toString() : null;
  }

}
