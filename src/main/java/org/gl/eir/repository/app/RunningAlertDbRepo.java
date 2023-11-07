package org.gl.eir.repository.app;

import org.gl.eir.model.app.RunningAlertDb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RunningAlertDbRepo extends JpaRepository<RunningAlertDb, Long> {

	public RunningAlertDb save(RunningAlertDb alertDb);
}
