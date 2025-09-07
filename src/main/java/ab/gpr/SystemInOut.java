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

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class SystemInOut implements User {

  protected Scanner scanner = new Scanner(System.in);

  public void printMessages(List<Chat.Message> messages) {
    for (Chat.Message message : messages) {
      String s = message.s;
      switch (message.c) {
        case USER: s = "\u001B[3m\u001B[38:5:28m" + s + "\u001B[0m"; break;
        case ERROR: s = "\u001B[31m" + s + "\u001B[0m"; break;
        case DEBUG: s = "\u001B[34m" + s + "\u001B[0m"; break;
      }
      System.out.println(s);
    }
  }

  @Override
  public void run(Gipper gipper) {
    List<Chat> chats = gipper.listChats();
    UUID chatUuid = chats.isEmpty() ? gipper.newChatUuid() : chats.get(0).uuid;
    System.out.println("/bye to exit, gipper -w for web");
    printMessages(gipper.loadChatMessages(chatUuid));
    String s;
    while (true) {
      s = scanner.nextLine();
      if (s.charAt(0) == '/') {
        if ("/bye".equals(s)) break;
        throw new IllegalStateException();
      } else {
        List<Chat.Message> messages = gipper.gipp(chatUuid, s);
        printMessages(messages);
      }
    }
  }

}
