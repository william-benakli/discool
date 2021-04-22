package app.web.components;

import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class UploadComponent extends Upload {
    /** The name of the directory to save the files to */
    private final String dirName;

    /** The full path to the file that was uploaded */
    @Getter @Setter
    private String fileName;

    /**
     *
     * Creates a Upload object
     * @param height                the height of the component on the page : eg "100px" or "2.5em"
     * @param width                 the width of the component on the page : eg "100px" or "2.5em"
     * @param maxFiles              the maximum number of files that can be uploaded at once
     * @param maxFileSize           the maximum size of the files in bytes
     * @param dirName               the directory in which to save the file(s)
     * @param acceptedFileTypes     the accepted file types (MIME type pattern, wildcards accepted) - null for no restrictions
     *
     *                                   * see MIME types here : https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
     */
    public UploadComponent(String height, String width, int maxFiles, int maxFileSize, String dirName,
                           String ... acceptedFileTypes) {
        this.setHeight(height);
        this.setWidth(width);
        this.setDropAllowed(true);
        this.setMaxFiles(maxFiles);
        this.setMaxFileSize(maxFileSize);
        this.dirName = dirName;
        if (acceptedFileTypes != null) {
            this.setAcceptedFileTypes(acceptedFileTypes);
        }
        this.setReceiver(new FileReceiver());
    }

    private class FileReceiver implements Receiver {
        public File file;

        @Override
        public OutputStream receiveUpload(String fileName, String mimeType) {
            FileOutputStream fos = null;
            try {
                // make sure the directory exists, and create it if it doesn't
                File dir = new File(dirName);
                dir.mkdirs();

                // save the file
                file = new File(dirName + "/" + fileName);
                setFileName(dirName + "/" + fileName);
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return fos;
        }
    }

}
