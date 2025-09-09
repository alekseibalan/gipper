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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Gipper {

  protected final Map<UUID, Chat> sessions = new LinkedHashMap<>();
  protected final ModelApi modelApi;
  protected final GrammarCheck grammarCheck;
  protected final Filter filter;

  public Gipper(ModelApi modelApi, GrammarCheck grammarCheck, Filter filter) {
    this.modelApi = modelApi;
    this.grammarCheck = grammarCheck;
    this.filter = filter;
  }

  protected List<Chat.Message> defaultPrompt() {
    List<Chat.Message> messages = new ArrayList<>();
    String[] prompt = {"You are Marv, a chatbot that reluctantly answers questions with sarcastic responses.\n" +
        "How many pounds are in a kilogram?",
        "This again? There are 2.2 pounds in a kilogram. Please make a note of this.",
        "What does HTML stand for?",
        "Was Google too busy? Hypertext Markup Language. The T is for try to ask better questions in the future.",
        "When did the first airplane fly?",
        "On December 17, 1903, Wilbur and Orville Wright made the first flights. I wish they’d come and take me away."};
    for (String s : prompt) messages.add(new Chat.Message(s, Chat.MessageType.PROMPT));
    return messages;
  }

  public UUID newChatUuid() {
    return newChatUuid("default");
  }

  public UUID newChatUuid(String prompt) {
    List<Chat.Message> messages = new ArrayList<>();
    List<String> list = Prompt.read(getClass().getResourceAsStream(prompt + "_prompt.txt"));
    for (String s : list) messages.add(new Chat.Message(s, Chat.MessageType.PROMPT));
    Chat chat = new Chat(messages);
    sessions.put(chat.uuid, chat);
    return chat.uuid;
  }

  public List<Chat> listChats() {
    return Chat.list().stream().map(chat -> {
      if (chat.title.isEmpty()) {
        UUID uuid = chat.uuid;
        chat = new Chat(uuid);
        Chat c = sessions.get(uuid);
        chat.title = c == null || c.title.isEmpty() ? uuid.toString() : c.title;
      }
      return chat;
    }).collect(Collectors.toList());
  }

  public List<Chat.Message> loadChatMessages(UUID uuid) {
    Chat chat = sessions.computeIfAbsent(uuid, Chat::load);
    return chat.getMessages().stream().filter(m -> !Chat.MessageType.PROMPT.equals(m.c))
        .collect(Collectors.toList());
  }

  public List<Chat.Message> gipp(UUID uuid, String s) {
    Chat chat = sessions.get(uuid);
    chat.add(new Chat.Message(s, Chat.MessageType.USER));
    ArrayList<Chat.Message> list = new ArrayList<>();
    try {
      //list.add(new Chat.Message("debug", Chat.MessageType.DEBUG));
      String grammarResult = grammarCheck.apply(s);
      if (grammarResult != null) throw new IllegalStateException(grammarResult);
      ArrayList<String> inputApi = new ArrayList<>();
      for (Chat.Message message : chat.getMessages()) {
        switch (message.c) {
          case DEBUG: break;
          case ERROR: inputApi.remove(inputApi.size() - 1); break;
          default: inputApi.add(message.s);
        }
      }
      String outputApi = modelApi.apply(inputApi);
      list.add(new Chat.Message(outputApi, Chat.MessageType.ROBOT));
    } catch (Exception e) {
      list.add(new Chat.Message(e.getMessage(), Chat.MessageType.ERROR));
    }
    list.forEach(chat::add); // chat.addAll(list);
    chat.save();
    if (chat.title.isEmpty()) {
      long count = chat.getMessages().stream().filter(m -> Chat.MessageType.ROBOT.equals(m.c)).count();
      if (count > 4) CompletableFuture.runAsync(() -> chat.makeTitle(modelApi));
    }
    return list;
  }

  public void run(User user) {
    user.run(this);
  }

}
