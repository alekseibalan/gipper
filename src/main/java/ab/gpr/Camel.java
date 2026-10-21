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
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Camel implements ModelApi {
  protected final Gson gson = new Gson();
  protected String url = "http://localhost:11434/api/";
  protected final Map<String, String> headers = new LinkedHashMap<>();
  // 1GiB: smollm2:135m
  // 2GiB: deepseek-r1:1.5b
  // 6GiB: llama3.2:3b, phi4-mini, huihui_ai/llama3.2-abliterate:3b
  String model;

  public Camel() {
    headers.put("Content-Type", "application/json");
  }

  @Override
  public Camel model(String model) {
    this.model = model;
    return this;
  }

  private void addRoleContent(List<Map<String, String>> messages, String role, String content) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("role", role);
    map.put("content", content);
    messages.add(map);
  }

  String getModel(String api) {
    String s = new Http().get(url + api);
    JsonArray array = JsonParser.parseString(s).getAsJsonObject().get("models").getAsJsonArray();
    if (array.isEmpty()) return null;
    System.out.println(s);
    return array.get(0).getAsJsonObject().get("name").getAsString();
  }

  String getModel() {
    if (model == null) model = getModel("ps");
//    if (model == null) model = getModel("tags");
    if (model == null) throw new IllegalStateException("model not found, try: ollama run llama3.2:3b hi");
    return model;
  }

  @Override
  public String apply(List<String> strings) {
    List<Map<String, String>> messages = new ArrayList<>();
    //if (systemPrompt != null) addRoleContent(messages, "system", systemPrompt);
    for (int i = 0, size = strings.size(); i < size; i++) {
      addRoleContent(messages, (size - i) % 2 == 0 ? "assistant" : "user", strings.get(i));
    }
    final Map<String, Object> request = new LinkedHashMap<>();
    request.put("model", getModel());
    request.put("messages", messages);
    request.put("stream", false);
    final String requestString = gson.toJson(request);
    String responseString = new Http().post(url + "chat", headers, requestString);
    return JsonParser.parseString(responseString)
        .getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
  }
}
