package com.nasnav.dao;

import com.nasnav.persistence.ProductThreeDModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreeDModelRepository extends JpaRepository<ProductThreeDModel,Long> {
    ProductThreeDModel findByBarcodeOrSku(String barcode ,String sku);
    boolean existsBySku(String sku);
    boolean existsByBarcode(String barcode);
}
