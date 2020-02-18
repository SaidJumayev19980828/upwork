package com.nasnav.service;

import com.nasnav.dao.ProductPositionsRepository;
import com.nasnav.dao.ShopFloorsRepository;
import com.nasnav.dao.ShopThreeSixtyRepository;
import com.nasnav.dto.ShopFloorDTO;
import com.nasnav.persistence.ProductPositionEntity;
import com.nasnav.persistence.ShopThreeSixtyEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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


}
