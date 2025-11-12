package com.stdev.smartmealtable.api.cart.service;

import com.stdev.smartmealtable.api.budget.service.DailyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.MonthlyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.dto.DailyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceRequest;
import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.api.cart.dto.*;
import com.stdev.smartmealtable.api.expenditure.service.CreateExpenditureService;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceRequest;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.cart.Cart;
import com.stdev.smartmealtable.domain.cart.CartDomainService;
import com.stdev.smartmealtable.domain.cart.CartItem;
import com.stdev.smartmealtable.domain.cart.CartRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 장바구니 Application Service
 * 유즈케이스에 집중하며 도메인 서비스를 조합하여 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartDomainService cartDomainService;
    private final StoreRepository storeRepository;
    private final FoodRepository foodRepository;
    private final CreateExpenditureService createExpenditureService;
    private final DailyBudgetQueryService dailyBudgetQueryService;
    private final MonthlyBudgetQueryService monthlyBudgetQueryService;

    
    /**
     * 장바구니에 아이템 추가
     * 다른 가게의 상품이 이미 담겨있을 때:
     * - replaceCart=true: 기존 장바구니를 비우고 새 아이템 추가
     * - replaceCart=false: 409 Conflict 에러 발생
     *
     * @param memberId 회원 ID
     * @param request 장바구니 아이템 추가 요청
     * @return 추가된 아이템 정보 및 장바구니 교체 여부
     */
    @Transactional
    public AddCartItemResponse addCartItem(Long memberId, AddCartItemRequest request) {
        Long storeId = request.getStoreId();
        Long foodId = request.getFoodId();
        int quantity = request.getQuantity();
        boolean replaceCart = request.getReplaceCart() != null && request.getReplaceCart();

        // 1. 입력 유효성 검증
        if (quantity <= 0) {
            throw new BusinessException(ErrorType.INVALID_INPUT_VALUE);
        }

        // 2. 가게 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));

        // 3. 음식 존재 여부 확인
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new BusinessException(ErrorType.FOOD_NOT_FOUND));

        // 4. 다른 가게의 장바구니가 있는지 확인
        boolean cartWasReplaced = false;
        List<Cart> memberCarts = cartRepository.findByMemberId(memberId);
        boolean hasCartFromDifferentStore = memberCarts.stream()
                .anyMatch(cart -> !cart.getStoreId().equals(storeId) && !cart.isEmpty());

        if (hasCartFromDifferentStore) {
            if (!replaceCart) {
                // replaceCart=false인 경우 409 Conflict 에러 발생
                throw new BusinessException(ErrorType.CART_CONFLICT);
            }
            // replaceCart=true인 경우 다른 가게의 장바구니 삭제
            cartDomainService.clearOtherCarts(memberId, storeId);
            cartWasReplaced = true;
        }

        // 5. 아이템 추가 (도메인 로직에서 중복 처리)
        Cart cart = cartDomainService.getOrCreateCart(memberId, storeId);
        cart.addItem(foodId, quantity);

        // 6. 저장
        Cart savedCart = cartRepository.save(cart);

        // 7. 추가된 아이템 찾기
        CartItem addedItem = savedCart.getItems().stream()
                .filter(item -> item.getFoodId().equals(foodId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorType.INTERNAL_SERVER_ERROR));

        return AddCartItemResponse.builder()
                .cartId(savedCart.getCartId())
                .cartItemId(addedItem.getCartItemId())
                .foodId(addedItem.getFoodId())
                .quantity(addedItem.getQuantity())
                .replacedCart(cartWasReplaced)
                .build();
    }
    
    /**
     * 장바구니 조회
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @return 장바구니 정보
     */
    public GetCartResponse getCart(Long memberId, Long storeId) {
        // 1. 장바구니 조회
        Cart cart = cartRepository.findByMemberIdAndStoreId(memberId, storeId)
                .orElseThrow(() -> new BusinessException(ErrorType.CART_NOT_FOUND));
        
        // 2. 가게 정보 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));
        
        // 3. 음식 정보 조회
        List<Long> foodIds = cart.getItems().stream()
                .map(CartItem::getFoodId)
                .collect(Collectors.toList());
        
        Map<Long, Food> foodMap = foodRepository.findByIdIn(foodIds).stream()
                .collect(Collectors.toMap(Food::getFoodId, food -> food));
        
        // 4. 응답 생성
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    Food food = foodMap.get(item.getFoodId());
                    if (food == null) {
                        throw new BusinessException(ErrorType.FOOD_NOT_FOUND);
                    }
                    
                    int subtotal = (food.getAveragePrice() != null ? food.getAveragePrice() : 0) * item.getQuantity();
                    
                    return CartItemResponse.builder()
                            .cartItemId(item.getCartItemId())
                            .foodId(food.getFoodId())
                            .foodName(food.getFoodName())
                            .imageUrl(food.getImageUrl())
                            .averagePrice(food.getAveragePrice())
                            .quantity(item.getQuantity())
                            .subtotal(subtotal)
                            .build();
                })
                .collect(Collectors.toList());
        
        int totalAmount = itemResponses.stream()
                .mapToInt(CartItemResponse::getSubtotal)
                .sum();
        
        return GetCartResponse.builder()
                .cartId(cart.getCartId())
                .storeId(cart.getStoreId())
                .storeName(store.getName())
                .items(itemResponses)
                .totalQuantity(cart.getTotalQuantity())
                .totalAmount(totalAmount)
                .build();
    }
    
    /**
     * 회원의 모든 장바구니 조회
     * 
     * @param memberId 회원 ID
     * @return 장바구니 목록
     */
    public List<GetCartResponse> getAllCarts(Long memberId) {
        // 1. 회원의 모든 장바구니 조회
        List<Cart> carts = cartRepository.findByMemberId(memberId);
        
        if (carts.isEmpty()) {
            return List.of();
        }
        
        // 2. 가게 정보 조회
        List<Long> storeIds = carts.stream()
                .map(Cart::getStoreId)
                .collect(Collectors.toList());
        
        Map<Long, Store> storeMap = storeIds.stream()
                .map(storeRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toMap(Store::getStoreId, store -> store));
        
        // 3. 음식 정보 조회
        List<Long> allFoodIds = carts.stream()
                .flatMap(cart -> cart.getItems().stream())
                .map(CartItem::getFoodId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Food> foodMap = foodRepository.findByIdIn(allFoodIds).stream()
                .collect(Collectors.toMap(Food::getFoodId, food -> food));
        
        // 4. 응답 생성
        return carts.stream()
                .map(cart -> {
                    Store store = storeMap.get(cart.getStoreId());
                    if (store == null) {
                        return null; // 가게가 삭제된 경우 스킵
                    }
                    
                    List<CartItemResponse> itemResponses = cart.getItems().stream()
                            .map(item -> {
                                Food food = foodMap.get(item.getFoodId());
                                if (food == null) {
                                    return null; // 음식이 삭제된 경우 스킵
                                }
                                
                                int subtotal = (food.getAveragePrice() != null ? food.getAveragePrice() : 0) * item.getQuantity();
                                
                                return CartItemResponse.builder()
                                        .cartItemId(item.getCartItemId())
                                        .foodId(food.getFoodId())
                                        .foodName(food.getFoodName())
                                        .imageUrl(food.getImageUrl())
                                        .averagePrice(food.getAveragePrice())
                                        .quantity(item.getQuantity())
                                        .subtotal(subtotal)
                                        .build();
                            })
                            .filter(item -> item != null)
                            .collect(Collectors.toList());
                    
                    int totalAmount = itemResponses.stream()
                            .mapToInt(CartItemResponse::getSubtotal)
                            .sum();
                    
                    return GetCartResponse.builder()
                            .cartId(cart.getCartId())
                            .storeId(cart.getStoreId())
                            .storeName(store.getName())
                            .items(itemResponses)
                            .totalQuantity(cart.getTotalQuantity())
                            .totalAmount(totalAmount)
                            .build();
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 장바구니 아이템 수량 수정
     * 
     * @param memberId 회원 ID
     * @param cartItemId 장바구니 아이템 ID
     * @param request 수량 수정 요청
     */
    @Transactional
    public void updateCartItemQuantity(Long memberId, Long cartItemId, UpdateCartItemQuantityRequest request) {
        int newQuantity = request.getQuantity();
        
        // 1. 입력 유효성 검증
        if (newQuantity <= 0) {
            throw new BusinessException(ErrorType.INVALID_INPUT_VALUE);
        }
        
        // 2. 회원의 장바구니에서 아이템 찾기
        List<Cart> carts = cartRepository.findByMemberId(memberId);
        
        Cart targetCart = null;
        for (Cart cart : carts) {
            boolean found = cart.getItems().stream()
                    .anyMatch(item -> item.getCartItemId().equals(cartItemId));
            if (found) {
                targetCart = cart;
                break;
            }
        }
        
        if (targetCart == null) {
            throw new BusinessException(ErrorType.CART_ITEM_NOT_FOUND);
        }
        
        // 3. 수량 변경
        targetCart.updateItemQuantity(cartItemId, newQuantity);
        
        // 4. 저장
        cartRepository.save(targetCart);
    }
    
    /**
     * 장바구니 아이템 삭제
     * 
     * @param memberId 회원 ID
     * @param cartItemId 장바구니 아이템 ID
     */
    @Transactional
    public void removeCartItem(Long memberId, Long cartItemId) {
        // 1. 회원의 장바구니에서 아이템 찾기
        List<Cart> carts = cartRepository.findByMemberId(memberId);
        
        Cart targetCart = null;
        for (Cart cart : carts) {
            boolean found = cart.getItems().stream()
                    .anyMatch(item -> item.getCartItemId().equals(cartItemId));
            if (found) {
                targetCart = cart;
                break;
            }
        }
        
        if (targetCart == null) {
            throw new BusinessException(ErrorType.CART_ITEM_NOT_FOUND);
        }
        
        // 2. 아이템 제거
        targetCart.removeItem(cartItemId);
        
        // 3. 장바구니가 비었으면 장바구니 자체를 삭제, 아니면 저장
        if (targetCart.isEmpty()) {
            cartRepository.delete(targetCart);
        } else {
            cartRepository.save(targetCart);
        }
    }
    
    /**
     * 장바구니 전체 삭제 (비우기)
     * 특정 가게의 장바구니만 삭제
     *
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     */
    @Transactional
    public void clearCart(Long memberId, Long storeId) {
        cartDomainService.clearCart(memberId, storeId);
    }

    /**
     * 모든 장바구니 삭제 (전체 비우기)
     * 회원의 모든 가게 장바구니를 삭제합니다.
     *
     * @param memberId 회원 ID
     */
    @Transactional
    public void clearAllCarts(Long memberId) {
        cartDomainService.clearAllCarts(memberId);
    }
    
    /**
     * 장바구니 체크아웃 (지출로 변환)
     * 장바구니의 모든 항목을 지출 내역으로 등록하고 장바구니를 비웁니다.
     * 
     * @param memberId 회원 ID
     * @param request 체크아웃 요청 정보
     * @return 생성된 지출 내역 정보
     */
    @Transactional
    public CartCheckoutResponse checkoutCart(Long memberId, CartCheckoutRequest request) {
        // 1. 가게 존재 여부 확인
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));
        
        // 2. 장바구니 조회
        Cart cart = cartRepository.findByMemberIdAndStoreId(memberId, request.storeId())
                .orElseThrow(() -> new BusinessException(ErrorType.CART_NOT_FOUND));
        
        // 3. 장바구니가 비어있지 않은지 확인
        if (cart.isEmpty()) {
            throw new BusinessException(ErrorType.INVALID_INPUT_VALUE);
        }
        
        // 4. 장바구니 항목별 총액 계산 및 CheckoutItemResponse 생성
        long subtotal = 0L;
        List<CartCheckoutResponse.CheckoutItemResponse> items = new java.util.ArrayList<>();
        List<CreateExpenditureServiceRequest.ExpenditureItemServiceRequest> expenditureItems = new java.util.ArrayList<>();
        
        for (CartItem cartItem : cart.getItems()) {
            Food food = foodRepository.findById(cartItem.getFoodId())
                    .orElseThrow(() -> new BusinessException(ErrorType.FOOD_NOT_FOUND));
            
            // 음식 가격 결정 (price가 있으면 price, 없으면 averagePrice 사용)
            long itemPrice = food.getPrice() != null ? food.getPrice() : 
                            (food.getAveragePrice() != null ? food.getAveragePrice() : 0L);
            long itemTotal = itemPrice * cartItem.getQuantity();
            subtotal += itemTotal;
            
            // 응답 DTO용 아이템
            items.add(CartCheckoutResponse.CheckoutItemResponse.builder()
                    .foodName(food.getFoodName())
                    .quantity(cartItem.getQuantity())
                    .price(itemPrice)
                    .build());
            
            // 지출 생성용 아이템
            expenditureItems.add(new CreateExpenditureServiceRequest.ExpenditureItemServiceRequest(
                    cartItem.getFoodId(),
                    food.getFoodName(),
                    cartItem.getQuantity(),
                    (int) itemPrice
            ));
        }
        
        // 5. 할인액이 소계를 초과하지 않는지 확인
        long discount = request.discount() != null ? request.discount() : 0L;
        if (discount > subtotal) {
            throw new BusinessException(ErrorType.INVALID_INPUT_VALUE);
        }
        
        // 6. 최종 결제 금액 계산
        long finalAmount = subtotal - discount;
        
        // 6.5. 지출 생성 전 예산 정보 조회 (before)
        DailyBudgetQueryServiceResponse budgetBefore = dailyBudgetQueryService.getDailyBudget(
                memberId,
                request.expendedDate()
        );
        
        // 월별 예산 조회 (지출 날짜 기준)
        YearMonth expenditureMonth = YearMonth.from(request.expendedDate());
        MonthlyBudgetQueryServiceResponse monthlyBudgetBefore = monthlyBudgetQueryService.getMonthlyBudget(
                new MonthlyBudgetQueryServiceRequest(
                        memberId,
                        expenditureMonth.getYear(),
                        expenditureMonth.getMonthValue()
                )
        );
        
        // 7. ExpenditureService를 통해 지출 내역 생성
        // Store의 첫 번째 카테고리를 사용 (없으면 null)
        Long categoryId = store.getCategoryIds() != null && !store.getCategoryIds().isEmpty()
                ? store.getCategoryIds().get(0)
                : null;

        CreateExpenditureServiceRequest expenditureRequest =
                new CreateExpenditureServiceRequest(
                        memberId,
                        request.storeId(),
                        store.getName(),
                        (int) finalAmount,  // 할인이 적용된 최종 금액 저장 (type: Integer)
                        request.expendedDate(),
                        request.expendedTime(),
                        categoryId,  // Store의 첫 번째 카테고리 사용
                        request.mealType(),
                        request.memo(),
                        discount,           // 할인액 전달
                        expenditureItems
                );
        
        CreateExpenditureServiceResponse expenditureResponse = createExpenditureService.createExpenditure(expenditureRequest);
        
        // 7.5. 지출 생성 후 예산 정보 조회 (after)
        DailyBudgetQueryServiceResponse budgetAfter = dailyBudgetQueryService.getDailyBudget(
                memberId,
                request.expendedDate()
        );
        
        MonthlyBudgetQueryServiceResponse monthlyBudgetAfter = monthlyBudgetQueryService.getMonthlyBudget(
                new MonthlyBudgetQueryServiceRequest(
                        memberId,
                        expenditureMonth.getYear(),
                        expenditureMonth.getMonthValue()
                )
        );
        
        // 8. 장바구니 비우기
        clearCart(memberId, request.storeId());
        
        // 식사별 예산 정보 추출
        Integer mealBudgetBefore = budgetBefore.mealBudgets().stream()
                .filter(mb -> mb.mealType() == request.mealType())
                .map(DailyBudgetQueryServiceResponse.MealBudgetInfo::remaining)
                .findFirst()
                .orElse(0);
        
        Integer mealBudgetAfter = budgetAfter.mealBudgets().stream()
                .filter(mb -> mb.mealType() == request.mealType())
                .map(DailyBudgetQueryServiceResponse.MealBudgetInfo::remaining)
                .findFirst()
                .orElse(0);
        
        // 9. 응답 생성
        return CartCheckoutResponse.builder()
                .expenditureId(expenditureResponse.expenditureId())
                .storeName(store.getName())
                .items(items)
                .subtotal(subtotal)
                .discount(discount)
                .finalAmount(finalAmount)
                .mealType(request.mealType().toString())
                .expendedDate(request.expendedDate())
                .expendedTime(request.expendedTime())
                .budgetSummary(CartCheckoutResponse.BudgetSummary.builder()
                        .mealBudgetBefore(mealBudgetBefore.longValue())
                        .mealBudgetAfter(mealBudgetAfter.longValue())
                        .dailyBudgetBefore(budgetBefore.remainingBudget().longValue())
                        .dailyBudgetAfter(budgetAfter.remainingBudget().longValue())
                        .monthlyBudgetBefore(monthlyBudgetBefore.remainingBudget().longValue())
                        .monthlyBudgetAfter(monthlyBudgetAfter.remainingBudget().longValue())
                        .build())
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }
}
