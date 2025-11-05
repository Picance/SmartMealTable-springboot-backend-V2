package com.stdev.smartmealtable.admin.store.service;

import com.stdev.smartmealtable.admin.store.service.dto.*;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.store.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.core.error.ErrorType.*;

/**
 * 음식점 관리 Application Service (ADMIN)
 * 
 * <p>트랜잭션 관리와 유즈케이스에 집중합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StoreApplicationService {

    private final StoreRepository storeRepository;

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
     * 음식점 상세 조회
     */
    public StoreServiceResponse getStore(Long storeId) {
        log.info("[ADMIN] 음식점 상세 조회 - storeId: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        log.info("[ADMIN] 음식점 상세 조회 완료 - name: {}", store.getName());
        
        return StoreServiceResponse.from(store);
    }

    /**
     * 음식점 생성
     */
    @Transactional
    public StoreServiceResponse createStore(CreateStoreServiceRequest request) {
        log.info("[ADMIN] 음식점 생성 요청 - name: {}", request.name());
        
        Store store = Store.create(
                request.name(),
                request.categoryId(),
                request.address(),
                request.lotNumberAddress(),
                request.latitude(),
                request.longitude(),
                request.phoneNumber(),
                request.description(),
                request.averagePrice(),
                0, // reviewCount
                0, // viewCount
                0, // favoriteCount
                request.storeType()
        );
        
        // sellerId, imageUrl은 별도로 설정
        Store storeWithDetails = Store.builder()
                .name(store.getName())
                .categoryId(store.getCategoryId())
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
                .imageUrl(request.imageUrl())
                .registeredAt(LocalDateTime.now())
                .build();
        
        Store savedStore = storeRepository.save(storeWithDetails);
        
        log.info("[ADMIN] 음식점 생성 완료 - storeId: {}, name: {}", 
                savedStore.getStoreId(), savedStore.getName());
        
        return StoreServiceResponse.from(savedStore);
    }

    /**
     * 음식점 수정
     */
    @Transactional
    public StoreServiceResponse updateStore(Long storeId, UpdateStoreServiceRequest request) {
        log.info("[ADMIN] 음식점 수정 요청 - storeId: {}, name: {}", storeId, request.name());
        
        Store existingStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        Store updatedStore = Store.builder()
                .storeId(existingStore.getStoreId())
                .name(request.name())
                .categoryId(request.categoryId())
                .sellerId(existingStore.getSellerId()) // sellerId는 수정하지 않음
                .address(request.address())
                .lotNumberAddress(request.lotNumberAddress())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .phoneNumber(request.phoneNumber())
                .description(request.description())
                .averagePrice(request.averagePrice())
                .reviewCount(existingStore.getReviewCount())
                .viewCount(existingStore.getViewCount())
                .favoriteCount(existingStore.getFavoriteCount())
                .storeType(request.storeType())
                .imageUrl(request.imageUrl())
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
