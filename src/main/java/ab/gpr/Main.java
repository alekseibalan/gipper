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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

  public static void main(String[] args) {
    Set<String> argSet = Arrays.stream(args).collect(Collectors.toSet());
    User user = argSet.contains("-w") ? new Localhost8080() : new SystemInOut();
    GrammarCheck grammarCheck = new GrammarAi(new Octopus().model("gpt-4o-mini"));
    ModelApi modelApi = new Camel().model("phi4-mini");
    new Gipper(modelApi, new LanguageTool(), new Filter()).run(user);
  }

}
