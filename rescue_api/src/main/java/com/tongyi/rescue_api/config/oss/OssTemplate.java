package com.tongyi.rescue_api.config.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Date;

@Configuration

public class OssTemplate {

    final OssConfig ossConfig;

    public OssTemplate(OssConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    private OSS getOssClient() {
        return new OSSClientBuilder().build(ossConfig.getEndpoint(), ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
    }

    public String uploadFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        OSS ossClient = getOssClient();
        try {
            ossClient.putObject(ossConfig.getBucketName(), fileName, file.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            ossClient.shutdown();
        }
        return fileName;
    }
    public String uploadFile(MultipartFile file,String fileName) {
        OSS ossClient = getOssClient();
        try {
            ossClient.putObject(ossConfig.getBucketName(), fileName, file.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            ossClient.shutdown();
        }
        return fileName;
    }

    public String getFileUrl(String fileName) {
        OSS ossClient = getOssClient();
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000*24); // 24小时过期
        URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), fileName, expiration);
        ossClient.shutdown();
        return url.toString();
    }

    public boolean deleteFile(String fileName) {
        OSS ossClient = getOssClient();
        try {
            ossClient.deleteObject(ossConfig.getBucketName(), fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            ossClient.shutdown();
        }
        return true;
    }
    public String getPermanentFileUrl(String fileName) {
        // 返回文件的公共 URL
        return String.format("https://%s.%s/%s", ossConfig.getBucketName(), ossConfig.getEndpoint().replace("https://", "").replace("http://", "").replace("/", ""), fileName);
    }
}

