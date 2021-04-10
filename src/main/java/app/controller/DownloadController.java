package app.controller;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class DownloadController {

    /**
     * Creates a .tar.gz archive with the contents of the directory
     * The archive will be in : downloads/studentName_studentId
     * @param sourceDir The source directory to archive
     */
    public static void createTarFile(String sourceDir, String studentName, long studentId){
        TarArchiveOutputStream tarOs;
        try {
            File source = new File(sourceDir);
            String outputName = "downloads/" + studentName + "_" + studentId + ".tar.gz";
            FileOutputStream fos = new FileOutputStream(source.getAbsolutePath().concat(".tar.gz"));
            GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
            tarOs = new TarArchiveOutputStream(gos);
            addFilesToTarGZ(sourceDir, "", tarOs);
            try {
                tarOs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            // a directory, just close the outputstream
            tarArchive.closeArchiveEntry();
            // for files in the directories
            for(File f : file.listFiles()){
                // recursively call the method for all the subdirectories
                addFilesToTarGZ(f.getAbsolutePath(), entryName+File.separator, tarArchive);
            }
        }
    }
}
