package com.example.currencyconverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.Map;
import java.util.Scanner;

public class CurrencyConverterApp {

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/";
    private static final String CURRENCIES_FILE = "currencies.json";
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(BLUE + "Introduce la moneda base (por ejemplo, USD):" + RESET);
        String baseCurrency = scanner.nextLine().toUpperCase();

        try {
            Map<String, Double> rates = getExchangeRates(baseCurrency);
            Map<String, String> currencies = getCurrencies();

            System.out.println(GREEN + "Elige la moneda de destino:" + RESET);
            printCurrenciesInColumns(currencies, 10);

            String targetCurrency = scanner.nextLine().toUpperCase();

            if (!rates.containsKey(targetCurrency)) {
                System.err.println(RED + "Moneda de destino no válida." + RESET);
                return;
            }

            System.out.println(BLUE + "Introduce la cantidad a convertir:" + RESET);
            double amount = scanner.nextDouble();

            double rate = rates.get(targetCurrency);
            double result = amount * rate;
            System.out.printf(GREEN + "Resultado: %.2f %s\n" + RESET, result, targetCurrency);
        } catch (IOException e) {
            System.err.println(RED + "Error al consumir la API: " + e.getMessage() + RESET);
        }
    }

    private static Map<String, Double> getExchangeRates(String baseCurrency) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(API_URL + baseCurrency);
        CloseableHttpResponse response = httpClient.execute(request);
        String jsonResponse = EntityUtils.toString(response.getEntity());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode ratesNode = objectMapper.readTree(jsonResponse).get("rates");
        return objectMapper.convertValue(ratesNode, Map.class);
    }

    private static Map<String, String> getCurrencies() throws IOException {
        File currenciesFile = new File(CURRENCIES_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        if (currenciesFile.exists()) {
            return objectMapper.readValue(currenciesFile, Map.class);
        } else {
            String currenciesJson = getCurrenciesFromApi();
            objectMapper.writeValue(currenciesFile, objectMapper.readTree(currenciesJson));
            return objectMapper.readValue(currenciesFile, Map.class);
        }
    }

    private static String getCurrenciesFromApi() throws IOException {
        String currenciesApiUrl = "https://openexchangerates.org/api/currencies.json";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(currenciesApiUrl);
        CloseableHttpResponse response = httpClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    private static void printCurrenciesInColumns(Map<String, String> currencies, int columns) {
        int count = 0;
        for (String currency : currencies.keySet()) {
            System.out.print(RED + currency + "\t" + RESET);
            count++;
            if (count % columns == 0) {
                System.out.println();
            }
        }
        if (count % columns != 0) {
            System.out.println();
        }
    }
}

