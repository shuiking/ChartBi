package com.lk.backend.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.lk.backend.config.MinioTemplate;
import com.lk.backend.model.dto.file.UploadFileRequest;
import com.lk.backend.model.entity.User;
import com.lk.backend.model.enums.FileUploadBizEnum;

import com.lk.backend.service.UserService;
import com.lk.common.api.BaseResponse;
import com.lk.common.api.ErrorCode;
import com.lk.common.api.ResultUtils;
import com.lk.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.Date;

/**
 * 文件上传接口
 * @Author : lk
 * @create 2023/10/15
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    @Resource
    private MinioTemplate minioTemplate;
    /**
     * 上传的文件夹(根据时间确定)
     */
    public static final String NORM_DAY_PATTERN = "yyyy/MM/dd";


    /**
     * 文件上传
     * @param multipartFile
     * @param uploadFileRequest
     * @return
     * @throws Exception
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile, UploadFileRequest uploadFileRequest) throws Exception {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 文件大小、格式校验
        validFile(multipartFile, fileUploadBizEnum);

        // 文件目录
        String dir = DateUtil.format(new Date(), NORM_DAY_PATTERN)+ "/";
        String fileName = IdUtil.simpleUUID();

        minioTemplate.uploadMinio(multipartFile.getBytes(), dir+fileName, multipartFile.getContentType());
        String url = minioTemplate.getPresignedObjectUrl(dir+fileName);
        System.out.println(url);
        return ResultUtils.success(url);
    }

    /**
     * 校验文件
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 5*1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 5M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

}
