package org.gl.eir.repository.audit;

import org.gl.eir.model.audit.ModulesAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ModulesAuditTrailRepository extends JpaRepository<ModulesAuditTrail,Long>, JpaSpecificationExecutor<ModulesAuditTrail> {
}
