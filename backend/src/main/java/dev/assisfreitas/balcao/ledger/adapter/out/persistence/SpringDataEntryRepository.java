package dev.assisfreitas.balcao.ledger.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataEntryRepository extends JpaRepository<EntryJpaEntity, UUID> {

  Optional<EntryJpaEntity> findByIdAndBusinessId(UUID id, UUID businessId);

  boolean existsByReversalOf(UUID reversalOf);
}
