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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

public class Http {

  public static final String USER_AGENT = "curl/8.12.1";

  public String post(String url, Map<String, String> headers, String data) {
    try {
      HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
      urlConnection.setRequestProperty("User-Agent", USER_AGENT);
      headers.forEach(urlConnection::setRequestProperty);
      urlConnection.setRequestMethod("POST");
      urlConnection.setDoOutput(true);
      byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
      urlConnection.setFixedLengthStreamingMode(dataBytes.length);
      urlConnection.connect();
      try (OutputStream os = urlConnection.getOutputStream()) {
        os.write(dataBytes);
      }
      try (InputStream inputStream = urlConnection.getInputStream();
          Scanner scanner = new Scanner(inputStream).useDelimiter("\\Z")) {
        return scanner.next();
      }
    } catch (IOException e) {
      if (e instanceof HttpRetryException) e = new IOException( // misleading "cannot retry" exception
          "Server returned HTTP response code: " + ((HttpRetryException) e).responseCode() + " for URL: " + url);
      throw new UncheckedIOException(e);
    }
  }

  public String get(String url, Map<String, String> headers) {
    try {
      URLConnection urlConnection = new URL(url).openConnection();
      urlConnection.setRequestProperty("User-Agent", USER_AGENT);
      headers.forEach(urlConnection::setRequestProperty);
      try (InputStream inputStream = urlConnection.getInputStream();
          Scanner scanner = new Scanner(inputStream).useDelimiter("\\Z")) {
        return scanner.next();
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
