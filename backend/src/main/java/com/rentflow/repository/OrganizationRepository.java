package com.rentflow.repository;

import com.rentflow.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
	long countByDeletedAtIsNull();

	long countByIdAndDeletedAtIsNull(UUID id);

	Page<Organization> findByDeletedAtIsNull(Pageable pageable);

	@Query("SELECT o FROM Organization o WHERE o.id = :id AND o.deletedAt IS NULL")
	Optional<Organization> findActiveById(@Param("id") UUID id);

	List<Organization> findByDeletedAtIsNullOrderByNameAsc();
}
