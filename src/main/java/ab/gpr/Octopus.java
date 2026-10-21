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
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Octopus implements ModelApi {
  protected final Gson gson = new Gson();
  protected String url = "https://api.openai.com/v1/responses";
  protected final Map<String, String> headers = new LinkedHashMap<>();
  // free: gpt-4.1-mini $1.60, gpt-4.1-nano $0.40, gpt-4o-mini $0.60
  // paid: gpt-4.1 $8.00, o3-mini $4.40
  // government-issued ID: o4-mini $4.40
  String model = "gpt-4.1-nano";

  public Octopus() {
    String openaiApiKey = Objects.requireNonNull(System.getenv("OPENAI_API_KEY"), "export OPENAI_API_KEY=");
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + openaiApiKey);
  }

  @Override
  public Octopus model(String model) {
    this.model = model;
    return this;
  }

  private void addRoleContent(List<Map<String, String>> input, String role, String content) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("role", role);
    map.put("content", content);
    input.add(map);
  }

  @Override
  public String apply(List<String> strings) {
    List<Map<String, String>> input = new ArrayList<>();
    //if (systemPrompt != null) addRoleContent(input, "system", systemPrompt);
    for (int i = 0, size = strings.size(); i < size; i++) {
      addRoleContent(input, (size - i) % 2 == 0 ? "assistant" : "user", strings.get(i));
    }
    final Map<String, Object> request = new LinkedHashMap<>();
    request.put("model", model);
    request.put("input", input);
    request.put("store", false);
    final String requestString = gson.toJson(request);
    String responseString = new Http().post(url, headers, requestString);
    String outputContentText = JsonParser.parseString(responseString).getAsJsonObject().get("output").getAsJsonArray()
        .get(0).getAsJsonObject().get("content").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
    return outputContentText;
  }
}
