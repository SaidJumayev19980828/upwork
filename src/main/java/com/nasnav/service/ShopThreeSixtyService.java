package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.ShopResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopThreeSixtyService {

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
        ShopThreeSixtyEntity shop = shop360Repo.findByShopsEntity_Id(shopId);
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
        if (jsonDataString.startsWith("--- '") && jsonDataString.endsWith("'\n"))
            jsonDataString = jsonDataString.substring(jsonDataString.indexOf("'")+1,jsonDataString.lastIndexOf("'"));
        return jsonDataString.replaceAll("\n", "");
    }

    public String getProductPositions(Long shopId) {
        ShopThreeSixtyEntity shop = shop360Repo.findByShopsEntity_Id(shopId);
        if (shop == null)
            return null;

        ProductPositionEntity productPosition = productPosRepo.findByShopsThreeSixtyEntity_Id(shop.getId());
        if (productPosition == null)
            return null;

        JSONObject positionsJson =  new JSONObject(getJsonDataStringSerlizable(productPosition.getPositionsJsonData()));

        return positionsJson.toString();
    }

    public List<ShopFloorDTO> getSections(Long shopId) {
        ShopThreeSixtyEntity shop = shop360Repo.findByShopsEntity_Id(shopId);
        if (shop == null)
            return new ArrayList<>();

        List<ShopFloorDTO> floors = shopFloorsRepo.findByShopThreeSixtyEntity_Id(shop.getId())
                                                          .stream()
                                                          .map(f -> (ShopFloorDTO) f.getRepresentation())
                                                          .collect(Collectors.toList());
        return floors;
    }

    public ShopThreeSixtyDTO getThreeSixtyShops(Long shopId) {
        ShopThreeSixtyEntity entity = shop360Repo.findByShopsEntity_Id(shopId);
        return entity != null ? (ShopThreeSixtyDTO) entity.getRepresentation() : null;
    }

    public ShopResponse updateThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) throws BusinessException {
        if (shopThreeSixtyDTO.getId() == null)
            return createThreeSixtyShop(shopThreeSixtyDTO);
        else
            return modifyThreeSixtyShop(shopThreeSixtyDTO);
    }

    private ShopResponse createThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) {
        ShopThreeSixtyEntity entity = new ShopThreeSixtyEntity();
        return saveShopThreeSixtyEntity(entity, shopThreeSixtyDTO.getName());
    }

    private ShopResponse modifyThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO) throws BusinessException {
        Optional<ShopThreeSixtyEntity> optionalEntity = shop360Repo.findById(shopThreeSixtyDTO.getId());
        if (!optionalEntity.isPresent())
            throw new BusinessException("Provided shop_id doesn't match any existing shop360s","INVALID_PARAM: id",
                    HttpStatus.NOT_ACCEPTABLE);
        ShopThreeSixtyEntity entity = optionalEntity.get();
        return saveShopThreeSixtyEntity(entity, shopThreeSixtyDTO.getName());
    }

    private ShopResponse saveShopThreeSixtyEntity(ShopThreeSixtyEntity entity, String shopName) {
        entity.setSceneName(shopName);
        shop360Repo.save(entity);
        return new ShopResponse(entity.getId(), HttpStatus.OK);
    }


    public ShopResponse updateThreeSixtyShopJsonData(ShopJsonDataDTO dataDTO) throws BusinessException {
        validateJsonData(dataDTO);

        Optional<ShopThreeSixtyEntity> entity = shop360Repo.findById(dataDTO.getView360Id());

        if (!entity.isPresent())
            throw new BusinessException("Provide view360_id doesn't match any existing shop",
                    "INVALID_PARAM: view360_id", HttpStatus.NOT_ACCEPTABLE);

        ShopThreeSixtyEntity shopEntity = entity.get();

        if (dataDTO.getType().equals("web"))
            shopEntity.setWebJsonData(dataDTO.getJson());
        else if (dataDTO.getType().equals("mobile"))
            shopEntity.setMobileJsonData(dataDTO.getJson());
        else
            throw new BusinessException("Provide type "+dataDTO.getType()+" is invalid",
                    "INVALID_PARAM: type", HttpStatus.NOT_ACCEPTABLE);

        shop360Repo.save(shopEntity);
        return new ShopResponse(shopEntity.getId(), HttpStatus.OK);
    }

    private void validateJsonData(ShopJsonDataDTO dataDTO) throws BusinessException {
        if (dataDTO.getView360Id() == null)
            throw new BusinessException("Must provide view360_id of JsonData",
                    "MISSING_PARAM: view360_id", HttpStatus.NOT_ACCEPTABLE);

        if (dataDTO.getType() == null)
            throw new BusinessException("Must provide type for JsonData (web or mobile)",
                    "MISSING_PARAM: type", HttpStatus.NOT_ACCEPTABLE);

    }


    public ShopResponse updateThreeSixtyShopProductPositions(ShopProductPositionsDTO productPositionsDTO) throws BusinessException {
        ProductPositionEntity entity = productPosRepo.findByShopsThreeSixtyEntity_Id(productPositionsDTO.getView360Id());

        if (entity == null)
            throw new BusinessException("Provide view360_id doesn't match any existing shop",
                    "INVALID_PARAM: view360_id", HttpStatus.NOT_ACCEPTABLE);


        entity.setPositionsJsonData(productPositionsDTO.getProductPositions());

        productPosRepo.save(entity);
        return new ShopResponse(entity.getId(), HttpStatus.OK);
    }

    public ShopResponse updateThreeSixtyShopSections(ShopThreeSixtyRequestDTO jsonDTO) throws BusinessException, IOException {
        OrganizationEntity org = securitySvc.getCurrentUserOrganization();

        checkImageUrls(org.getId(),jsonDTO.getShopFloorsRequestDTO());
        clearOldShop360Date(org.getId(), jsonDTO.getView360Id());
        Long shopId = createShop360Floor(org, jsonDTO);
        return new ShopResponse(shopId, HttpStatus.OK);
    }


    private void clearOldShop360Date(Long orgId, Long viewId) {
        shopFloorsRepo.deleteByShopThreeSixtyEntity_IdAndOrganizationEntity_id(viewId, orgId);
    }

    private Long createShop360Floor(OrganizationEntity org, ShopThreeSixtyRequestDTO dto) {
        ShopThreeSixtyEntity shop = shop360Repo.findById(dto.getView360Id()).get();
        ShopFloorsEntity floor = new ShopFloorsEntity();
        ShopFloorsRequestDTO floorDTO = dto.getShopFloorsRequestDTO();

        floor.setName(floorDTO.getName());
        floor.setNumber(floorDTO.getNumber());
        floor.setShopThreeSixtyEntity(shop);
        floor.setOrganizationEntity(org);

        ShopFloorsEntity savedFloor = shopFloorsRepo.save(floor);

        for(ShopSectionsRequestDTO sectionsDTO: floorDTO.getShopSections())
            createShop360Section(sectionsDTO, savedFloor, shop, org);

        return shop.getId();
    }

    private void createShop360Section(ShopSectionsRequestDTO dto, ShopFloorsEntity floor,
                                      ShopThreeSixtyEntity shop, OrganizationEntity org) {

        ShopSectionsEntity section = new ShopSectionsEntity();
        section.setName(dto.getName());
        section.setImage(dto.getImageUrl());
        section.setShopFloorsEntity(floor);
        section.setOrganizationEntity(org);
        ShopSectionsEntity savedSection = sectionsRepo.save(section);
        for(ShopScenesRequestDTO scene: dto.getShopScenes())
            createShop360Scene(scene, savedSection, shop, org);

    }

    private void createShop360Scene(ShopScenesRequestDTO dto, ShopSectionsEntity section,
                                    ShopThreeSixtyEntity shop, OrganizationEntity org) {
        ShopScenesEntity scene = new ShopScenesEntity();
        scene.setName(dto.getName());
        scene.setImage(dto.getImageUrl());
        //scene.setThumbnail();
        //scene.setResized();
        scene.setShopSectionsEntity(section);
        scene.setOrganizationEntity(org);
        scenesRepo.save(scene);
    }
    private void checkImageUrls(Long orgId, ShopFloorsRequestDTO dto) throws BusinessException, IOException {

        List<String> urls = new ArrayList<>();
        for(ShopSectionsRequestDTO section: dto.getShopSections()) {
            urls.add(section.getImageUrl());
            for(ShopScenesRequestDTO scene: section.getShopScenes()) {
                urls.add(scene.getImageUrl());
            }
        }

        for(String url: urls) {
            FileEntity image = filesRepo.findByUrl(orgId + "/" + url);

            if(image == null)
                throw new BusinessException("Thie image_url("+url+") doesn't exist!",
                        "INVALID_PARAM: image_url", HttpStatus.NOT_ACCEPTABLE);

            String resized1024 = url.substring(0, url.lastIndexOf("."))
                                 + "_resized1024" +
                                url.substring(url.lastIndexOf("."), url.length()-1);

            String resized4096 = url.substring(0, url.lastIndexOf("."))
                    + "_resized4096" +
                    url.substring(url.lastIndexOf("."), url.length()-1);

            if(filesRepo.findByUrl(orgId + "/" + resized1024) == null)
                resizeImage(1024, image);

            if(filesRepo.findByUrl(orgId + "/" + resized4096) == null)
                resizeImage(4096, image);
        }

    }


    private void resizeImage(int imageSize, FileEntity image) throws IOException {
        Path location = basePath.resolve(image.getLocation());

        BufferedImage inputImage = ImageIO.read(new File(location.toString()));
        BufferedImage outputImage = new BufferedImage(imageSize,
                imageSize, inputImage.getType());

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, imageSize, imageSize, null);
        g2d.dispose();
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }
}
