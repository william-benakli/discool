package app.web.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

import java.io.*;

public class UploadComponent extends Upload {

    /**
     * Creates a Upload object
     * @param height                the height of the component on the page : eg "100px" or "2.5em"
     * @param width                 the width of the component on the page : eg "100px" or "2.5em"
     * @param maxFiles              the maximum number of files that can be uploaded at once
     * @param maxFileSize           the maximum size of the files in bytes
     * @param acceptedFileTypes     the accepted file types (MIME type pattern, wildcards accepted) - null for no restrictions
     *
     * see MIME types here : https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
     */
    public UploadComponent(String height, String width, int maxFiles, int maxFileSize, String ... acceptedFileTypes) {
        this.setHeight(height);
        this.setWidth(width);
        this.setDropAllowed(true);
        //this.setAutoUpload(false);
        this.setMaxFiles(maxFiles);
        this.setMaxFileSize(maxFileSize);
        this.setAcceptedFileTypes(acceptedFileTypes);

        this.setReceiver(new FileReceiver());
    }

    private static class FileReceiver implements Receiver {
        public File file;

        @Override
        public OutputStream receiveUpload(String fileName, String mimeType) {
            FileOutputStream fos = null;
            try {
                file = new File("tmp/" + fileName);
                fos = new FileOutputStream(file);
                Notification.show("YEAH !!");
            } catch (FileNotFoundException e) {
                Notification.show("CAN'T UPLOAD");
            }
            return fos;
        }
    }

}
