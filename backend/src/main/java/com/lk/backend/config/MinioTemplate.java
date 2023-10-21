package com.lk.backend.config;

import com.lk.common.api.ErrorCode;
import com.lk.common.exception.BusinessException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * minio文件上传
 * @Author : lk
 * @create 2023/10/23
 */
@Component
public class MinioTemplate implements InitializingBean {
    @Resource
    private OssConfig ossConfig;

    private MinioClient minioClient;

    static  final Logger logger = LoggerFactory.getLogger(MinioTemplate.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        this.minioClient =  MinioClient.builder().endpoint(ossConfig.getEndpoint())
                .credentials(ossConfig.getAccessKey(), ossConfig.getSecretKey())
                .build();
    }

    /**
     * 删除文件
     * @param objectName
     * @throws Exception
     */
    public void removeObject(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().object(objectName).bucket(ossConfig.getBucket()).build());
    }

    /**
     * 获得上传的URL
     * @param objectName
     */
    public String getPresignedObjectUrl(String objectName){
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(ossConfig.getBucket())
                            .object(objectName)
                            .expiry(1,TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }


    /**
     * 上传文件
     * @param bytes
     * @param filePath
     * @param contentType
     * @return 可访问url
     * @throws IOException
     */
    public void uploadMinio(byte[] bytes, String filePath, String contentType) throws IOException {
        InputStream input = null;
        try {
            input = new ByteArrayInputStream(bytes);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(ossConfig.getBucket())
                            .contentType(contentType)
                            .stream(input, input.available(), -1)
                            .object(filePath)
                            .build()
            );
        } catch (Exception e) {
            logger.error("minio上传文件错误：", e);
        } finally {
            if (Objects.nonNull(input)) {
                input.close();
            }
        }
    }

}
