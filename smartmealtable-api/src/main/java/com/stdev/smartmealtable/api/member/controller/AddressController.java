package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.member.dto.request.AddressRequest;
import com.stdev.smartmealtable.api.member.dto.response.AddressResponse;
import com.stdev.smartmealtable.api.member.dto.response.PrimaryAddressResponse;
import com.stdev.smartmealtable.api.member.service.AddressService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주소 관리 API Controller
 */
@RestController
@RequestMapping("/api/v1/members/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 10.3 주소 목록 조회
     * GET /api/v1/members/me/addresses
     */
    @GetMapping
    public ApiResponse<List<AddressResponse>> getAddresses(@AuthUser AuthenticatedUser user) {
        List<AddressResponse> responses = addressService.getAddresses(user.memberId())
                .stream()
                .map(AddressResponse::from)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    /**
     * 10.4 주소 추가
     * POST /api/v1/members/me/addresses
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> addAddress(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = AddressResponse.from(
                addressService.addAddress(user.memberId(), request.toServiceRequest())
        );
        return ApiResponse.success(response);
    }

    /**
     * 10.5 주소 수정
     * PUT /api/v1/members/me/addresses/{addressHistoryId}
     */
    @PutMapping("/{addressHistoryId}")
    public ApiResponse<AddressResponse> updateAddress(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long addressHistoryId,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = AddressResponse.from(
                addressService.updateAddress(user.memberId(), addressHistoryId, request.toServiceRequest())
        );
        return ApiResponse.success(response);
    }

    /**
     * 10.6 주소 삭제
     * DELETE /api/v1/members/me/addresses/{addressHistoryId}
     */
    @DeleteMapping("/{addressHistoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long addressHistoryId
    ) {
        addressService.deleteAddress(user.memberId(), addressHistoryId);
    }

    /**
     * 10.7 기본 주소 설정
     * PUT /api/v1/members/me/addresses/{addressHistoryId}/primary
     */
    @PutMapping("/{addressHistoryId}/primary")
    public ApiResponse<PrimaryAddressResponse> setPrimaryAddress(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long addressHistoryId
    ) {
        addressService.setPrimaryAddress(user.memberId(), addressHistoryId);
        PrimaryAddressResponse response = PrimaryAddressResponse.of(addressHistoryId, true);
        return ApiResponse.success(response);
    }
}
