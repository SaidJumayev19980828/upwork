package com.nasnav.integration.events;

import com.nasnav.integration.events.data.ShopsFetchParam;
import com.nasnav.integration.model.ImportedShop;

import java.util.List;
import java.util.function.Consumer;

public class ShopsImportEvent extends Event<ShopsFetchParam, List<ImportedShop>>{
}
