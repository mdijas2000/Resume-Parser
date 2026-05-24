package com.resumeparser.parser_service.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.resumeparser.parser_service.client.CandidateClient;
import com.resumeparser.parser_service.dto.EducationDTO;
import com.resumeparser.parser_service.dto.ExperienceDTO;
import com.resumeparser.parser_service.dto.ParsedResumeDTO;
import com.resumeparser.parser_service.dto.ProjectDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumeParserService {

    private final CandidateClient candidateClient;

    public ParsedResumeDTO parseResume(MultipartFile file) {
        try {
            Tika tika = new Tika();
            String rawText = tika.parseToString(file.getInputStream());

            Map<String, String> resumeSections = chunkResumeBySections(rawText);
            System.out.println("---EDUCATION BLOCK---");
            System.out.println(resumeSections.get("EDUCATION"));
            System.out.println("--------------------");
            System.out.println("---EXPERIENCE BLOCK");
            System.out.println(resumeSections.get("EXPERIENCE"));
            System.out.println("--------------------");

            ParsedResumeDTO dto = new ParsedResumeDTO();
            dto.setName(extractName(rawText));
            dto.setEmail(extractEmail(rawText));
            dto.setPhnNo(extractPhoneNumber(rawText));
            dto.setSkills(extractSkills(rawText));
            dto.setEducation(extractEducationDetails(resumeSections.get("EDUCATION")));
            dto.setExperience(extractExperienceDetails(resumeSections.get("EXPERIENCE")));
            dto.setProjects(extractProjectsDetails(resumeSections.get("PROJECTS")));
            dto.setCertifications(extractCertifications(resumeSections.get("CERTIFICATIONS")));
            // Actually call the method and set the total experience!
            dto.setTotalExperience(calculateTotalExperience(dto.getExperience()));

            ParsedResumeDTO saveOrExistingDTO=candidateClient.saveCandidate(dto);
            return saveOrExistingDTO;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from file", e);
        }
    }

    private String extractName(String text) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();
            if (lower.equals("resume") ||
                    lower.equals("cv") ||
                    lower.equals("curriculam vitae") ||
                    lower.equals("profile")) {
                continue;
            }
            if (!trimmed.isEmpty() && trimmed.length() > 2) {
                return trimmed;
            }
        }
        return "Name not found";
    }

    private String extractEmail(String text) {
        String emailRegex = "[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }
        return "Email ID not found";
    }

    private String extractPhoneNumber(String text) {
        String phoneRegex = "(?<!\\w)(?<!\\d)\\+?[1-9]\\d{0,2}[-\\s]?\\(?\\d{1,4}\\)?([-.\\s]?\\d{1,4}){1,4}(?!\\w)";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Phone Number not found";
    }

    private List<String> extractSkills(String text) {
        List<String> knownSkills = List.of(
                "Java", "Spring Boot", "React", "Python", "SQL", "HTML", "CSS", "JavaScript", "Docker", "Git", "AWS",
                "Docker", "Kubernates", "Microservices", "Rest APIs", "MySql", "MongoDB", "Express JS", "Node JS",
                "Angular", "Linux", "JSON", "XML", "Maven", "Gradle", "Git", "GitHub", "GitLab", "Junit",
                "Mockito", "PostMan", "Swagger", "Jenkins", "Jira", "Agile", "Scrum", "SDLC", "STLC",
                "PostgreSQL", "Oracle DB", "Core Java", "Advanced Java", "HTML5", "CSS3", "BOOTSTRAP4",
                "TAILWINDCSS", "REACT.JS", "JAVASCRIPT", "RESTFUL WEB SERVICES", "GITHUB", "GIT",
                "POSTMAN", "INTELLIJ IDEA", "VSCODE", "ECLIPSE", "SCALA", "KOTLIN", "LINUX", "GITLAB", "JENKINS");

        List<String> foundSkills = new ArrayList<>();
        String lowerText = text.toLowerCase();
        for (String skill : knownSkills) {
            if (lowerText.contains(skill.toLowerCase())) {
                foundSkills.add(skill);
            }
        }
        return foundSkills;
    }

    private Map<String, String> chunkResumeBySections(String text) {
        Map<String, StringBuilder> sections = new HashMap<>();
        String currentSection = "HEADER";
        sections.put(currentSection, new StringBuilder());

        String[] lines = text.split("\r?\n");

        for (String line : lines) {
            String normalizedLine = line.trim().toUpperCase();
            if (normalizedLine.isEmpty())
                continue;

            if (normalizedLine.contains("EDUCATION") || normalizedLine.startsWith("ACADEMIC")) {
                currentSection = "EDUCATION";
                sections.putIfAbsent(currentSection, new StringBuilder());
            } else if (normalizedLine.contains("EXPERIENCE") || normalizedLine.contains("WORK HISTORY")) {
                currentSection = "EXPERIENCE";
                sections.putIfAbsent(currentSection, new StringBuilder());
            } else if (normalizedLine.contains("PROJECTS") || normalizedLine.contains("ACCOMPLISHMENTS")) {
                currentSection = "PROJECTS";
                sections.putIfAbsent(currentSection, new StringBuilder());
            } else if (normalizedLine.contains("CERTIFICATION") || normalizedLine.contains("COURSE")) {
                currentSection = "CERTIFICATIONS";
                sections.putIfAbsent(currentSection, new StringBuilder());
            } else if (normalizedLine.contains("SKILLS")) {
                currentSection = "SKILLS";
                sections.putIfAbsent(currentSection, new StringBuilder());
            } else {
                sections.get(currentSection).append(line).append("\n");
            }
        }

        Map<String, String> result = new HashMap<>();
        sections.forEach((key, builder) -> result.put(key, builder.toString().trim()));
        return result;
    }

    private List<EducationDTO> extractEducationDetails(String educationText) {
        List<EducationDTO> educationList = new ArrayList<>();
        if (educationText == null || educationText.isEmpty())
            return educationList;

        String[] lines = educationText.split("\\r?\\n");

        EducationDTO currentEdu = new EducationDTO();

        for (String line : lines) {
            String lowerLine = line.toLowerCase();

            boolean hasDegreeKeyword = lowerLine.contains("b.tech") || lowerLine.contains("b.e")
                    || lowerLine.contains("bachelor")
                    || lowerLine.contains("hsc") || lowerLine.contains("12th")
                    || lowerLine.contains("sslc") || lowerLine.contains("10th") || lowerLine.contains("master");

            boolean hasSchoolKeyword = lowerLine.contains("university") || lowerLine.contains("college")
                    || lowerLine.contains("institute") || lowerLine.contains("school");

            boolean isNewEntry = (hasDegreeKeyword && currentEdu.getDegree() != null)
                    || (hasSchoolKeyword && currentEdu.getInstitution() != null);

            if (isNewEntry && (currentEdu.getPassingYear() != null || currentEdu.getInstitution() != null)) {
                addEducationSafely(educationList, currentEdu);
                currentEdu = new EducationDTO();
            }

            if (lowerLine.contains("hsc") || lowerLine.contains("12th"))
                currentEdu.setDegree("HSC / 12th Grade");
            else if (lowerLine.contains("sslc") || lowerLine.contains("10th"))
                currentEdu.setDegree("SSLC / 10th Grade");
            else if (lowerLine.contains("master") || lowerLine.contains("m.tech") || lowerLine.contains("m.e"))
                currentEdu.setDegree("Master's Degree");
            else if (lowerLine.contains("b.tech") || lowerLine.contains("b.e") || lowerLine.contains("bachelor"))
                currentEdu.setDegree("Bachelor's Degree");

            if (lowerLine.contains("computer science") || lowerLine.contains("cse"))
                currentEdu.setBranch("Computer Science");
            else if (lowerLine.contains("information technology") || lowerLine.contains("it"))
                currentEdu.setBranch("Information Technology");
            else if (lowerLine.contains("mechanical"))
                currentEdu.setBranch("Mechanical Engineering");
            else if (lowerLine.contains("electronics") || lowerLine.contains("ece"))
                currentEdu.setBranch("Electronics and Communication");

            if (lowerLine.contains("university") || lowerLine.contains("college") || lowerLine.contains("institute")
                    || lowerLine.contains("school")) {
                currentEdu.setInstitution(line.trim());
            }

            Matcher yearMatcher = Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(line);
            if (yearMatcher.find()) {
                currentEdu.setPassingYear(yearMatcher.group());
            }

            Matcher scoreMatcher = Pattern
                    .compile("(?i)\\b\\d{1,2}\\.\\d{1,2}\\b|\\b\\d{2,3}\\s*%|\\bcgpa\\s*[:-]?\\s*\\d{1,2}")
                    .matcher(line);
            if (scoreMatcher.find()) {
                currentEdu.setPercentage(scoreMatcher.group());
            }
        }

        if (currentEdu.getInstitution() != null || currentEdu.getDegree() != null
                || currentEdu.getPassingYear() != null) {
            addEducationSafely(educationList, currentEdu);
        }

        return educationList;
    }

    private void addEducationSafely(List<EducationDTO> list, EducationDTO edu) {
        if (edu.getInstitution() == null)
            return;

        for (EducationDTO existing : list) {
            if (existing.getInstitution().equals(edu.getInstitution())) {
                if (existing.getDegree() == null)
                    existing.setDegree(edu.getDegree());
                if (existing.getBranch() == null)
                    existing.setBranch(edu.getBranch());
                if (existing.getPassingYear() == null)
                    existing.setPassingYear(edu.getPassingYear());
                if (existing.getPercentage() == null)
                    existing.setPercentage(edu.getPercentage());
                return;
            }
        }
        list.add(edu);
    }

    private List<ExperienceDTO> extractExperienceDetails(String experienceText) {
        List<ExperienceDTO> experienceList = new ArrayList<>();
        if (experienceText == null || experienceText.isEmpty()) {
            return experienceList;
        }

        String[] lines = experienceText.split("\\r?\\n");
        ExperienceDTO currentExp = new ExperienceDTO();

        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.trim().isEmpty())
                continue;

            boolean hasDate = lowerLine.matches(".*(201\\d|202\\d).*[-–—to]+.*(201\\d|202\\d|present|current).*");

            boolean hasRole = lowerLine.matches(".*\\b(intern|developer|engineer|manager|analyst|coder|specialist|associate|lead|executive|architect|consultant|officer)\\b.*");

            boolean isNewEntry = (hasDate && currentExp.getDuration() != null);

            if (isNewEntry && (currentExp.getRole() != null || currentExp.getCompany() != null)) {
                addExperienceSafely(experienceList, currentExp);
                currentExp = new ExperienceDTO();
            }

            if (hasDate) {
                currentExp.setDuration(line.trim());
                if (lowerLine.contains("present") || lowerLine.contains("current")) {
                    currentExp.setCurrentJob(true);
                }

                if (currentExp.getCompany() == null) {
                    // Match date with optional day of month, e.g. "16 Aug"
                    String companyGuess = line.replaceAll(
                            "(?i)\\b(\\d{1,2}(st|nd|rd|th)?\\s+)?(jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|september|oct|october|nov|november|dec|december|201\\d|202\\d)\\b.*$",
                            "").trim();
                    companyGuess = companyGuess.replaceAll("[-–—|(),]+$", "").trim();
                    if (!companyGuess.isEmpty() && companyGuess.length() < 50) {
                        currentExp.setCompany(companyGuess);
                    }
                }
            } else if (hasRole && currentExp.getRole() == null) {
                currentExp.setRole(line.trim());
            } else if (currentExp.getCompany() == null && line.trim().length() < 40 && !lowerLine.contains("project")) {
                if (!line.trim().endsWith(".") || lowerLine.contains("inc.") || lowerLine.contains("ltd.")) {
                    currentExp.setCompany(line.trim());
                }
            }
        }

        if (currentExp.getRole() != null || currentExp.getCompany() != null) {
            addExperienceSafely(experienceList, currentExp);
        }

        return experienceList;
    }

    private void addExperienceSafely(List<ExperienceDTO> list, ExperienceDTO exp) {
        if (exp.getCompany() == null || exp.getDuration() == null)
            return;

        for (ExperienceDTO existing : list) {
            if (existing.getCompany().equals(exp.getCompany()) && existing.getDuration().equals(exp.getDuration())) {
                if (existing.getRole() == null && exp.getRole() != null) {
                    existing.setRole(exp.getRole());
                }
                return;
            }
        }
        list.add(exp);
    }

    private double calculateTotalExperience(List<ExperienceDTO> experienceList) {
        int totalMonths = 0;
        int currentYear = java.time.Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();

        Map<String, Integer> monthMap = new HashMap<>();
        monthMap.put("jan", 1);
        monthMap.put("january", 1);
        monthMap.put("feb", 2);
        monthMap.put("february", 2);
        monthMap.put("mar", 3);
        monthMap.put("march", 3);
        monthMap.put("apr", 4);
        monthMap.put("april", 4);
        monthMap.put("may", 5);
        monthMap.put("jun", 6);
        monthMap.put("june", 6);
        monthMap.put("jul", 7);
        monthMap.put("july", 7);
        monthMap.put("aug", 8);
        monthMap.put("august", 8);
        monthMap.put("sep", 9);
        monthMap.put("september", 9);
        monthMap.put("oct", 10);
        monthMap.put("october", 10);
        monthMap.put("nov", 11);
        monthMap.put("november", 11);
        monthMap.put("dec", 12);
        monthMap.put("december", 12);

        for (ExperienceDTO exp : experienceList) {
            String duration = exp.getDuration();
            if (duration == null)
                continue;
            String lowerDur = duration.toLowerCase();

            Matcher yearMatcher = Pattern.compile("(19|20)\\d{2}").matcher(duration);
            List<Integer> years = new ArrayList<>();
            while (yearMatcher.find()) {
                years.add(Integer.parseInt(yearMatcher.group()));
            }

            Matcher monthMatcher = Pattern.compile(
                    "\\b(january|jan|february|feb|march|mar|april|apr|may|june|jun|july|jul|august|aug|september|sep|october|oct|november|nov|december|dec)\\b")
                    .matcher(lowerDur);
            List<Integer> months = new ArrayList<>();
            while (monthMatcher.find()) {
                months.add(monthMap.get(monthMatcher.group()));
            }

            if (years.size() >= 2) {
                int startYear = years.get(0);
                int endYear = years.get(years.size() - 1);
                int startMonth = months.size() > 0 ? months.get(0) : 1;
                int endMonth = months.size() >= 2 ? months.get(months.size() - 1) : 12;

                int monthsDiff = ((endYear - startYear) * 12) + (endMonth - startMonth) + 1;
                totalMonths += Math.max(1, monthsDiff);
            } else if (years.size() == 1 && exp.isCurrentJob()) {
                int startYear = years.get(0);
                int startMonth = months.size() > 0 ? months.get(0) : 1;

                int monthsDiff = ((currentYear - startYear) * 12) + (currentMonth - startMonth) + 1;
                totalMonths += Math.max(1, monthsDiff);
            } else if (years.size() == 1) {
                totalMonths += 12;
            }
        }

        double exactYears = totalMonths / 12.0;
        return Math.round(exactYears * 10.0) / 10.0;
    }
    private List<ProjectDTO> extractProjectsDetails(String projectsText) {
        List<ProjectDTO> projectList = new ArrayList<>();
        if (projectsText == null || projectsText.isEmpty()) {
            return projectList;
        }

        String[] lines = projectsText.split("\\r?\\n");
        ProjectDTO currentProject = new ProjectDTO();
        StringBuilder descBuilder = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            
            String lowerLine = trimmed.toLowerCase();

            // Detect Technologies
            if (lowerLine.contains("technologies") || lowerLine.contains("tech stack") || lowerLine.contains("skills used")) {
                String techString = trimmed.replaceAll("(?i).*(technologies|tech stack|skills used)\\s*[:-]?\\s*", "");
                currentProject.setTechnologies(techString);
                continue;
            }

            // Assume short lines without periods are Project Titles (if we don't have one yet)
            if (currentProject.getTitle() == null && trimmed.length() < 60 && !trimmed.endsWith(".")) {
                currentProject.setTitle(trimmed);
            } else {
                // Otherwise it's description
                descBuilder.append(trimmed).append(" ");
            }

            // If we have a title and we hit a new title (or long gap), save it. 
            // For simplicity, we just save when we see a new title candidate, but that's hard to guess.
            // A simple heuristic: if we have a title and we find another short line after some description
            if (currentProject.getTitle() != null && !currentProject.getTitle().equals(trimmed) 
                && trimmed.length() < 50 && !trimmed.endsWith(".") && descBuilder.length() > 20) {
                
                currentProject.setDescription(descBuilder.toString().trim());
                projectList.add(currentProject);
                
                // Start new project
                currentProject = new ProjectDTO();
                currentProject.setTitle(trimmed);
                descBuilder = new StringBuilder();
            }
        }

        if (currentProject.getTitle() != null || descBuilder.length() > 0) {
            currentProject.setDescription(descBuilder.toString().trim());
            projectList.add(currentProject);
        }

        return projectList;
    }

    private List<String> extractCertifications(String certText){
        List<String> certList=new ArrayList<>();
        if(certText==null || certText.isEmpty()){
            return certList;
        }
        String[] lines=certText.split("\\r?\\n");
        for(String line:lines){
            String trimmed=line.trim();
            String lower=trimmed.toLowerCase();

            if(lower.equals("certifications") || lower.equals("courses") || lower.equals("training")){
                continue;
            }
            if(!trimmed.isEmpty() && trimmed.length()>5){
                certList.add(trimmed);
            }
        }
        return certList;
    }
}
