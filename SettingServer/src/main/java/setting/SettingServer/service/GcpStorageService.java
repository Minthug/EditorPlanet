package setting.SettingServer.service;

import com.google.cloud.storage.*;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import setting.SettingServer.entity.GcpStorageFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcpStorageService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private List<GcpStorageFile> uploadFiles(String uploadFilePath, List<MultipartFile> multipartFiles) throws IOException {
        List<GcpStorageFile> gcpFiles = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.isEmpty()) {
                continue;
            }
            try {
                GcpStorageFile uploadResult = uploadSingleFile(multipartFile, uploadFilePath);
                gcpFiles.add(uploadResult);
            } catch (IOException e) {
                log.error("File upload failed for file: {}", multipartFile.getOriginalFilename(), e);
                gcpFiles.add(createFailedGcpStorageFile(multipartFile.getOriginalFilename(), e.getMessage()));
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
        return gcpFiles;
    }

    private GcpStorageFile uploadSingleFile(MultipartFile multipartFile, String uploadFilePath) throws java.io.IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilename = getUUIDFilename(originalFilename);
        String blobName = uploadFilePath + "/" + uploadFilename;

        BlobId blobId = BlobId.of(bucketName, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(multipartFile.getContentType())
                .build();

        try {
            Blob blob = storage.create(blobInfo, multipartFile.getBytes());
            String uploadFileUrl = blob.getMediaLink();

            return GcpStorageFile.builder()
                    .originalFilename(originalFilename)
                    .uploadFilename(uploadFilename)
                    .uploadFilePath(uploadFilePath)
                    .uploadFileUrl(uploadFileUrl)
                    .uploadSuccessful(true)
                    .build();
        } catch (StorageException e) {
            return createFailedGcpStorageFile(originalFilename, e.getMessage());
        }
    }

    private String getUUIDFilename(String fileName) {
        String ext = fileName.substring(fileName.indexOf(".") + 1);
        return UUID.randomUUID().toString() + "." + ext;
    }

    private GcpStorageFile createGcpStorageFile(String originalFilename, String uploadFilename, String uploadFilePath, String uploadFileUrl) {
        return GcpStorageFile.builder()
                .originalFilename(originalFilename)
                .uploadFilename(originalFilename)
                .uploadFilePath(uploadFilePath)
                .uploadFileUrl(uploadFileUrl)
                .build();
    }

    private GcpStorageFile createFailedGcpStorageFile(String originalFilename, String message) {
        return GcpStorageFile.builder()
                .originalFilename(originalFilename)
                .uploadFilename(null)
                .uploadFilePath(null)
                .uploadFileUrl(null)
                .uploadSuccessful(false)
                .errorMessage(message)
                .build();
    }

//    public boolean deleteFolder(Long memberId, Long boardId) {
//        String folderPrefix = String.format("members/%d/%d/", memberId, boardId);
//
//        try {
//            Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(folderPrefix));
//
//            for (Blob blob : blobs.iterator()) {
//
//            }
//        }

    public boolean deleteFile(String fileUrl) {
        try {
            BlobId blobId = BlobId.of(bucketName, getBlobNameFromUrl(fileUrl));
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                log.info("File delete is complated. BlobName: {}", blobId.getName());
            } else {
                log.warn("File does not exist. BlobName: {}", blobId.getName());
            }
            return deleted;
        } catch (StorageException e) {
            log.error("Failed to delete File: {}", e);
            throw new RuntimeException("Failed to delete File. {}", e);
        }
    }

    private String getBlobNameFromUrl(String fileUrl) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
