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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ab.MuCrisp.m;

public class Localhost8080 implements User {

  protected HttpServer server;
  protected Gipper gipper;

  @Override
  public void run(Gipper gipper) {
    this.gipper = gipper;
    try {
      server = HttpServer.create(new InetSocketAddress(8080), 0);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    server.createContext("/", this::handle);
    server.start();
    System.out.println("http://localhost:8080/");
    synchronized (server) {
      try {
        while (true) server.wait();
      } catch (InterruptedException ignore) {}
    }
    server.stop(1);
  }

  public String ltGt(String s) {
    return s.replace("<", "&lt;").replace(">", "&gt;");
  }

  public void handle(HttpExchange exchange) throws IOException {
    String uri = exchange.getRequestURI().toString();
    UUID session = null;
    if (uri.length() == 37 && uri.charAt(0) == '/') {
      try {
        session = UUID.fromString(uri.substring(1));
      } catch (IllegalArgumentException ignore) {}
    }
    if (session == null) {
      exchange.getResponseHeaders().add("Location", "/" + gipper.newChatUuid());
      exchange.sendResponseHeaders(302, 0);
      exchange.getResponseBody().close();
      return;
    }

    if ("POST".equals(exchange.getRequestMethod())) {
      try (InputStream stream = exchange.getRequestBody()) {
        String s = new String(stream.readAllBytes());
        if (s.startsWith("m=")) {
          s = URLDecoder.decode(s.substring(2), StandardCharsets.UTF_8);
          gipper.gipp(session, s);
        }
      }
    }

    String response;
    try {
      List<Chat.Message> list = gipper.loadChatMessages(session);
      response = m("html",
          m("main", list.stream()
              .filter(m -> !Chat.MessageType.DEBUG.equals(m.c))
              .map(m -> "<div" + (m.c == null ? "" : " class=" + m.c) + ">" + ltGt(m.s) + "</div>\n")
              .collect(Collectors.joining()))
      ).toString();
    } catch (Exception e) {
      throw e;
    }
    byte[] responseBytes = response.getBytes();
    exchange.sendResponseHeaders(200, responseBytes.length);
    try (OutputStream stream = exchange.getResponseBody()) {
      stream.write(responseBytes);
    }
  }

}
