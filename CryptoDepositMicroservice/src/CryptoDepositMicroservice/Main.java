package CryptoDepositMicroservice;


import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class Main {


    private static final String API_KEY_CRYPTOBOT = CONST.getAPI_KEY_CRYPTOBOT();
    private static final String URL = CONST.getUrl();
    private static final String CREATEINVOICE_URL = URL + CONST.getCreateinvoiceEndpoint();
    private static final String CHECKINVOICE_URL = URL + CONST.getCheckinvoiceEndpoint();
    private static final Scanner scanner = new Scanner(System.in);


    public static String createInvoice(String amount) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        String json = String.format("{\"asset\": \"USDT\", \"amount\": \"%s\"}", amount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CREATEINVOICE_URL))
                .header("Crypto-Pay-API-Token", API_KEY_CRYPTOBOT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String checkInvoice(String invoiceId) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        String checkUrl = CHECKINVOICE_URL + invoiceId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(checkUrl))
                .header("Crypto-Pay-API-Token", API_KEY_CRYPTOBOT)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String extractPayUrl(String jsonResponse) {
        try {

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonObject result = jsonObject.getAsJsonObject("result");
            if (result.has("pay_url")) {
                return result.get("pay_url").getAsString();
            } else {
                return "Не найдено pay_url в ответе";
            }

        } catch (Exception e) {
            return "Ошибка при извлечении pay_url: " + e.getMessage();
        }
    }

    public static String extractInvoiceId(String jsonResponse) {
        try {

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonObject result = jsonObject.getAsJsonObject("result");

            if (result.has("invoice_id")) {
                return result.get("invoice_id").getAsString();
            } else {
                return "Не найдено invoice_id в ответе";
            }
        } catch (Exception e) {
            return "Ошибка при извлечении invoice_id: " + e.getMessage();
        }
    }

    public static String extractStatus(String jsonResponse) {
        try {

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            String status = jsonObject.getAsJsonObject("result")
                    .getAsJsonArray("items")
                    .get(0)
                    .getAsJsonObject()
                    .get("status")
                    .getAsString();

            if (status.equals("active")) {
                return "Не оплачен";
            } else if (status.equals("paid")) {
                return "Оплачен";
            }
            return "";
        } catch (Exception e) {
            return "Ошибка при извлечении status: " + e.getMessage();
        }
    }

    public static String extractAmount(String jsonResponse) {
        try {

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonObject invoice = jsonObject.getAsJsonObject("result")
                    .getAsJsonArray("items")
                    .get(0)
                    .getAsJsonObject();

            String status = invoice.get("status").getAsString();
            String amount;

            if ("paid".equals(status)) {
                amount = invoice.get("paid_amount").getAsString();

                return "Оплата подтверждена!\nБаланс пополнен на " + amount + " USDT";
            } else {
                amount = invoice.get("amount").getAsString();
                return "Сумма к оплате: " + amount + " USDT";
            }

        } catch (Exception e) {
            return "Ошибка при извлечении amount: " + e.getMessage();
        }
    }


    public static void main(String[] args) throws Exception {

        System.out.println("\nВаш ключ: " + API_KEY_CRYPTOBOT);

        boolean flag = true;

        while (flag) {
            System.out.println("Введите сумму пополнения(в USDT): ");
            String input = scanner.nextLine().trim();

            if (input.isBlank()) {
                System.out.println("Пустой ввод! Введите число.");
                continue;
            }

            try {
                String normalizedInput = input.replace(',', '.');
                BigDecimal amount = new BigDecimal(normalizedInput);

                if (amount.compareTo(BigDecimal.valueOf(0.01)) >= 0) {

                    System.out.println("Сумма для пополнения: " + amount + " USDT");
                    String response = createInvoice(amount.toString());
                    String payUrl = extractPayUrl(response);
                    String invoiceId = extractInvoiceId(response);
                    System.out.println("\nСсылка для оплаты: " + payUrl);
//                    System.out.println(response);

                    boolean checkFlag = true;

                    while (checkFlag) {

                        System.out.println("\n1 - Проверить оплату\n2 - Завершить работу программы");
                        String checkInput = scanner.nextLine().trim();

                        if (checkInput.equals("1")) {

                            String checkResponse = checkInvoice(invoiceId);
                            String status = extractStatus(checkResponse);
                            String amountInfo = extractAmount(checkResponse);
                            System.out.println("Статус счета: " + status);
                            System.out.println(amountInfo);

                            if (status.equals("Оплачен")) {

                                checkFlag = false;
                                flag = false;
                            }
                        } else if (checkInput.equals("2")) {

                            System.out.println("Программа завершена.");

                            checkFlag = false;
                            flag = false;

                        } else {
                            System.out.println("Некорректный ввод! Введите 1 или 2");
                        }
                    }
                } else {
                    System.out.println("Сумма должна быть больше 0.009!");
                }

            } catch (NumberFormatException e) {
                System.out.println("Неверный формат! Вы ввели: '" + input + "'. Введите число.");
            }
        }
    }
}