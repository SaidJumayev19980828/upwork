package com.nasnav.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.tika.Tika;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.MediaType;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.FilesRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.FileEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.service.model.FileUrlResource;

@Service
public class FileService {
	
	private static Logger logger = Logger.getLogger(FileService.class);
	
	@Value("${files.basepath}")
	private String basePathStr;
	
	private Path basePath;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	
	@Autowired
	private FilesRepository filesRepo;
	
	@PostConstruct
	public void setupFileLocation() throws BusinessException {
		this.basePath = Paths.get(basePathStr);
		
		if(!Files.exists(basePath) ) {
			try {
	            Files.createDirectories(basePath);
	        } catch (Exception ex) {
	            throw new BusinessException("Could not create the Base directory for storing files!", "", HttpStatus.INTERNAL_SERVER_ERROR);
	        }
		}
	}
	
	
	
	
	public String saveFile(MultipartFile file, Long orgId) throws BusinessException {
		
		if(orgId != null && !orgRepo.existsById(orgId)) {
			throw new BusinessException("No Organization exists with id: " + orgId, "INVALID PARAM:org_id", HttpStatus.NOT_ACCEPTABLE);									
		}
		if(StringUtils.isBlankOrNull(file.getOriginalFilename()) ) {
			throw new BusinessException("No file name provided!", "INVALID PARAM:file", HttpStatus.NOT_ACCEPTABLE);
		}
		
		String origName = file.getOriginalFilename();	
		String uniqeFileName = getUniqueName(origName, orgId );
		String url = getUrl(uniqeFileName, orgId);
		Path location = getRelativeLocation(uniqeFileName, orgId); 		

		saveFile(file, uniqeFileName, orgId );
		
		if(Files.exists(basePath.resolve(location) )) {
			saveToDatabase(origName, location , url, orgId);
		}	
		
		return url;
	}




	private void saveToDatabase(String origName, Path location, String url, Long orgId) throws BusinessException {		
		
		OrganizationEntity org = orgRepo.findOneById(orgId);
		String mimeType = getMimeType(basePath.resolve(location));
		
		FileEntity fileEntity = new FileEntity();
		fileEntity.setLocation( location.toString().replace("\\", "/") );
		fileEntity.setOriginalFileName(origName);
		fileEntity.setUrl(url);
		fileEntity.setMimetype(mimeType);
		fileEntity.setOrganization(org);
		
		fileEntity = filesRepo.save(fileEntity);
	}




	private String getMimeType(Path file) throws BusinessException {
		String mimeType = MediaType.OCTET_STREAM.toString();
		
		Tika tika = new Tika();
		try {
			mimeType = tika.detect(file);
		} catch (IOException e) {
			logger.error(e,e);
			throw new BusinessException("Failed to parse MIME type for the file: "+ file, "INTERNAL ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return mimeType;
	}




	private Path getSaveDir(Long orgId) {
		Path saveDir = Optional.ofNullable(orgId)
							.map(id -> id.toString())
							.map(basePath::resolve)
							.orElse(basePath);
		return saveDir;
	}




	private String getUniqueName(String origName, Long orgId) {		
		return	Optional.of(origName)					
							.map(this::sanitize) 
							.filter(name -> notUniqueFileName(name, orgId))					
							.map(this::getUniqueRandomName)					
						.or( () -> Optional.of(origName))
							.map(this::sanitize) 
							.get();
	}


	
	private String sanitize(String name) {	
		return StringUtils.getFileNameSanitized(name);
	}


	private boolean notUniqueFileName(String origName, Long orgId) {
		String url = getUrl(origName, orgId);		
		Path location = getRelativeLocation(origName, orgId);		
		
		return  filesRepo.existsByUrl(url) 
					|| filesRepo.existsByLocation(location.toString())
					|| Files.exists(location) ;
	}




	private Path getRelativeLocation(String origName, Long orgId) {
		return basePath.relativize( getSaveDir(orgId) )
						.resolve(origName);
	}




	private String getUrl(String origName, Long orgId) {
		String url = Optional.ofNullable(orgId)
								.map(id -> String.format("%d/%s", id, origName))
								.orElse(origName);
		return url;
	}




	private String getUniqueRandomName(String origName) {
		String ext = com.google.common.io.Files.getFileExtension(origName);
		String origNameNoExtension = com.google.common.io.Files.getNameWithoutExtension(origName);
		String uuid = UUID.randomUUID().toString().replace("-", "");
		return String.format("%s-%s.%s", origNameNoExtension, uuid , ext);
	}




	private void saveFile(MultipartFile file, String uniqeFileName, Long orgId) throws BusinessException {
		Path saveDir = getSaveDir(orgId);
		createDirIfNotExists(saveDir);
		Path targetLocation = saveDir.resolve(uniqeFileName);		
		try {
			file.transferTo(targetLocation);
		} catch (IOException e) {
			logger.error(e,e);
			throw new BusinessException("Failed to save file Organization directory at location : " + saveDir, "FAILED TO SAVE FILE", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}




	private void createDirIfNotExists(Path saveDir) throws BusinessException {
		if(!Files.exists(saveDir)) {
			try {
				Files.createDirectories(saveDir);
			} catch (IOException e) {				
				logger.error(e,e);
				throw new BusinessException("Failed to create directory at location : " + saveDir, "FAILED TO CREATE DIRECTORY", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}




	public FileUrlResource getFileAsResource(String url) throws BusinessException {
				
		String modUrl = reformUrl(url);
		FileEntity fileInfo = filesRepo.findByUrl(modUrl);
		
		if(fileInfo == null) {
			throw new BusinessException("No file exists with url: " + url, "INVALID PARAM:url", HttpStatus.NOT_ACCEPTABLE);
		}
		
		Path location = basePath.resolve(fileInfo.getLocation());
		
		if(!Files.exists(location)){
			throw new BusinessException("No file exists with url: " + url, "INVALID PARAM:url", HttpStatus.NOT_ACCEPTABLE);
		}
		
		FileUrlResource resource = null;
		try {
			resource =  new FileUrlResource(location.toUri(), fileInfo.getMimetype(), fileInfo.getOriginalFileName());
		} catch (MalformedURLException e) {
			logger.error(e,e);
			throw new BusinessException("Failed to download file with url: " + url, "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
		}
			
		return resource;
	}




	private String reformUrl(String url) throws BusinessException {
		return Optional.ofNullable(url)
							.filter(u -> u.length()> 2)
							.filter(u -> u.startsWith("/"))
							.map(u -> u.substring(1))
						.orElseThrow(() -> new BusinessException("Invalid URL : " + url, "INVALID PARAM:url", HttpStatus.NOT_ACCEPTABLE));
	}
}
