package com.nasnav.service.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.google.common.net.MediaType;
import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.enumerations.ConvertedImageTypes;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.FileService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.model.FileUrlResource;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.PostConstruct;
import javax.cache.annotation.CacheResult;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.*;
import java.util.*;

import static com.google.common.io.Files.getNameWithoutExtension;
import static com.nasnav.cache.Caches.FILES;
import static com.nasnav.cache.Caches.IMGS_RESIZED;
import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.constatnts.ConfigConstants.STATIC_FILES_URL;
import static com.nasnav.enumerations.Roles.ORGANIZATION_ADMIN;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static net.coobird.thumbnailator.geometry.Positions.CENTER;
import static net.coobird.thumbnailator.resizers.Resizers.BICUBIC;
import static org.springframework.http.HttpStatus.*;

@Service
public class FileServiceImpl implements FileService {


	private static Logger logger = Logger.getLogger(FileServiceImpl.class);

	@Autowired
	private AppConfig appConfig;

	private Path basePath;

	@Autowired
	private OrganizationRepository orgRepo;

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private FilesResizedRepository filesResizedRepo;
	@Autowired
	private ProductImagesRepository productImgRepo;
	@Autowired
	private FilesRepository filesRepo;
	@Autowired
	private OrganizationImagesRepository orgImagesRepo;

	@Autowired
	private SecurityService securityService;

	@Override
	@PostConstruct
	public void setupFileLocation() throws BusinessException {
		this.basePath = Paths.get(appConfig.getBasePathStr());

		if(!Files.exists(basePath) ) {
			try {
				Files.createDirectories(basePath);
			} catch (Exception ex) {
				throw new BusinessException("Could not create the Base directory for storing files!", "", INTERNAL_SERVER_ERROR);
			}
		}
	}


	@Override
	public String saveFile(MultipartFile file, Long orgId, boolean crop) {
		if (crop) {
			try {
				var imgOutStream = new ByteArrayOutputStream();
				BufferedImage image = ImageIO.read(file.getInputStream());
				int targetWidth = 100;
				int targetHeight = 100;
				Thumbnails
						.of(image)
						.sourceRegion(CENTER, targetWidth, targetHeight)
						.scale(1.0)
						.resizer(BICUBIC)
						.outputFormat(getFileExtension(file.getBytes()).substring(1))
						.toOutputStream(imgOutStream);
				return saveFile(getCommonsMultipartFile(file.getOriginalFilename(), file.getOriginalFilename(), file.getContentType(), imgOutStream), orgId);
			} catch (Exception e) {
				logger.error(e, e);
				return saveFile(file, orgId);
			}
		}
		return saveFile(file, orgId);
	}

	@Override
	public String saveFile(MultipartFile file, Long orgId) {

		validateMimeType(file.getContentType());

		if(orgId != null && !orgRepo.existsById(orgId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001, orgId);
		}
		if(isBlankOrNull(file.getOriginalFilename()) ) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0008);
		}

		String originalFileName = file.getOriginalFilename();
		String uniqueFileName = getUniqueName(originalFileName, orgId );
		String fileUrl = getUrl(uniqueFileName, orgId);
		Path fileRelativeLocation = getRelativeLocation(uniqueFileName, orgId);

		saveFile(file, uniqueFileName, orgId );

		if(Files.exists(basePath.resolve(fileRelativeLocation) )) {
			saveToDatabase(originalFileName, fileRelativeLocation , fileUrl, orgId);
		}

		return fileUrl;
	}

	@Override
	public String saveFileForUser(MultipartFile file, Long userId) {

		validateImageMimetype(file);

		if(userId != null && !userRepo.existsById(userId)){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, userId);
		}
		if(isBlankOrNull(file.getOriginalFilename()) ) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0008);
		}

		String originalFileName = file.getOriginalFilename();
		String uniqueFileName = getUniqueNameForUser(originalFileName, userId);
		String fileUrl = getUrlForUser(uniqueFileName, userId);
		Path fileRelativeLocation = getRelativeLocationForUser(uniqueFileName, userId);

		saveFileForUser(file, uniqueFileName, userId );

		saveToDatabaseForUser(originalFileName, fileRelativeLocation , fileUrl, userId);

		return fileUrl;
	}


	@Override
	public void deleteOldFileForUserIfExists(String originalName, Long userId, String oldImageUrl){

		//get path relative save dir /customers/userId/oldImageUrl if old photo exists then delete
		Path existingFilePath = basePath.resolve("customers").resolve(userId.toString()).resolve(oldImageUrl);

		// Check if a file with the same name already exists
		if (Files.exists(existingFilePath)) {
			try {
				Files.deleteIfExists(existingFilePath);
				deleteFileByUrl(existingFilePath.toString());
			}catch (IOException e){
				logger.error(e,e);
				throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0023, oldImageUrl, existingFilePath.toString());
			}
		}
	}


	private void validateImageMimetype(MultipartFile image) {
		String mimeType = image.getContentType();
		if (!mimeType.startsWith("image"))
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0018, mimeType);
	}

	private void validateMimeType(String mimeType) {
		String fileType = mimeType.substring(0, mimeType.indexOf('/'));

		if(!fileType.equalsIgnoreCase("image") && !fileType.equalsIgnoreCase("video")){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$IMG$0010, mimeType);
		}
	}

	private void saveToDatabase(String origName, Path location, String url, Long orgId) {

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
	private void saveToDatabaseForUser(String origName, Path location, String url, Long userId){

		UserEntity user = userRepo.findById(userId).orElseThrow(()
				-> new RuntimeBusinessException(NOT_FOUND, U$0001));

		String mimeType = getMimeType(basePath.resolve(location));

		FileEntity fileEntity = new FileEntity();
		fileEntity.setLocation( location.toString().replace("\\", "/") );
		fileEntity.setOriginalFileName(origName);
		fileEntity.setUrl(url);
		fileEntity.setMimetype(mimeType);
		fileEntity.setUserId(user);

		fileEntity = filesRepo.save(fileEntity);
	}



	private String getMimeType(Path file) {
		String mimeType = MediaType.OCTET_STREAM.toString();

		Tika tika = new Tika();
		try {
			mimeType = tika.detect(file);
		} catch (IOException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0013, file.toString());
		}

		return mimeType;
	}

	private String getFileExtension(byte[] bytes) {
		try {
			String contentType = new Tika().detect(bytes);
			TikaConfig config = TikaConfig.getDefaultConfig();
			MimeType mimeType = config.getMimeRepository().forName(contentType);
			String extension = mimeType.getExtension();
			return extension;
		} catch (MimeTypeException e) {
			return "png";
		}
	}


	private Path getSaveDir(Long orgId) {
		return ofNullable(orgId)
				.map(Object::toString)
				.map(basePath::resolve)
				.orElse(basePath);
	}

	private Path getSaveDirForUser(Long userId) {
		return ofNullable(userId)
				.map(Object::toString)
				.map(s -> "customers/" + s) // add customers/ prefix
				.map(basePath::resolve)
				.orElse(basePath);
	}


	private String getUniqueName(String origName, Long orgId) {
		Optional<String> opt = Optional.of(origName)
				.map(this::sanitize)
				.filter(name -> notUniqueFileName(name, orgId))
				.map(this::getUniqueRandomName);
		if (opt.isPresent()) {
			return opt.get();
		}
		return Optional.of(origName)
				.map(this::sanitize)
				.get();
	}

	private String getUniqueNameForUser(String originalName, Long userId) {
		Optional<String> opt = Optional.of(originalName)
				.map(this::sanitize)
				.filter(name -> notUniqueFileNameForUser(name, userId))
				.map(this::getUniqueRandomName);
		if (opt.isPresent()) {
			return opt.get();
		}
		return Optional.of(originalName)
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

	private boolean notUniqueFileNameForUser(String origName, Long userId) {
		String url = getUrlForUser(origName, userId);
		Path location = getRelativeLocationForUser(origName, userId);

		return  filesRepo.existsByUrl(url)
				|| filesRepo.existsByLocation(location.toString())
				|| Files.exists(location) ;
	}


	private Path getRelativeLocation(String origName, Long orgId) {
		return basePath
				.relativize( getSaveDir(orgId) )
				.resolve(origName);
	}

	private Path getRelativeLocationForUser(String originalName, Long userId) {
		return basePath
				.relativize( getSaveDirForUser(userId) )
				.resolve(originalName);
	}

	private String getUrl(String originalName, Long orgId) {
		return ofNullable(orgId)
				.map(id -> String.format("%d/%s", id, originalName))
				.orElse(originalName);
	}

	private String getUrlForUser(String originalName, Long userId) {
		return ofNullable(userId)
				.map(id -> String.format("customers/%d/%s", id, originalName))
				.orElse("customers/"+originalName);
	}

	private String getUniqueRandomName(String origName) {
		String ext = com.google.common.io.Files.getFileExtension(origName);
		String origNameNoExtension = getNameWithoutExtension(origName);
		String uuid = UUID.randomUUID().toString().replace("-", "");
		return String.format("%s-%s.%s", origNameNoExtension, uuid , ext);
	}



	private void saveFile(MultipartFile file, String uniqeFileName, Long orgId) {
		Path saveDir = getSaveDir(orgId);
		createDirIfNotExists(saveDir);
		Path targetLocation = saveDir.resolve(uniqeFileName);
		try {
			file.transferTo(targetLocation);
		} catch (IOException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0009, saveDir);
		}
	}

	private void saveFileForUser(MultipartFile file, String uniqeFileName, Long userId) {
		if (file == null || file.isEmpty())
			throw new IllegalArgumentException("Invalid file");

		Path saveDir = getSaveDirForUser(userId);
		createDirIfNotExists(saveDir);
		Path targetLocation = saveDir.resolve(uniqeFileName);

		try {
			file.transferTo(targetLocation);
		} catch (IOException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0009, saveDir);
		}
	}


	private void createDirIfNotExists(Path saveDir) {
		if(!Files.exists(saveDir)) {
			try {
				Files.createDirectories(saveDir);
				/** It gives the required permissions for WINDOWS OS to make dirs **/
				AclFileAttributeView aclView = Files.getFileAttributeView(saveDir, AclFileAttributeView.class);
				if (aclView != null) {
					AclEntry entry = AclEntry.newBuilder()
							.setType(AclEntryType.ALLOW)
							.setPrincipal(aclView.getOwner())
							.setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA, AclEntryPermission.APPEND_DATA, AclEntryPermission.READ_ATTRIBUTES, AclEntryPermission.WRITE_ATTRIBUTES, AclEntryPermission.READ_NAMED_ATTRS, AclEntryPermission.WRITE_NAMED_ATTRS, AclEntryPermission.READ_ACL, AclEntryPermission.WRITE_ACL, AclEntryPermission.SYNCHRONIZE)
							.build();
					aclView.setAcl(Collections.singletonList(entry));
				}
			} catch (IOException e) {
				logger.error(e,e);
				throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0010, saveDir);
			}
		}
	}




	@Override
	public FileUrlResource getFileAsResource(String url) throws BusinessException {

		String modUrl = reformUrl(url);
		FileEntity fileInfo = filesRepo.findByUrl(modUrl);

		if(fileInfo == null) {
			throw new BusinessException("No file exists with url: " + url, "INVALID PARAM:url", NOT_ACCEPTABLE);
		}

		Path location = basePath.resolve(fileInfo.getLocation());

		if(!Files.exists(location)){
			throw new BusinessException("No file exists with url: " + url, "INVALID PARAM:url", NOT_ACCEPTABLE);
		}

		FileUrlResource resource = null;
		try {
			resource =  new FileUrlResource(location.toUri(), fileInfo.getMimetype(), fileInfo.getOriginalFileName());
		} catch (MalformedURLException e) {
			logger.error(e,e);
			throw new BusinessException("Failed to download file with url: " + url, "INTERNAL_ERROR", INTERNAL_SERVER_ERROR);
		}

		return resource;
	}




	private String reformUrl(String url) {
		return ofNullable(url)
				.filter(u -> u.length()> 2)
				.filter(u -> u.startsWith("/"))
				.map(u -> u.substring(1))
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0012, url));
	}

	@Override
	public void deleteFileByUrl(String url) {
		FileEntity file = filesRepo.findByUrl(url);
		deleteFile(file);
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = {FILES, IMGS_RESIZED})
	public void deleteFile(FileEntity file) {
		if(file == null) 	//if file doesn't exist in database, then job's done!
			return;

		Path path = basePath.resolve(file.getLocation());

		try {
			for(FilesResizedEntity resizedEntity : filesResizedRepo.findByOriginalFile(file)) {
				Path resizedPath = basePath.resolve(resizedEntity.getImageUrl());
				filesResizedRepo.deleteById(resizedEntity.getId());
				Files.deleteIfExists(resizedPath);
			}
			filesRepo.delete(file);
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR,GEN$0023, file.getUrl(), path.toString());
		}
	}

	@Override
	public void deleteOrganizationFile(String fileName) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		FileEntity file = filesRepo.findByUrlAndOrganization_Id(fileName, orgId);
		deleteFile(file);
	}

	private String addSlash(String url) {
		return url.startsWith("/") ? url : "/" + url;
	}

	@Override
	public String getResourceInternalUrlByOrg(String orgSpeificUrl, Long orgId, Integer width, Integer height, ConvertedImageTypes type) {
		String url = "/" + orgId + addSlash(orgSpeificUrl);
		return getResourceInternalUrl(url, width, height, type);
	}


	@Override
	public String getResourceInternalUrl(String url, Integer width, Integer height, ConvertedImageTypes type) {
		String normalizedUrl = addSlash(url);

		//TODO: why don't we support changing type without resizing?
		return width == null && height == null ? getResourceInternalUrl(normalizedUrl)
				: getResizedImageInternalUrl(normalizedUrl, width, height, type);
	}


	@Override
	@CacheResult(cacheName = FILES)
	public String getResourceInternalUrl(String url) {
		String modUrl = reformUrl(url);
		FileEntity fileInfo = filesRepo.findByUrl(modUrl);
		if(fileInfo == null) {
			throw new RuntimeBusinessException(NOT_FOUND, GEN$0011, url);
		}
		return STATIC_FILES_URL + "/" + fileInfo.getLocation();
	}


	@Override
	@CacheResult(cacheName = IMGS_RESIZED)
	public String getResizedImageInternalUrl(String url, Integer width, Integer height, ConvertedImageTypes type) {
		String modUrl = reformUrl(url);
		FileEntity originalFile = ofNullable(filesRepo.findByUrl(modUrl))
				.orElseThrow(() ->  new RuntimeBusinessException(NOT_FOUND, GEN$0011, url));
		final String fileType = getImageType(type, originalFile.getMimetype())
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0014, url));

		if (!originalFile.getMimetype().contains("image")) {
			logger.error(String.format("Couldn't resize image : file has mimetype [%s]!", originalFile.getMimetype()));
			return STATIC_FILES_URL + "/" + originalFile.getLocation();
		}
		try {
			FilesResizedEntity resizedFile = getResizedFiles(originalFile, width, height)
					.stream()
					.filter(f -> f.getImageUrl().endsWith(fileType))
					.findFirst()
					.orElseGet(() -> createResizedImageEntity(originalFile, width, height, fileType));

			return STATIC_FILES_URL + "/" + resizedFile.getImageUrl();
		} catch (Exception e) {
			logger.error("Couldn't resize image : " + e.getMessage());
			return STATIC_FILES_URL + "/" + originalFile.getLocation();
		}
	}


	private FilesResizedEntity createResizedImageEntity(FileEntity originalFile, Integer width, Integer height, String fileType) {
		try {
			Path location = basePath.resolve(originalFile.getLocation());
			File file = location.toFile();
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			BufferedImage image = ImageIO.read(file);
			int targetWidth = getProperWidth(width, height, image);
			String resizedFileName = getResizedImageName(file.getName(), targetWidth, fileType);
			MultipartFile multipartFile = resizeImage(image, targetWidth, fileType, resizedFileName, metadata);
			Long orgId = originalFile.getOrganization().getId();
			return saveResizedFileEntity(originalFile, multipartFile, width, height, orgId);
		}catch (Exception e) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0006, e.getMessage());
		}
	}


	private int getProperWidth(Integer width, Integer height, BufferedImage image) {
		int targetWidth = 10;
		if (width!= null) {
			targetWidth = width.intValue();
		}
		if(isTargetWidthGreaterThanImageWidth(width, image.getWidth())) {
			targetWidth = image.getWidth();
		}
		if(isTargetHeightGreaterThanImageHeight(height, image.getHeight())) {
			height = image.getHeight();
		}
		if(isWidthAndHeightProvided(width, height)) {
			if (isWidthRatioLessThanHeightRatio(width, height, image)) {
				targetWidth = calculateWidth(height, image);
			}
		} else if (isOnlyHeightProvided(width, height)) {
			targetWidth = calculateWidth(height, image);
		}
		return targetWidth;
	}

	private boolean isOnlyHeightProvided(Integer width, Integer height) {
		return height != null && width == null;
	}

	private boolean isOnlyWidthProvided(Integer width, Integer height) {
		return height == null && width != null;
	}

	private boolean isWidthAndHeightProvided(Integer width, Integer height) {
		return width != null && height != null;
	}

	private boolean isTargetWidthGreaterThanImageWidth(Integer width, Integer imageWidth) {
		return width != null && width > imageWidth;
	}

	private boolean isTargetHeightGreaterThanImageHeight(Integer height, Integer imageHeight) {
		return height != null && height > imageHeight;
	}

	private boolean isWidthRatioLessThanHeightRatio(Integer width, Integer height, BufferedImage image) {
		return width/(image.getWidth()*1.0) < height/(image.getHeight()*1.0);
	}


	private List<FilesResizedEntity> getResizedFiles(FileEntity originalFile, Integer width, Integer height) {
		List<FilesResizedEntity> resizedFiles;
		if (isOnlyHeightProvided(width, height) ){
			resizedFiles = filesResizedRepo.findByOriginalFileAndHeightAndWidthIsNull(originalFile, height);
		} else if (isOnlyWidthProvided(width, height)) {
			resizedFiles = filesResizedRepo.findByOriginalFileAndWidthAndHeightIsNull(originalFile, width);
		} else {
			resizedFiles = filesResizedRepo.findByOriginalFileAndHeightAndWidth(originalFile, height, width);
		}
		return resizedFiles;
	}


	private FilesResizedEntity saveResizedFileEntity(FileEntity originalFile, MultipartFile multipartFile,
													 Integer width, Integer height, Long orgId) {
		saveFile(multipartFile, multipartFile.getOriginalFilename(), orgId);

		FilesResizedEntity resizedFile = new FilesResizedEntity();
		resizedFile.setOriginalFile(originalFile);
		resizedFile.setWidth(width);
		resizedFile.setHeight(height);
		resizedFile.setImageUrl(getUrl(multipartFile.getOriginalFilename(), orgId));

		return filesResizedRepo.save(resizedFile);
	}


	private Integer calculateHeight(Integer targetWidth, BufferedImage image) {
		return (int)(targetWidth * (image.getHeight() / (image.getWidth() * 1.0)));
	}



	private Integer calculateWidth(int targetHeight, BufferedImage image) {
		return (int)Math.ceil(targetHeight * (image.getWidth() / (image.getHeight() * 1.0)));
	}



	private Optional<String> getImageType(ConvertedImageTypes type, String mimeType) {
		if (nonNull(type)) {
			return ofNullable(type.getValue());
		}
		return ofNullable(mimeType.substring(mimeType.indexOf("/") + 1));
	}



	private String getResizedImageName(String imageName, int size, String type) {
		return getNameWithoutExtension(imageName) + "-" + size + "." + type;
	}



	private MultipartFile resizeImage(BufferedImage image, Integer targetWidth, String fileType, String resizedFileName,
									  Metadata metadata) throws IOException {
		int rotation = getImageRotation(metadata);
		var imgOutStream = new ByteArrayOutputStream();
		Thumbnails.of(image)
				.width(targetWidth)
				.rotate(rotation)
				.outputFormat(fileType)
				.toOutputStream(imgOutStream);
		return getCommonsMultipartFile(resizedFileName, resizedFileName, fileType, imgOutStream);
	}

	private int getImageRotation(Metadata metadata)  {
		try {
			ExifIFD0Directory exifIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			int orientation = exifIFD0.getInt(ExifIFD0Directory.TAG_ORIENTATION);

			switch (orientation) {
				case 1: // [Exif IFD0] Orientation - Top, left side (Horizontal / normal)
					return 0;
				case 6: // [Exif IFD0] Orientation - Right side, top (Rotate 90 CW)
					return 90;
				case 3: // [Exif IFD0] Orientation - Bottom, right side (Rotate 180)
					return 180;
				case 8: // [Exif IFD0] Orientation - Left side, bottom (Rotate 270 CW)
					return 270;
				default:
					return 0;
			}
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public MultipartFile getCommonsMultipartFile(String fieldName, String resizedFileName, String fileType, ByteArrayOutputStream imgOutStream) throws IOException {
		FileItem fileItem = createFileItem(fieldName, resizedFileName, fileType);
		readIntoFileItem(imgOutStream, fileItem);
		return new CommonsMultipartFile(fileItem);
	}



	private FileItem createFileItem(String fieldName, String fileName, String fileType) {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024*1024);
		return factory.createItem(fieldName, fileType, false, fileName);
	}



	private void readIntoFileItem(ByteArrayOutputStream imgStream, FileItem fileItem) throws IOException {
		var inStream = new ByteArrayInputStream(imgStream.toByteArray());
		byte[] buffer = new byte[1024*100];
		var fos = fileItem.getOutputStream();
		int len;
		while ((len = inStream.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}
	}



	@Override
	public ByteArrayOutputStream getImagesInfo(Long orgId) {
		if (securityService.currentUserHasRole(ORGANIZATION_ADMIN) || orgId == null){
			orgId = securityService.getCurrentUserOrganizationId();
		}

		List<OrganizationImagesEntity> orgImages = orgImagesRepo.findByOrganizationEntity_Id(orgId);
		Map<String, List<OrganizationImagesEntity>> orgImagesMap = orgImages.stream().collect(groupingBy(OrganizationImagesEntity::getUri));

		List<ProductImagesEntity> prodImages = 	productImgRepo.findByProductEntity_OrganizationId(orgId);
		Map<String, List<ProductImagesEntity>> prodImagesMap = 	prodImages.stream().collect(groupingBy(ProductImagesEntity::getUri));;

		return getImagesFiles(orgImages, prodImages)
				.stream()
				.map(img -> toImageInfo(img, orgImagesMap, prodImagesMap))
				.flatMap(List::stream)
				.sorted(comparing(ImageInfo::getSize).reversed())
				.map(this::normalizeImageInfoRow)
				.collect(collectingAndThen(toList(), this::writeImagesInfoToCsv));
	}




	private List<FileEntity> getImagesFiles(List<OrganizationImagesEntity> orgImages, List<ProductImagesEntity> prodImages) {
		List<String> allUrls =
				orgImages
						.stream()
						.map(OrganizationImagesEntity::getUri)
						.collect(toList());
		prodImages
				.stream()
				.map(ProductImagesEntity::getUri)
				.forEach(allUrls::add);
		return mapInBatches(allUrls, 500
				, batch -> filesRepo.findByUrlInAndMimetypeContaining(batch, "image"));
	}




	private ByteArrayOutputStream writeImagesInfoToCsv(List<String[]> images) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		Writer outputWriter = new OutputStreamWriter(outStream);
		BeanWriterProcessor<String> rowProcessor = new BeanWriterProcessor<>(String.class);
		CsvWriterSettings settings = new CsvWriterSettings();
		settings.setRowWriterProcessor(rowProcessor);
		CsvWriter writer = new CsvWriter(outputWriter, settings);
		List<String> headers =
				asList("name", "width","height","size","id", "product_id", "product_name", "variant_id", "variant_name", "variant_barcode", "priority", "type");
		writer.writeHeaders(headers.stream().toArray(String[]::new));
		writer.writeStringRowsAndClose(images);
		return outStream;
	}




	private List<ImageInfo> toImageInfo(FileEntity originalFile, Map<String, List<OrganizationImagesEntity>> orgImages,
										Map<String, List<ProductImagesEntity>> productsImages) {
		String originalFileUrl = originalFile.getUrl();
		if (productsImages.containsKey(originalFileUrl)) {
			return toProductImageInfo(originalFile, productsImages);
		} else if (orgImages.containsKey(originalFileUrl)) {
			return toOrgImageInfo(originalFile, orgImages);
		}else {
			return emptyList();
		}
	}



	private List<ImageInfo> toOrgImageInfo(FileEntity originalFile, Map<String, List<OrganizationImagesEntity>> orgImages) {
		String originalFileUrl = originalFile.getUrl();
		return orgImages
				.get(originalFileUrl)
				.stream()
				.map(orgImg -> createOrgImageInfo(originalFile, orgImg))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}



	private List<ImageInfo> toProductImageInfo(FileEntity originalFile, Map<String, List<ProductImagesEntity>> productsImages) {
		String originalFileUrl = originalFile.getUrl();
		return productsImages
				.get(originalFileUrl)
				.stream()
				.map(prodImg -> createProductImageInfo(originalFile, prodImg))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}



	private Optional<ImageInfo> createOrgImageInfo(FileEntity originalFile, OrganizationImagesEntity orgImg){
		return createNewImageInfo(originalFile)
				.map(info -> {info.setType(orgImg.getType()); return info;});
	}




	private Optional<ImageInfo> createProductImageInfo(FileEntity originalFile, ProductImagesEntity prodImg){
		return createNewImageInfo(originalFile)
				.map(info -> addProductAndVariantInfo(info, prodImg));
	}



	private ImageInfo addProductAndVariantInfo(ImageInfo info, ProductImagesEntity prodImg){
		addProductInfo(info, prodImg);
		if (prodImg.getProductVariantsEntity() != null) {
			addVariantInfo(info, prodImg.getProductVariantsEntity());
		}
		return info;
	}




	private Optional<ImageInfo> createNewImageInfo(FileEntity originalFile) {
		try{
			this.basePath = Paths.get(appConfig.getBasePathStr());
			Path location = basePath.resolve(originalFile.getLocation());
			File file = location.toFile();
			BufferedImage image = ImageIO.read(file);
			ImageInfo imgInfo =  new ImageInfo(file.getName(), image.getWidth(), image.getHeight(), file.length()/1024 +" KB");
			return Optional.of(imgInfo);
		}catch(Throwable e){
			logger.error(e,e);
			return empty();
		}
	}



	private void addProductInfo(ImageInfo info, ProductImagesEntity prodImg) {
		info.setId(prodImg.getId());
		info.setProductId(prodImg.getProductEntity().getId());
		info.setProductName(prodImg.getProductEntity().getName());
		info.setPriority(prodImg.getPriority());
		info.setType(prodImg.getType());
	}




	private void addVariantInfo(ImageInfo info, ProductVariantsEntity variant) {
		info.setVariantId(variant.getId());
		info.setVariantName(variant.getName());
		info.setVariantBarcode(variant.getBarcode());
	}




	private String[] normalizeImageInfoRow(ImageInfo info) {
		return new String[]{info.getName(),info.getWidth()+"",info.getHeight()+"",info.getSize()+"",
				info.getId()+"",info.getProductId()+"",info.getProductName(),
				info.getVariantId()+"",info.getVariantName(),info.getVariantBarcode(),
				info.getPriority()+"",info.getType()+""};
	}
}


@Data
@NoArgsConstructor
@AllArgsConstructor
class ImageInfo {
	private String name;
	private Integer width;
	private Integer height;
	private String size;
	private Long id;
	private Long productId;
	private String productName;
	private Long variantId;
	private String variantName;
	private String variantBarcode;
	private Integer priority;
	private Integer type;

	ImageInfo(String name, Integer width, Integer height, String size) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.size = size;
	}
}
