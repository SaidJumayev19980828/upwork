package com.nasnav.service;

import static com.google.common.io.Files.getFileExtension;
import static com.google.common.io.Files.getNameWithoutExtension;
import static com.nasnav.cache.Caches.FILES;
import static com.nasnav.cache.Caches.IMGS_RESIZED;
import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.constatnts.ConfigConstants.STATIC_FILES_URL;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.enumerations.Roles.ORGANIZATION_ADMIN;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.awt.SystemColor.info;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.cache.annotation.CacheResult;
import javax.imageio.ImageIO;

import com.nasnav.commons.utils.CollectionUtils;
import com.nasnav.dao.*;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.jboss.logging.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.MediaType;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.model.FileUrlResource;

@Service
public class FileService {

	private static Logger logger = Logger.getLogger(FileService.class);

	private static List<String> SUPPORTED_IMAGE_FORMATS = asList("jpg", "jpeg", "png", "webp");

	@Value("${files.basepath}")
	private String basePathStr;

	private Path basePath;

	@Autowired
	private OrganizationRepository orgRepo;
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

	@PostConstruct
	public void setupFileLocation() throws BusinessException {
		this.basePath = Paths.get(basePathStr);

		if(!Files.exists(basePath) ) {
			try {
				Files.createDirectories(basePath);
			} catch (Exception ex) {
				throw new BusinessException("Could not create the Base directory for storing files!", "", INTERNAL_SERVER_ERROR);
			}
		}
	}




	public String saveFile(MultipartFile file, Long orgId) {

		if(orgId != null && !orgRepo.existsById(orgId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$0001, orgId);
		}
		if(isBlankOrNull(file.getOriginalFilename()) ) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0008);
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




	private Path getSaveDir(Long orgId) {
		Path saveDir = ofNullable(orgId)
				.map(id -> id.toString())
				.map(basePath::resolve)
				.orElse(basePath);
		return saveDir;
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
		String url = ofNullable(orgId)
				.map(id -> String.format("%d/%s", id, origName))
				.orElse(origName);
		return url;
	}




	private String getUniqueRandomName(String origName) {
		String ext = getFileExtension(origName);
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




	private void createDirIfNotExists(Path saveDir) {
		if(!Files.exists(saveDir)) {
			try {
				Files.createDirectories(saveDir);
			} catch (IOException e) {
				logger.error(e,e);
				throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0010, saveDir);
			}
		}
	}




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




	public void deleteFileByUrl(String url) throws BusinessException{
		FileEntity file = filesRepo.findByUrl(url);

		if(file == null) 	//if file doesn't exist in database, then job's done!
			return;

		Path path = basePath.resolve(file.getLocation());

		try {
			filesRepo.delete(file);
			filesResizedRepo.deleteByOriginalFile(file);
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logger.error(e,e);
			throw new BusinessException(
					format("Failed to delete file with url[%s] at location [%s]", url, path.toString())
					, "FAILURE"
					, INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	
	@CacheResult(cacheName = FILES)
	public String getResourceInternalUrl(String url) {
		String modUrl = reformUrl(url);
		FileEntity fileInfo = filesRepo.findByUrl(modUrl);
		if(fileInfo == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0011, url);
		}
		return STATIC_FILES_URL + "/" + fileInfo.getLocation();
	}


	@CacheResult(cacheName = IMGS_RESIZED)
	public String getResizedImageInternalUrl(String url, Integer width, Integer height, String type) {
		String modUrl = reformUrl(url);
		final String fileType = getImageType(url, type)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0014, url));
		FileEntity originalFile = ofNullable(filesRepo.findByUrl(modUrl))
				.orElseThrow(() ->  new RuntimeBusinessException(NOT_FOUND, GEN$0011, url));

		if (!originalFile.getMimetype().contains("image")) {
			return STATIC_FILES_URL + "/" + originalFile.getLocation();
		}

		FilesResizedEntity resizedFile = getResizedFiles(originalFile, width, height)
				.stream()
				.filter(f -> f.getImageUrl().endsWith(fileType))
				.findFirst()
				.orElseGet(() -> createResizedImageEntity(originalFile, width, height, fileType));

		return STATIC_FILES_URL + "/" + resizedFile.getImageUrl();
	}


	private FilesResizedEntity createResizedImageEntity(FileEntity originalFile, Integer width, Integer height, String fileType) {
		try {
			this.basePath = Paths.get(basePathStr);
			Path location = basePath.resolve(originalFile.getLocation());
			File file = location.toFile();
			BufferedImage image = ImageIO.read(file);
			if(width != null && width > image.getWidth()) {
				width = image.getWidth();
			}
			if(height != null && height > image.getHeight()) {
				height = image.getHeight();
			}
			if(width != null && height != null && (height/width > 1)) {
				width = calculateWidth(height, image);
			} else if (width != null && height == null) {
				height = calculateHeight(width ,image);
			} else if (height != null && width == null) {
				width = calculateWidth(height, image);
			}
			String resizedFileName = getResizedImageName(file.getName(), width, fileType);
			MultipartFile multipartFile = resizeImage(image, width, height, fileType, resizedFileName);
			Long orgId = originalFile.getOrganization().getId();
			return saveResizedFileEntity(originalFile, multipartFile, width, height, orgId);
		}catch (Exception e) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0006, e.getMessage());
		}
	}


	private List<FilesResizedEntity> getResizedFiles(FileEntity originalFile, Integer width, Integer height) {
		List<FilesResizedEntity> resizedFiles;
		if (width == null && height == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0007);
		} else if (width != null && height == null) {
			resizedFiles = filesResizedRepo.findByOriginalFileAndWidth(originalFile, width);
		} else if (width == null ){
			resizedFiles = filesResizedRepo.findByOriginalFileAndHeight(originalFile, height);
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
		return (int)(targetHeight * (image.getWidth() / (image.getHeight() * 1.0)));
	}



	private Optional<String> getImageType(String fileName, String type) {
		if (nonNull(type) && SUPPORTED_IMAGE_FORMATS.contains(type.toLowerCase())) {
			return ofNullable(type.toLowerCase());
		}
		return ofNullable(getFileExtension(fileName));
	}



	private String getResizedImageName(String imageName, int size, String type) {
		return getNameWithoutExtension(imageName) + "-"+ size + "." + type;
	}



	private MultipartFile resizeImage(BufferedImage image, Integer targetWidth, Integer targetHeight, String fileType,
									  String resizedFileName) throws IOException {
		ByteArrayOutputStream outputImageFile = new ByteArrayOutputStream();
		Thumbnails.of(image)
				.size(targetWidth, targetHeight)
				.outputFormat(fileType)
				.toOutputStream(outputImageFile);

		MultipartFile multipartFile = new MockMultipartFile(resizedFileName,
				resizedFileName, fileType, outputImageFile.toByteArray());
		return multipartFile;
	}



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
			this.basePath = Paths.get(basePathStr);
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