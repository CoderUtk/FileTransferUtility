package filetransferutility.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    private Utils() {
    }

    public static void deleteFileFolder(File file) throws IOException {
        Path path = Paths.get(file.getPath());
        Files.delete(path);
    }
}
