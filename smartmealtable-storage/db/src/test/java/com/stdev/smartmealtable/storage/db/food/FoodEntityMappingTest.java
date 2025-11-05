package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.Food;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FoodEntityMappingTest {

    @Test
    void roundtrip_fromDomain_toDomain() {
        Food original = Food.reconstitute(10L, "Sushi", 1L, 3L, "d", "i", 7000);
        FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
        Food mapped = entity.toDomain();

        assertThat(mapped.getFoodId()).isEqualTo(10L);
        assertThat(mapped.getFoodName()).isEqualTo("Sushi");
        assertThat(mapped.getCategoryId()).isEqualTo(3L);
        assertThat(mapped.getAveragePrice()).isEqualTo(7000);
    }
}
