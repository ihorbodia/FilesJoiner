import java.io.*;

class HeaderFileObject extends File {

    private String header = null;
    private BufferedReader bufferedReader;
    private long rowsCount;
    HeaderFileObject(String pathname) {
        super(pathname);
    }

    String getHeader() {
        return header;
    }

    void setHeader(String header) {
        this.header = header;
    }


    BufferedReader getBufferedReader() throws FileNotFoundException {
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
