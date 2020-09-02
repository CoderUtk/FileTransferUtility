package filetransferutility.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private List<String> fileList;
    private String outputZipFile;
    private String sourceFolder;

    public void zipFolder(String source, String destination) {
        sourceFolder = source;
        outputZipFile = destination;
        generateFileList(new File(sourceFolder));
        zipIt(outputZipFile);
    }

    public ZipUtils() {
        fileList = new ArrayList<>();
    }

    public void zipIt(String zipFile) {
        byte[] buffer = new byte[1024];
        String source = new File(sourceFolder).getName();

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            System.out.println("Output to Zip : " + zipFile);

            for (String file : this.fileList) {
                System.out.println("File Added : " + file);
                ZipEntry ze = new ZipEntry(source + File.pathSeparator + file);
                zos.putNextEntry(ze);
                try (FileInputStream in = new FileInputStream(sourceFolder + File.pathSeparator + file)) {
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
            }

            zos.closeEntry();
            System.out.println("Folder successfully compressed");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void generateFileList(File node) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename));
            }
        }
    }

    private String generateZipEntry(String file) {
        return file.substring(sourceFolder.length() + 1, file.length());
    }

    public void unZipIt(String zipFile, String outputFolder) {

        byte[] buffer = new byte[1024];

        try {
            //create output directory is not exists
            String zipFolder = new File(zipFile).getName().replace(".zip", "");
            File folder = new File(outputFolder + File.pathSeparator + zipFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            //get the zip file content
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();
                while (ze != null) {
                    String fileName = ze.getName();
                    if (!ze.getName().endsWith("/")) {
                        File newFile = new File(outputFolder + File.pathSeparator + zipFolder + File.pathSeparator + fileName);
                        System.out.println("file unzip : " + newFile.getAbsoluteFile());
                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        File tempFile = new File(newFile.getParent());
                        if (!tempFile.exists()) {
                            tempFile.mkdirs();
                        }
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    ze = zis.getNextEntry();
                }
                zis.closeEntry();
            }
            System.out.println("Finished");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
