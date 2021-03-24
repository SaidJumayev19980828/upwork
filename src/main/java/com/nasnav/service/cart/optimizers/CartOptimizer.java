package com.nasnav.service.cart.optimizers;

import java.util.Optional;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;


/**
 * an interface for Cart Optimizers.
 * In our model , a single product can have multiple variants, and each variant can have multiple stocks, a stock for
 * each shop, and each shop can have different prices for the same variant.
 * so, cart items are actually assigned per-stock, not per variant, so we can calculate the total price.
 *
 * Cart optimizers answers the question of : which shop should we pick the item from ?
 * They automatically select a certain stock for the item based on certain business rules.
 * Each organization may have its own business rules and validations to answer this question, and a cart optimizer implements
 * these rules.
 * We use Strategy pattern here to make things flexible. an Organization can select from a list of available optimizers
 * and the selected optimizer name and its configuration are saved into the database.
 * During the optimization process of customer cart, CartOptimizationService will check the database, select the optimizer by name,
 * and fetch its configuration for the customer organization, it will also extract some parameters from the checkout data
 * to provide it for the optimizer.
 * The logic for extracting these parameters is provided by the method createCartOptimizationParameters in the optimizer
 * implementation.
 *
 * To create a new optimizer implementation:
 * - first add a name of the optimizer as a public static constant in the class OptimizationStratigiesNames
 * - then add it to CartOptimizationStrategy enum, using the same constant in OptimizationStratigiesNames.
 * - create data class that defines the optimization parameters per each checkout process.
 * - create data class that will hold the optimizer configuration data, the data will be parsed from a json string into
 * an instance of this class using the default jackson instance.
 * - create the implementation of the optimizer that implements CartOptimizer interface, this is a parameterized interface
 * that will take the class types of the Cart parameters and the configuration parameters.
 * - the implementation MUST be a spring bean, so, it should be annotated with @Service annotation with optimizer name
 * as the bean name. ex: @Service(SAME_CITY)
 *
 * @param: CartParams: the class that holds the cart parameters. these are parameters collected from the checkout
 * data -for each order of course- to help the optimizer do the selection of the shop for this specific order.
 * If the optimizer needs no parameters per order, you can use EmptyParams class.
 * @param: CommonParams: class that holds configuration that is used by the optimizer for all orders, so, these are data per
 * organization-optimizer combination, and it is saved in the database as json value.
 * If the optimizer needs no configuration, you can use EmptyParams class.
 * CartOptimizationService is responsible for constructing an instance of both CartParams and CommonParams and providing
 * them to the optimizer instance during the optimization process.
 * */
public interface CartOptimizer<CartParams, CommonParams> {
	Optional<CartParams> createCartOptimizationParameters(CartCheckoutDTO dto);
	Optional<OptimizedCart> createOptimizedCart(Optional<CartParams> parameters, Cart cart );
	Class<? extends CartParams> getCartParametersClass();
	Class<? extends CommonParams> getCommonParametersClass();
	Boolean areCommonParametersValid(CommonParams parameters);
	Boolean areCartParametersValid(CartParams parameters);
}
