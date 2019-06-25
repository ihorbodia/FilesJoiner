import org.apache.commons.io.FilenameUtils;
import java.io.*;

class ExtendedFile extends File {

    ExtendedFile(String pathname) {
        super(pathname);
    }

    String getFileExtension() {
        return FilenameUtils.getExtension(this.getAbsolutePath());
    }
}
