/*
 * Copyright (C) 2026 Aleksei Balan
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Elephant implements ModelApi {
  String url = "https://api.anthropic.com/v1/";
  final Map<String, String> headers = new LinkedHashMap<>();
  // claude-haiku-5-5, claude-haiku-4-5-20251001
  // claude-sonnet-5-5, claude-sonnet-5, claude-sonnet-4-6
  // claude-opus-5-5, claude-opus-5, claude-opus-4-8, claude-opus-4-7, claude-opus-4-6, claude-opus-4-5-20251101
  // claude-fable-5-1, claude-fable-5
  String model;

  public Elephant() {
    String anthropicApiKey = Objects.requireNonNull(System.getenv("ANTHROPIC_API_KEY"), "export ANTHROPIC_API_KEY=");
    headers.put("Authorization", "Bearer " + anthropicApiKey);
    headers.put("anthropic-version", "2026-10-21");
  }

  List<Model> getModels() {
    String s = new Http().get(url + "models", headers);
    return JsonParser.parseString(s).getAsJsonObject().get("data").getAsJsonArray().asList().stream().map(e -> {
      JsonObject element = e.getAsJsonObject();
      Model model = new Model();
      model.id = element.get("id").getAsString();
      model.line = element.get("line").getAsString();
      model.created = Instant.parse(element.get("created_at").getAsString());
      return model;
    }).collect(Collectors.toList());
  }

  @Override
  public Elephant model(String model) {
    this.model = model;
    char c = model.charAt(model.length() - 1);
    if (c >= '0' && c <= '9') return this; // user knows the exact version of the model
    List<Model> models = getModels();
    for (Model m : models) {
      this.model = m.id;
      if (model.equals(m.id) || model.equals(m.line)) return this;
    }
    throw new IllegalStateException("model not found: " + model);
  }

  @Override
  public String apply(List<String> strings) {
    throw new IllegalStateException("TODO: give Anthropic time to fix their credit card payments");
  }

  public static class Model {
    String id;
    String line;
    Instant created;
    @Override
    public String toString() {
      return id;
    }
  }
}
