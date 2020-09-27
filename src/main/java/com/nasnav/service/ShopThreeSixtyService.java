package com.nasnav.service;

import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.imageio.ImageIO;

import com.google.common.primitives.Shorts;
import com.nasnav.dao.*;
import com.nasnav.dto.request.ProductPositionDTO;
import com.nasnav.dto.response.PostProductPositionsResponse;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.apache.tika.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.Prices;
import com.nasnav.dto.ShopFloorDTO;
import com.nasnav.dto.ShopFloorsRequestDTO;
import com.nasnav.dto.ShopScenesRequestDTO;
import com.nasnav.dto.ShopSectionsRequestDTO;
import com.nasnav.dto.ShopThreeSixtyDTO;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ShopResponse;

@Service
public class ShopThreeSixtyService {

    @Value("${files.basepath}")
    private String basePathStr;

    private Path basePath;

    @Autowired
    private ShopsRepository shopRepo;

    @Autowired
    private ShopThreeSixtyRepository shop360Repo;

    @Autowired
    private ShopFloorsRepository shopFloorsRepo;

    @Autowired
    private ShopSectionsRepository sectionsRepo;

    @Autowired
    private ShopScenesRepository scenesRepo;

    @Autowired
    private FilesRepository filesRepo;

    @Autowired
    private ProductRepository productsRepo;

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private Product360ShopsRepository product360ShopsRepo;

    @Autowired
    private SecurityService securitySvc;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileService fileSvc;

    public String getShop360JsonInfo(Long shopId, String type, Boolean publish) {
        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);
        if (shop == null)
            return null;

        String data = "";
        if(type.equals("web")) {
            if (publish)
                data = getJsonDataStringSerlizable(shop.getWebJsonData());
            else
                data = getJsonDataStringSerlizable(shop.getPreviewJsonData());
        }
        else if (type.equals("mobile"))
            data = getJsonDataStringSerlizable(shop.getMobileJsonData());


        return data;
    }


    // ! custom modifier to deal with mailformed json data in shop360s !
    private String getJsonDataStringSerlizable(String oldJsonDataString) {
        String jsonDataString = oldJsonDataString;

        if (jsonDataString == null)
            return null;

        if (jsonDataString.startsWith("--- '") && (jsonDataString.endsWith("'\n") || jsonDataString.endsWith("'\\n")))
            jsonDataString = jsonDataString.substring(jsonDataString.indexOf("'")+1,jsonDataString.lastIndexOf("'"));

        return jsonDataString.replaceAll("\n", "");
    }


    public ProductsPositionDTO getProductsPositions(Long shopId, short published, Long sceneId, Long sectionId, Long floorId) {
        List<ProductPositionDTO> productPositions = product360ShopsRepo.findProductsPositionsFullData(shopId, published);

        Map<Long, ProductPositionDTO> products =  productPositions
                .stream()
                .filter(p -> p.getProductType() == 0)
                .collect(toMap(ProductPositionDTO::getId, p -> p));

        Map<Long, ProductPositionDTO> collections =  productPositions
                .stream()
                .filter(p -> p.getProductType() == 2)
                .collect(toMap(ProductPositionDTO::getId, p -> p));

        ProductsPositionDTO response = new ProductsPositionDTO();
        response.setShopId(shopId);
        response.setProductsData(products);
        response.setCollectionsData(collections);

        return response;
    }


    public List<ShopFloorDTO> getSections(Long shopId) {
        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);
        if (shop == null)
            throw new RuntimeBusinessException(NOT_FOUND, S$360$0001);

        List<ShopFloorDTO> floors = shopFloorsRepo.findByShopThreeSixtyEntity_Id(shop.getId())
                                                          .stream()
                                                          .map(f -> (ShopFloorDTO) f.getRepresentation())
                                                          .sorted(Comparator.comparing(f -> getFloorNumber(f.getNumber())))
                                                          .collect(toList());
        return floors;
    }


    private Integer getFloorNumber(Integer floorNumber) {
        if (floorNumber == null)
            return Integer.MAX_VALUE;
        return floorNumber;
    }


    public ShopThreeSixtyDTO getThreeSixtyShops(Long shopId) {
        return (ShopThreeSixtyDTO) ofNullable(shop360Repo.getFirstByShopsEntity_Id(shopId))
                                             .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,S$360$0001))
                                             .getRepresentation();
    }


    public ShopResponse updateThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) throws BusinessException {
        if (shopThreeSixtyDTO.getId() == null)
            return createThreeSixtyShop(shopThreeSixtyDTO);
        else
            return modifyThreeSixtyShop(shopThreeSixtyDTO);
    }


    private ShopResponse createThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) throws BusinessException {
        if (shop360Repo.getFirstByShopsEntity_Id(shopThreeSixtyDTO.getShopId()) != null)
            throw new BusinessException("There exists shop360 attached to this shop already!",
                    "INVALID_PARAM: shop_id", NOT_ACCEPTABLE);
        ShopThreeSixtyEntity entity = new ShopThreeSixtyEntity();
        return saveShopThreeSixtyEntity(entity, shopThreeSixtyDTO.getName(), shopThreeSixtyDTO.getShopId());
    }


    private ShopResponse modifyThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) throws BusinessException {
        Optional<ShopThreeSixtyEntity> optionalEntity = shop360Repo.findById(shopThreeSixtyDTO.getId());
        if (!optionalEntity.isPresent())
            throw new BusinessException("Provided shop_id doesn't match any existing shop360s","INVALID_PARAM: id",
                    NOT_ACCEPTABLE);
        ShopThreeSixtyEntity entity = optionalEntity.get();
        return saveShopThreeSixtyEntity(entity, shopThreeSixtyDTO.getName(), shopThreeSixtyDTO.getShopId());
    }


    private ShopResponse saveShopThreeSixtyEntity(ShopThreeSixtyEntity entity, String shopName, Long shopId) throws BusinessException {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopsEntity shop = shopRepo.findByIdAndOrganizationEntity_IdAndRemoved(shopId, orgId, 0);
        if (shop == null) {
            if (entity.getShopsEntity() == null)
                throw new BusinessException("Must provide shop_id to attach shop360s to it",
                        "INVALID_PARAM: shop_id", NOT_ACCEPTABLE);
        } else {
            entity.setShopsEntity(shop);
        }
        entity.setSceneName(shopName);
        shop360Repo.save(entity);
        return new ShopResponse(entity.getId(), OK);
    }


    public ShopResponse updateThreeSixtyShopJsonData(Long shopId, String type, String dataDTO)
            throws BusinessException, UnsupportedEncodingException {

        validateJsonData(shopId, type);

        ShopThreeSixtyEntity shopEntity = shop360Repo.getFirstByShopsEntity_Id(shopId);

        validateShop360Link(shopEntity);

        if (type.equals("web"))
            shopEntity.setPreviewJsonData(decodeUrl(dataDTO));
        else if (type.equals("mobile"))
            shopEntity.setMobileJsonData(decodeUrl(dataDTO));
        else
            throw new BusinessException("Provide type "+type+" is invalid",
                    "INVALID_PARAM: type", NOT_ACCEPTABLE);

        shop360Repo.save(shopEntity);
        return new ShopResponse(shopEntity.getId(), OK);
    }


    private String decodeUrl(String url) throws UnsupportedEncodingException {
        return URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
    }


    private void validateJsonData(Long shopId, String type) throws BusinessException {
        if (shopId == null)
            throw new BusinessException("Required shop_id is missing!",
                    "MISSING_PARAM: shop_id", NOT_ACCEPTABLE);

        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);
        validateShop360Link(shop);

        if (type == null)
            throw new BusinessException("Must provide type for JsonData (web or mobile)",
                    "MISSING_PARAM: type", NOT_ACCEPTABLE);

    }


    @Transactional
    public PostProductPositionsResponse updateThreeSixtyShopProductsPositions(Long shopId,  List<ProductPositionDTO> json) throws BusinessException {
        Long orgId = securitySvc.getCurrentUserOrganizationId();

        validateProductPositionsUpdateDTO(shopId);

        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);

        Map<Long, Shop360ProductsEntity> productsMap = getShop360ProductsMap(shopId);

        List<Long> missingProducts = new ArrayList<>();
        List<Long> missingScenes = new ArrayList<>();

        createShop360ProductsEntity(productsMap, json, shop, orgId, missingProducts, missingScenes);

        return new PostProductPositionsResponse(missingProducts, missingScenes);
    }


    private void createShop360ProductsEntity(Map<Long, Shop360ProductsEntity> productsMap, List<ProductPositionDTO> json,
                                             ShopThreeSixtyEntity shop,Long orgId, List<Long> missingProducts, List<Long> missingScenes) {
        for(ProductPositionDTO dto : json) {
            Shop360ProductsEntity product;
            if (productsMap.get(dto.getId()) != null) {
                product = productsMap.get(dto.getId());
            } else {
                Optional<ProductEntity> optionalProductEntity = productsRepo
                        .findByIdAndOrganizationId(dto.getId(), orgId);
                if (!optionalProductEntity.isPresent()) {
                    missingProducts.add(dto.getId());
                    continue;
                }
                product = new Shop360ProductsEntity();
                product.setShopEntity(shop.getShopsEntity());
                product.setProductEntity(optionalProductEntity.get());
            }

            Optional<ShopScenesEntity> optionalScene = scenesRepo.findByIdAndOrganizationEntity_Id(dto.getSceneId(), orgId);
            if (!optionalScene.isPresent()) {
                missingScenes.add(dto.getSceneId());
                continue;
            }
            product = addProductAdditionalData(product, optionalScene.get(), dto);
            product360ShopsRepo.save(product);
        }
    }


    private Shop360ProductsEntity addProductAdditionalData(Shop360ProductsEntity product, ShopScenesEntity scene, ProductPositionDTO dto) {
        product.setScene(scene);
        product.setSection(scene.getShopSectionsEntity());
        product.setFloor(scene.getShopSectionsEntity().getShopFloorsEntity());
        product.setPitch(dto.getPitch());
        product.setYaw(dto.getYaw());
        product.setPublished((short)1);

        return product;
    }


    private Map<Long, Shop360ProductsEntity> getShop360ProductsMap(Long shopId) {
        List<Shop360ProductsEntity> shop360ProductsEntities = product360ShopsRepo.findProductsPositionsByShopId(shopId);

        return shop360ProductsEntities
                .stream()
                .collect(toMap(e -> e.getProductEntity().getId(), e -> e));
    }

    private void validateProductPositionsUpdateDTO(Long shopId) throws BusinessException {

        if (shopId == null)
            throw new BusinessException("Required parameter (shop_id) is missing!",
                    "MISSING_PARAM: shop_id", NOT_ACCEPTABLE);

        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);

        validateShop360Link(shop);
    }


    private void validateShop360Link(ShopThreeSixtyEntity shop) throws BusinessException {
        if (shop == null)
            throw new BusinessException("No 360 shops linked to shop_id!",
                    "INVALID_PARAM: shop_id", NOT_ACCEPTABLE);

        Long orgId = securitySvc.getCurrentUserOrganizationId();

        if (!shop.getShopsEntity().getOrganizationEntity().getId().equals(orgId))
            throw new BusinessException("360 shop is not under organization",
                    "INVALID_PARAM: shop_id", NOT_ACCEPTABLE);
    }


    @Transactional
    public ShopResponse updateThreeSixtyShopSections(Long shopId, List<ShopFloorsRequestDTO> jsonDTO) throws BusinessException, IOException {
        OrganizationEntity org = securitySvc.getCurrentUserOrganization();

        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);

        validateShop360Link(shop);

        Map<String, List<String>> resizedImagesMap = generateImageUrls(org.getId(),jsonDTO);
        //clearOldShop360Date(org.getId(), shop.getId());
        createShop360Floor(org, shop.getId(), jsonDTO, resizedImagesMap);
        return new ShopResponse(shopId, OK);
    }



    private void clearOldShop360Date(Long orgId, Long viewId) {
        shopFloorsRepo.deleteByShopThreeSixtyEntity_IdAndOrganizationEntity_id(viewId, orgId);
    }


    private Long createShop360Floor(OrganizationEntity org, Long viewId, List<ShopFloorsRequestDTO> dto,
                                    Map<String, List<String>> resizedImagesMap) throws BusinessException {
        ShopThreeSixtyEntity shop = shop360Repo.findById(viewId).get();
        ShopFloorsEntity floor;

        for(ShopFloorsRequestDTO floorDTO : dto) {
            if (floorDTO.getId() == null) {
                floor = new ShopFloorsEntity();
                if (floorDTO.getNumber() == null)
                    throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$F$0001, floorDTO.getId());
            }
            else {

                if (!shopFloorsRepo.existsById(floorDTO.getId()))
                    throw new BusinessException("Provided floor No. " + floorDTO.getId() + " doesn't exist!",
                            "INVALID_PARAM: floor_id", NOT_ACCEPTABLE);

                floor = shopFloorsRepo.findById(floorDTO.getId()).get();
                if (!floor.getShopThreeSixtyEntity().getId().equals(shop.getId()))
                    throw new BusinessException("Provided floor No. " + floorDTO.getId() + " doesn't belong to shop No. "+shop.getId(),
                            "INVALID_PARAM: floor_id", NOT_ACCEPTABLE);
            }
            if (floorDTO.getName() != null)
                floor.setName(floorDTO.getName());
            if (floorDTO.getNumber() != null)
                floor.setNumber(floorDTO.getNumber());
            floor.setShopThreeSixtyEntity(shop);
            floor.setOrganizationEntity(org);

            ShopFloorsEntity savedFloor = shopFloorsRepo.save(floor);
            List<ShopSectionsRequestDTO> sectionsDTO = getSections(floorDTO);
            for (int i=0;i<sectionsDTO.size();i++) {
                createShop360Section(sectionsDTO.get(i), savedFloor, org, resizedImagesMap, i);
            }
        }

        return shop.getId();
    }


    private void createShop360Section(ShopSectionsRequestDTO dto, ShopFloorsEntity floor, OrganizationEntity org,
                                      Map<String, List<String>> resizedImagesMap, int index) throws BusinessException {
        ShopSectionsEntity section;
        if (dto.getId() == null)
            section = new ShopSectionsEntity();
        else {
            if (!sectionsRepo.existsById(dto.getId()))
                throw new BusinessException("Provided section No. " + dto.getId() + " doesn't exist!",
                        "INVALID_PARAM: section_id", NOT_ACCEPTABLE);

            section = sectionsRepo.findById(dto.getId()).get();

            if (!section.getShopFloorsEntity().getId().equals(floor.getId()))
                throw new BusinessException("Provided section No. " + dto.getId() + " doesn't belong to floor No. "+floor.getId(),
                        "INVALID_PARAM: section_id", NOT_ACCEPTABLE);
        }
        if (dto.getName() != null)
            section.setName(dto.getName());

        if (dto.getPriority() != null) {
            section.setPriority(dto.getPriority());
        } else {
            section.setPriority(index);
        }

        if (dto.getImageUrl() != null)
            section.setImage(dto.getImageUrl());

        section.setShopFloorsEntity(floor);
        section.setOrganizationEntity(org);
        ShopSectionsEntity savedSection = sectionsRepo.save(section);
        List<ShopScenesRequestDTO> scenesDTO = getScenes(dto);
        for(int i=0;i<scenesDTO.size();i++) {
        	createShop360Scene(scenesDTO.get(i), savedSection, org, resizedImagesMap, i);
        }
    }


    private void createShop360Scene(ShopScenesRequestDTO dto, ShopSectionsEntity section, OrganizationEntity org,
                                    Map<String, List<String>> resizedImagesMap, int index) throws BusinessException {
        ShopScenesEntity scene;
        if (dto.getId() == null)
            scene = new ShopScenesEntity();
        else {
            if (sectionsRepo.existsById(dto.getId()))
                throw new BusinessException("Provided scene No. " + dto.getId() + " doesn't exist!",
                        "INVALID_PARAM: scene_id", NOT_ACCEPTABLE);

            scene = scenesRepo.findById(dto.getId()).get();

            if (!scene.getShopSectionsEntity().getId().equals(section.getId()))
                throw new BusinessException("Provided scene No. " + dto.getId() + " doesn't belong to section No. " + section.getId(),
                        "INVALID_PARAM: scene_id", NOT_ACCEPTABLE);
        }

        if (dto.getName() != null)
            scene.setName(dto.getName());

        if(dto.getPriority() != null) {
            scene.setPriority(dto.getPriority());
        } else {
            scene.setPriority(index);
        }

        if(dto.getImageUrl() != null) {
            scene.setImage(dto.getImageUrl());
            if (resizedImagesMap.get(dto.getImageUrl()) != null)
                if (!resizedImagesMap.get(dto.getImageUrl()).isEmpty()) {
                    scene.setThumbnail(resizedImagesMap.get(dto.getImageUrl()).get(0));
                    scene.setResized(resizedImagesMap.get(dto.getImageUrl()).get(1));
                }
        }

        scene.setShopSectionsEntity(section);
        scene.setOrganizationEntity(org);
        scenesRepo.save(scene);
    }

    private Map<String, List<String>> generateImageUrls(Long orgId, List<ShopFloorsRequestDTO> dto) throws BusinessException, IOException {

        List<String> urls = getSectionsAndScenesImages(dto);

        Map<String, List<String>> resizedImagesMap = new HashMap<>();
        for(String url: urls) {
            FileEntity image = filesRepo.findByUrl(url);

            if(image == null)
                throw new BusinessException("The image_url("+url+") doesn't exist!",
                        "INVALID_PARAM: image_url", NOT_ACCEPTABLE);

            String resized1024 = getResizedImageName(url, 1024);

            String resized4096 = getResizedImageName(url, 4096);

            if(filesRepo.findByUrl(resized1024) == null)
                resized1024 = resizeImage(1024, image, orgId);

            if(filesRepo.findByUrl(resized4096) == null)
                resized4096 = resizeImage(4096, image, orgId);

            if (resizedImagesMap.get(url) == null) {
                List<String> resizedImgs = new ArrayList<>();
                resizedImgs.add(resized1024);
                resizedImgs.add(resized4096);
                resizedImagesMap.put(url, resizedImgs);
            }
        }
        return resizedImagesMap;
    }


	private List<String> getSectionsAndScenesImages(List<ShopFloorsRequestDTO> dto) {
		List<String> urls = new ArrayList<>();
        for(ShopFloorsRequestDTO floor: dto) {
        	List<ShopSectionsRequestDTO> sections = getSections(floor);
            for(ShopSectionsRequestDTO section: sections) {
                if(section.getImageUrl() != null) {
                    urls.add(section.getImageUrl());
                }
                List<ShopScenesRequestDTO> scences = getScenes(section);
                for (ShopScenesRequestDTO scene : scences) {
                    if(scene.getImageUrl() != null) {
                        urls.add(scene.getImageUrl());
                    }
                }
            }
        }
		return urls;
	}


	private List<ShopSectionsRequestDTO> getSections(ShopFloorsRequestDTO floor) {
		return ofNullable(floor)
		.map(ShopFloorsRequestDTO::getShopSections)
		.orElse(emptyList());
	}


	private List<ShopScenesRequestDTO> getScenes(ShopSectionsRequestDTO section) {
		return ofNullable(section)
		.map(ShopSectionsRequestDTO::getShopScenes)
		.orElse(emptyList());
	}


    private String resizeImage(int imageWidth, FileEntity image, Long orgId) throws IOException, BusinessException {
        this.basePath = Paths.get(basePathStr);
        Path location = basePath.resolve(image.getLocation());

        BufferedImage inputImage = ImageIO.read(new File(location.toString()));
        int imageHeight = (int) (imageWidth * (inputImage.getHeight()/(inputImage.getWidth()*1.0)));
        BufferedImage outputImage = new BufferedImage(imageWidth,
                imageHeight, inputImage.getType());


        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, imageWidth, imageHeight, null);
        g2d.dispose();

        String imageName = getResizedImageName(image.getUrl().substring(image.getUrl().lastIndexOf('/')+1), imageWidth);
        File outputImageFile = new File(imageName);
        ImageIO.write(outputImage, "jpg", outputImageFile );

        FileInputStream output = new FileInputStream(outputImageFile);
        MultipartFile multipartFile = new MockMultipartFile("fileItem",
                outputImageFile.getName(), "image/jpg", IOUtils.toByteArray(output));

        return fileSvc.saveFile(multipartFile, orgId);
    }

    private String getResizedImageName(String imageName, int size) {
        return imageName.substring(0, imageName.lastIndexOf("."))
                + "-resized"+ size +
                imageName.substring(imageName.lastIndexOf("."));
    }


    public LinkedHashMap getShop360Products(Long shopId, String name, Integer count, Integer productType,
                                            Short published, boolean has360) throws BusinessException {
        if (!shopRepo.existsById(shopId))
            throw new BusinessException("Provided shop_id doesn't match any existing shop!",
                    "INVALID_PARAM: shop_id", NOT_ACCEPTABLE);

        name = ofNullable(name).map(String::toLowerCase).orElse("");
        List<ThreeSixtyProductsDTO> products = new ArrayList<>();
        List<ThreeSixtyProductsDTO> collections = new ArrayList<>();

        List<Short> publishedFilter = normalizePublishedFilter(published);
        return fetchProductsAndCollections(shopId, name, productType, has360, count, publishedFilter, products, collections);
    }


    private LinkedHashMap fetchProductsAndCollections(Long shopId, String name, Integer productType, boolean has360, Integer count,
                                                      List<Short> publishedFilter, List<ThreeSixtyProductsDTO> products,
                                                      List<ThreeSixtyProductsDTO> collections) {
        if (isFindProductsRequired(productType)) {
            products = productsRepo.find360Products(name, shopId, has360, publishedFilter, PageRequest.of(0, count));
            if (products.size() > 0) {
                products = getProductsListAdditionalData(products);
            }
        }

        if (isFindCollectionsRequired(productType)) {
            collections = productsRepo.find360Collections(name, shopId, has360, publishedFilter, PageRequest.of(0, count));
            if (collections.size() > 0) {
                collections = getCollectionsListAdditionalData(collections);
            }
        }

        products.addAll(collections);
        if (products.size() > count) {
            products = products.subList(0,count);
        }
        LinkedHashMap response = new LinkedHashMap();
        response.put("products", products);
        return response;
    }


    private boolean isFindProductsRequired(Integer productType) {
        return productType == null || productType == 0;
    }


    private boolean isFindCollectionsRequired(Integer productType) {
        return productType == null || productType == 2;
    }


    private List<ThreeSixtyProductsDTO> getProductsListAdditionalData(List<ThreeSixtyProductsDTO> products) {
        List<Long> productIds = products.stream().map(p -> p.getId()).collect(toList());
        Map<Long, List<ProductImagesEntity>> productsImagesMap = productImageService.getProductsImageList(productIds);
        Map<Long, Prices> productsPricesMap = stockRepo.getProductsPrices(productIds)
                .stream()
                .collect(toMap(Prices::getId, p -> new Prices(p.getMinPrice(), p.getMaxPrice())));

        return setPricesAndImages(products, productsImagesMap, productsPricesMap);
    }


    private List<ThreeSixtyProductsDTO> getCollectionsListAdditionalData(List<ThreeSixtyProductsDTO> collections) {
        List<Long> collectionIds = collections.stream().map(p -> p.getId()).collect(toList());

        Map<Long, List<ProductImagesEntity>> collectionsImagesMap = productImageService.getProductsImageList(collectionIds);
        Map<Long, Prices> collectionsPricesMap = stockRepo.getCollectionsPrices(collectionIds)
                .stream()
                .collect(toMap(Prices::getId, p -> new Prices(p.getMinPrice(), p.getMaxPrice())));

        return setPricesAndImages(collections, collectionsImagesMap, collectionsPricesMap);
    }

    private List<Short> normalizePublishedFilter(Short published) {
        if (published != null) {
            return Shorts.asList(published);
        }
        return Shorts.asList((short)1, (short)2);
    }

    private List<ThreeSixtyProductsDTO> setPricesAndImages(List<ThreeSixtyProductsDTO> list, Map<Long, List<ProductImagesEntity>> imagesMap, Map<Long, Prices> pricesMap) {
        for (ThreeSixtyProductsDTO dto : list) {
            if (imagesMap.get(dto.getId()) != null) {
                Set<String> imagesSet = imagesMap
                        .get(dto.getId())
                        .stream()
                        .map(i -> of(i.getUri()).orElse(null))
                        .collect(toSet());
                dto.setImages(imagesSet);
                String coverImage = productImageService.getProductCoverImage(dto.getId());
                dto.setImageUrl(coverImage);
            }
            ofNullable(dto.getId())
                    .map(pricesMap::get)
                    .ifPresent(dto::setPrices);

        }
        return list;
    }


    public ShopResponse publishJsonData(Long shopId) throws BusinessException {
        ShopThreeSixtyEntity shop360 = shop360Repo.findByShopsEntity_Id(shopId);
        if (shop360 == null)
            throw new BusinessException("missing shop data", "INVALID_PARAM: shop_id", NOT_ACCEPTABLE);

        product360ShopsRepo.deleteByShopId(shopId);
        List<Shop360ProductsEntity> existingProductsPositions = product360ShopsRepo.findProductsPositionsByShopId(shopId);

        List<Shop360ProductsEntity> publishedProductsPositions = new ArrayList<>();

        for(Shop360ProductsEntity product : existingProductsPositions) {
            Shop360ProductsEntity entity = copyProductPositionEntity(product);
            publishedProductsPositions.add(entity);
        }
        product360ShopsRepo.saveAll(publishedProductsPositions);

        shop360.setWebJsonData(shop360.getPreviewJsonData());

        shop360Repo.save(shop360);

        return new ShopResponse(shopId, OK);
    }


    private Shop360ProductsEntity copyProductPositionEntity(Shop360ProductsEntity product) {
        Shop360ProductsEntity entity = new Shop360ProductsEntity();

        BeanUtils.copyProperties(product, entity, new String[]{"id"});
        entity.setProductEntity(product.getProductEntity());
        entity.setShopEntity(product.getShopEntity());
        entity.setFloor(product.getFloor());
        entity.setSection(product.getSection());
        entity.setScene(product.getScene());
        entity.setPublished((short)2);

        return entity;
    }


    public void deleteShop360Floors(Long shopId) throws BusinessException {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopThreeSixtyEntity shopThreeSixtyEntity = shop360Repo.getFirstByShopsEntity_Id(shopId);

        validateShop360Link(shopThreeSixtyEntity);

        clearOldShop360Date(orgId, shopThreeSixtyEntity.getId());
    }


    public void deleteShop360Floor(Long shopId, Long floorId) throws BusinessException {
        ShopThreeSixtyEntity shopThreeSixtyEntity = shop360Repo.getFirstByShopsEntity_Id(shopId);

        validateShop360Link(shopThreeSixtyEntity);

        ShopFloorsEntity floor = shopFloorsRepo.findByIdAndShopThreeSixtyEntity_Id(floorId, shopThreeSixtyEntity.getId());
        if (floor == null)
            throw new BusinessException("No floor found", "INVALID_PARAM: floor_id", NOT_ACCEPTABLE);

        shopFloorsRepo.delete(floor);
    }


    public void deleteShop360Section(Long shopId, Long sectionId) throws BusinessException {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopThreeSixtyEntity shopThreeSixtyEntity = shop360Repo.getFirstByShopsEntity_Id(shopId);

        validateShop360Link(shopThreeSixtyEntity);

        ShopSectionsEntity section = sectionsRepo.findByIdAndOrganizationEntity_Id(sectionId, orgId);
        if (section == null)
            throw new BusinessException("No section found", "INVALID_PARAM: section_id", NOT_ACCEPTABLE);

        if(!section.getShopFloorsEntity().getShopThreeSixtyEntity().equals(shopThreeSixtyEntity))
            throw new BusinessException("Section doesn't belong to current org!", "INVALID_PARAM: section_id", NOT_ACCEPTABLE);

        sectionsRepo.delete(section);
    }


    public void deleteShop360Scene(Long shopId, Long sceneId) throws BusinessException {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopThreeSixtyEntity shopThreeSixtyEntity = shop360Repo.getFirstByShopsEntity_Id(shopId);

        validateShop360Link(shopThreeSixtyEntity);

        Optional<ShopScenesEntity> scene = scenesRepo.findByIdAndOrganizationEntity_Id(sceneId, orgId);
        if (!scene.isPresent())
            throw new BusinessException("No scene found", "INVALID_PARAM: scene_id", NOT_ACCEPTABLE);

        if(!scene.get().getOrganizationEntity().equals(shopThreeSixtyEntity.getShopsEntity().getOrganizationEntity()))
            throw new BusinessException("Scene doesn't belong to current org!", "INVALID_PARAM: section_id", NOT_ACCEPTABLE);


        scenesRepo.delete(scene.get());
    }
}
