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

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static ab.MuCrisp.m;
import static org.junit.jupiter.api.Assertions.*;

class MuCrispTest {

  protected String r() {
    return UUID.randomUUID().toString();
  }

  @Test
  void test() {
    assertEquals("<html></html>", m("html", "<html></html>").toString());
    String htmlFixed = m("html",
        m("header",
            m("logo",
                m("alt", "logo")),
            m("title", "Title")),
        m("footer",
            m("company", "Company")),
        m("text", "Working hours")
    ).toString();
    assertEquals(
        "<html>\n" +
        "<head>\n" +
        "  <title>Title</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "  <header>\n" +
        "    <img src=\"logo.png\" alt=\"logo\">\n" +
        "    Title\n" +
        "  </header>\n" +
        "  <main>\n" +
        "    Working hours\n" +
        "  </main>\n" +
        "  <footer>\n" +
        "    &copy; Company\n" +
        "  </footer>\n" +
        "</body>\n" +
        "</html>\n", htmlFixed);
    String alt = r();
    String title = r();
    String company = r();
    String text = r();
    String htmlRandom = m("html",
        m("header",
            m("logo",
                m("alt", alt)),
            m("title", title)),
        m("footer",
            m("company", company)),
        m("text", text)
    ).toString();
    assertEquals(
        "<html>\n" +
        "<head>\n" +
        "  <title>" + title + "</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "  <header>\n" +
        "    <img src=\"logo.png\" alt=\"" + alt + "\">\n" +
        "    " + title + "\n" +
        "  </header>\n" +
        "  <main>\n" +
        "    " + text + "\n" +
        "  </main>\n" +
        "  <footer>\n" +
        "    &copy; " + company + "\n" +
        "  </footer>\n" +
        "</body>\n" +
        "</html>\n", htmlRandom);
  }

}
