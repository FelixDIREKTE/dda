package com.dd.dda.service.zip;

import com.dd.dda.model.exception.DDAException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class ZipUtilService {

    /**
     * Method to Zip a Single File, 2 Params Needed
     *
     * @param sourceFilePath Path to Single File not Directory
     * @param zipOutputPath  Path + Filename.zip where the Zip file should be stored   C:\wks\static-file\g\multipleFiles.zip
     */
    public void zipSingleFile(String sourceFilePath, String zipOutputPath) {
        if (sourceFilePath == null || sourceFilePath.trim().isEmpty() || !new File(sourceFilePath).exists() || new File(sourceFilePath).isDirectory()) {
            throw new DDAException("SourceFilePath must have a value, a directory is Not allowed and no object of the same name already exists.");
        }

        checkZipout(zipOutputPath);

        try (FileOutputStream fos = new FileOutputStream(zipOutputPath)) {
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            readFileForZipOutputStream(sourceFilePath, zipOut);
            zipOut.close();
        } catch (IOException | DDAException e) {
            deleteFileOnFail(zipOutputPath);
            throw new DDAException("Zip file could not be created. sourceFilePath: '" + sourceFilePath + "' zipOutputPath: '" + zipOutputPath + "'");
        }
    }


    public void zipMultipleFilesPath(List<Path> sourceFilePathList, String zipOutputPath) {
        List<String> sourceFileStringPathList = new ArrayList<>();

        for (Path filePath : sourceFilePathList) {
            if(filePath != null) {
                sourceFileStringPathList.add(filePath.toString());
            }
        }

        zipMultipleFiles(sourceFileStringPathList, zipOutputPath);
    }

    /**
     * Method to Zip Mulziple Files, 2 Params Needed
     *
     * @param sourceFilePathList List of Strings for SourceFiles C:\wks\static-file\g\multipleFiles\test.txt, C:\wks\static-file\g\multipleFiles\test2.txt
     * @param zipOutputPath      Path + Filename.zip where the Zip file should be stored   C:\wks\static-file\g\multipleFiles.zip
     */
    public void zipMultipleFiles(List<String> sourceFilePathList, String zipOutputPath) {
        if (sourceFilePathList == null || sourceFilePathList.isEmpty()) {
            deleteFileOnFail(zipOutputPath);
            throw new DDAException("SourceFilePath must have one or more Paths");
        }

        checkZipout(zipOutputPath);

        try (FileOutputStream fos = new FileOutputStream(zipOutputPath)) {
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (String sourceFilePath : sourceFilePathList) {
                readFileForZipOutputStream(sourceFilePath, zipOut);
            }
            zipOut.close();
        } catch (IOException | DDAException e) {
            deleteFileOnFail(zipOutputPath);
            throw new DDAException("Zip Multiple files could not be created. zipOutputPath: '" + zipOutputPath + "'\n" + e);
        }
    }


    /**
     * Method to Zip a Complete Directory, 2 Params Needed
     *
     * @param directoryPath Path to the Dirctory C:\wks\static-file\g\multipleFiles
     * @param zipOutputPath Path + Filename.zip where the Zip file should be stored   C:\wks\static-file\g\multipleFiles.zip
     */
    public void zipDirectory(String directoryPath, String zipOutputPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty() || new File(directoryPath).isFile()) {
            throw new DDAException("This must be an directoryPath not an File Path");
        }

        checkZipout(zipOutputPath);

        try {
            try (FileOutputStream fos = new FileOutputStream(zipOutputPath)) {
                ZipOutputStream zipOutPutPathAndName = new ZipOutputStream(fos);

                File directoryToZip = new File(directoryPath);
                if (!directoryToZip.isDirectory()) {
                    throw new DDAException("directoryPath is not a Directory");
                }

                zipDirectory(directoryToZip, directoryToZip.getName(), zipOutPutPathAndName);

                zipOutPutPathAndName.close();
            }
        } catch (IOException | DDAException e) {
            deleteFileOnFail(zipOutputPath);
            throw new DDAException("Zip could not be Created", e);
        }
    }

    private void zipDirectory(File directoryToZip, String directoryName, ZipOutputStream zipOut) {
        try {
            if (directoryToZip.isHidden()) {
                throw new DDAException("Directory is invisible");
            }

            if (directoryToZip.isDirectory()) {
                if (directoryName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(directoryName));
                    zipOut.closeEntry();
                } else {
                    zipOut.putNextEntry(new ZipEntry(directoryName + "/"));
                    zipOut.closeEntry();
                }

                File[] children = directoryToZip.listFiles();

                for (File childFile : children) {
                    zipDirectory(childFile, directoryName + "/" + childFile.getName(), zipOut);
                }
                return;
            }


            try (FileInputStream fis = new FileInputStream(directoryToZip)) {
                ZipEntry zipEntry = new ZipEntry(directoryName);
                zipOut.putNextEntry(zipEntry);

                IOUtils.copy(fis, zipOut);
                zipOut.closeEntry();
            }
        } catch (IOException e) {
            throw new DDAException("Zip could not be Created", e);
        }
    }


    /**
     * Method to Unzip a File, with Multiple Sub Directory
     *
     * @param zipFilePath   Path to the Zip Archive C:\wks\static-file\g\multipleFiles
     * @param outputDirPath Output Path for the Directory, there will the Zip extracted    C:\wks\static-file\g\multipleFiles.zip
     */
    public void unzipFile(String zipFilePath, String outputDirPath) {
        if (!StringUtils.isNoneBlank(zipFilePath) || !StringUtils.isNoneBlank(outputDirPath) || outputDirPath.trim().endsWith(".zip")) {
            throw new DDAException("ZipOutputPath must have a value and end with .zip");
        }

        File dir = new File(outputDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            byte[] buffer = new byte[1024];
            try (FileInputStream fis = new FileInputStream(zipFilePath)) {
                try (ZipInputStream zis = new ZipInputStream(fis)) {
                    ZipEntry ze = zis.getNextEntry();

                    while (ze != null) {
                        String fileName = ze.getName();

                        if (!fileName.endsWith("/")) {
                            File newFile = new File(outputDirPath + File.separator + fileName);
                            new File(newFile.getParent()).mkdirs();
                            try (FileOutputStream fos = new FileOutputStream(newFile)) {
                                int length;
                                while ((length = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, length);
                                }
                            }
                        }
                        zis.closeEntry();
                        ze = zis.getNextEntry();
                    }

                    zis.closeEntry();
                }
            }
        } catch (IOException | DDAException e) {
            try {
                Files.deleteIfExists(Paths.get(outputDirPath));
            } catch (IOException e2) {
                throw new DDAException("SourceFilePath must have one or more Paths", e2);
            }
            throw new DDAException("Zip couldn't extracted", e);
        }
    }


    //Universal Functions
    private void readFileForZipOutputStream(String sourceFilePath, ZipOutputStream zipOut) {
        File fileToZip = new File(sourceFilePath);

        if (fileToZip.isDirectory()) {
            throw new DDAException("Source File Path is not a file instead a folder");
        }
        if (!fileToZip.exists()) {
            throw new DDAException("Source File doen't exist {" + fileToZip + "}");
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
            IOUtils.copy(fis, zipOut);
            zipOut.closeEntry();
        } catch (IOException e) {
            throw new DDAException("File couldn't read in ZipOutputStream", e);
        }
    }

    private void deleteFileOnFail(String zipOutputPath) {
        if (!new File(zipOutputPath).isDirectory() && new File(zipOutputPath).isFile() && new File(zipOutputPath).exists()) {
            try {
                Files.deleteIfExists(Paths.get(zipOutputPath));
            } catch (IOException e2) {
                throw new DDAException("Zip File couldn't deleted");
            }
        }
    }

    private void checkZipout(String zipOutputPath) {
        if (zipOutputPath == null || zipOutputPath.trim().isEmpty() || !zipOutputPath.trim().endsWith(".zip")) {
            deleteFileOnFail(zipOutputPath);
            throw new DDAException("ZipOutputPath must have a value and end with .zip");
        }
    }

}








