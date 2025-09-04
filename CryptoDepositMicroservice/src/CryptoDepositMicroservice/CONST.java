package CryptoDepositMicroservice;


public class CONST {


    private static final String API_KEY_CRYPTOBOT = "Вставьте ваш токен";
    private static final String URL = "https://pay.crypt.bot";
    private static final String CREATEINVOICE_ENDPOINT = "/api/createInvoice";
    private static final String CHECKINVOICE_ENDPOINT = "/api/getInvoices?invoice_ids=";


    public static String getAPI_KEY_CRYPTOBOT() {
        return API_KEY_CRYPTOBOT;
    }

    public static String getUrl() {
        return URL;
    }

    public static String getCreateinvoiceEndpoint() {
        return CREATEINVOICE_ENDPOINT;
    }

    public static String getCheckinvoiceEndpoint() {
        return CHECKINVOICE_ENDPOINT;
    }


}
