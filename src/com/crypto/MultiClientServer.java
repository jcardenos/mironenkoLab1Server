package com.crypto;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiClientServer {
    private static final int PORT = 12345;
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен, ожидает подключения клиентов на порту " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Ожидаем подключения клиента
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientWriters);
                pool.execute(clientHandler); // Обрабатываем клиента в отдельном потоке
            }
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }

    public static void addClient(PrintWriter out) {
        clientWriters.add(out);
    }

    public static void removeClient(PrintWriter out) {
        clientWriters.remove(out);
    }
}
