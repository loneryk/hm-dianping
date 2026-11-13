package com.hmdp.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.Result;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {

    @PostMapping("blog")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        try {
            if (image == null || image.isEmpty()) {
                return Result.fail("文件不能为空");
            }
            String originalFilename = image.getOriginalFilename();
            String fileName = createNewFileName(originalFilename);
            File targetFile = new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName);
            File parentFile = targetFile.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            image.transferTo(targetFile);
            log.debug("文件上传成功，{}", fileName);
            return Result.ok(fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @GetMapping("/blog/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        if (StrUtil.isBlank(filename) || filename.contains("..") || !filename.startsWith("/blogs/")) {
            return Result.fail("错误的文件名称");
        }
        File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, filename);
        if (file.isDirectory()) {
            return Result.fail("错误的文件名称");
        }
        FileUtil.del(file);
        return Result.ok();
    }

    private String createNewFileName(String originalFilename) {
        String suffix = resolveSuffix(originalFilename);
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }

    private String resolveSuffix(String originalFilename) {
        if (StrUtil.isBlank(originalFilename)) {
            return "jpg";
        }
        String fileName = originalFilename.replace('\\', '/');
        int index = fileName.lastIndexOf('/');
        if (index >= 0) {
            fileName = fileName.substring(index + 1);
        }
        int suffixIndex = fileName.lastIndexOf('.');
        if (suffixIndex <= 0 || suffixIndex == fileName.length() - 1) {
            return "jpg";
        }
        String suffix = fileName.substring(suffixIndex + 1).trim().toLowerCase(Locale.ROOT);
        if (StrUtil.isBlank(suffix) || "undefined".equalsIgnoreCase(suffix) || "null".equalsIgnoreCase(suffix)) {
            return "jpg";
        }
        return suffix;
    }
}
