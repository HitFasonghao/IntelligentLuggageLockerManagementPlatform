package org.example.audit.service.impl;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.example.audit.config.MinioConfig;
import org.example.audit.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 文件存储服务实现（MinIO）
 * @author fasonghao
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        try {
            ensureBucketExists();

            // 生成唯一文件名：directory/UUID.ext
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = directory + "/" + UUID.randomUUID() + ext;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // 返回访问URL
            return minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {
            // 从URL中解析objectName: endpoint/bucket/objectName
            String prefix = minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/";
            if (!fileUrl.startsWith(prefix)) {
                log.warn("文件URL不属于当前MinIO存储，跳过删除: {}", fileUrl);
                return;
            }
            String objectName = fileUrl.substring(prefix.length());

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .build());
            log.info("已删除文件: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioConfig.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .build());
            // 设置bucket为公开读，使上传的图片可以直接通过URL访问
            String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [{
                        "Effect": "Allow",
                        "Principal": {"AWS": ["*"]},
                        "Action": ["s3:GetObject"],
                        "Resource": ["arn:aws:s3:::%s/*"]
                      }]
                    }
                    """.formatted(minioConfig.getBucket());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .config(policy)
                    .build());
            log.info("已创建MinIO存储桶: {}", minioConfig.getBucket());
        }
    }
}
