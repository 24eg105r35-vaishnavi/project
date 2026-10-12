package app.repository;

import app.model.ReportSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportSnapshotRepository extends JpaRepository<ReportSnapshot, Long> {
    List<ReportSnapshot> findAllByOrderByGeneratedAtDesc();
}
