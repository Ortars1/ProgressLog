package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URLDecoder;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.sql.*;

import static java.lang.Integer.*;


public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        String url = "jdbc:sqlite:src/main/db/database.db";
        Connection connection;

        // Подключение к БД
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Соединение с базой данных успешно установлено.");
        } catch(SQLException e) {
            System.err.println("Ошибка при подключении к базе данных: " + e.getMessage());
            return;
        }

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Чтение данных из тела запроса
                    InputStream requestBody = exchange.getRequestBody();
                    String requestBodyString = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println(requestBodyString);
                    String decodedRequestBody = URLDecoder.decode(requestBodyString, StandardCharsets.UTF_8);
                    System.out.println(decodedRequestBody);
                    Map <String, String> parsedDataForm =  parseRequest(decodedRequestBody);
                    System.out.println(parsedDataForm);
                    System.out.println(parsedDataForm.values());
                    String filePath = "src/main/web/test.xls";


                    if (parsedDataForm.containsKey("time")) {
                        String insertQuery = "INSERT INTO log (id_category, date, time) VALUES (?, ?, ?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setInt(1, parseInt(parsedDataForm.get("category-list")));
                            insertStatement.setString(2, parsedDataForm.get("date"));
                            insertStatement.setInt(3, convertTimeToMinutes(parsedDataForm.get("time")));
                            insertStatement.executeUpdate();
                            exchange.sendResponseHeaders(205, -1); // 205 чтобы не обновлялась (пустой экран)
                        } catch (SQLException e) {
                            e.printStackTrace();
                            exchange.sendResponseHeaders(500, -1);
                        } finally {
                            exchange.close();
                        }
                    } else if (parsedDataForm.containsKey("new-category")) {
                        String insertQuery = "INSERT OR IGNORE INTO category (category) VALUES (?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setString(1, parsedDataForm.get("new-category"));
                            int rowsAffected = insertStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                exchange.sendResponseHeaders(201, -1);
                            } else {
                                exchange.sendResponseHeaders(200, -1);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            exchange.sendResponseHeaders(500, -1);
                        } finally {
                            exchange.close();
                        }
                    }
                } else {         // if GET
                    String filePath = null;
                    String requestURI = exchange.getRequestURI().toString();
                    if (requestURI.equals("/style.css")) {
                        filePath = "src/main/web/style.css";
                        exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
                    } else if (requestURI.equals("/activity-graph.css")) {
                        filePath = "src/main/web/activity-graph.css";
                        exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
                    } else if (requestURI.equals("/activity-graph.js")) {
                        filePath = "src/main/web/activity-graph.js";
                        exchange.getResponseHeaders().set("Content-Type", "application/javascript; charset=UTF-8");
                    } else {
                        filePath = "src/main/web/index.html";
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    }
                    Path indexPath = Paths.get(filePath);
                    byte[] responseBytes = Files.readAllBytes(indexPath);
                    exchange.sendResponseHeaders(200, responseBytes.length); // Код ответа 200 (OK)

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                }
            }
        });

        server.createContext("/api/get-categories", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String jsonData;
                String selectQuery = "SELECT * FROM category";

                try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                    // Выполнение GET-запроса
                    ResultSet resultSet = selectStatement.executeQuery();

                    jsonData = prepareJsonData("categories", resultSet);
                    byte[] responseBytes = jsonData.getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                } finally {
                    exchange.close();
                }
            }
        });

        server.createContext("/api/get-years", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String jsonData;
                String selectQuery = "SELECT date FROM log";

                try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                    // Выполнение GET-запроса
                    ResultSet resultSet = selectStatement.executeQuery();

                    jsonData = prepareJsonData("years", resultSet);
                    byte[] responseBytes = jsonData.getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                } finally {
                    exchange.close();
                }
            }
        });

        /*server.createContext("/submit", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Чтение данных из тела запроса
                    InputStream requestBody = exchange.getRequestBody();
                    String requestBodyString = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println(requestBodyString);
                    // Парсинг данных формы
                    //Map<String, String> formData = parseFormData(requestBodyString);

                    // Извлечение данных
                    //String name = formData.get("name");
                    //String age = formData.get("age");

                    // Сохранение данных в переменные или выполнение действий
                    //System.out.println("Имя: " + name);
                    //System.out.println("Возраст: " + age);

                    // Ответ клиенту
                    String response = "<html><body><h1>Данные получены!</h1></body></html>";
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length); // Код ответа 200 (OK)

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } else {
                    // Если запрос не POST, возвращаем ошибку
                    String response = "<html><body><h1>Метод не поддерживается</h1></body></html>";
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(405, responseBytes.length); // Код ответа 405 (Method Not Allowed)

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                }
            }
        });*/

        server.start();

        System.out.println("Сервер запущен на http://localhost:8080");

    }

    private static Map<String, String> parseRequest(String requestBodyString) {
        Map<String, String> result = new LinkedHashMap<>();
        String[] data = requestBodyString.split("[=&]");
        for (int i = 0; i < data.length; i += 2) {
            result.put(data[i], data[i + 1]);
        }
        return result;
    }

    private static String prepareJsonData(String key, ResultSet values) throws SQLException {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{ \"");
        jsonBuilder.append(key);
        jsonBuilder.append("\": [");

        ResultSetMetaData metaData = values.getMetaData();
        int columnCount = metaData.getColumnCount();

        boolean isFirstObject = true;

        while (values.next()) {
            if (!isFirstObject) {
                jsonBuilder.append(", "); // Добавляем запятую !перед каждым! объектом, кроме первого
            }
            jsonBuilder.append("{");
            boolean isFirstField = true;

            for (int i = 1; i <= columnCount; i++) {
                if (!isFirstField) {
                    jsonBuilder.append(", "); // Добавляем запятую !перед каждым! полем, кроме первого
                }
                String columnName = metaData.getColumnName(i);
                String columnValue = values.getString(i);
                jsonBuilder.append("\"")
                        .append(columnName)
                        .append("\": \"")
                        .append(columnValue)
                        .append("\"");
                isFirstField = false;
            }

            jsonBuilder.append("}");
            isFirstObject = false;
        }

        jsonBuilder.append("] }");
        return jsonBuilder.toString();
    }

    private static int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = parseInt(parts[0]);
        int minunes = Integer.parseInt(parts[1]);

        return hours * 60 + minunes;
    }

}