package com.nasnav.service.cart.optimizers;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.service.cart.optimizers.parameters.EmptyParams;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.TRANSPARENT;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;


/**
 * Cart optimizer that basically do nothing.
 * It will just return the same cart that was given to it, this can be used in cases where the shops were selected
 * explicitly by the customer on the frontend.
 * */
@Service(TRANSPARENT)
public class TransparentOptimizer implements CartOptimizer<EmptyParams, EmptyParams>{


    @Override
    public Optional<EmptyParams> createCartOptimizationParameters(CartCheckoutDTO dto) {
        return Optional.empty();
    }



    @Override
    public Class<? extends EmptyParams> getCartParametersClass() {
        return EmptyParams.class;
    }



    @Override
    public Class<EmptyParams> getConfigurationClass() {
        return EmptyParams.class;
    }



    @Override
    public Boolean isConfigValid(EmptyParams parameters) {
        return true;
    }



    @Override
    public Boolean areCartParametersValid(EmptyParams parameters) {
        return true;
    }



    @Override
    public String getOptimizerName() {
        return TRANSPARENT;
    }



    @Override
    public Optional<OptimizedCart> createOptimizedCart(Optional<EmptyParams> parameters, EmptyParams config, Cart cart) {
        return cart
                .getItems()
                .stream()
                .map(item -> new OptimizedCartItem(item, false))
                .collect(collectingAndThen(
                        toList()
                        , items -> Optional.of(new OptimizedCart(items))));
    }
}
