package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.Canvas;
import ntou.cse.feelink_backend.chatroom.repository.CanvasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.bson.Document;
import java.util.Optional;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class CanvasService {

    @Autowired
    private CanvasRepository canvasRepository;
    
    @Autowired
    private GridFsTemplate gridFsTemplate;

    // 压缩数据
    private byte[] compressData(String data) throws Exception {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(data.getBytes(StandardCharsets.UTF_8));
        }
        return byteStream.toByteArray();
    }

    // 解压数据
    private String decompressData(InputStream inputStream) throws Exception {
        try (GZIPInputStream gzipStream = new GZIPInputStream(inputStream);
             ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = gzipStream.read(buffer)) > 0) {
                byteStream.write(buffer, 0, len);
            }
            return byteStream.toString(StandardCharsets.UTF_8);
        }
    }

    public Canvas saveCanvas(String chatroomId, Document canvasData) {
        try {
            // 先删除旧的 GridFS 文件
            Query query = new Query(Criteria.where("metadata.chatroomId").is(chatroomId));
            gridFsTemplate.delete(query);
            
            // 将 Document 转为 JSON 字符串并压缩
            String jsonData = canvasData.toJson();
            byte[] compressedData = compressData(jsonData);
            InputStream inputStream = new ByteArrayInputStream(compressedData);
            
            // 存储到 GridFS
            Document metadata = new Document("chatroomId", chatroomId)
                    .append("compressed", true)
                    .append("originalSize", jsonData.length())
                    .append("compressedSize", compressedData.length);
            
            org.bson.types.ObjectId fileId = gridFsTemplate.store(
                inputStream, 
                "canvas_" + chatroomId, 
                "application/gzip",  // 修改 content type
                metadata
            );
            
            // 在 Canvas 表中只存储 GridFS 的文件 ID
            Optional<Canvas> existingCanvas = canvasRepository.findByChatroomId(chatroomId);
            
            if (existingCanvas.isPresent()) {
                Canvas canvas = existingCanvas.get();
                Document refDoc = new Document("gridFsFileId", fileId.toString());
                canvas.setCanvasData(refDoc);
                return canvasRepository.save(canvas);
            } else {
                Document refDoc = new Document("gridFsFileId", fileId.toString());
                Canvas newCanvas = new Canvas(chatroomId, refDoc);
                return canvasRepository.save(newCanvas);
            }
        } catch (Exception e) {
            throw new RuntimeException("存储画布失败: " + e.getMessage(), e);
        }
    }

    public Optional<Canvas> getCanvasByChatroomId(String chatroomId) {
        Optional<Canvas> canvas = canvasRepository.findByChatroomId(chatroomId);
        
        if (canvas.isPresent()) {
            try {
                // 从 GridFS 读取实际数据
                Query query = new Query(Criteria.where("metadata.chatroomId").is(chatroomId));
                GridFSFile gridFSFile = gridFsTemplate.findOne(query);
                
                if (gridFSFile != null) {
                    InputStream inputStream = gridFsTemplate.getResource(gridFSFile).getInputStream();
                    
                    // 检查是否为压缩数据
                    Document metadata = gridFSFile.getMetadata();
                    String jsonData;
                    if (metadata != null && metadata.getBoolean("compressed", false)) {
                        jsonData = decompressData(inputStream);
                    } else {
                        jsonData = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    }
                    
                    Document canvasData = Document.parse(jsonData);
                    canvas.get().setCanvasData(canvasData);
                }
            } catch (Exception e) {
                System.err.println("读取 GridFS 数据失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return canvas;
    }

    public void deleteCanvasByChatroomId(String chatroomId) {
        // 删除 GridFS 文件
        Query query = new Query(Criteria.where("metadata.chatroomId").is(chatroomId));
        gridFsTemplate.delete(query);
        
        // 删除 Canvas 记录
        Optional<Canvas> existingCanvas = canvasRepository.findByChatroomId(chatroomId);
        existingCanvas.ifPresent(canvasRepository::delete);
    }
}
