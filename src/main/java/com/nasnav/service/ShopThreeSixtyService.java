package com.nasnav.service;

import com.nasnav.dao.ProductPositionsRepository;
import com.nasnav.dao.ShopFloorsRepository;
import com.nasnav.dao.ShopThreeSixtyRepository;
import com.nasnav.dto.ShopFloorDTO;
import com.nasnav.dto.ShopJsonDataDTO;
import com.nasnav.dto.ShopProductPositionsDTO;
import com.nasnav.dto.ShopThreeSixtyDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductPositionEntity;
import com.nasnav.persistence.ShopThreeSixtyEntity;
import com.nasnav.response.ShopResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopThreeSixtyService {

    @Autowired
    private ShopThreeSixtyRepository shop360Repo;

    @Autowired
    private ProductPositionsRepository productPosRepo;

    @Autowired
    private ShopFloorsRepository shopFloorsRepo;


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
}
