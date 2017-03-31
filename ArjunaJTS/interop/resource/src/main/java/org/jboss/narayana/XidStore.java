package org.jboss.narayana;

import javax.transaction.xa.Xid;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.APPEND;

public class XidStore {
    private String xidFileName = "xids.txt";

    public XidStore(String xidFileName) {
         this.xidFileName = xidFileName;
    }

    public static void main(String[] args) throws Exception {
    }

    public boolean write(Xid ... lines) throws Exception {
        return write((Xid xid) -> new XidImpl(xid).toString(), lines);
    }

    @SafeVarargs
    public final <T> boolean write(Function<T, String> typeToString, T... lines) throws Exception {
        FileOutputStream fos= new FileOutputStream(xidFileName, true);
        FileLock fl = fos.getChannel().lock();

        List<String> xidList = Arrays.stream(lines).map(typeToString).collect(Collectors.toList());

        if (fl != null) {
            try {
                try {
                    Files.write(Paths.get(xidFileName), xidList, StandardCharsets.UTF_8, APPEND);
                    return true;
                } finally {
                    fl.release();
                }
            } finally {
                fos.close();
            }
        }

        return false;

    }

    public List<Xid> read(List<Xid> lines) throws Exception {
        FileInputStream fos= new FileInputStream(xidFileName);
        FileLock fl = fos.getChannel().tryLock(0L, Long.MAX_VALUE, true);

        try {
            if (fl != null) {
                try {
                    Files.readAllLines(Paths.get(xidFileName)).forEach((String item) -> lines.add(new XidImpl(item)));
                } finally {
                    fl.release();
                }
            }
        } finally{
            fos.close();
        }

        return lines;
    }

    public void remove(Xid xid) throws Exception {
        FileInputStream fos= new FileInputStream(xidFileName);
        FileLock fl = fos.getChannel().tryLock(0L, Long.MAX_VALUE, true);

        try {
            if (fl != null) {
                List<String> lines = new ArrayList<>();

                try {
                    lines.addAll(Files.readAllLines(Paths.get(xidFileName)));

                    if (lines.remove(new XidImpl(xid).toString())) {
                        Files.write(Paths.get(xidFileName), lines);
                    }

                } finally {
                    fl.release();
                }
            }
        } finally{
            fos.close();
        }
    }
}
