package com.restaurent.manager.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import com.restaurent.manager.entity.Package;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class PackageRespositoryTest {
    @Autowired
    private PackageRepository packageRepository;

    @Test
    void testChuan1_findPackageByName() {
        // Giả sử có dữ liệu mẫu như bên dưới
        Package pkg = Package.builder()
                .packName("Premium")
                .permissions(new HashSet<>())
                .pricePerMonth(100.0d)
                .restaurants(new HashSet<>())
                .build();
        packageRepository.save(pkg);
        packageRepository.flush();

        // when
        Optional<Package> result = packageRepository.findByPackName("Premium");

        // then
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getPackName()).isEqualTo("Premium");
        Assertions.assertThat(result.get().getId()).isEqualTo(pkg.getId());
        Assertions.assertThat(result.get().getPermissions()).isEmpty();
        Assertions.assertThat(result.get().getPricePerMonth()).isEqualTo(100.0d);
        Assertions.assertThat(result.get().getRestaurants()).isEmpty();
    }

    // Không tồn tại package nào có tên đang tìm kiếm
    @Test
    void testNgoaiLe1_findPackageByName() {
        Optional<Package> result = packageRepository.findByPackName("Premium");

        Assertions.assertThat(result).isEmpty();
    }

    @Test
    void testChuan1_findByPricePerMonthGreaterThan() {
        // Tạo và lưu dữ liệu mẫu
        Package pkg1 = Package.builder()
                .packName("Premium")
                .restaurants(new HashSet<>())
                .permissions(new HashSet<>())
                .pricePerMonth(100.0d)
                .build();

        Package pkg2 = Package.builder()
                .packName("Standard")
                .restaurants(new HashSet<>())
                .permissions(new HashSet<>())
                .pricePerMonth(99.0d)
                .build();


        Package pkg3 = Package.builder()
                .packName("Trial")
                .restaurants(new HashSet<>())
                .permissions(new HashSet<>())
                .pricePerMonth(100.0d)
                .build();

        packageRepository.saveAllAndFlush(List.of(pkg1, pkg2, pkg3));

        // Chạy thử
        var result = packageRepository.findByPricePerMonthGreaterThan(99D);

        Assertions.assertThat(result).isNotEmpty();
        // Kiểm tra xem có đúng 2 gói không
        Assertions.assertThat(result.size()).isEqualTo(2);
        // Kiểm tra xem có đúng 2 gói có tên là Trial và Premium
        Assertions.assertThat(result).extracting(Package::getPackName).containsExactlyInAnyOrder("Trial", "Premium");
        // Kiểm tra có đúng 2 gói có giá lớn hơn 99D
        result.forEach(e -> Assertions.assertThat(e.getPricePerMonth()).isEqualTo(100.0d));
    }
}
