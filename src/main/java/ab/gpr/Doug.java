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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Doug is the state of the art chatbot developed in the beginning of 2023 for the December project.
 *
 * How many pounds are in a kilogram?
 * angel in how kilogram? not
 * What does HTML stand for?
 * html today took cube in
 * When did the first airplane fly?
 * first visited did first in this
 * What time is it?
 * time in is cruelty
 */
public class Doug implements ModelApi {

  public static final Random RANDOM = ThreadLocalRandom.current();
  public static final String FIGURE = "cube";
  public static final String POETRY =
      " not in cruelty not in wrath the reaper came today" +
      " an angel visited this gray path and took the cube away";

  @Override
  public ModelApi model(String model) {
    throw new IllegalStateException();
  }

  @Override
  public String apply(List<String> strings) {
    String s = strings.get(strings.size() - 1);
    if (s.length() < FIGURE.length()) return FIGURE;
    s = s.toLowerCase();
    StringBuilder stringBuilder = new StringBuilder(s);
    while (stringBuilder.length() < POETRY.length()) stringBuilder.append(' ').append(s);
    stringBuilder.append(POETRY);
    List<String> list = Arrays.asList(stringBuilder.toString().split("\\s+"));
    Collections.shuffle(list);
    return String.join(" ", list.subList(0, Math.min(list.size(), RANDOM.nextInt(4) + 4)));
  }
}
