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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ab.MuCrisp.m;

public class Localhost8080 implements User {

  public static class Message {
    final String s;
    final String c;

    public Message(String s) {
      this(s, null);
    }

    public Message(String s, String c) {
      this.s = s;
      this.c = c;
    }
  }

  protected HttpServer server;
  protected Consumer<String> inListener;
  protected List<Message> list = new ArrayList<>();

  public Localhost8080() {
    try {
      server = HttpServer.create(new InetSocketAddress(8080), 0);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    server.createContext("/", this::handle);
    server.start();
  }

  @Override
  public void close() {
    synchronized (list) {
      list.notifyAll();
    }
    server.stop(1);
  }

  @Override
  public void setInListener(Consumer<String> inListener) {
    this.inListener = inListener;
  }

  @Override
  public void info(String s) {
    list.add(new Message(s));
    synchronized (list) {
      list.notify();
    }
  }

  @Override
  public void error(String s) {
    list.add(new Message(s, "error"));
    synchronized (list) {
      list.notify();
    }
  }

  public String ltGt(String s) {
    return s.replace("<", "&lt;").replace(">", "&gt;");
  }

  public void handle(HttpExchange exchange) throws IOException {
    if ("POST".equals(exchange.getRequestMethod())) {
      Consumer<String> listener = inListener;
      try (InputStream stream = exchange.getRequestBody()) {
        String s = new String(stream.readAllBytes());
        if (listener != null && s.startsWith("m=")) {
          s = URLDecoder.decode(s.substring(2), StandardCharsets.UTF_8);
          list.add(new Message(s, "user"));
          listener.accept(s);
          synchronized (list) {
            try { list.wait(); } catch (InterruptedException ignore) {}
          }
        }
      }
    }
    String response;
    try {
      response = m("html",
          m("main", list.stream()
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
