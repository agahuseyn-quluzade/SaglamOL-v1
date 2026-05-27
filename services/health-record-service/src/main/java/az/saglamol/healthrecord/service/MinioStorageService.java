package az.saglamol.healthrecord.service;

import az.saglamol.healthrecord.exception.HealthRecordException;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageService(
            MinioClient minioClient,
            @Value("${minio.bucket.medical-documents:medical-documents}") String bucketName
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception exception) {
            throw new HealthRecordException("MINIO_OPERATION_FAILED", "Failed to ensure MinIO bucket exists");
        }
    }

    public String generatePresignedUploadUrl(String objectKey) {
        ensureBucketExists();
        return presignedUrl(Method.PUT, objectKey, 15);
    }

    public String generatePresignedDownloadUrl(String objectKey) {
        return presignedUrl(Method.GET, objectKey, 5);
    }

    public void deleteObject(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
        } catch (Exception exception) {
            throw new HealthRecordException("MINIO_OPERATION_FAILED", "Failed to delete MinIO object");
        }
    }

    public String bucketName() {
        return bucketName;
    }

    private String presignedUrl(Method method, String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(method)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception exception) {
            throw new HealthRecordException("MINIO_OPERATION_FAILED", "Failed to generate MinIO presigned URL");
        }
    }
}
