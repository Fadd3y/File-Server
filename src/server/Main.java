package server;

import answer.Answer;
import fileHandler.FileStorage;
import request.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

//"File Main\\task\\src\\server\\data"

public class Main {
    private static final int PORT = 23456;
    private static final Path FILES_DIRECTORY = Path.of(System.getProperty("user.dir") + "\\src\\server\\data\\");
    private static final Path SAVE_LIST_LOCATION = Path.of("C:\\Users\\fadde\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\save.txt");
    private static volatile boolean isExit = false;

    private static volatile HashMap<Integer, String> files = new HashMap<>();  // <id, name>

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server started!");
        recoverFileList();

        try (ServerSocket serverSocket = new ServerSocket(PORT)
        ) {
            serverSocket.setSoTimeout(100);
            while (!isExit) {
                try {
                    new ServerThread(serverSocket.accept(), serverSocket);

                } catch (SocketTimeoutException e) {
                    if (isExit) {
                        //System.out.println("Server is shutting down...");
                    } else {
                        //ждем дальше...
                    }
                }
            }
            saveFileList();
        }
    }

    private static void saveFileList() throws IOException {
        if (!Files.exists(SAVE_LIST_LOCATION)) {
            Files.createFile(SAVE_LIST_LOCATION);
        }
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(SAVE_LIST_LOCATION.toString()))) {
            outputStream.writeObject(files);
        } catch (FileNotFoundException e) {

        }
    }

    private static void recoverFileList() throws IOException {
        try (ObjectInputStream outputStream = new ObjectInputStream(new FileInputStream(SAVE_LIST_LOCATION.toString()))) {
            files = (HashMap<Integer, String>) outputStream.readObject();
        } catch (Exception e) {

        }
    }

    private static class ServerThread extends Thread {
        Socket client;
        Request request;
        FileStorage clientFile;
        Answer answer;
        int fileId;

        public ServerThread(Socket socket, ServerSocket serverSocket) {
            this.client = socket;
            this.start();
        }

        @Override
        public void run() {
            try (DataInputStream inputStream = new DataInputStream(client.getInputStream());
                 DataOutputStream outputStream = new DataOutputStream(client.getOutputStream())
            ) {
                System.out.println("Пользователь подключился " + Thread.currentThread().getName());
                request = new Request(inputStream.readUTF());
                printFiles();
                switch (request.getActionType().toString()) {
                    case "GET" -> {
                        String fileName = getFileNameByIdOrName();
                        try {
                            Path filePath = FILES_DIRECTORY.resolve(fileName);
                            clientFile = FileStorage.readFileData(filePath);
                            answer = new Answer("200", null);
                            send(answer, outputStream);
                            send(clientFile, outputStream);
                        } catch (Exception e) {
                            answer = new Answer("404", null);
                            send(answer, outputStream);
                        }
                    }
                    case "PUT" -> {
                        try {
                            clientFile = FileStorage.readFileData(request.getFileName(), inputStream);
                            createFile(clientFile);
                            answer = new Answer("200", String.valueOf(fileId));
                            send(answer, outputStream);
                        } catch (Exception e) {
                            answer = new Answer("403", null);
                            send(answer, outputStream);
                        }
                    }
                    case "DELETE" -> {
                        try {
                            deleteFile(request.getFileName(), request.getAccessType());
                            answer = new Answer("200", null);
                            send(answer, outputStream);
                        } catch (Exception e) {
                            answer = new Answer("404", null);
                            send(answer, outputStream);
                        }
                    }
                    case "EXIT" -> {
                        answer = new Answer("0", null);
                        send(answer, outputStream);
                        stopServer();
                    }
                }
            } catch (IOException e) {
                //ничего не поделать. увы
            }
        }

        private void send(FileStorage clientFile, DataOutputStream outputStream) throws IOException {
            outputStream.writeInt(clientFile.getLength());
            outputStream.write(clientFile.getData());
        }

        private void send(Answer answer, DataOutputStream outputStream) throws IOException {
            outputStream.writeUTF(answer.toString());
        }

        private void createFile(FileStorage clientFile) throws IOException {
            if (clientFile.getFileName().equals("noName" + request.getFileExtension())) {
                clientFile.setFileName(genUniqueName());
            }

            Path filePath = FILES_DIRECTORY.resolve(clientFile.getFileName());
            synchronized (Main.class) {
                Files.createFile(filePath);
                try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toString())
                ) {
                    fileOutputStream.write(clientFile.getData());

                    int id = genUniqueId();
                    fileId = id;

                    files.put(id, clientFile.getFileName());
                }
            }
        }

        private void deleteFile(String name, AccessTypes accessTypes) throws IOException {
            synchronized (Main.class) {
                if (accessTypes == AccessTypes.BY_ID) {
                    int id = Integer.parseInt(name);
                    Path filePath = FILES_DIRECTORY.resolve(Path.of(files.get(id)));
                    Files.delete(filePath);
                    files.remove(id);
                } else {
                    for (var file : files.entrySet()) {
                        if (file.getValue().equals(name)) {
                            Path filePath = FILES_DIRECTORY.resolve(Path.of(name));
                            Files.delete(filePath);
                            files.remove(file.getKey());
                            break;
                        }
                    }
                }
            }
        }

        private String getFileNameByIdOrName() {
            synchronized (Main.class) {
                return switch (request.getAccessType().toString()) {
                    case "BY_ID" -> files.get(request.getFileID());
                    case "BY_NAME" -> request.getFileName();
                    default -> "";
                };
            }
        }

        private String genUniqueName() {
            String name = "file";
            int id = 0;
            while (files.containsValue(name + id + request.getFileExtension())) {
                id++;
            }
            return name + id + request.getFileExtension();
        }

        private int genUniqueId() {
            int id = 0;
            while (files.containsKey(id)) {
                id++;
            }
            return id;
        }

        private static void stopServer() {
            isExit = true;
        }

        public void printFiles() {
            for (var file : files.entrySet()) {
                System.out.println(file.getKey() + " - " + file.getValue());
            }
        }

    }
}
