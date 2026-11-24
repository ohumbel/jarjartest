package st.extreme.calling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

class CommonsCompress {

    public static String compress(String data) throws IOException {
        String returnData = "";
        try (ByteArrayOutputStream compressedArray = new ByteArrayOutputStream()) {
            BZip2CompressorOutputStream bzbuf = new BZip2CompressorOutputStream(
                    compressedArray);
            bzbuf.write(data.getBytes());
            bzbuf.finish();
            returnData = new String(compressedArray.toString("iso-8859-1"));
        }
        return returnData;
    }

}
