import java.io.*;

public class HeaderFileObject extends File {

    private String header = null;
    private BufferedReader bufferedReader;
    private long rowsCount;
    public HeaderFileObject(String pathname) {
        super(pathname);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }


    public BufferedReader getBufferedReader() throws FileNotFoundException {
        if (bufferedReader == null) {
            bufferedReader = new BufferedReader(new FileReader(this));
        }
        return bufferedReader;
    }

    public long getRowsCount() {
        try {
            if (this.exists()) {
                FileReader fr = new FileReader(this);
                LineNumberReader lnr = new LineNumberReader(fr);
                int linenumber = 0;
                while (lnr.readLine() != null) {
                    linenumber++;
                }
                lnr.close();
                rowsCount = linenumber;
            } else {
                System.out.println("File does not exists!");
            }

        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        return rowsCount;
    }
}
