package fileHandler;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class FileStorage {

    private String fileName;
    private byte[] data;
    private int length;

    private FileStorage(String fileName, byte[] data, int length) {
        this.fileName = fileName;
        this.data = data;
        this.length = length;
    }

    public static FileStorage readFileData(String fileName, DataInputStream inputStream) throws IOException {
        int length = inputStream.readInt();;
        byte[] data = new byte[length];
        inputStream.readFully(data, 0, length);
        return new FileStorage(fileName, data, length);
    }

    public static FileStorage readFileData(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString();
        byte[] data;
        int length;

        try (FileInputStream input = new FileInputStream(filePath.toString())) {
            data = input.readAllBytes();
            length = data.length;
        }
        return new FileStorage(fileName, data, length);
    }

    public void setDataManually(String content) {
        if (this.isEmpty()) {
            System.out.println("пустой файл");
            data = content.getBytes();
            this.length = data.length;
        }
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return length;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isEmpty() {
        //System.out.println(length == 0);
        return length == 0;
    }
}
