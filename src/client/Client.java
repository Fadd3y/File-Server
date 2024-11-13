package client;

import answer.Answer;
import fileHandler.FileStorage;
import request.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Client {
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;
    private static final Path FILES_DIRECTORY = Path.of(System.getProperty("user.dir") + "\\src\\client\\data\\");

    //C:\Users\fadde\IdeaProjects\File Main\src\client\data
    public static void main(String[] args) throws IOException, InterruptedException {
        Thread.sleep(10);
        try (Socket socket = new Socket(IP_ADDRESS, PORT);
             DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())
        ) {
            Scanner scanner = new Scanner(System.in);
            Request request = new Request();

            System.out.print("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ");

            switch (scanner.nextLine()) {
                case "1" -> {       // Get a file
                    request.setActionType(ActionTypes.GET);

                    System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
                    String choice = scanner.nextLine();

                    if (choice.equals("1")) {
                        request.setAccessType(AccessTypes.BY_NAME);
                        System.out.print("Enter name: ");
                        request.setFileName(scanner.nextLine());
                    } else if (choice.equals("2")) {
                        request.setAccessType(AccessTypes.BY_ID);
                        System.out.print("Enter id: ");
                        request.setFileID(scanner.nextLine());
                    }

                    send(request, outputStream);
                }
                case "2" -> {       // Save a file
                    request.setActionType(ActionTypes.PUT);

                    System.out.print("Enter name of the file: ");

                    String fileName = scanner.nextLine();
                    Path filePath = FILES_DIRECTORY.resolve(fileName);
                    FileStorage clientFile = FileStorage.readFileData(filePath);

                    request.setFileExtension(fileName);

                    System.out.print("Enter name of the file to be saved on server: ");

                    request.setFileName(scanner.nextLine());

                    if (clientFile.isEmpty()) {
                        System.out.print("Enter the content of the file: ");
                        clientFile.setDataManually(scanner.nextLine());
                    }

                    send(request, outputStream);
                    send(clientFile, outputStream);
                }
                case "3" -> {       // Delete a file
                    request.setActionType(ActionTypes.DELETE);

                    System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
                    String choice = scanner.nextLine();

                    if (choice.equals("1")) {
                        request.setAccessType(AccessTypes.BY_NAME);
                        System.out.print("Enter name: ");
                        request.setFileName(scanner.nextLine());
                    } else if (choice.equals("2")) {
                        request.setAccessType(AccessTypes.BY_ID);
                        System.out.print("Enter id: ");
                        request.setFileID(scanner.nextLine());
                    }

                    send(request, outputStream);
                }
                case "exit" -> {
                    request.setActionType(ActionTypes.EXIT);
                    send(request, outputStream);
                }
            }
            System.out.println("The request was sent.");

            Answer answer; // = new Answer(inputStream.readUTF());

            switch (request.getActionType().toString()) {
                case "GET" -> { // receive file
                    answer = new Answer(inputStream.readUTF());
                    if (answer.getStatusCode().equals("200")) {
                        FileStorage receivedFile = FileStorage.readFileData("received", inputStream);
                        System.out.print("The file was downloaded! Specify a name for it:");
                        String fileName = scanner.nextLine();
                        createFile(fileName, receivedFile.getData());
                        System.out.println("File saved on the hard drive!");
                    } else {
                        System.out.println("The response says that this file is not found!");
                    }
                }
                case "PUT" -> { // send file
                    answer = new Answer(inputStream.readUTF());
                    System.out.println(answer);
                    if (answer.getStatusCode().equals("200")) {
                        System.out.printf("Response says that file is saved! ID = %s\n", answer.getId());
                    } else {
                        System.out.println("The response says that creating the file was forbidden!");
                    }
                }
                case "DELETE" -> { // delete file
                    answer = new Answer(inputStream.readUTF());
                    if (answer.getStatusCode().equals("200")) {
                        System.out.println("The response says that this file was deleted successfully!");
                    } else {
                        System.out.println("The response says that the file is not found!");
                    }
                }
                case "EXIT" -> { // stop server
                    //System.out.println("Сервер сдох");
                }
            }

            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void send(Request request, DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(request.toString());
    }

    private static void send(FileStorage clientFile, DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(clientFile.getLength());
        outputStream.write(clientFile.getData());
    }

    private static void createFile(String name, byte[] data) throws IOException {
        Path filePath = FILES_DIRECTORY.resolve(name);
        Files.createFile(filePath);
        try (FileOutputStream outputStream = new FileOutputStream(filePath.toString())) {
            outputStream.write(data);
        }
    }
}