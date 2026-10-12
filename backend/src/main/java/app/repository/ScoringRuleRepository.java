package app.repository;

import app.model.AchievementLevel;
import app.model.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {
    List<ScoringRule> findByCategoryCode(String categoryCode);
    Optional<ScoringRule> findByCategoryCodeAndLevelAndSubType(String categoryCode, AchievementLevel level, String subType);
}
