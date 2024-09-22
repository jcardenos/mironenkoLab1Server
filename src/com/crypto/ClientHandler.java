package com.crypto;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private final Set<PrintWriter> clientWriters;

    public ClientHandler(Socket socket, Set<PrintWriter> clientWriters) {
        this.clientSocket = socket;
        this.clientWriters = clientWriters;
    }

    @Override
    public void run() {
        try {
            //todo эту штучку выкинуть тоже в messageservice
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);

            // Добавляем клиента в список
            MultiClientServer.addClient(out);

            String message;
            while ((message = in.readLine()) != null) {
                // Логируем сообщения о присоединении и выходе только в консоль
                if (message.contains("присоединился к чату") || message.contains("покинул чат")) {
                    System.out.println(message);
                } else {
                    // Отправляем все остальные сообщения всем клиентам
                    broadcastMessage(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при работе с клиентом: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                // Удаляем клиента из списка и выводим сообщение
                MultiClientServer.removeClient(out);
                System.out.println("Клиент отключен");
            } catch (IOException e) {
                System.err.println("Ошибка при закрытии сокета: " + e.getMessage());
            }
        }
    }

    private void broadcastMessage(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }
}
