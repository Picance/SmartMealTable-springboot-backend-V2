package com.stdev.smartmealtable.storage.db.common.vo;

import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 임베디드 타입 (JPA Embeddable)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressEmbeddable {
    
    @Column(name = "alias", length = 50, nullable = false)
    private String alias;
    
    @Column(name = "lot_number_address", length = 255)
    private String lotNumberAddress;
    
    @Column(name = "street_name_address", length = 255, nullable = false)
    private String streetNameAddress;
    
    @Column(name = "detailed_address", length = 255)
    private String detailedAddress;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", length = 20)
    private AddressType addressType;
    
    /**
     * Domain VO → JPA Embeddable 변환
     */
    public static AddressEmbeddable from(Address domain) {
        if (domain == null) {
            return null;
        }
        
        AddressEmbeddable embeddable = new AddressEmbeddable();
        embeddable.alias = domain.getAlias();
        embeddable.lotNumberAddress = domain.getLotNumberAddress();
        embeddable.streetNameAddress = domain.getStreetNameAddress();
        embeddable.detailedAddress = domain.getDetailedAddress();
        embeddable.latitude = domain.getLatitude();
        embeddable.longitude = domain.getLongitude();
        embeddable.addressType = domain.getAddressType();
        return embeddable;
    }
    
    /**
     * JPA Embeddable → Domain VO 변환
     */
    public Address toDomain() {
        return Address.of(
                this.alias,
                this.lotNumberAddress,
                this.streetNameAddress,
                this.detailedAddress,
                this.latitude,
                this.longitude,
                this.addressType
        );
    }
}
