package org.example.audit.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 * @author fasonghao
 */
public interface FileStorageService {

    /**
     * 上传文件到对象存储，返回文件访问URL
     * @param file 上传的文件
     * @param directory 存储目录（如 "license"）
     * @return 文件URL
     */
    String uploadFile(MultipartFile file, String directory);

    /**
     * 根据文件URL删除对象存储中的文件
     * @param fileUrl 文件的完整访问URL
     */
    void deleteFile(String fileUrl);
}
