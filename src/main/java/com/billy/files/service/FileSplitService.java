package com.billy.files.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSplitService {

	@Autowired
    private SimpMessagingTemplate messagingTemplate;

	@Async
    public void splitFile(File localFile, String originalFilename, int segmentSizeKB, String sessionId) throws IOException {
		
		// 1) Elimina los segmentos viejos del mismo archivo
	    File tempDir = localFile.getParentFile();
	    File[] oldFiles = tempDir.listFiles();
	    if (oldFiles != null) {
	        for (File f : oldFiles) {
	            // Si coinciden con "ojoDerecho.pdf." (ejemplo) se borran
	            if (f.getName().startsWith(originalFilename + ".")) {
	                f.delete();
	            }
	        }
	    }
		
        long segmentSize = (long) segmentSizeKB * 1024L;
        long totalBytes = localFile.length();

        // Abrimos el File "localFile" que YA existe y NO se va a borrar por Tomcat
        try (FileChannel inChannel = FileChannel.open(localFile.toPath())) {
            long position = 0;
            int partNumber = 0;

            ByteBuffer buffer = ByteBuffer.allocate(8 * 1024); // 8KB buffer

            while (position < totalBytes) {
            	String partName = originalFilename + "." + partNumber;
                tempDir = localFile.getParentFile();
                File outFile = new File(tempDir, partName);

                try (FileOutputStream fos = new FileOutputStream(outFile);
                     FileChannel outChannel = fos.getChannel()) {

                    long bytesToRead = segmentSize;
                    while (bytesToRead > 0) {
                        int bytesRead = inChannel.read(buffer);
                        if (bytesRead == -1) break; // EOF
                        buffer.flip();
                        outChannel.write(buffer);
                        buffer.clear();

                        position += bytesRead;
                        bytesToRead -= bytesRead;

                        // Notifica progreso
                        double progress = (double) position / (double) totalBytes * 100.0;
                        messagingTemplate.convertAndSend("/topic/progress/" + sessionId, progress);
                    }
                }
                partNumber++;
            }
        }finally {
            // Borrar la copia local una vez terminada la lectura
            if (localFile.exists()) {
                localFile.delete();
            }
        }

        // Al terminar, notifica
        messagingTemplate.convertAndSend("/topic/progress/" + sessionId, "DONE");
    }
}
