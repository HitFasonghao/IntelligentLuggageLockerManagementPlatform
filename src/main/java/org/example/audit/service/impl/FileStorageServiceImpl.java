package org.example.audit.service.impl;

import org.example.audit.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务实现（占位）
 * TODO: 接入对象存储服务（如 MinIO、阿里云OSS 等），实现真正的文件上传逻辑
 * @author fasonghao
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        // TODO: 实现对象存储上传逻辑，返回文件访问URL
        // 示例：
        // 1. 生成唯一文件名
        // 2. 上传到对象存储
        // 3. 返回访问URL
        return "ok";
    }
}
