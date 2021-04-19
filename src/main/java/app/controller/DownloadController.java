package app.controller;

import com.vaadin.flow.function.SerializableConsumer;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.vaadin.firitin.components.DynamicFileDownloader;

import java.io.*;

public class DownloadController extends DynamicFileDownloader {

    /**
     * Creates a new link to download a file
     * @param text              The name of the link
     * @param fileName          The path to the file to download
     * @param contentWriter     The lambda to download a file with a FileOutputStream
     */
    public DownloadController(String text, String fileName, SerializableConsumer<OutputStream> contentWriter) {
        super(text, fileName, contentWriter);
    }

    /**
     * Creates an "empty" link, so that it doesn't show up if there is nothing to download
     */
    public DownloadController() {
        setText("");
    }

    /**
     * Creates a .tar.gz archive with the contents of the directory
     * The archive will be in the downloads directory
     */
    public void createTarGZ(String sourceDir, String outputName) {
        try {
            FileOutputStream fOut = new FileOutputStream(new File(outputName));
            BufferedOutputStream bOut = new BufferedOutputStream(fOut);
            GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(bOut);
            TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut);
            addFileToTarGz(tOut, sourceDir, "");
            tOut.finish();
            tOut.close();
            gzOut.close();
            bOut.close();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base) throws IOException  {
        File f = new File(path);
        String entryName = base + f.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
        tOut.putArchiveEntry(tarEntry);
        if (f.isFile()) {
            IOUtils.copy(new FileInputStream(f), tOut);
            tOut.closeArchiveEntry();
        } else {
            tOut.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }
}
