package ntou.cse.feelink_backend.service;

import com.google.cloud.vertexai.generativeai.PartMaker;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Component
public class ImageProcessor {

    public Object processImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return PartMaker.fromMimeTypeAndData(file.getContentType(), file.getBytes());
    }
}
