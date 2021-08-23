package com.nasnav.service;

import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dao.ReturnRequestItemRepository;
import com.nasnav.dao.ReturnRequestRepository;
import com.nasnav.dao.RoleEmployeeUserRepository;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.ReturnRequestSearchParams;
import com.nasnav.dto.request.ReturnRequestRejectDTO;
import com.nasnav.dto.request.order.returned.ReceivedBasketItem;
import com.nasnav.dto.request.order.returned.ReceivedItem;
import com.nasnav.dto.request.order.returned.ReceivedItemsDTO;
import com.nasnav.dto.request.order.returned.ReturnRequestItemsDTO;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.dto.response.ReturnRequestItemDTO;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.ReturnRequestStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.ReturnRequestsResponse;
import com.nasnav.service.model.mail.MailAttachment;
import com.nasnav.shipping.model.ReturnShipmentTracker;
import com.nasnav.shipping.model.ShippingDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.*;
import static com.nasnav.commons.utils.MapBuilder.buildMap;
import static com.nasnav.constatnts.EmailConstants.*;
import static com.nasnav.enumerations.OrderStatus.DELIVERED;
import static com.nasnav.enumerations.ReturnRequestStatus.*;
import static com.nasnav.enumerations.Roles.*;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.enumerations.TransactionCurrency.getTransactionCurrency;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;
import static javax.persistence.criteria.JoinType.INNER;
import static javax.persistence.criteria.JoinType.LEFT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@Service
public class OrderReturnServiceImpl implements OrderReturnService{

    private static final Logger logger = LogManager.getLogger();

    private static final String DEFAULT_REJECTION_MESSAGE =
            "We are very sorry to inform you that we were unable to fulfill your order due to some issues."
                    + " Your order will be refunded shortly, but the refund operation may take several business days "
                    + "depending on the payment method.";

    private static final Set<OrderStatus> acceptableStatusForReturn = setOf(DELIVERED);

    public static final int MAX_RETURN_TIME_WINDOW = 14;

    @PersistenceContext
    @Autowired
    private EntityManager em;

    @Autowired
    private ReturnRequestRepository returnRequestRepo;

    @Autowired
    private ReturnRequestItemRepository returnRequestItemRepo;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PaymentsRepository paymentsRepo;

    @Autowired
    private ProductImageService imgService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ShippingManagementService shippingMgrService;

    @Autowired
    private MailService mailService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private OrderServiceHelper orderServiceHelper;

    @Autowired
    private OrderEmailServiceHelper orderEmailHelper;

    @Autowired
    private RoleEmployeeUserRepository empRoleRepo;

    @Autowired
    private StockService stockService;


    private Map<ReturnRequestStatus, Set<ReturnRequestStatus>> orderReturnStateMachine;



    public OrderReturnServiceImpl(){
        buildOrderReturnStatusTransitionMap();
    }


    @Override
    public ReturnRequestsResponse getOrderReturnRequests(ReturnRequestSearchParams params) {
        setOrderReturnDefaultParams(params);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        return getReturnRequests(params, builder);
    }





    @Override
    public ReturnRequestDTO getOrderReturnRequest(Long id){
        Long orgId = securityService.getCurrentUserOrganizationId();
        ReturnRequestEntity returnRequestEntity =
                returnRequestRepo
                        .findByReturnRequestId(id, orgId)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, id));
        Long metaOrderId = returnRequestEntity.getMetaOrder().getId();
        Map<Long, PaymentEntity> paymentsCache = createPaymentCache(asList(metaOrderId));
        Map<Long, Optional<String>> variantCoverImgs = getReturnRequestItemsCoverImgs(asList(returnRequestEntity));
        return createReturnRequestDto(returnRequestEntity, paymentsCache, variantCoverImgs);
    }




    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void confirmReturnRequest(Long requestId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        ReturnRequestEntity request =
                returnRequestRepo.findByIdAndOrganizationId(requestId, orgId)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, requestId));;
        request = updateReturnRequestStatus(request, CONFIRMED);
        List<ReturnShipmentTracker> trackers =
                shippingMgrService
                        .requestReturnShipments(request)
                        .collectList()
                        .blockOptional()
                        .orElseThrow( () -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0004, requestId));
        em.flush();
        em.refresh(request);
        ReturnRequestEntity requestUpdated =
                returnRequestRepo
                        .findByReturnRequestId(requestId, orgId)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, requestId));
        sendReturnRequestConfirmationEmail(requestUpdated, trackers);
    }




    @Override
    @Transactional
    public void rejectReturnRequest(ReturnRequestRejectDTO dto) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        ReturnRequestEntity returnRequest =
                returnRequestRepo
                        .findByIdAndOrganizationId(dto.getReturnRequestId(), orgId)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, dto.getReturnRequestId()));

        updateReturnRequestStatus(returnRequest, REJECTED);

        sendRejectionEmailToCustomer(returnRequest, dto.getRejectionReason(), orgId);
    }




    @Override
    public Long createReturnRequest(ReturnRequestItemsDTO itemsList) {
        List<ReturnRequestBasketItem> returnedItems =
                itemsList
                    .getItemList()
                    .stream()
                    .map(item -> new ReturnRequestBasketItem(item.getOrderItemId(), item.getReturnedQuantity(), 0))
                    .collect(toList());
        ReturnRequestEntity request = createReturnRequest(returnedItems);
        request = returnRequestRepo.save(request);

        sendOrderReturnNotificationEmail(request.getId());
        return request.getId();
    }




    @Override
    @Transactional
    public void receiveItems(ReceivedItemsDTO returnedItemsDTO) {

        validateReturnedItemsDTO(returnedItemsDTO);

        List<ReceivedItem> returnRequestItems = returnedItemsDTO.getReturnedItems();
        List<ReceivedBasketItem> returnBasketItems = returnedItemsDTO.getBasketItems();

        ReturnRequestEntity returnRequest;

        if (isOnlyReturnedBasketItemsProvided(returnRequestItems, returnBasketItems)) {
            List<ReturnRequestBasketItem> items = getReturnRequestBasketItemList(returnedItemsDTO);
            returnRequest = createReturnRequest(items);
        }else {
            List<ReturnRequestItemEntity> returnRequestItemEntities =
                    getAndValidateReturnRequestItemEntities(returnRequestItems);
            ReturnRequestEntity requestEntity = returnRequestItemEntities.get(0).getReturnRequest();
            assignReturnBasketItemsToReturnRequest(requestEntity, returnBasketItems);
            returnRequest = receiveReturnRequestItems(returnRequestItems, returnRequestItemEntities);
        }

        if (returnRequest != null) {
            returnRequest.setStatus(RECEIVED.getValue());
            returnRequest = returnRequestRepo.save(returnRequest);
            increaseReturnRequestStock(returnRequest);

            sendItemReceivedEmailToCustomer(returnRequest.getId());
            //TODO refund ??
        }
    }




    private void setOrderReturnDefaultParams(ReturnRequestSearchParams params) {
        if(params.getStart() == null || params.getStart() < 0){
            params.setStart(0);
        }
        if(params.getCount() == null || (params.getCount() < 1)){
            params.setCount(10);
        } else if (params.getCount() > 1000) {
            params.setCount(1000);
        }
    }



    private ReturnRequestsResponse getReturnRequests(ReturnRequestSearchParams params, CriteriaBuilder builder) {

        CriteriaQuery<ReturnRequestEntity> query = builder.createQuery(ReturnRequestEntity.class).distinct(true);
        Root<ReturnRequestEntity> root = query.from(ReturnRequestEntity.class);
        root.fetch("createdByUser", LEFT);
        root.fetch("createdByEmployee", LEFT);
        Fetch metaOrderFetch = root.fetch("metaOrder", LEFT);
        //TODO: while there is no need for pormotions here, for some reason hibernate fetch it for
        //each meta-order when running the query, although this is a lazy fetched entity.
        metaOrderFetch.fetch("promotions", LEFT);
        metaOrderFetch.fetch("user", LEFT);
        Fetch subOrderFetch = metaOrderFetch.fetch("subOrders", LEFT);
        subOrderFetch.fetch("addressEntity", LEFT);
        subOrderFetch.fetch("shipment", LEFT);
        Fetch returnItemsFtech = root.fetch("returnedItems", LEFT);
        returnItemsFtech.fetch("returnShipment", LEFT);
        Fetch basketFetch = returnItemsFtech.fetch("basket", LEFT);
        Fetch basketSubOrderFetch = basketFetch.fetch("ordersEntity", LEFT);
        basketSubOrderFetch
                .fetch("addressEntity", LEFT)
                .fetch("areasEntity", LEFT)
                .fetch("citiesEntity", LEFT)
                .fetch("countriesEntity", LEFT);
        basketSubOrderFetch.fetch("shipment", LEFT);

        Fetch stockFetch = basketFetch.fetch("stocksEntity", LEFT);
        stockFetch.fetch("shopsEntity", LEFT);
        stockFetch.fetch("productVariantsEntity", LEFT)
                .fetch("productEntity", LEFT);

        Predicate[] predicatesArr = getReturnRequestQueryPredicates(params, builder, root);

        query.where(predicatesArr);

        javax.persistence.criteria.Order order = builder.desc(root.get("id"));

        query.orderBy(order);

        List<ReturnRequestEntity> returnRequests =
                em
                        .createQuery(query)
                        .setFirstResult(params.getStart())
                        .setMaxResults(params.getCount())
                        .getResultList();

        Set<ReturnRequestDTO> returnRequestDTOS = getReturnRequestDTOS(returnRequests);
        Long count = getOrderReturnRequestsCount(builder, predicatesArr);

        return new ReturnRequestsResponse(count, returnRequestDTOS);
    }




    private Set<ReturnRequestDTO> getReturnRequestDTOS(List<ReturnRequestEntity> returnRequests) {
        List<Long> metaOrderIds =
                returnRequests
                        .stream()
                        .map(ReturnRequestEntity::getMetaOrder)
                        .map(MetaOrderEntity::getId)
                        .collect(toList());

        Map<Long, PaymentEntity> paymentsCache = createPaymentCache(metaOrderIds);
        Map<Long, Optional<String>> variantCoverImgs = getReturnRequestItemsCoverImgs(returnRequests);
        Set<ReturnRequestDTO> returnRequestDTOS =
                returnRequests
                        .stream()
                        .map(request -> createReturnRequestDto(request, paymentsCache, variantCoverImgs))
                        .collect(toSet());
        if (!returnRequestDTOS.isEmpty()) {
            addItemsCount(returnRequestDTOS);
        }
        return returnRequestDTOS;
    }





    private void addItemsCount(Set<ReturnRequestDTO> returnRequestDTOS) {
        Set<Long> returnRequestIds = returnRequestDTOS
                .stream()
                .map(ReturnRequestDTO::getId)
                .collect(toSet());
        Map<Long, Long> itemsCountMap =
                returnRequestRepo
                        .getReturnRequestsItemsCount(returnRequestIds)
                        .stream()
                        .collect(toMap(p -> p.getFirst(), p -> p.getSecond()));
        returnRequestDTOS.forEach(r -> r.setItemsCount(itemsCountMap.get(r.getId())));
    }



    private Predicate[] getReturnRequestQueryPredicates(ReturnRequestSearchParams params, CriteriaBuilder builder, Root<ReturnRequestEntity> root) {
        List<Predicate> predicates = new ArrayList<>();
        Long currentOrgId = securityService.getCurrentUserOrganizationId();
        Path<Map<String, String>> returnedItems = root.join("returnedItems", INNER);

        Predicate orgId = builder.equal(root.get("metaOrder").get("organization").get("id"), currentOrgId);
        predicates.add(orgId);

        parseTimeString(params.getDate_from())
                .map(from -> builder.greaterThanOrEqualTo(root.get("createdOn"), from))
                .ifPresent(predicates::add);

        parseTimeString(params.getDate_to())
                .map(to -> builder.lessThanOrEqualTo(root.get("createdOn"), to))
                .ifPresent(predicates::add);

        if (params.getStatus() != null) {
            Predicate status = builder.equal(root.get("status"), params.getStatus().getValue());
            predicates.add(status);
        }
        if (params.getMeta_order_id() != null) {
            Predicate metaOrderId = builder.equal(root.get("metaOrder").get("id"), params.getMeta_order_id());
            predicates.add(metaOrderId);
        }
        if (securityService.currentUserHasRole(STORE_MANAGER) && !securityService.currentUserHasRole(ORGANIZATION_ADMIN)) {
            Long shopId = ((EmployeeUserEntity)securityService.getCurrentUser()).getShopId();
            params.setShop_id(shopId);
        }
        if(params.getShop_id() != null) {
            Predicate shopId = builder.equal(returnedItems.get("basket").get("stocksEntity").get("shopsEntity").get("id"), params.getShop_id());
            predicates.add(shopId);
        }
        return predicates.toArray(new Predicate[0]);
    }




    private Map<Long, PaymentEntity> createPaymentCache(List<Long> metaOrderIds) {
        return paymentsRepo
                .findByMetaOrderIdIn(metaOrderIds)
                .stream()
                .collect(toMap(PaymentEntity::getMetaOrderId, pay -> pay));
    }




    private ReturnRequestDTO createReturnRequestDto(ReturnRequestEntity returnRequestEntity, Map<Long,PaymentEntity> payments
            , Map<Long, Optional<String>> variantCoverImgs) {
        Long metaOrderId = returnRequestEntity.getMetaOrder().getId();

        Optional<PaymentEntity> payment = ofNullable(payments.get(metaOrderId));
        String operator = payment.map(PaymentEntity::getOperator).orElse(null);
        String uid = payment.map(PaymentEntity::getUid).orElse(null);

        Set<ReturnRequestItemDTO> requestItems = getRequestItemsDto(returnRequestEntity, variantCoverImgs);

        ReturnRequestDTO dto = (ReturnRequestDTO) returnRequestEntity.getRepresentation();
        dto.setOperator(operator);
        dto.setPaymentUid(uid);
        dto.setReturnedItems(requestItems);
        if (!requestItems.isEmpty()) {
            AddressRepObj address = requestItems.stream().findFirst().get().getAddress();
            dto.setAddress(address);
        }
        return dto;
    }




    private Long getOrderReturnRequestsCount(CriteriaBuilder builder, Predicate[] predicatesArr) {
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        countQuery.select(  builder.count(
                (Expression<?>) countQuery
                        .from(ReturnRequestEntity.class)
                        .join("returnedItems", INNER)
                        .alias("generatedAlias1")) )
                .where(predicatesArr);
        return em.createQuery(countQuery).getSingleResult();
    }



    private Set<ReturnRequestItemDTO> getRequestItemsDto(ReturnRequestEntity returnRequestEntity
            , Map<Long, Optional<String>> variantCoverImgs) {
        Set<ReturnRequestItemDTO> requestItems =
                returnRequestEntity
                        .getReturnedItems()
                        .stream()
                        .map(i -> (ReturnRequestItemDTO) i.getRepresentation())
                        .collect(toSet());
        return setReturnRequestItemVariantsAdditionalData(requestItems, variantCoverImgs);
    }





    private Set<ReturnRequestItemDTO> setReturnRequestItemVariantsAdditionalData(Set<ReturnRequestItemDTO> requestItems
            , Map<Long, Optional<String>> variantCoverImgs) {
        for(ReturnRequestItemDTO dto : requestItems) {
            variantCoverImgs
                    .get(dto.getVariantId())
                    .ifPresent(dto::setCoverImage);
        }
        return requestItems;
    }




    private Map<Long, Optional<String>> getReturnRequestItemsCoverImgs(List<ReturnRequestEntity> returnRequests) {
        return returnRequests
                .stream()
                .map(ReturnRequestEntity::getReturnedItems)
                .flatMap(Set::stream)
                .map(ReturnRequestItemEntity::getBasket)
                .map(BasketsEntity::getStocksEntity)
                .map(StocksEntity::getProductVariantsEntity)
                .map(ProductVariantsEntity::getId)
                .collect(collectingAndThen(toList(), imgService::getVariantsCoverImages));
    }




    private ReturnRequestEntity updateReturnRequestStatus(ReturnRequestEntity request, ReturnRequestStatus newStatus){
        ReturnRequestStatus currentStatus = ReturnRequestStatus.findEnum(request.getStatus());

        if(!canOrderReturnStatusChangeTo(currentStatus, newStatus)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0018, currentStatus.name(), newStatus.name());
        }
        request.setStatus(newStatus.getValue());
        return returnRequestRepo.save(request);
    }




    private boolean canOrderReturnStatusChangeTo(ReturnRequestStatus currentStatus, ReturnRequestStatus newStatus) {
        return ofNullable(currentStatus)
                .map(orderReturnStateMachine::get)
                .orElse(emptySet())
                .contains(newStatus);
    }




    private void buildOrderReturnStatusTransitionMap() {
        orderReturnStateMachine = new HashMap<>();
        buildMap(orderReturnStateMachine)
                .put(ReturnRequestStatus.NEW	, setOf(CONFIRMED, REJECTED))
                .put(CONFIRMED					, setOf(RECEIVED, REJECTED));
    }




    private void sendReturnRequestConfirmationEmail(ReturnRequestEntity request, List<ReturnShipmentTracker> trackers) {
        String orgName = ofNullable(request)
                .map(ReturnRequestEntity::getMetaOrder)
                .map(MetaOrderEntity::getOrganization)
                .map(OrganizationEntity::getName)
                .orElse("Nasnav");
        Optional<String> email =
                ofNullable(request)
                        .map(ReturnRequestEntity::getMetaOrder)
                        .map(MetaOrderEntity::getUser)
                        .map(UserEntity::getEmail);
        if(!email.isPresent()) {
            return;
        }
        String subject = ORDER_RETURN_CONFIRM_SUBJECT;
        Map<String,Object> parametersMap = createReturnConfirmEmailParams(request, trackers);
        List<MailAttachment> airwayBills = createReturnConfirmMailAttachments(trackers);
        String template = ORDER_RETURN_CONFIRM_TEMPLATE;
        try {
            mailService.sendThymeleafTemplateMail(orgName, email.get(), subject,  template, parametersMap, airwayBills);
        } catch (MessagingException e) {
            logger.error(e, e);
        }
    }




    private Map<String, Object> createReturnConfirmEmailParams(ReturnRequestEntity request, List<ReturnShipmentTracker> trackers) {
        Optional<UserEntity> user =  getCustomerName(request);
        String userName = user.map(UserEntity::getFirstName).orElse("Dear Customer");
        Optional<OrganizationEntity> org =
                ofNullable(request)
                        .map(ReturnRequestEntity::getMetaOrder)
                        .map(MetaOrderEntity::getOrganization);

        String msg = getReturnConfirmationEmailMsg(trackers);
        String shippingService = getShippingService(request);
        AddressRepObj pickupAddr = getPickupAddress(request);
        String phone =
                ofNullable(pickupAddr)
                        .map(AddressRepObj::getPhoneNumber)
                        .orElseGet(() -> user.map(UserEntity::getPhoneNumber).orElse(""));
        List<ReturnShipment> returnShipmentsData = getReturnShipmentsData(request);
        Map<String, Object> params = createOrgPropertiesParams(org.get());
        params.put("userName", userName);
        params.put("requestId", request.getId());
        params.put("msg", msg);
        params.put("phone", phone);
        params.put("pickupAddr", pickupAddr);
        params.put("shippingService", shippingService);
        params.put("returnShipments", returnShipmentsData);
        return params;
    }




    private Optional<UserEntity> getCustomerName(ReturnRequestEntity request) {
        return ofNullable(request)
                .map(ReturnRequestEntity::getMetaOrder)
                .map(MetaOrderEntity::getUser);
    }




    private String getReturnConfirmationEmailMsg(List<ReturnShipmentTracker> trackers) {
        return trackers
                .stream()
                .map(ReturnShipmentTracker::getEmailMessage)
                .findFirst()
                .orElse("Thanks for you patience!");
    }




    private String getShippingService(ReturnRequestEntity request) {
        return request
                .getReturnedItems()
                .stream()
                .findFirst()
                .map(ReturnRequestItemEntity::getReturnShipment)
                .map(ReturnShipmentEntity::getShippingServiceId)
                .orElse("N/A");
    }




    private AddressRepObj getPickupAddress(ReturnRequestEntity request) {
        return ofNullable(request)
                .map(ReturnRequestEntity::getMetaOrder)
                .map(MetaOrderEntity::getSubOrders)
                .map(Set::stream)
                .flatMap(Stream::findFirst)
                .map(OrdersEntity::getAddressEntity)
                .map(AddressesEntity::getRepresentation)
                .map(AddressRepObj.class::cast)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0018));
    }



    private List<ReturnShipment> getReturnShipmentsData(ReturnRequestEntity request) {
        Map<Long, Optional<String>> variantsCoverImages =
                ofNullable(request)
                        .map(ReturnRequestEntity::getMetaOrder)
                        .map(orderServiceHelper::getVariantsImagesList)
                        .orElse(emptyMap());

        return ofNullable(request.getReturnedItems())
                .orElse(emptySet())
                .stream()
                .collect(
                        collectingAndThen(
                                groupingBy(this::getTrackNumber)
                                , itmsGroupedByShipment -> createReturnShipmentData(itmsGroupedByShipment, variantsCoverImages)));
    }




    private Map<String, Object> createOrgPropertiesParams(OrganizationEntity org) {
        String domain = domainService.getBackendUrl();
        String orgDomain = domainService.getOrganizationDomainAndSubDir(org.getId());
        String orgLogo = domain + "/files/"+ orderEmailHelper.getOrganizationLogo(org);
        String orgName = org.getName();
        String year = LocalDateTime.now().getYear()+"";

        Map<String, Object> params = new HashMap<>();
        params.put("orgDomain", orgDomain);
        params.put("domain", domain);
        params.put("orgName", orgName);
        params.put("orgLogo", orgLogo);
        params.put("year", year);

        return params;
    }



    private List<MailAttachment> createReturnConfirmMailAttachments(List<ReturnShipmentTracker> trackers) {
        Optional<Long> returnRequestId =
                trackers
                    .stream()
                    .map(ReturnShipmentTracker::getShippingDetails)
                    .map(ShippingDetails::getReturnRequestId)
                    .filter(Objects::nonNull)
                    .findFirst();
        return range(0, trackers.size())
                .mapToObj(i -> doCreateReturnConfirmAirwaybill(i, trackers.get(i), trackers.size()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }



    private Optional<MailAttachment> doCreateReturnConfirmAirwaybill(int i, ReturnShipmentTracker tracker, Integer shipmentsCount) {
        String fileName =
                ofNullable(tracker)
                        .map(ReturnShipmentTracker::getTracker)
                        .map(trackNum -> format("airwaybill-%s.pdf", trackNum))
                        .orElse("airwaybill.pdf");
        return ofNullable(tracker)
                .map(ReturnShipmentTracker::getAirwayBillFile)
                .map(Base64.getDecoder()::decode)
                .map(fileDecoded -> new MailAttachment(fileName, new ByteArrayResource(fileDecoded)));
    }



    private  String getTrackNumber(ReturnRequestItemEntity itm) {
        return ofNullable(itm)
                .map(ReturnRequestItemEntity::getReturnShipment)
                .map(ReturnShipmentEntity::getTrackNumber)
                .orElse("N/A");
    }




    private List<ReturnShipment> createReturnShipmentData(Map<String, List<ReturnRequestItemEntity>> groupedbByTrackNum
            , Map<Long, Optional<String>> variantsCoverImages) {
        return groupedbByTrackNum
                .entrySet()
                .stream()
                .map(ent -> toReturnShipment(ent.getKey(), ent.getValue(), variantsCoverImages))
                .collect(toList());
    }




    private ReturnShipment toReturnShipment(String trackNum, List<ReturnRequestItemEntity> itemEntities
            , Map<Long, Optional<String>> variantsCoverImages) {
        List<ReturnShipmentItem> items =
                itemEntities
                        .stream()
                        .map(itm -> toReturnShipmentItem(itm, variantsCoverImages))
                        .collect(toList());
        ReturnShipment shipment = new ReturnShipment();
        shipment.setTrackNumber(trackNum);
        shipment.setItems(items);
        return shipment;
    }





    private ReturnShipmentItem toReturnShipmentItem(ReturnRequestItemEntity returnRequestItemEntity
            , Map<Long, Optional<String>> variantsCoverImages) {
        BasketsEntity orderItem = returnRequestItemEntity.getBasket();
        ProductVariantsEntity variant = orderItem.getStocksEntity().getProductVariantsEntity();
        ProductEntity product = variant.getProductEntity();
        BigDecimal price = orderItem.getPrice();
        String thumb = variantsCoverImages.get(variant.getId()).orElse("NA");
        String currency = ofNullable(getTransactionCurrency(orderItem.getCurrency())).orElse(EGP).name();
        Map<String, String> variantFeatures = productService.parseVariantFeatures(variant, 0);

        ReturnShipmentItem item = new ReturnShipmentItem();
        item.setName(product.getName());
        item.setQuantity(returnRequestItemEntity.getReturnedQuantity());
        item.setReceivedQuantity(returnRequestItemEntity.getReceivedQuantity());
        item.setCurrency(currency);
        item.setPrice(price);
        item.setVariantFeatures(variantFeatures);
        item.setThumb(thumb);
        item.setSku(variant.getSku());
        item.setProductCode(variant.getProductCode());
        return item;
    }



    private void sendRejectionEmailToCustomer(ReturnRequestEntity request, String rejectionReason, Long orgId) {

        String orgName = request.getMetaOrder().getOrganization().getName();
        String to = request.getMetaOrder().getUser().getEmail();
        String subject = ORDER_RETURN_REJECT_SUBJECT;
        List<String> bcc = empRoleRepo.findEmailOfEmployeeWithRoleAndOrganization(ORGANIZATION_MANAGER.getValue(), orgId);
        Map<String,Object> parametersMap = createRejectionEmailParams(request, rejectionReason);
        String template = ORDER_RETURN_REJECT_TEMPLATE;
        try {
            mailService.sendThymeleafTemplateMail(orgName, asList(to), subject, emptyList(), bcc, template, parametersMap);
        } catch (IOException | MessagingException e) {
            logger.error(e, e);
        }
    }



    private Map<String, Object> createRejectionEmailParams(ReturnRequestEntity returnRequest, String rejectionReason) {
        Map<String,Object> params = new HashMap<>();
        String message =
                ofNullable(rejectionReason)
                        .orElse(DEFAULT_REJECTION_MESSAGE);
        String year = LocalDateTime.now().getYear()+"";
        params.put("id", returnRequest.getId().toString());
        params.put("rejectionReason", message);
        params.put("returnRequest", returnRequest);
        params.put("year", year);
        return params;
    }



    private void sendOrderReturnNotificationEmail(Long returnRequestId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        String orgName = securityService.getCurrentUserOrganization().getName();
        ReturnRequestEntity request =
                returnRequestRepo
                        .findByReturnRequestId(returnRequestId, orgId)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, returnRequestId));
        List<String> emails = empRoleRepo.findEmailOfEmployeeWithRoleAndOrganization(ORGANIZATION_MANAGER.getValue(), orgId);
        if(emails.isEmpty()) {
            return;
        }
        String subject = format(ORDER_RETURN_NOTIFY_SUBJECT, returnRequestId);
        Map<String,Object> parametersMap = createOrderReturnNotificationEmailParams(request);
        String template = ORDER_RETURN_NOTIFICATION_TEMPLATE;
        try {
            mailService.sendThymeleafTemplateMail(orgName, emails ,subject, emptyList(),  template, parametersMap);
        } catch (MessagingException | IOException e) {
            logger.error(e, e);
        }
    }




    private Map<String, Object> createOrderReturnNotificationEmailParams(ReturnRequestEntity request) {
        Optional<UserEntity> user =  getCustomerName(request);
        String userName = user.map(UserEntity::getFirstName).orElse("Dear Customer");
        String creationDate =
                DateTimeFormatter.ofPattern("dd/MM/YYYY hh:mm").format(request.getCreatedOn());

        Optional<OrganizationEntity> org =
                ofNullable(request)
                        .map(ReturnRequestEntity::getMetaOrder)
                        .map(MetaOrderEntity::getOrganization);

        Long orgId = org.map(OrganizationEntity::getId).orElse(-1L);
        String shippingService = getShippingService(request);
        AddressRepObj pickupAddr = getPickupAddress(request);
        String returnOrderPageUrl = domainService.buildDashboardReturnRequestPageUrl(request.getId(), orgId);
        String phone =
                ofNullable(pickupAddr)
                        .map(AddressRepObj::getPhoneNumber)
                        .orElseGet(() -> user.map(UserEntity::getPhoneNumber).orElse(""));
        List<ReturnShipment> returnShipmentsData = getReturnShipmentsData(request);
        Long metaOrderId = request.getMetaOrder().getId();
        Optional<PaymentEntity> payment = paymentsRepo.findByMetaOrderId(metaOrderId);
        Optional<String> operator = payment.map(PaymentEntity::getOperator);
        Optional<String> payUid = payment.map(PaymentEntity::getUid);

        Map<String, Object> params = createOrgPropertiesParams(org.get());
        params.put("userName", userName);
        params.put("requestId", request.getId());
        params.put("creationDate", creationDate);
        params.put("pickupAddr", pickupAddr);
        params.put("phone", phone);
        params.put("shippingService", shippingService);
        params.put("returnShipments", returnShipmentsData);
        params.put("returnOrderPageUrl", returnOrderPageUrl);
        params.put("paymentOperator", operator);
        params.put("paymentUid", payUid);
        return params;
    }





    private ReturnRequestEntity createReturnRequest(List<ReturnRequestBasketItem> returnedItems) {
        List<Long> returnBasketIds =
                returnedItems
                        .stream()
                        .map(ReturnRequestBasketItem::getOrderItemId)
                        .collect(toList());
        Map<Long, BasketsEntity> basketsMap = orderServiceHelper.getBasketsMap(returnBasketIds);

        MetaOrderEntity metaOrder = getMetaOrderFromBasketItems(returnBasketIds, basketsMap);

        List<ReturnRequestItemEntity> returnItemsEntities =
                createAndValidateReturnItemEntities(returnedItems, basketsMap, metaOrder);

        ReturnRequestEntity returnRequest = new ReturnRequestEntity();
        returnRequest.setMetaOrder(metaOrder);
        returnRequest.setStatus(ReturnRequestStatus.NEW.getValue());
        returnItemsEntities.forEach(returnRequest::addItem);
        setReturnRequestCreator(returnRequest);

        return returnRequest;
    }




    private MetaOrderEntity getMetaOrderFromBasketItems(List<Long> returnBasketIds, Map<Long, BasketsEntity> basketsMap) {
        return basketsMap
                .values()
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(BasketsEntity::getOrdersEntity)
                .map(OrdersEntity::getMetaOrder)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0008, returnBasketIds.toString()));
    }




    private List<ReturnRequestItemEntity> createAndValidateReturnItemEntities(
            List<ReturnRequestBasketItem> returnedItems, Map<Long, BasketsEntity> basketsCache, MetaOrderEntity metaOrder) {
        List<Long> returnBasketIds =
                returnedItems
                        .stream()
                        .map(ReturnRequestBasketItem::getOrderItemId)
                        .collect(toList());

        validateReturnBasketItemsAreReturnable(basketsCache);
        validateReturnBasketItemsIsNew(returnBasketIds);
        validateReturnBasketItemQuantity(basketsCache, returnedItems);
        validateBasketsItemsMetaOrder(basketsCache.values(), metaOrder);
        validateAllBasketsExisting(returnBasketIds, basketsCache);

        return returnedItems
                .stream()
                .map(item -> createReturnRequestItemEntity(item, basketsCache))
                .collect(toList());
    }




    private void validateReturnBasketItemsAreReturnable(Map<Long, BasketsEntity> basketsCache) {
        boolean allReturnable =
                basketsCache
                .values()
                .stream()
                .allMatch(this::isReturnable);
        if(!allReturnable){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0022);
        }
    }




    private void setReturnRequestCreator(ReturnRequestEntity returnRequest) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            EmployeeUserEntity emp = (EmployeeUserEntity)user;
            returnRequest.setCreatedByEmployee(emp);
        }else if(user instanceof UserEntity) {
            UserEntity customer = (UserEntity)user;
            returnRequest.setCreatedByUser(customer);
        }
    }




    private void validateReturnBasketItemsIsNew(List<Long> basketItemsIds) {
        List<ReturnRequestItemEntity> items = returnRequestItemRepo.findByBasket_IdIn(basketItemsIds);
        if (!items.isEmpty())
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0012, items.get(0).getBasket().getId());
    }




    private ReturnRequestItemEntity createReturnRequestItemEntity(ReturnRequestBasketItem itemDto
            , Map<Long,BasketsEntity> basketCache) {
        Integer returnedQty = itemDto.getReturnedQuantity();
        Integer receivedQty = itemDto.getReceivedQuantity();
        BasketsEntity basket =
                ofNullable(itemDto.getOrderItemId())
                        .map(basketCache::get)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0008));

        ReturnRequestItemEntity item = new ReturnRequestItemEntity();
        item.setBasket(basket);
        item.setReturnedQuantity(returnedQty);

        BaseUserEntity user = securityService.getCurrentUser();
        if(securityService.currentUserIsCustomer()) {
            UserEntity customer = (UserEntity)user;
            item.setCreatedByUser(customer);
        }else if(user instanceof EmployeeUserEntity) {
            EmployeeUserEntity emp = (EmployeeUserEntity)user;
            item.setCreatedByEmployee(emp);
            item.setReceivedBy(emp);
            item.setReceivedOn(now());
            item.setReceivedQuantity(receivedQty);
        }

        return item;
    }



    private void increaseReturnRequestStock(ReturnRequestEntity requestEntity) {
        for(ReturnRequestItemEntity item : requestEntity.getReturnedItems()) {
            Integer receivedQuantity = item.getReceivedQuantity();
            StocksEntity stock = item.getBasket().getStocksEntity();
            stockService.incrementStockBy(stock, receivedQuantity);
        }
    }




    private ReturnRequestEntity receiveReturnRequestItems(List<ReceivedItem> returnRequestItems
            ,List<ReturnRequestItemEntity> returnRequestItemEntities) {
        Map<Long, ReturnRequestItemEntity> returnRequestItemEntityMap =
                returnRequestItemEntities
                        .stream()
                        .collect(toMap(ReturnRequestItemEntity::getId, e -> e));

        validateReturnRequestItemQuantity(returnRequestItemEntityMap, returnRequestItems);

        for (ReceivedItem item : returnRequestItems) {
            ReturnRequestItemEntity itemEntity = returnRequestItemEntityMap.get(item.getReturnRequestItemId());
            BaseUserEntity emp = securityService.getCurrentUser();

            itemEntity.setReceivedBy((EmployeeUserEntity)emp);
            itemEntity.setReceivedOn(now());
            itemEntity.setReceivedQuantity(item.getReceivedQuantity());
        }
        return returnRequestItemEntities.get(0).getReturnRequest();
    }



    private void validateReturnRequestItemQuantity(Map<Long, ReturnRequestItemEntity> requestItemEntityMap, List<ReceivedItem> returnedItems) {
        for (ReceivedItem item : returnedItems) {
            Integer basketQuantity = requestItemEntityMap.get(item.getReturnRequestItemId()).getBasket().getQuantity().intValue();
            if(basketQuantity < item.getReceivedQuantity()) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0002);
            }
        }
    }




    private void validateReturnBasketItemQuantity(Map<Long, BasketsEntity> basketsMap, List<ReturnRequestBasketItem> returnedBasketItems) {
        for (ReturnRequestBasketItem item : returnedBasketItems) {
            Long id = item.getOrderItemId();
            Integer returnedQty =  item.getReturnedQuantity();
            Integer basketQuantity =
                    ofNullable(basketsMap.get(id))
                            .map(BasketsEntity::getQuantity)
                            .map(BigDecimal::intValue)
                            .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0014, id));

            if(returnedQty <= 0) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0013);
            }else if(basketQuantity < returnedQty) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0002);
            }
        }
    }



    private void validateBasketsItemsMetaOrder(Collection<BasketsEntity> basketsEntities, MetaOrderEntity metaOrder) {
        validateBasketItemsFromSameMetaOrder(basketsEntities, metaOrder);
        validateItemsBelongToCustomer(metaOrder);
        validateBasketItemsAreReturnable(basketsEntities, metaOrder);
    }



    private void validateBasketItemsAreReturnable(Collection<BasketsEntity> basketsEntities, MetaOrderEntity metaOrder) {
        boolean allReturnable =
                basketsEntities
                        .stream()
                        .allMatch(this::isReturnable);
        if(!allReturnable){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0022);
        }
    }




    public boolean isReturnable(BasketsEntity basketsEntity) {
        return  isWithinReturnTimeWindow(basketsEntity)
                && hasProperOrderStatusForReturn(basketsEntity);
    }



    private boolean isWithinReturnTimeWindow(BasketsEntity basketsEntity) {
        LocalDateTime orderCreationTime =
                ofNullable(basketsEntity)
                        .map(BasketsEntity::getOrdersEntity)
                        .map(OrdersEntity::getMetaOrder)
                        .map(MetaOrderEntity::getCreatedAt)
                        .orElse(LocalDateTime.MIN);
        Long orderAge = Duration.between(orderCreationTime, now()).toDays();
        return orderAge <= MAX_RETURN_TIME_WINDOW;
    }



    private Boolean hasProperOrderStatusForReturn(BasketsEntity basketsEntity) {
        return ofNullable(basketsEntity)
                .map(BasketsEntity::getOrdersEntity)
                .map(OrdersEntity::getStatus)
                .map(OrderStatus::findEnum)
                .map(acceptableStatusForReturn::contains)
                .orElse(false);
    }


    private void validateItemsBelongToCustomer(MetaOrderEntity metaOrder) {
        Long orderCreatorId = metaOrder.getUser().getId();
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof  UserEntity && !Objects.equals( orderCreatorId, user.getId())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0015);
        }
    }



    private void validateBasketItemsFromSameMetaOrder(Collection<BasketsEntity> basketsEntities, MetaOrderEntity metaOrder) {
        boolean hasSameMetaOrder =
                basketsEntities
                        .stream()
                        .map(BasketsEntity::getOrdersEntity)
                        .map(OrdersEntity::getMetaOrder)
                        .allMatch(m -> m.getId().equals(metaOrder.getId()));
        if (!hasSameMetaOrder) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0009);
        }
    }



    private void validateAllBasketsExisting(List<Long> ids, Map<Long, BasketsEntity> basketsMap) {
        Set<Long> fetchedBasketsIds = basketsMap.keySet();
        ids.removeAll(fetchedBasketsIds);
        if (!ids.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0008, ids.toString());
        }
    }



    private void sendItemReceivedEmailToCustomer(Long returnRequestId) {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        ReturnRequestEntity request =
                returnRequestRepo
                        .findByReturnRequestId(returnRequestId, org.getId())
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, returnRequestId));
        Optional<String> email =
                ofNullable(request)
                        .map(ReturnRequestEntity::getMetaOrder)
                        .map(MetaOrderEntity::getUser)
                        .map(UserEntity::getEmail);
        if(!email.isPresent()) {
            return;
        }
        String subject = ORDER_RETURN_RECEIVE_SUBJECT;
        Map<String,Object> parametersMap = createReturnItemsReceiptionEmailParams(request);
        String template = ORDER_RETURN_RECEIVED_TEMPLATE;
        try {
            mailService.sendThymeleafTemplateMail(org.getName(), email.get(), subject,  template, parametersMap);
        } catch (MessagingException e) {
            logger.error(e, e);
        }
    }




    private Map<String, Object> createReturnItemsReceiptionEmailParams(ReturnRequestEntity request) {
        Optional<UserEntity> user =  getCustomerName(request);
        String userName = user.map(UserEntity::getFirstName).orElse("Dear Customer");
        String creationDate =
                DateTimeFormatter.ofPattern("dd/MM/YYYY hh:mm").format(request.getCreatedOn());

        Optional<OrganizationEntity> org =
                ofNullable(request)
                        .map(ReturnRequestEntity::getMetaOrder)
                        .map(MetaOrderEntity::getOrganization);
        String shippingService = getShippingService(request);
        AddressRepObj pickupAddr = getPickupAddress(request);
        String phone =
                ofNullable(pickupAddr)
                        .map(AddressRepObj::getPhoneNumber)
                        .orElseGet(() -> user.map(UserEntity::getPhoneNumber).orElse(""));

        List<ReturnShipment> returnShipmentsData = getReturnShipmentsData(request);
        Map<String, Object> params = createOrgPropertiesParams(org.get());
        params.put("userName", userName);
        params.put("requestId", request.getId());
        params.put("creationDate", creationDate);
        params.put("pickupAddr", pickupAddr);
        params.put("phone", phone);
        params.put("shippingService", shippingService);
        params.put("returnShipments", returnShipmentsData);
        return params;
    }




    private List<ReturnRequestBasketItem> getReturnRequestBasketItemList(ReceivedItemsDTO returnedItemsDTO) {
        return ofNullable(returnedItemsDTO.getBasketItems())
                .orElse(emptyList())
                .stream()
                .map(item -> new ReturnRequestBasketItem(item.getOrderItemId(), item.getReceivedQuantity(), item.getReceivedQuantity()))
                .collect(toList());
    }


    private void validateReturnedItemsDTO(ReceivedItemsDTO returnedItemsDTO) {
        boolean hasReturnItems = !isNullOrEmpty(returnedItemsDTO.getReturnedItems());
        boolean hasBasketItems = !isNullOrEmpty(returnedItemsDTO.getBasketItems());
        boolean hasEitherReturnItemsOrBasketItems =  hasReturnItems ^  hasBasketItems;
        if (!hasEitherReturnItemsOrBasketItems) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0003);
        }
        if (hasReturnItems) {
            validateReturnedItemsList(returnedItemsDTO.getReturnedItems());
        }
        if (hasBasketItems) {
            validateReturnedBasketItem(returnedItemsDTO.getBasketItems());
        }
    }



    private void validateReturnedItemsList(List<ReceivedItem> returnRequestItems) {
        for(ReceivedItem item : returnRequestItems) {
            if (Objects.equals(item.getReturnRequestItemId(), null)) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0004);
            }
            if (Objects.equals(item.getReceivedQuantity(), null)) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0005);
            }
        }
    }




    private void validateReturnedBasketItem(List<ReceivedBasketItem> returnBasketItems) {
        for(ReceivedBasketItem item : returnBasketItems) {
            if (Objects.equals(item.getOrderItemId(), null)) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0006);
            }
            if (Objects.equals(item.getReceivedQuantity(), null)) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0007);
            }
        }
    }




    private boolean isOnlyReturnedItemsProvided(List<ReceivedItem> returnRequestItems, List<ReceivedBasketItem> returnBasketItems) {
        return !isNullOrEmpty(returnRequestItems) && isNullOrEmpty(returnBasketItems);
    }



    private boolean isOnlyReturnedBasketItemsProvided(List<ReceivedItem> returnRequestItems, List<ReceivedBasketItem> returnBasketItems) {
        return isNullOrEmpty(returnRequestItems) && !isNullOrEmpty(returnBasketItems);
    }



    private boolean areBothReturnedItemsAndBasketItemsProvided(List<ReceivedItem> returnRequestItems, List<ReceivedBasketItem> returnBasketItems) {
        return !isNullOrEmpty(returnRequestItems) && !isNullOrEmpty(returnBasketItems);
    }





    private ReturnRequestEntity assignReturnBasketItemsToReturnRequest(ReturnRequestEntity requestEntity,
                                                                       List<ReceivedBasketItem> receivedBasketItems) {
        if(receivedBasketItems.isEmpty()){
            return requestEntity;
        }
        MetaOrderEntity metaOrder = requestEntity.getMetaOrder();
        Map<Long, BasketsEntity> basketsCache = createOrderItemsEntityCache(receivedBasketItems);
        receivedBasketItems
                .stream()
                .map(this::createReturnRequestBasketItem)
                .collect(collectingAndThen(
                        toList()
                        , items -> createAndValidateReturnItemEntities(items, basketsCache, metaOrder)))
                .forEach(requestEntity::addItem);

        return requestEntity;
    }




    private Map<Long, BasketsEntity> createOrderItemsEntityCache(List<ReceivedBasketItem> receivedBasketItems) {
        List<Long> returnBasketIds =
                receivedBasketItems
                        .stream()
                        .map(ReceivedBasketItem::getOrderItemId)
                        .collect(toList());

        return orderServiceHelper.getBasketsMap(returnBasketIds);
    }




    private ReturnRequestBasketItem createReturnRequestBasketItem(ReceivedBasketItem item) {
        Integer receivedQty = item.getReceivedQuantity();
        return new ReturnRequestBasketItem(item.getOrderItemId(), receivedQty, receivedQty);
    }



    private List<ReturnRequestItemEntity> getAndValidateReturnRequestItemEntities (List<ReceivedItem> returnRequestItems) {
        List<Long> returnItemsIds = returnRequestItems
                .stream()
                .map(ReceivedItem::getReturnRequestItemId)
                .collect(toList());

        List<ReturnRequestItemEntity> returnRequestItemEntities = returnRequestItemRepo.findByIdIn(returnItemsIds);
        validateAllReturnItemsExisting(returnItemsIds, returnRequestItemEntities);
        validateAllReturnItemsHasSameReturnRequest(returnRequestItemEntities);
        validateAllReturnItemsAreNotReceivedBefore(returnRequestItemEntities);
        return returnRequestItemEntities;
    }




    private void validateAllReturnItemsAreNotReceivedBefore(List<ReturnRequestItemEntity> items) {
        boolean someItemAlreadyReceived =
                !items
                        .stream()
                        .allMatch(item -> allIsNull(item.getReceivedBy(), item.getReceivedOn()));
        boolean isStoreManager = securityService.currentUserHasRole(STORE_MANAGER);
        if(someItemAlreadyReceived && isStoreManager){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0020);
        }
    }



    private void validateAllReturnItemsExisting(List<Long> ids, List<ReturnRequestItemEntity> returnRequestItemEntities) {
        List<Long> existingIds = returnRequestItemEntities
                .stream()
                .map(ReturnRequestItemEntity::getId)
                .collect(toList());

        ids.removeAll(existingIds);

        if(!ids.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0001, ids.toString());
        }
    }





    private void validateAllReturnItemsHasSameReturnRequest(List<ReturnRequestItemEntity> returnRequestItemEntities) {
        if(!returnRequestItemEntities.isEmpty()) {
            ReturnRequestEntity returnRequestEntity = returnRequestItemEntities
                    .stream()
                    .findFirst()
                    .get()
                    .getReturnRequest();

            boolean hasSameReturnRequest = returnRequestItemEntities
                    .stream()
                    .map(ReturnRequestItemEntity::getReturnRequest)
                    .allMatch(r -> Objects.equals(r,returnRequestEntity));
            if(!hasSameReturnRequest) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0010);
            }
        }
    }

}
