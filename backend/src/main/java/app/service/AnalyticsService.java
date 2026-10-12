package app.service;

import app.model.Achievement;
import app.model.AchievementLevel;
import app.model.AchievementStatus;
import app.model.ReportSnapshot;
import app.repository.AchievementRepository;
import app.repository.ReportSnapshotRepository;
import app.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AchievementRepository achievementRepository;
    private final StudentRepository studentRepository;
    private final ReportSnapshotRepository reportSnapshotRepository;

    public Map<String, Object> getOverallStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalAchievements = achievementRepository.count();
        long approved = achievementRepository.countByStatus(AchievementStatus.APPROVED);
        long pending = achievementRepository.countByStatus(AchievementStatus.PENDING);
        long returned = achievementRepository.countByStatus(AchievementStatus.RETURNED_FOR_CORRECTION);
        long rejected = achievementRepository.countByStatus(AchievementStatus.REJECTED);
        long totalStudents = studentRepository.count();

        stats.put("totalAchievements", totalAchievements);
        stats.put("approved", approved);
        stats.put("pending", pending);
        stats.put("returned", returned);
        stats.put("rejected", rejected);
        stats.put("totalStudents", totalStudents);

        double verificationRate = totalAchievements > 0 ? (approved * 100.0 / totalAchievements) : 0.0;
        stats.put("verificationRate", String.format("%.1f", verificationRate));

        return stats;
    }

    public Map<String, Long> getCategoryDistribution() {
        List<Object[]> raw = achievementRepository.countApprovedByCategory();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : raw) {
            String name = (String) row[0];
            Long count = (Long) row[1];
            result.put(name, count);
        }
        return result;
    }

    public Map<String, Long> getLevelDistribution() {
        List<Object[]> raw = achievementRepository.countApprovedByLevel();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : raw) {
            AchievementLevel level = (AchievementLevel) row[0];
            Long count = (Long) row[1];
            result.put(level != null ? level.getDisplayName() : "Unknown", count);
        }
        return result;
    }

    public Map<String, Long> getBranchDistribution() {
        List<Object[]> raw = achievementRepository.countApprovedByBranch();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : raw) {
            String branch = (String) row[0];
            Long count = (Long) row[1];
            result.put(branch != null ? branch : "General", count);
        }
        return result;
    }

    /**
     * Generates a clean, server-side SVG bar chart. Zero client JavaScript charting required!
     */
    public String generateBarChartSvg(Map<String, Long> data, String primaryColorHex) {
        if (data.isEmpty()) {
            return "<svg viewBox='0 0 500 120' class='svg-chart'><text x='250' y='60' text-anchor='middle' fill='#64748b' font-family='sans-serif' font-size='14'>No data recorded yet</text></svg>";
        }

        long maxVal = data.values().stream().max(Long::compare).orElse(1L);
        if (maxVal == 0) maxVal = 1;

        int width = 580;
        int rowHeight = 36;
        int height = Math.max(160, data.size() * rowHeight + 40);
        int labelWidth = 160;
        int chartWidth = width - labelWidth - 70;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format("<svg viewBox='0 0 %d %d' class='svg-chart' xmlns='http://www.w3.org/2000/svg'>", width, height));

        int y = 30;
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            long count = entry.getValue();
            int barWidth = (int) ((count * 1.0 / maxVal) * chartWidth);
            if (barWidth < 4 && count > 0) barWidth = 4;

            // Label
            svg.append(String.format("<text x='%d' y='%d' text-anchor='end' fill='#334155' font-family='Outfit, sans-serif' font-weight='500' font-size='13'>%s</text>",
                    labelWidth - 10, y + 14, escapeXml(entry.getKey())));

            // Background track
            svg.append(String.format("<rect x='%d' y='%d' width='%d' height='18' rx='4' fill='#f1f5f9'/>",
                    labelWidth, y, chartWidth));

            // Foreground bar with nice gradient / color
            svg.append(String.format("<rect x='%d' y='%d' width='%d' height='18' rx='4' fill='%s'/>",
                    labelWidth, y, barWidth, primaryColorHex));

            // Count label
            svg.append(String.format("<text x='%d' y='%d' text-anchor='start' fill='#0f172a' font-family='Outfit, sans-serif' font-weight='700' font-size='13'>%d</text>",
                    labelWidth + barWidth + 8, y + 14, count));

            y += rowHeight;
        }

        svg.append("</svg>");
        return svg.toString();
    }

    @Transactional
    public ReportSnapshot createReportSnapshot(String reportType, String title, String academicYear, String generatedBy) {
        List<Achievement> approved = achievementRepository.findAllApproved();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRecords", approved.size());
        summary.put("categoryCounts", getCategoryDistribution());
        summary.put("levelCounts", getLevelDistribution());
        summary.put("branchCounts", getBranchDistribution());

        String summaryText = "Accreditation Report (" + reportType + ") for AY " + academicYear +
                ". Total verified records: " + approved.size() + ". Generated by " + generatedBy;

        ReportSnapshot snapshot = ReportSnapshot.builder()
                .reportType(reportType)
                .title(title)
                .academicYear(academicYear)
                .generatedBy(generatedBy)
                .totalRecordsIncluded((long) approved.size())
                .summaryText(summaryText)
                .snapshotDataJson(summary.toString())
                .generatedAt(LocalDateTime.now())
                .build();

        return reportSnapshotRepository.save(snapshot);
    }

    public List<ReportSnapshot> getAllSnapshots() {
        return reportSnapshotRepository.findAllByOrderByGeneratedAtDesc();
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
