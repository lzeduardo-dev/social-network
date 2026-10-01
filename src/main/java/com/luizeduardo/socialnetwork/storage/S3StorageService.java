package com.luizeduardo.socialnetwork.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String publicUrl;

    public S3StorageService(S3Client s3Client,
                            @Value("${aws.s3.bucket}") String bucket,
                            @Value("${aws.s3.public-url:}") String publicUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicUrl = StringUtils.hasText(publicUrl) ? StringUtils.trimTrailingCharacter(publicUrl, '/') : null;
    }

    // Envia o arquivo para "<pasta>/<uuid>.<ext>" e retorna a chave do objeto no bucket
    public String upload(MultipartFile file, String folder) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String key = folder + "/" + UUID.randomUUID() + (extension != null ? "." + extension : "");

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", e);
        }
        return key;
    }

    // Com aws.s3.public-url definida (CDN, LocalStack visto do host) usa-a como base;
    // sem ela, monta a URL padrao do S3 a partir do client
    public String getUrl(String key) {
        if (publicUrl != null) {
            return publicUrl + "/" + key;
        }
        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build())
                .toString();
    }

    public boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }
}
