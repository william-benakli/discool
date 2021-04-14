package app.controller;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.zip.GZIPOutputStream;

public class DownloadController {

    /**
     * Creates a .tar.gz archive with the contents of the directory
     * The archive will be in : downloads/studentName_studentId
     * @param sourceDir The source directory to archive
     */
    public static void createTarFile(String outputName, String sourceDir){
        TarArchiveOutputStream tarOs;
        try {
            File outputFile = new File(outputName);
            FileOutputStream fos = new FileOutputStream(outputFile);
            GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
            tarOs = new TarArchiveOutputStream(gos);
            addFilesToTarGZ(sourceDir, "", tarOs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addFilesToTarGZ(String filePath, String parent, TarArchiveOutputStream tarArchive) throws IOException {
        File file = new File(filePath);
        // Create entry name relative to parent file path
        String entryName = parent + file.getName();
        // add tar ArchiveEntry
        tarArchive.putArchiveEntry(new TarArchiveEntry(file, entryName));
        if(file.isFile()){
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            // Write file content to archive
            IOUtils.copy(bis, tarArchive);
            tarArchive.closeArchiveEntry();
            bis.close();
        }else if(file.isDirectory()){
            // no need to copy any content since it is
            // a directory, just close the outputStream
            tarArchive.closeArchiveEntry();
            // for files in the directories
            for(File f : file.listFiles()){
                // recursively call the method for all the subdirectories
                addFilesToTarGZ(f.getAbsolutePath(), entryName+File.separator, tarArchive);
            }
        }
    }
}
