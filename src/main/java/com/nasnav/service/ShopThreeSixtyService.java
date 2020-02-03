package com.nasnav.service;

import com.nasnav.dao.ProductPositionsRepository;
import com.nasnav.dao.ShopThreeSixtyRepository;
import com.nasnav.persistence.ProductPositionEntity;
import com.nasnav.persistence.ShopThreeSixtyEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopThreeSixtyService {

    @Autowired
    private ShopThreeSixtyRepository shop360Repo;

    @Autowired
    private ProductPositionsRepository productPosRepo;


    public String getShop360JsonInfo(Long shopId, String type) {
        ShopThreeSixtyEntity shop = shop360Repo.findByShopsEntity_Id(shopId);
        if (shop == null)
            return null;

        JSONObject data = new JSONObject();
        if(type.equals("web"))
            data = new JSONObject(shop.getWebJsonData());
        else if (type.equals("mobile"))
            data = new JSONObject(shop.getMobileJsonData());

        return data.toString();
    }

    public String getProductPositions(Long shopId) {
        ShopThreeSixtyEntity shop = shop360Repo.findByShopsEntity_Id(shopId);
        if (shop == null)
            return null;

        ProductPositionEntity productPosition = productPosRepo.findByShopsThreeSixtyEntity_Id(shop.getId());
        if (productPosition == null)
            return null;

        JSONObject positionsJson =  new JSONObject(productPosition.getPositionsJsonData());

        return positionsJson.toString();
    }

    public List getSections(Long shopId) {

        return null;
    }


}
