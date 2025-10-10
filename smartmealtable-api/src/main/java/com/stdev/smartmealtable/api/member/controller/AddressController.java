package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.member.dto.request.AddAddressRequest;
import com.stdev.smartmealtable.api.member.dto.request.UpdateAddressRequest;
import com.stdev.smartmealtable.api.member.dto.response.AddressListResponse;
import com.stdev.smartmealtable.api.member.dto.response.AddressResponse;
import com.stdev.smartmealtable.api.member.dto.response.PrimaryAddressResponse;
import com.stdev.smartmealtable.api.member.service.AddressManagementService;
import com.stdev.smartmealtable.api.member.service.dto.AddAddressServiceRequest;
import com.stdev.smartmealtable.api.member.service.dto.UpdateAddressServiceRequest;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 주소 관리 API Controller
 */
@RestController
@RequestMapping("/api/v1/members/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressManagementService addressManagementService;

    /**
     * 10.3 주소 목록 조회
     */
    @GetMapping
    public ApiResponse<AddressListResponse> getAddresses(@RequestHeader("X-Member-Id") Long memberId) {
        AddressListResponse response = addressManagementService.getAddressList(memberId);
        return ApiResponse.success(response);
    }

    /**
     * 10.4 주소 추가
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> addAddress(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody AddAddressRequest request
    ) {
        AddAddressServiceRequest serviceRequest = AddAddressServiceRequest.of(memberId, request);
        AddressResponse response = addressManagementService.addAddress(serviceRequest);
        return ApiResponse.success(response);
    }

    /**
     * 10.5 주소 수정
     */
    @PutMapping("/{addressHistoryId}")
    public ApiResponse<AddressResponse> updateAddress(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long addressHistoryId,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        UpdateAddressServiceRequest serviceRequest = UpdateAddressServiceRequest.of(
                memberId, addressHistoryId, request
        );
        AddressResponse response = addressManagementService.updateAddress(serviceRequest);
        return ApiResponse.success(response);
    }

    /**
     * 10.6 주소 삭제
     */
    @DeleteMapping("/{addressHistoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long addressHistoryId
    ) {
        addressManagementService.deleteAddress(memberId, addressHistoryId);
    }

    /**
     * 10.7 기본 주소 설정
     */
    @PutMapping("/{addressHistoryId}/primary")
    public ApiResponse<PrimaryAddressResponse> setPrimaryAddress(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long addressHistoryId
    ) {
        PrimaryAddressResponse response = addressManagementService.setPrimaryAddress(memberId, addressHistoryId);
        return ApiResponse.success(response);
    }
}
