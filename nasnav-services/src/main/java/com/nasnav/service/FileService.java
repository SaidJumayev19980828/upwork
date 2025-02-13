package com.nasnav.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.enumerations.ConvertedImageTypes;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.FileEntity;
import com.nasnav.service.model.FileUrlResource;

public interface FileService {

  void setupFileLocation() throws BusinessException;

  String saveFile(MultipartFile file, Long orgId, boolean crop);

  String saveFile(MultipartFile file, Long orgId);

  FileUrlResource getFileAsResource(String url) throws BusinessException;

  void deleteFileByUrl(String url);
  String saveFileForUser(MultipartFile file, Long userId);
  String saveFileFor3DModel(MultipartFile file, Long modelId);
  List<String> getUrlsByModelId(Long modelId);
  void deleteFile(FileEntity file);

  void deleteOrganizationFile(String fileName);

  String getResourceInternalUrlByOrg(String orgSpeificUrl, Long orgId, Integer width, Integer height, ConvertedImageTypes type);

  String getResourceInternalUrl(String url, Integer width, Integer height, ConvertedImageTypes type);

  String getResourceInternalUrl(String url);

  String getResizedImageInternalUrl(String url, Integer width, Integer height, ConvertedImageTypes type);

  MultipartFile getCommonsMultipartFile(String fieldName, String resizedFileName, String fileType,
      ByteArrayOutputStream imgOutStream) throws IOException;

  ByteArrayOutputStream getImagesInfo(Long orgId);

  void delete(String fileName, String fileUrl, Long modelId);
}