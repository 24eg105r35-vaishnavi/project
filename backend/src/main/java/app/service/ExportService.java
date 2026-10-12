package app.service;

import app.model.Achievement;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Service
public class ExportService {

    public String generateCsv(List<Achievement> achievements) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // CSV Header
        pw.println("Roll Number,Student Name,Branch,Category,Achievement Title,Event Name,Organizer,Level,Date,Points,Status");

        for (Achievement a : achievements) {
            String roll = escapeCsv(a.getStudent().getRollNumber());
            String name = escapeCsv(a.getStudent().getUser().getFullName());
            String branch = escapeCsv(a.getStudent().getBranch());
            String cat = escapeCsv(a.getCategory().getName());
            String title = escapeCsv(a.getTitle());
            String event = escapeCsv(a.getEventName());
            String org = escapeCsv(a.getOrganizer());
            String level = escapeCsv(a.getLevel() != null ? a.getLevel().getDisplayName() : "");
            String date = a.getEventDate() != null ? a.getEventDate().toString() : "";
            int points = a.getPointsAwarded() != null ? a.getPointsAwarded() : 0;
            String status = a.getStatus().getLabel();

            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\"%n",
                    roll, name, branch, cat, title, event, org, level, date, points, status);
        }

        pw.flush();
        return sw.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
