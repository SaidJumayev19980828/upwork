package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.ShopResponse;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopThreeSixtyService {

    @Value("${files.basepath}")
    private String basePathStr;

    @Autowired
    private ShopsRepository shopRepo;

    @Autowired
    private ShopThreeSixtyRepository shop360Repo;

    @Autowired
    private ProductPositionsRepository productPosRepo;

    @Autowired
    private ShopFloorsRepository shopFloorsRepo;

    @Autowired
    private ShopSectionsRepository sectionsRepo;

    @Autowired
    private ShopScenesRepository scenesRepo;

    @Autowired
    private FilesRepository filesRepo;

    @Autowired
    private SecurityService securitySvc;

    @Autowired
    private FileService fileSvc;

    private Path basePath;

    public String getShop360JsonInfo(Long shopId, String type) {
        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);
        if (shop == null)
            return null;

        JSONObject data = new JSONObject();
        if(type.equals("web")) {
            data = new JSONObject(getJsonDataStringSerlizable(shop.getWebJsonData()));
        }
        else if (type.equals("mobile")) {
            data = new JSONObject(getJsonDataStringSerlizable(shop.getMobileJsonData()));
        }

        return data.toString();
    }

    // ! custom modifier to deal with mailformed json data in shop360s !
    private String getJsonDataStringSerlizable(String oldJsonDataString) {
        String jsonDataString = oldJsonDataString;
        if (jsonDataString == null)
            return "{}";

        if (jsonDataString.startsWith("--- '") && jsonDataString.endsWith("'\n"))
            jsonDataString = jsonDataString.substring(jsonDataString.indexOf("'")+1,jsonDataString.lastIndexOf("'"));

        return jsonDataString.replaceAll("\n", "");
    }

    public String getProductPositions(Long shopId) {
        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);
        if (shop == null)
            return null;

        ProductPositionEntity productPosition = productPosRepo.findByShopsThreeSixtyEntity_Id(shop.getId());
        if (productPosition == null || productPosition.getPositionsJsonData() == null)
            return null;

        JSONObject positionsJson =  new JSONObject(getJsonDataStringSerlizable(productPosition.getPositionsJsonData()));

        return positionsJson.toString();
    }

    public List<ShopFloorDTO> getSections(Long shopId) {
        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);
        if (shop == null)
            return new ArrayList<>();

        List<ShopFloorDTO> floors = shopFloorsRepo.findByShopThreeSixtyEntity_Id(shop.getId())
                                                          .stream()
                                                          .map(f -> (ShopFloorDTO) f.getRepresentation())
                                                          .collect(Collectors.toList());
        return floors;
    }

    public ShopThreeSixtyDTO getThreeSixtyShops(Long shopId) {
        ShopThreeSixtyEntity entity = shop360Repo.getFirstByShopsEntity_Id(shopId);
        return entity != null ? (ShopThreeSixtyDTO) entity.getRepresentation() : null;
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
                    "INVALID_PARAM: shop_id", HttpStatus.NOT_ACCEPTABLE);
        ShopThreeSixtyEntity entity = new ShopThreeSixtyEntity();
        return saveShopThreeSixtyEntity(entity, shopThreeSixtyDTO.getName(), shopThreeSixtyDTO.getShopId());
    }

    private ShopResponse modifyThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) throws BusinessException {
        Optional<ShopThreeSixtyEntity> optionalEntity = shop360Repo.findById(shopThreeSixtyDTO.getId());
        if (!optionalEntity.isPresent())
            throw new BusinessException("Provided shop_id doesn't match any existing shop360s","INVALID_PARAM: id",
                    HttpStatus.NOT_ACCEPTABLE);
        ShopThreeSixtyEntity entity = optionalEntity.get();
        return saveShopThreeSixtyEntity(entity, shopThreeSixtyDTO.getName(), shopThreeSixtyDTO.getShopId());
    }

    private ShopResponse saveShopThreeSixtyEntity(ShopThreeSixtyEntity entity, String shopName, Long shopId) throws BusinessException {
        Long orgId = securitySvc.getCurrentUserOrganizationId();
        ShopsEntity shop = shopRepo.findByIdAndOrganizationEntity_Id(shopId, orgId);
        if (shop == null) {
            if (entity.getShopsEntity() == null)
                throw new BusinessException("Must provide shop_id to attach shop360s to it",
                        "INVALID_PARAM: shop_id", HttpStatus.NOT_ACCEPTABLE);
        } else {
            entity.setShopsEntity(shop);
        }
        entity.setSceneName(shopName);
        shop360Repo.save(entity);
        return new ShopResponse(entity.getId(), HttpStatus.OK);
    }


    public ShopResponse updateThreeSixtyShopJsonData(Long shopId, String type, String dataDTO) throws BusinessException {
        validateJsonData(shopId, type);

        ShopThreeSixtyEntity shopEntity = shop360Repo.getFirstByShopsEntity_Id(shopId);

        if (shopEntity == null)
            throw new BusinessException("Provide view360_id doesn't match any existing shop",
                    "INVALID_PARAM: view360_id", HttpStatus.NOT_ACCEPTABLE);

        if (type.equals("web"))
            shopEntity.setWebJsonData(dataDTO);
        else if (type.equals("mobile"))
            shopEntity.setMobileJsonData(dataDTO);
        else
            throw new BusinessException("Provide type "+type+" is invalid",
                    "INVALID_PARAM: type", HttpStatus.NOT_ACCEPTABLE);

        shop360Repo.save(shopEntity);
        return new ShopResponse(shopEntity.getId(), HttpStatus.OK);
    }

    private void validateJsonData(Long shopId, String type) throws BusinessException {
        if (shopId == null)
            throw new BusinessException("Required shop_id is missing!",
                    "MISSING_PARAM: shop_id", HttpStatus.NOT_ACCEPTABLE);

        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);
        if (shop == null)
            throw new BusinessException("Shop360s not found",
                    "INVALID_PARAM: shop_id", HttpStatus.NOT_ACCEPTABLE);

        if (type == null)
            throw new BusinessException("Must provide type for JsonData (web or mobile)",
                    "MISSING_PARAM: type", HttpStatus.NOT_ACCEPTABLE);

    }


    public ShopResponse updateThreeSixtyShopProductPositions(Long shopId,  String json) throws BusinessException {

        validateProductPositionsUpdateDTO(shopId);

        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);

        ProductPositionEntity entity = productPosRepo.findByShopsThreeSixtyEntity_Id(shop.getId());

        if (entity == null) {
            entity = new ProductPositionEntity();
            OrganizationEntity org = securitySvc.getCurrentUserOrganization();
            entity.setOrganizationEntity(org);
            entity.setShopsThreeSixtyEntity(shop);
        }

        entity.setPositionsJsonData(json);

        productPosRepo.save(entity);
        return new ShopResponse(entity.getId(), HttpStatus.OK);
    }

    private void validateProductPositionsUpdateDTO(Long shopId) throws BusinessException {

        if (shopId == null)
            throw new BusinessException("Required parameter (shop_id) is missing!",
                    "MISSING_PARAM: shop_id", HttpStatus.NOT_ACCEPTABLE);

        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);

        if (shop == null)
            throw new BusinessException("Provided shopId doesn't match any existing shop360",
                    "INVALID_PARAM: shop_id", HttpStatus.NOT_ACCEPTABLE);
    }

    public ShopResponse updateThreeSixtyShopSections(Long shopId, List<ShopFloorsRequestDTO> jsonDTO) throws BusinessException, IOException {
        OrganizationEntity org = securitySvc.getCurrentUserOrganization();

        ShopThreeSixtyEntity shop = shop360Repo.getFirstByShopsEntity_Id(shopId);
        if (shop == null)
            throw new BusinessException("No 360 shops linked to shop_id!",
                    "INVALID_PARAM: shop_id", HttpStatus.NOT_ACCEPTABLE);
        Map<String, List<String>> resizedImagesMap = generateImageUrls(org.getId(),jsonDTO);
        clearOldShop360Date(org.getId(), shop.getId());
        createShop360Floor(org, shop.getId(), jsonDTO, resizedImagesMap);
        return new ShopResponse(shopId, HttpStatus.OK);
    }



    private void clearOldShop360Date(Long orgId, Long viewId) {
        shopFloorsRepo.deleteByShopThreeSixtyEntity_IdAndOrganizationEntity_id(viewId, orgId);
    }

    private Long createShop360Floor(OrganizationEntity org, Long viewId, List<ShopFloorsRequestDTO> dto,
                                    Map<String, List<String>> resizedImagesMap) {
        ShopThreeSixtyEntity shop = shop360Repo.findById(viewId).get();
        ShopFloorsEntity floor = new ShopFloorsEntity();

        for(ShopFloorsRequestDTO floorDTO : dto) {
            floor.setName(floorDTO.getName());
            floor.setNumber(floorDTO.getNumber());
            floor.setShopThreeSixtyEntity(shop);
            floor.setOrganizationEntity(org);

            ShopFloorsEntity savedFloor = shopFloorsRepo.save(floor);

            for (ShopSectionsRequestDTO sectionsDTO : floorDTO.getShopSections())
                createShop360Section(sectionsDTO, savedFloor, org, resizedImagesMap);
        }

        return shop.getId();
    }

    private void createShop360Section(ShopSectionsRequestDTO dto, ShopFloorsEntity floor, OrganizationEntity org,
                                      Map<String, List<String>> resizedImagesMap) {

        ShopSectionsEntity section = new ShopSectionsEntity();
        section.setName(dto.getName());
        section.setImage(dto.getImageUrl());
        section.setShopFloorsEntity(floor);
        section.setOrganizationEntity(org);
        ShopSectionsEntity savedSection = sectionsRepo.save(section);
        for(ShopScenesRequestDTO scene: dto.getShopScenes())
            createShop360Scene(scene, savedSection, org, resizedImagesMap);

    }

    private void createShop360Scene(ShopScenesRequestDTO dto, ShopSectionsEntity section, OrganizationEntity org,
                                    Map<String, List<String>> resizedImagesMap) {
        ShopScenesEntity scene = new ShopScenesEntity();
        scene.setName(dto.getName());
        scene.setImage(dto.getImageUrl());
        if(dto.getImageUrl() != null) {
            scene.setThumbnail(resizedImagesMap.get(dto.getImageUrl()).get(0));
            scene.setResized(resizedImagesMap.get(dto.getImageUrl()).get(1));
        }
        scene.setShopSectionsEntity(section);
        scene.setOrganizationEntity(org);
        scenesRepo.save(scene);
    }

    private Map generateImageUrls(Long orgId, List<ShopFloorsRequestDTO> dto) throws BusinessException, IOException {

        List<String> urls = new ArrayList<>();
        for(ShopFloorsRequestDTO floor: dto) {
            for(ShopSectionsRequestDTO section: floor.getShopSections()) {
                urls.add(section.getImageUrl());
                for (ShopScenesRequestDTO scene : section.getShopScenes()) {
                    urls.add(scene.getImageUrl());
                }
            }
        }

        Map<String, List<String>> resizedImagesMap = new HashMap<>();
        for(String url: urls) {
            FileEntity image = filesRepo.findByUrl(url);

            if(image == null)
                throw new BusinessException("The image_url("+url+") doesn't exist!",
                        "INVALID_PARAM: image_url", HttpStatus.NOT_ACCEPTABLE);

            String resized1024 = insertResizedImageName(url, 1024);

            String resized4096 = insertResizedImageName(url, 4096);

            if(filesRepo.findByUrl(resized1024) == null)
                resized1024 = resizeImage(1024, image, orgId);

            if(filesRepo.findByUrl(resized4096) == null)
                resized4096 = resizeImage(4096, image, orgId);

            if (resizedImagesMap.get(image) == null) {
                List<String> resizedImgs = new ArrayList<>();
                resizedImgs.add(resized1024);
                resizedImgs.add(resized4096);
                resizedImagesMap.put(url, resizedImgs);
            }
        }
        return resizedImagesMap;
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


        String imageName = insertResizedImageName(image.getOriginalFileName(), imageWidth);
        File i = new File(imageName);
        ImageIO.write(outputImage, "jpg", i );

        DiskFileItem fileItem = new DiskFileItem(imageName, "Image/jpg",
                false, imageName, (int) i.length() , i);
        fileItem.getOutputStream();

        MultipartFile multipartFile = new CommonsMultipartFile(fileItem);

        return fileSvc.saveFile(multipartFile, orgId);
    }

    private String insertResizedImageName(String imageName, int size) {
        return imageName.substring(0, imageName.lastIndexOf("."))
                + "-resized"+ size +
                imageName.substring(imageName.lastIndexOf("."), imageName.length());
    }
}
