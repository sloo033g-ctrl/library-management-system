package util;

import java.io.*;
import java.util.List;

/**
 * Generic serialization-based persistence helper.
 * Addresses the Reliability non-functional requirement:
 * in-memory state survives across program restarts.
 */
public class FileManager {

    @SuppressWarnings("unchecked")
    public static <T> List<T> load(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Logger.error("Failed to load data from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    public static <T> void save(String filePath, List<T> data) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(data);
            }
        } catch (IOException e) {
            Logger.error("Failed to save data to " + filePath + ": " + e.getMessage());
        }
    }
}
