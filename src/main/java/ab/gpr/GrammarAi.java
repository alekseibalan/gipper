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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GrammarAi implements GrammarCheck {

  protected final Gson gson = new Gson();
  protected final List<String> prompt;
  protected final ModelApi modelApi;

  public GrammarAi(ModelApi modelApi) {
    this.modelApi = modelApi;
    this.prompt = Prompt.read(getClass().getResourceAsStream("grammar_prompt.txt"));
  }

  @Override
  public String apply(String s) {
    List<String> list = new ArrayList<>(prompt);
    list.add("text: " + s);
    String json = modelApi.apply(list);
    List<Map<String, String>> errors = gson.fromJson(json, List.class);
    if (errors.isEmpty()) return null;
    StringBuilder stringBuilder = new StringBuilder();
    for (Map<String, String> e : errors) stringBuilder.append(
        String.format("Error: %s, must use: %s\n", e.get("error"), e.get("correct")));
    return stringBuilder.toString();
  }

}
