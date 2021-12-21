package com.nasnav.service;

import com.google.common.primitives.Shorts;
import com.nasnav.AppConfig;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.ProductPositionDTO;
import com.nasnav.dto.response.PostProductPositionsResponse;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.ShopResponse;
import lombok.Builder;
import lombok.Data;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ShopThreeSixtyService {

    @Autowired
    private AppConfig appConfig;

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
        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId)
                .orElse(null);
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

        return new ProductsPositionDTO(shopId, products, collections);
    }


    public List<ShopFloorDTO> getSections(Long shopId) {
        ShopThreeSixtyEntity shop = getShopThreeSixtyEntity(shopId);
        return shopFloorsRepo
                .findByShopThreeSixtyEntity_Id(shop.getId())
                .stream()
                .map(f -> (ShopFloorDTO) f.getRepresentation())
                .sorted(Comparator.comparing(f -> getFloorNumber(f.getNumber())))
                .collect(toList());
    }


    private Integer getFloorNumber(Integer floorNumber) {
        return floorNumber == null ? Integer.MAX_VALUE : floorNumber;
    }


    public ShopThreeSixtyDTO getThreeSixtyShops(Long shopId, boolean yeshteryState) {
        ShopThreeSixtyEntity shop = (yeshteryState ? shop360Repo.getYeshteryShop(shopId): shop360Repo.getFirstByShopsEntity_Id(shopId))
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,S$360$0001));
        return (ShopThreeSixtyDTO) shop.getRepresentation();
    }


    public ShopResponse updateThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) {
        if (shopThreeSixtyDTO.getId() == null)
            return createThreeSixtyShop(shopThreeSixtyDTO);
        else
            return modifyThreeSixtyShop(shopThreeSixtyDTO);
    }


    private ShopResponse createThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) {
        if (shop360Repo.existsByShopsEntity_Id(shopThreeSixtyDTO.getShopId()))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$0004);
        ShopThreeSixtyEntity entity = new ShopThreeSixtyEntity();
        return saveShopThreeSixtyEntity(entity, shopThreeSixtyDTO.getName(), shopThreeSixtyDTO.getShopId());
    }


    private ShopResponse modifyThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopThreeSixtyEntity entity = shop360Repo.findByIdAndShopsEntity_OrganizationEntity_Id(shopThreeSixtyDTO.getId(), orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$0001));

        return saveShopThreeSixtyEntity(entity, shopThreeSixtyDTO.getName(), shopThreeSixtyDTO.getShopId());
    }


    private ShopResponse saveShopThreeSixtyEntity(ShopThreeSixtyEntity entity, String shopName, Long shopId) {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        var shop = shopRepo.findByIdAndOrganizationEntity_IdAndRemoved(shopId, orgId, 0);
        if (shop.isEmpty()) {
            if (entity.getShopsEntity() == null)
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$0003);
        } else {
            entity.setShopsEntity(shop.get());
        }
        entity.setSceneName(shopName);
        shop360Repo.save(entity);
        return new ShopResponse(entity.getId());
    }


    public ShopResponse updateThreeSixtyShopJsonData(Long shopId, String type, String dataDTO) throws UnsupportedEncodingException {
        if (type == null || !asList("web", "mobile").contains(type))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$0005);

        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopThreeSixtyEntity shopEntity = shop360Repo.findByShopsEntity_IdAndShopsEntity_OrganizationEntity_Id(shopId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE ,S$360$0001));

        if (type.equals("web"))
            shopEntity.setPreviewJsonData(decodeUrl(dataDTO));
        else if (type.equals("mobile"))
            shopEntity.setMobileJsonData(decodeUrl(dataDTO));

        shop360Repo.save(shopEntity);
        return new ShopResponse(shopEntity.getId());
    }


    private String decodeUrl(String url) throws UnsupportedEncodingException {
        return URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
    }


    @Transactional
    public PostProductPositionsResponse updateThreeSixtyShopProductsPositions(Long shopId,  List<ProductPositionDTO> json) {
        Long orgId = securitySvc.getCurrentUserOrganizationId();

        ShopThreeSixtyEntity shop = getShopThreeSixtyEntity(shopId);

        Map<Long, Shop360ProductsEntity> productsMap = getShop360ProductsMap(shopId);

        List<Long> missingProducts = new ArrayList<>();
        List<Long> missingScenes = new ArrayList<>();

        List<Long> updatedProductsPositions = createShop360ProductsEntity(productsMap, json, shop, orgId, missingProducts, missingScenes);

        product360ShopsRepo.deleteByShopEntity_IdAndIdNotInAndPublished(shopId, updatedProductsPositions, (short) 1);

        return new PostProductPositionsResponse(missingProducts, missingScenes);
    }


    private List<Long> createShop360ProductsEntity(Map<Long, Shop360ProductsEntity> productsMap, List<ProductPositionDTO> json,
                                             ShopThreeSixtyEntity shop,Long orgId, List<Long> missingProducts, List<Long> missingScenes) {
        List<Long> updatedProductsPositions = new ArrayList<>();
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
            updatedProductsPositions.add(product360ShopsRepo.save(product).getId());
        }
        return updatedProductsPositions;
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

    @Transactional
    public ShopResponse updateThreeSixtyShopSections(Long shopId, List<ShopFloorsRequestDTO> jsonDTO) throws BusinessException, IOException {
        OrganizationEntity org = securitySvc.getCurrentUserOrganization();

        ShopThreeSixtyEntity shop = getShopThreeSixtyEntity(shopId);

        Map<String, List<String>> resizedImagesMap = generateImageUrls(org.getId(),jsonDTO);
        createShop360Floor(org, shop.getId(), jsonDTO, resizedImagesMap);
        return new ShopResponse(shopId);
    }



    private void clearOldShop360Date(Long orgId, Long viewId) {
        product360ShopsRepo.deleteByShopId(viewId);
        shopFloorsRepo.deleteByShopThreeSixtyEntity_IdAndOrganizationEntity_id(viewId, orgId);
    }


    private Long createShop360Floor(OrganizationEntity org, Long viewId, List<ShopFloorsRequestDTO> dto,
                                    Map<String, List<String>> resizedImagesMap) {
        ShopThreeSixtyEntity shop = shop360Repo.findById(viewId).get();
        ShopFloorsEntity floor;

        for(ShopFloorsRequestDTO floorDTO : dto) {
            if (floorDTO.getId() == null) {
                floor = new ShopFloorsEntity();
                if (floorDTO.getNumber() == null)
                    throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$F$0001, floorDTO.getId());

                floor.setShopThreeSixtyEntity(shop);
                floor.setOrganizationEntity(org);
            }
            else {
                floor = shopFloorsRepo.findByIdAndShopThreeSixtyEntity_Id(floorDTO.getId(), shop.getId())
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$F$0003, floorDTO.getId()));
            }
            if (floorDTO.getName() != null)
                floor.setName(floorDTO.getName());
            if (floorDTO.getNumber() != null)
                floor.setNumber(floorDTO.getNumber());

            ShopFloorsEntity savedFloor = shopFloorsRepo.save(floor);
            List<ShopSectionsRequestDTO> sectionsDTO = getSections(floorDTO);
            for (int i=0;i<sectionsDTO.size();i++) {
                createShop360Section(sectionsDTO.get(i), savedFloor, resizedImagesMap, i);
            }
        }

        return shop.getId();
    }


    private void createShop360Section(ShopSectionsRequestDTO dto, ShopFloorsEntity floor,
                                      Map<String, List<String>> resizedImagesMap, int index) {
        ShopSectionsEntity section;
        if (dto.getId() == null) {
            section = new ShopSectionsEntity();
            section.setShopFloorsEntity(floor);
            section.setOrganizationEntity(floor.getOrganizationEntity());
        }
        else {
            section = sectionsRepo.findByIdAndShopFloorsEntity_Id(dto.getId(), floor.getId())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$S$0003, dto.getId()));
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
        ShopSectionsEntity savedSection = sectionsRepo.save(section);
        List<ShopScenesRequestDTO> scenesDTO = getScenes(dto);
        for(int i=0;i<scenesDTO.size();i++) {
        	createShop360Scene(scenesDTO.get(i), savedSection, resizedImagesMap, i);
        }
    }


    private void createShop360Scene(ShopScenesRequestDTO dto, ShopSectionsEntity section,
                                    Map<String, List<String>> resizedImagesMap, int index) {
        ShopScenesEntity scene;
        if (dto.getId() == null) {
            scene = new ShopScenesEntity();
            scene.setShopSectionsEntity(section);
            scene.setOrganizationEntity(section.getOrganizationEntity());
        }
        else {
            scene = scenesRepo.findByIdAndShopSectionsEntity_Id(dto.getId(), section.getId())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$S$0004, dto.getId()));
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
                List<ShopScenesRequestDTO> scenes = getScenes(section);
                for (ShopScenesRequestDTO scene : scenes) {
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


    private String resizeImage(int imageWidth, FileEntity image, Long orgId) throws IOException {
        this.basePath = Paths.get(appConfig.getBasePathStr());
        Path location = basePath.resolve(image.getLocation());

        BufferedImage inputImage = ImageIO.read(location.toFile());
        int imageHeight = (int) (imageWidth * (inputImage.getHeight()/(inputImage.getWidth()*1.0)));
        var outputImageFile = new ByteArrayOutputStream();
        String imageName = getResizedImageName(image.getUrl().substring(image.getUrl().lastIndexOf('/')+1), imageWidth);

        Thumbnails.of(inputImage)
                .height(imageHeight)
                .outputFormat("jpg")
                .toOutputStream(outputImageFile);

        MultipartFile multipartFile = fileSvc.getCommonsMultipartFile("fileItem",
                imageName, "image/jpg", outputImageFile );

        return fileSvc.saveFile(multipartFile, orgId);
    }

    private String getResizedImageName(String imageName, int size) {
        return imageName.substring(0, imageName.lastIndexOf("."))
                + "-resized"+ size +
                imageName.substring(imageName.lastIndexOf("."));
    }


    public LinkedHashMap<String,List<ThreeSixtyProductsDTO>> getShop360Products(Long shopId, String name, Integer count, Integer productType,
                                            Short published, boolean has360, boolean includeOutOfStock) throws BusinessException {
        if (!shopRepo.existsById(shopId))
            throw new BusinessException("Provided shop_id doesn't match any existing shop!",
                    "INVALID_PARAM: shop_id", NOT_ACCEPTABLE);

        name = ofNullable(name).map(String::toLowerCase).orElse("");
        List<ThreeSixtyProductsDTO> products = new ArrayList<>();
        List<ThreeSixtyProductsDTO> collections = new ArrayList<>();

        List<Short> publishedFilter = normalizePublishedFilter(published);
        ProductFetchParams params =
                ProductFetchParams
                .builder()
                .shopId(shopId)
                .name(name)
                .productType(productType)
                .has360(has360)
                .count(count)
                .publishedFilter(publishedFilter)
                .products(products)
                .collections(collections)
                .includeOutOfStock(includeOutOfStock)
                .build();
        return fetchProductsAndCollections(params);
    }



    private LinkedHashMap<String,List<ThreeSixtyProductsDTO>> fetchProductsAndCollections(ProductFetchParams productFetchParams) {
        if (isFindProductsRequired(productFetchParams.getProductType())) {
            productFetchParams.setProducts(productsRepo.find360Products(productFetchParams.getName(), productFetchParams.getShopId(), productFetchParams.isHas360(), productFetchParams.getPublishedFilter(), PageRequest.of(0, productFetchParams.getCount())));
            if (productFetchParams.getProducts().size() > 0) {
                productFetchParams.setProducts(getProductsListAdditionalData(productFetchParams.getProducts(), productFetchParams.isIncludeOutOfStock()));
            }
        }

        if (isFindCollectionsRequired(productFetchParams.getProductType())) {
            productFetchParams.setCollections(productsRepo.find360Collections(productFetchParams.getName(), productFetchParams.getShopId(), productFetchParams.isHas360(), productFetchParams.getPublishedFilter(), PageRequest.of(0, productFetchParams.getCount())));
            if (productFetchParams.getCollections().size() > 0) {
                productFetchParams.setCollections(getCollectionsListAdditionalData(productFetchParams.getCollections(), productFetchParams.isIncludeOutOfStock()));
            }
        }

        productFetchParams.getProducts().addAll(productFetchParams.getCollections());
        if (productFetchParams.getProducts().size() > productFetchParams.getCount()) {
            productFetchParams.setProducts(productFetchParams.getProducts().subList(0, productFetchParams.getCount()));
        }
        LinkedHashMap<String,List<ThreeSixtyProductsDTO>> response = new LinkedHashMap<>();
        response.put("products", productFetchParams.getProducts());
        return response;
    }


    private boolean isFindProductsRequired(Integer productType) {
        return productType == null || productType == 0;
    }


    private boolean isFindCollectionsRequired(Integer productType) {
        return productType == null || productType == 2;
    }


    private List<ThreeSixtyProductsDTO> getProductsListAdditionalData(List<ThreeSixtyProductsDTO> products, boolean includeOutOfStock) {
        List<Long> productIds = products.stream().map(p -> p.getId()).collect(toList());
        Map<Long, List<ProductImagesEntity>> productsImagesMap = productImageService.getProductsImageList(productIds);
        Map<Long, Prices> productsPricesMap = stockRepo.getProductsPrices(productIds, includeOutOfStock)
                .stream()
                .collect(toMap(Prices::getId, p -> new Prices(p.getMinPrice(), p.getMaxPrice())));

        return setPricesAndImages(products, productsImagesMap, productsPricesMap);
    }


    private List<ThreeSixtyProductsDTO> getCollectionsListAdditionalData(List<ThreeSixtyProductsDTO> collections, boolean includeOutOfStock) {
        List<Long> collectionIds = collections.stream().map(p -> p.getId()).collect(toList());

        Map<Long, List<ProductImagesEntity>> collectionsImagesMap = productImageService.getProductsImageList(collectionIds);
        Map<Long, Prices> collectionsPricesMap = stockRepo.getCollectionsPrices(collectionIds, includeOutOfStock)
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


    public ShopResponse publishJsonData(Long shopId) {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopThreeSixtyEntity shop360 = shop360Repo.findByShopsEntity_IdAndShopsEntity_OrganizationEntity_Id(shopId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, S$0002, shopId));

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

        return new ShopResponse(shopId);
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


    public void deleteShop360Floors(Long shopId) {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopThreeSixtyEntity shopThreeSixtyEntity = getShopThreeSixtyEntity(shopId);

        clearOldShop360Date(orgId, shopThreeSixtyEntity.getId());
    }


    @Transactional
    public void deleteShop360Floor(Long shopId, Long floorId, boolean confirm) {
        ShopThreeSixtyEntity shopThreeSixtyEntity = getShopThreeSixtyEntity(shopId);

        ShopFloorsEntity floor = shopFloorsRepo.findByIdAndShopThreeSixtyEntity_Id(floorId, shopThreeSixtyEntity.getId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$360$F$0002));

        if (confirm) {
            product360ShopsRepo.deleteByFloor(floor);
        } else {
            if (product360ShopsRepo.existsByFloor(floor)) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$PRO$POS$001, "Floor");
            }
        }
        shopFloorsRepo.delete(floor);
    }


    @Transactional
    public void deleteShop360Section(Long sectionId, boolean confirm) {
        Long orgId = securitySvc.getCurrentUserOrganizationId();

        ShopSectionsEntity section = sectionsRepo.findByIdAndOrganizationEntity_Id(sectionId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$360$S$0002));

        if (confirm) {
            product360ShopsRepo.deleteBySection(section);
        } else {
            if (product360ShopsRepo.existsBySection(section)) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$PRO$POS$001, "Section");
            }
        }
        sectionsRepo.delete(section);
    }


    @Transactional
    public void deleteShop360Scene(Long sceneId, boolean confirm) {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopScenesEntity scene = scenesRepo.findByIdAndOrganizationEntity_Id(sceneId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$360$S$0002));

        if (confirm) {
            product360ShopsRepo.deleteByScene(scene);
        } else {
            if (product360ShopsRepo.existsByScene(scene)) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$360$PRO$POS$001, "Scene");
            }
        }

        scenesRepo.delete(scene);
    }

    private ShopThreeSixtyEntity getShopThreeSixtyEntity(Long shopId) {
        return shop360Repo.getFirstByShopsEntity_Id(shopId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$360$0001));
    }

    public void exportThreeSixtyImages(Long shopId, HttpServletResponse response) throws IOException {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        this.basePath = Paths.get(appConfig.getBasePathStr());
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        List<String> images = new ArrayList<>();
        if (shopId != null) {
            images = scenesRepo.findByOrganizationEntity_IdAndShopId(orgId, shopId);
        } else {
            images = scenesRepo.findByOrganizationEntity_Id(orgId);
        }
        images
            .stream()
            .map(img -> basePath.resolve(img))
            .map(Path::toFile)
            .forEach(file -> createZipEntry(zipOut, file));
        zipOut.close();
    }

    private void createZipEntry(ZipOutputStream zipOutputStream,  File file) {
        try {
            zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream fileInputStream = new FileInputStream(file);

            IOUtils.copy(fileInputStream, zipOutputStream);

            fileInputStream.close();
            zipOutputStream.closeEntry();
        } catch (Exception e){}
    }


    @Builder
    @Data
    private static class ProductFetchParams {
        private final Long shopId;
        private final String name;
        private final Integer productType;
        private final boolean has360;
        private final Integer count;
        private final List<Short> publishedFilter;
        private List<ThreeSixtyProductsDTO> products;
        private List<ThreeSixtyProductsDTO> collections;
        private final boolean includeOutOfStock;
    }
}
