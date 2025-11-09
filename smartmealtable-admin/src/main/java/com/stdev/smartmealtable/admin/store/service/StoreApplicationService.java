package com.stdev.smartmealtable.admin.store.service;

import com.stdev.smartmealtable.admin.store.service.dto.*;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.map.AddressSearchResult;
import com.stdev.smartmealtable.domain.map.MapService;
import com.stdev.smartmealtable.domain.store.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.core.error.ErrorType.*;

/**
 * 음식점 관리 Application Service (ADMIN) - v2.0
 * 
 * <p>트랜잭션 관리와 유즈케이스에 집중합니다.</p>
 * <p>주소 기반 지오코딩을 통해 좌표를 자동으로 계산합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StoreApplicationService {

    private final StoreRepository storeRepository;
    private final MapService mapService;
    private final StoreImageService storeImageService;

    /**
     * 음식점 목록 조회 (페이징)
     */
    public StoreListServiceResponse getStores(StoreListServiceRequest request) {
        log.info("[ADMIN] 음식점 목록 조회 - categoryId: {}, name: {}, type: {}, page: {}, size: {}", 
                request.categoryId(), request.name(), request.storeType(), request.page(), request.size());
        
        StorePageResult pageResult = storeRepository.adminSearch(
                request.categoryId(),
                request.name(),
                request.storeType(),
                request.page(),
                request.size()
        );
        
        List<StoreServiceResponse> stores = pageResult.content().stream()
                .map(StoreServiceResponse::from)
                .collect(Collectors.toList());
        
        log.info("[ADMIN] 음식점 목록 조회 완료 - 총 {}개, {}페이지", 
                pageResult.totalElements(), pageResult.totalPages());
        
        return StoreListServiceResponse.of(
                stores,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }

    /**
     * 음식점 상세 조회 - v2.0
     * 
     * <p>이미지 목록도 함께 조회합니다.</p>
     */
    public StoreServiceResponse getStore(Long storeId) {
        log.info("[ADMIN] 음식점 상세 조회 - storeId: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        // 이미지 목록 조회
        List<StoreImage> images = storeImageService.getStoreImages(storeId);
        
        log.info("[ADMIN] 음식점 상세 조회 완료 - name: {}, images: {}", store.getName(), images.size());
        
        return StoreServiceResponse.from(store, images);
    }

    /**
     * 음식점 생성 - v2.0
     * 
     * <p>주소를 기반으로 지오코딩하여 좌표를 자동으로 계산합니다.</p>
     */
    @Transactional
    public StoreServiceResponse createStore(CreateStoreServiceRequest request) {
        log.info("[ADMIN] 음식점 생성 요청 - name: {}, address: {}", request.name(), request.address());
        
        // 1. 주소로 좌표 검색 (Naver Maps Geocoding API)
        List<AddressSearchResult> results = mapService.searchAddress(request.address(), 1);
        
        if (results.isEmpty()) {
            log.error("[ADMIN] 유효하지 않은 주소 - address: {}", request.address());
            throw new BusinessException(INVALID_ADDRESS);
        }
        
        AddressSearchResult addressResult = results.get(0);
        BigDecimal latitude = addressResult.latitude();
        BigDecimal longitude = addressResult.longitude();
        
        log.info("[ADMIN] 지오코딩 완료 - lat: {}, lng: {}", latitude, longitude);
        
        // 2. Store 엔티티 생성 (좌표 자동 설정)
        Store store = Store.create(
                request.name(),
                request.categoryIds(),
                request.address(),
                request.lotNumberAddress(),
                latitude,  // 지오코딩 결과 사용
                longitude, // 지오코딩 결과 사용
                request.phoneNumber(),
                request.description(),
                request.averagePrice(),
                0, // reviewCount
                0, // viewCount
                0, // favoriteCount
                request.storeType()
        );
        
        // sellerId는 별도로 설정
        Store storeWithDetails = Store.builder()
                .name(store.getName())
                .categoryIds(store.getCategoryIds())
                .sellerId(request.sellerId())
                .address(store.getAddress())
                .lotNumberAddress(store.getLotNumberAddress())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .phoneNumber(store.getPhoneNumber())
                .description(store.getDescription())
                .averagePrice(store.getAveragePrice())
                .reviewCount(store.getReviewCount())
                .viewCount(store.getViewCount())
                .favoriteCount(store.getFavoriteCount())
                .storeType(store.getStoreType())
                .registeredAt(LocalDateTime.now())
                .build();
        
        Store savedStore = storeRepository.save(storeWithDetails);
        
        log.info("[ADMIN] 음식점 생성 완료 - storeId: {}, name: {}", 
                savedStore.getStoreId(), savedStore.getName());
        
        return StoreServiceResponse.from(savedStore);
    }

    /**
     * 음식점 수정 - v2.0
     * 
     * <p>주소 변경 시 자동으로 지오코딩 수행합니다.</p>
     */
    @Transactional
    public StoreServiceResponse updateStore(Long storeId, UpdateStoreServiceRequest request) {
        log.info("[ADMIN] 음식점 수정 요청 - storeId: {}, name: {}", storeId, request.name());
        
        Store existingStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        // 주소 기반 지오코딩 수행 (항상 수행)
        log.info("[ADMIN] 주소 기반 지오코딩 수행 - address: {}", request.address());
        
        List<AddressSearchResult> results = mapService.searchAddress(request.address(), 1);
        
        if (results.isEmpty()) {
            log.error("[ADMIN] 유효하지 않은 주소 - address: {}", request.address());
            throw new BusinessException(INVALID_ADDRESS);
        }
        
        AddressSearchResult addressResult = results.get(0);
        BigDecimal latitude = addressResult.latitude();
        BigDecimal longitude = addressResult.longitude();
        
        log.info("[ADMIN] 지오코딩 완료 - lat: {}, lng: {}", latitude, longitude);
        
        Store updatedStore = Store.builder()
                .storeId(existingStore.getStoreId())
                .name(request.name())
                .categoryIds(request.categoryIds())
                .sellerId(existingStore.getSellerId()) // sellerId는 수정하지 않음
                .address(request.address())
                .lotNumberAddress(request.lotNumberAddress())
                .latitude(latitude)
                .longitude(longitude)
                .phoneNumber(request.phoneNumber())
                .description(request.description())
                .averagePrice(request.averagePrice())
                .reviewCount(existingStore.getReviewCount())
                .viewCount(existingStore.getViewCount())
                .favoriteCount(existingStore.getFavoriteCount())
                .storeType(request.storeType())
                .registeredAt(existingStore.getRegisteredAt())
                .deletedAt(existingStore.getDeletedAt())
                .build();
        
        Store savedStore = storeRepository.save(updatedStore);
        
        log.info("[ADMIN] 음식점 수정 완료 - storeId: {}", storeId);
        
        return StoreServiceResponse.from(savedStore);
    }

    /**
     * 음식점 삭제 (논리적 삭제)
     */
    @Transactional
    public void deleteStore(Long storeId) {
        log.info("[ADMIN] 음식점 삭제 요청 - storeId: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        if (store.isDeleted()) {
            throw new BusinessException(STORE_ALREADY_DELETED);
        }
        
        storeRepository.softDelete(storeId);
        
        log.info("[ADMIN] 음식점 삭제 완료 - storeId: {}", storeId);
    }

    // ===== 영업시간 관리 =====

    /**
     * 영업시간 목록 조회
     */
    public List<OpeningHourServiceResponse> getOpeningHours(Long storeId) {
        log.info("[ADMIN] 영업시간 목록 조회 - storeId: {}", storeId);
        
        // 음식점 존재 확인
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        List<StoreOpeningHour> openingHours = storeRepository.findOpeningHoursByStoreId(storeId);
        
        log.info("[ADMIN] 영업시간 목록 조회 완료 - count: {}", openingHours.size());
        
        return openingHours.stream()
                .map(OpeningHourServiceResponse::from)
                .toList();
    }

    /**
     * 영업시간 추가
     */
    @Transactional
    public OpeningHourServiceResponse addOpeningHour(Long storeId, OpeningHourServiceRequest request) {
        log.info("[ADMIN] 영업시간 추가 요청 - storeId: {}, dayOfWeek: {}", storeId, request.dayOfWeek());
        
        // 음식점 존재 확인
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        StoreOpeningHour openingHour = new StoreOpeningHour(
                null, // ID는 저장 시 자동 생성
                storeId,
                request.dayOfWeek(),
                request.openTime(),
                request.closeTime(),
                request.breakStartTime(),
                request.breakEndTime(),
                request.isHoliday()
        );
        
        StoreOpeningHour saved = storeRepository.saveOpeningHour(openingHour);
        
        log.info("[ADMIN] 영업시간 추가 완료 - openingHourId: {}", saved.storeOpeningHourId());
        
        return OpeningHourServiceResponse.from(saved);
    }

    /**
     * 영업시간 수정
     */
    @Transactional
    public OpeningHourServiceResponse updateOpeningHour(
            Long storeId, 
            Long openingHourId, 
            OpeningHourServiceRequest request
    ) {
        log.info("[ADMIN] 영업시간 수정 요청 - storeId: {}, openingHourId: {}", storeId, openingHourId);
        
        StoreOpeningHour existing = storeRepository.findOpeningHourById(openingHourId)
                .orElseThrow(() -> new BusinessException(OPENING_HOUR_NOT_FOUND));
        
        if (!existing.storeId().equals(storeId)) {
            throw new BusinessException(OPENING_HOUR_NOT_BELONG_TO_STORE);
        }
        
        StoreOpeningHour updated = new StoreOpeningHour(
                existing.storeOpeningHourId(),
                existing.storeId(),
                request.dayOfWeek(),
                request.openTime(),
                request.closeTime(),
                request.breakStartTime(),
                request.breakEndTime(),
                request.isHoliday()
        );
        
        StoreOpeningHour saved = storeRepository.saveOpeningHour(updated);
        
        log.info("[ADMIN] 영업시간 수정 완료 - openingHourId: {}", openingHourId);
        
        return OpeningHourServiceResponse.from(saved);
    }

    /**
     * 영업시간 삭제
     */
    @Transactional
    public void deleteOpeningHour(Long storeId, Long openingHourId) {
        log.info("[ADMIN] 영업시간 삭제 요청 - storeId: {}, openingHourId: {}", storeId, openingHourId);
        
        StoreOpeningHour existing = storeRepository.findOpeningHourById(openingHourId)
                .orElseThrow(() -> new BusinessException(OPENING_HOUR_NOT_FOUND));
        
        if (!existing.storeId().equals(storeId)) {
            throw new BusinessException(OPENING_HOUR_NOT_BELONG_TO_STORE);
        }
        
        storeRepository.deleteOpeningHourById(openingHourId);
        
        log.info("[ADMIN] 영업시간 삭제 완료 - openingHourId: {}", openingHourId);
    }

    // ===== 임시 휴무 관리 =====

    /**
     * 임시 휴무 목록 조회
     */
    public List<TemporaryClosureServiceResponse> getTemporaryClosures(Long storeId) {
        log.info("[ADMIN] 임시 휴무 목록 조회 - storeId: {}", storeId);
        
        // 음식점 존재 확인
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        List<StoreTemporaryClosure> closures = storeRepository.findTemporaryClosuresByStoreId(storeId);
        
        log.info("[ADMIN] 임시 휴무 목록 조회 완료 - count: {}", closures.size());
        
        return closures.stream()
                .map(TemporaryClosureServiceResponse::from)
                .toList();
    }

    /**
     * 임시 휴무 등록
     */
    @Transactional
    public TemporaryClosureServiceResponse addTemporaryClosure(
            Long storeId, 
            TemporaryClosureServiceRequest request
    ) {
        log.info("[ADMIN] 임시 휴무 등록 요청 - storeId: {}, closureDate: {}", storeId, request.closureDate());
        
        // 음식점 존재 확인
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        StoreTemporaryClosure closure = new StoreTemporaryClosure(
                null, // ID는 저장 시 자동 생성
                storeId,
                request.closureDate(),
                request.startTime(),
                request.endTime(),
                request.reason()
        );
        
        StoreTemporaryClosure saved = storeRepository.saveTemporaryClosure(closure);
        
        log.info("[ADMIN] 임시 휴무 등록 완료 - closureId: {}", saved.storeTemporaryClosureId());
        
        return TemporaryClosureServiceResponse.from(saved);
    }

    /**
     * 임시 휴무 삭제
     */
    @Transactional
    public void deleteTemporaryClosure(Long storeId, Long closureId) {
        log.info("[ADMIN] 임시 휴무 삭제 요청 - storeId: {}, closureId: {}", storeId, closureId);
        
        StoreTemporaryClosure existing = storeRepository.findTemporaryClosureById(closureId)
                .orElseThrow(() -> new BusinessException(TEMPORARY_CLOSURE_NOT_FOUND));
        
        if (!existing.storeId().equals(storeId)) {
            throw new BusinessException(TEMPORARY_CLOSURE_NOT_BELONG_TO_STORE);
        }
        
        storeRepository.deleteTemporaryClosureById(closureId);
        
        log.info("[ADMIN] 임시 휴무 삭제 완료 - closureId: {}", closureId);
    }
}
