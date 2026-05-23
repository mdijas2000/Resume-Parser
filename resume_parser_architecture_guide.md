# Complete Study Notes: Resume Parser Microservice

These are your detailed "student notes" for the entire Resume Parser backend project. This document breaks down *why* we wrote the code the way we did, the computer science concepts behind the solutions, and how all the moving parts connect.

---

## Module 1: The Microservice Architecture
Instead of building one massive Spring Boot app (a "Monolith"), we built a distributed system. Why? Because text parsing is **computationally heavy**. If the parser crashes because of a corrupted PDF, we don't want the database (Candidate Service) to go down with it.

### The 4 Pillars of our Architecture:
1. **Discovery Server (Netflix Eureka)**: Think of this as a phonebook. Every time a microservice starts up, it registers its name here.
2. **API Gateway (Spring Cloud Gateway)**: The bouncer at the club. The frontend only ever talks to the Gateway on port `8080`. The Gateway looks at the URL (like `/api/parser/upload`) and forwards the request to the correct microservice.
3. **Candidate Service**: The database manager. It connects to PostgreSQL and handles standard CRUD operations using Spring Data JPA.
4. **Parser Service**: The brain. It accepts `MultipartFile` uploads, does heavy text analysis, and generates JSON.

### Inter-Service Communication (OpenFeign)
How does the Parser send data to the Database without the frontend having to make two separate API calls? We used **Spring Cloud OpenFeign**. 
In the Parser Service, we created an interface `CandidateClient`. Because of Eureka, we didn't need to hardcode `localhost:8081`; we just told Feign to look for the name `candidate-service`, and it automatically made the internal HTTP POST request for us!

---

## Module 2: The Core Problem - Extracting Text
To read PDFs and Word docs, we used **Apache Tika**. 
```java
Tika tika = new Tika();
String rawText = tika.parseToString(file.getInputStream());
```
**The Problem:** Tika destroys all visual formatting. Tables, bold text, and two-column layouts are completely flattened into a single, chaotic `String`. If a resume has the Company Name on the left and the Date on the right, Tika might put them on two completely different lines, or smush them together.

---

## Module 3: The "Chunking" Algorithm (Divide & Conquer)
Because the text is chaotic, we cannot read it from top to bottom. We must divide it into logical blocks first.

**How it works (`chunkResumeBySections`):**
1. We read the `rawText` line by line.
2. We look for **Fuzzy Triggers**. We use `.contains("EXPERIENCE")` instead of `.equals("EXPERIENCE")` because human beings format headers weirdly (e.g., "Professional Experience", "Work History").
3. We maintain a "Current Section" state. When we hit the word "EDUCATION", we change our state to `currentSection = "EDUCATION"`.
4. Every subsequent line is appended to a `StringBuilder` mapped to "EDUCATION" until we hit a new trigger word.

**The Result:** We successfully slice the resume into isolated `String` variables. When we parse Education, we don't have to worry about accidentally reading a Job Title.

---

## Module 4: Data Extraction & The "State Machine"
Extracting basic strings like Phone Numbers is easy with Regular Expressions (Regex). But extracting a `List<EducationDTO>` is incredibly hard.

### The "Overwrite" Bug
Initially, our parser read the file, found "RMK College", and saved it to an object. Two lines later, it found "CK Matriculation School", and it overwrote "RMK College"! It didn't know they were two separate schools.

### The Computer Science Solution: State Machines
We built a state machine in `extractEducationDetails` and `extractExperienceDetails`. 
The algorithm keeps a "working memory" object (`currentExp`). For every line it reads, it asks a logical question:

> *"I just found a Date string. Does my `currentExp` object ALREADY have a Date saved? If yes, it means I have moved on to a completely new job entry!"*

```java
// State Machine trigger logic
boolean hasDate = ...;
boolean isNewEntry = (hasDate && currentExp.getDuration() != null);

if (isNewEntry) {
    experienceList.add(currentExp);   // Save the old job
    currentExp = new ExperienceDTO(); // Start a fresh blank job!
}
```

---

## Module 5: Conquering Real-World Edge Cases
Resumes are the hardest documents in the world to parse. Here is how we fixed the major bugs we encountered:

### 1. The En-Dash Bug
* **Symptom:** The parser completely failed to see the date `March 2023 – July 2025`.
* **Root Cause:** The resume used an En-Dash (`–`) instead of a keyboard Hyphen (`-`). This invisible formatting character broke our Regex `\b` (word boundary) logic.
* **The Fix:** We stripped away boundary constraints and built a brutal regex: `Pattern.compile("(19|20)\\d{2}")` which just hunts for ANY four digits starting with 19 or 20, ignoring whatever punctuation surrounds it.

### 2. Sentence Fragments as Companies
* **Symptom:** The company name was extracted as `"systems that drive business success."`
* **Root Cause:** Our heuristic assumed any short line (under 40 chars) was a company name. 
* **The Fix:** We implemented a grammar check. If a line ends in a period `.`, it is a sentence fragment, not a company name (unless it contains `Inc.` or `Ltd.`).

### 3. Inline Data Splitting
* **Symptom:** The resume printed `Wipro - March 2023 - Present` on a single line.
* **The Fix:** We used Regex to "rip" the date off the end of the line dynamically.
```java
// Removes everything from the first month/year to the end of the line
String companyGuess = line.replaceAll("(?i)\\b(jan|feb|mar...|202\\d)\\b.*$", "");
```

---

## Module 6: The Upsert Strategy (Handling Duplicates)
* **The Problem:** The user's resume had a footer that repeated the names of their schools and jobs. The State Machine extracted them a second time, resulting in duplicate JSON.
* **Why standard Java `Set` failed:** A `Set` uses `.equals()`. The master job entry had a `Role`, but the duplicated footer entry didn't (`role: null`). Because the objects weren't 100% identical, `Set` didn't filter them.
* **The Fix:** We built the `addExperienceSafely` "Upsert" (Update or Insert) method. 

```java
private void addExperienceSafely(List<ExperienceDTO> list, ExperienceDTO exp) {
    for (ExperienceDTO existing : list) {
        // If Company and Duration match perfectly...
        if (existing.getCompany().equals(exp.getCompany()) && ...) {
            // It's a duplicate! Don't add it. Instead, merge any missing data!
            if (existing.getRole() == null) existing.setRole(exp.getRole());
            return;
        }
    }
    list.add(exp); // Only add if it's completely unique
}
```

---

## Module 7: Analytical Computation (Total Experience)
We didn't just want to extract text; we wanted to generate new data. 
The `calculateTotalExperience` method:
1. Loops through the deduplicated list of jobs.
2. Extracts all 4-digit years from the `duration` strings into a List.
3. If it finds 2 years (e.g. `2020` and `2023`), it calculates `Math.max(1, 2023 - 2020) = 3 years`.
4. If it finds 1 year and `isCurrentJob` is true, it subtracts it from the current system year (`2026 - 2023 = 3 years`).
5. Sums it all up and assigns it to `totalExperience`.
