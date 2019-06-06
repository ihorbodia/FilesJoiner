import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;

public class HeaderFileObject extends File {

    private String header = null;
    private LineIterator lineIterator;
    public HeaderFileObject(String pathname) {
        super(pathname);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public LineIterator getLineIterator() throws IOException {
        lineIterator = FileUtils.lineIterator(this, "UTF-8");
        return lineIterator;
    }

    public void setLineIterator(LineIterator lineIterator) {
        this.lineIterator = lineIterator;
    }
}
