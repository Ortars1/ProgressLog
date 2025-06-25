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
                        /*String insertQuery = "INSERT INTO log () VALUES (?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

                        } catch (SQLException e) {

                        } finally {

                        }*/
                            /*try {
                                String year = String.valueOf(LocalDate.parse(parsedDataForm.get("date")).getYear());
                                // Открываем файл для чтения
                                Workbook workbook = Workbook.getWorkbook(new File(filePath));
                                Sheet readSheet = workbook.getSheet(year);
                                if (readSheet == null) {
                                    WritableWorkbook writableWorkbook = Workbook.createWorkbook(new File(filePath), workbook);
                                    WritableSheet writeSheet = writableWorkbook.createSheet(year, 2);

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    Date date = dateFormat.parse(parsedDataForm.get("date"));
                                    DateFormat customDateFormat = new DateFormat("yyyy-MM-dd"); // Формат даты
                                    WritableCellFormat dateCellFormat = new WritableCellFormat(customDateFormat);

                                    DateFormat customTimeFormat = new DateFormat("HH:mm"); // Формат "00:18"
                                    WritableCellFormat timeCellFormat = new WritableCellFormat(customTimeFormat);

                                    // Записываем дату как объект DateTime
                                    DateTime dateCell = new DateTime(0, 0, date, dateCellFormat);
                                    Formula timeFormula = new Formula(1, 0, "TIMEVALUE(\"" + parsedDataForm.get("time") + "\")", timeCellFormat);
                                    Label category = new Label(2, 0, parsedDataForm.get("category-list"));

                                    //Formula monthFormula = new Formula(3, 0, "=ТЕКСТ(A1; \"ММММ\"");
                                    System.out.println("Строка даты: ");
                                    System.out.println(dateCell.getRow());

                                    writeSheet.addCell(dateCell);
                                    writeSheet.addCell(timeFormula);
                                    writeSheet.addCell(category);

                                    //writeSheet.addCell(monthFormula);

                                    writableWorkbook.write();
                                    writableWorkbook.close();
                                    workbook.close();
                                } else {

                                    System.out.println(findLastFilledRow(readSheet, 0));


                                    String currentDateString = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                                    Sheet categorySheet = workbook.getSheet("Категории");
                                    String [][] currentData = new String[findLastFilledRow(categorySheet, 0) + 1][readSheet.getColumns()];


                            for (int i = 0; i < readSheet.getRows(); i++) {
                                Cell dateCell = readSheet.getCell(0, i);
                                if (dateCell.getContents().equals(currentDateString)) {
                                    currentData[i][0] = dateCell.getContents();
                                    for (int j = 1; j < readSheet.getColumns(); j++) {
                                        Cell cell = readSheet.getCell(j, i);
                                        currentData[i][j] = cell.getContents();
                                    }
                                }
                            }
                            System.out.println(customFormat(currentData));
                                    // Записываем новое значение на следующую строку
                                    //String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    Date date = dateFormat.parse(parsedDataForm.get("date"));
                                    DateFormat customDateFormat = new DateFormat("yyyy-MM-dd"); // Формат даты
                                    WritableCellFormat dateCellFormat = new WritableCellFormat(customDateFormat);


                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                Date time = timeFormat.parse(parsedDataForm.get("time"));
                                DateFormat customTimeFormat = new DateFormat("HH:mm"); // Формат "00:18"
                                WritableCellFormat timeCellFormat = new WritableCellFormat(customTimeFormat);


                                    DateFormat customTimeFormat = new DateFormat("HH:mm"); // Формат "00:18"
                                    WritableCellFormat timeCellFormat = new WritableCellFormat(customTimeFormat);

                                    DateTime dateCell = new DateTime(0, findLastFilledRow(readSheet, 0) + 1, date, dateCellFormat);
                                    Formula timeFormula = new Formula(1, findLastFilledRow(readSheet, 0) + 1, "TIMEVALUE(\"" + parsedDataForm.get("time") + "\")", timeCellFormat);
                                    Label category = new Label(2, findLastFilledRow(readSheet, 0) + 1, parsedDataForm.get("category-list"));

                                    // Создаем копию файла с возможностью записи

                                    WritableWorkbook writableWorkbook = Workbook.createWorkbook(new File(filePath), workbook);
                                    WritableSheet writeSheet = writableWorkbook.getSheet(year);
                                    writeSheet.addCell(dateCell);
                                    writeSheet.addCell(timeFormula);
                                    writeSheet.addCell(category);


                                    // Сохраняем изменения и закрываем файл
                                    writableWorkbook.write();
                                    writableWorkbook.close();
                                    workbook.close();
                                }
                            } catch (IOException | WriteException | BiffException | ParseException e) {
                                e.printStackTrace();
                            }*/

                            //String response = "<html><body><h1>Данные получены!</h1></body></html>";
                            //byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                            //exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(205, -1); // Код ответа 200 (OK)
                        exchange.close();
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

}