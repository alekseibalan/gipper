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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Chat {
  protected static final String FILE_PREFIX = "assets/gipper_";
  protected static final String FILE_SUFFIX = ".txt";
  public final UUID uuid;
  public String title = "";
  private final List<Message> messages;

  public Chat(UUID uuid) {
    this.uuid = uuid;
    this.messages = new ArrayList<>();
  }

  public Chat() {
    this(UUID.randomUUID());
  }

  public Chat(List<Message> messages) {
    this();
    this.messages.addAll(messages);
  }

  public List<Message> getMessages() {
    return List.copyOf(messages);
  }

  public void add(Message message) {
    messages.add(message);
  }

  public void save() {
    StringBuilder s = new StringBuilder();
    s.append(title);
    for (Message message : messages) {
      s.append("\n{\"time\":\"" + message.instant + "\",\"class\":\"" + message.c.name().toLowerCase() + "\"}\n");
      s.append(message.s);
    }
    try {
      Files.writeString(Paths.get(FILE_PREFIX + uuid + FILE_SUFFIX), s.toString());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static Chat load(UUID uuid) {
    String s;
    try {
      s = Files.readString(Paths.get(FILE_PREFIX + uuid + FILE_SUFFIX));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    final Gson gson = new Gson();
    List<Message> messages = new ArrayList<>();
    while (true) {
      int beginIndex = s.lastIndexOf("\n{\"time\":\"");
      if (beginIndex < 0) break;
      int endIndex = s.indexOf("}\n", beginIndex) + 2;
      String json = s.substring(beginIndex, endIndex);
      Map<String, String> map = gson.fromJson(json, Map.class);
      messages.add(new Message(s.substring(endIndex),
          MessageType.valueOf(map.get("class").toUpperCase()), Instant.parse(map.get("time"))));
      s = s.substring(0, beginIndex);
    }
    Collections.reverse(messages);
    Chat chat = new Chat(uuid);
    chat.title = s;
    chat.messages.addAll(messages);
    return chat;
  }

  public static List<Chat> list() {
    String fileMask = FILE_PREFIX + "*" + FILE_SUFFIX;
    Path pathMask = Paths.get(fileMask);
    List<Chat> list = new ArrayList<>();
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
        pathMask.getParent(), pathMask.getFileName().toString())) {
      for (Path path : directoryStream) {
        String pathString = path.toString();
        int endIndex = pathString.length() - FILE_SUFFIX.length();
        Chat chat = new Chat(UUID.fromString(pathString.substring(endIndex - 36, endIndex)));
        try (BufferedReader reader = Files.newBufferedReader(path)) {
          chat.title = reader.readLine();
        }
        list.add(chat);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return list;
  }

  public enum MessageType {
    USER, INFO, ERROR, DEBUG, PROMPT
  }

  public static class Message {
    public final String s;
    public final MessageType c;
    public final Instant instant;

    public Message(String s, MessageType c, Instant instant) {
      this.s = s;
      this.c = c;
      this.instant = instant;
    }

    public Message(String s, MessageType c) {
      this(s, c, Instant.now());
    }

    @Override
    public String toString() {
      return c.name().toLowerCase() + ": " + s.substring(0, Math.min(32, s.length()));
    }
  }
}
