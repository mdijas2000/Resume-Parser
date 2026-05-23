# Microservice Resume Parser

A highly scalable, distributed resume parsing system built with **Java** and **Spring Boot**. This system uses a microservice architecture to ingest unstructured resumes (PDF/Word), process the raw text using intelligent state-machine heuristics, and safely store the structured candidate data.

## 🏗️ Architecture overview

Text parsing is computationally heavy and error-prone. To ensure system stability, this project moves away from a monolithic approach and utilizes a resilient distributed architecture:

* **Discovery Server (Netflix Eureka):** Acts as the service registry, allowing microservices to find and communicate with each other dynamically.
* **API Gateway (Spring Cloud Gateway):** The single entry point for the frontend, routing requests to the appropriate underlying services.
* **Parser Service:** The "brain" of the application. It accepts document uploads, uses **Apache Tika** for text extraction, and runs complex state machine algorithms to chunk sections and extract targeted data (Education, Experience, etc.).
* **Candidate Service:** The database manager handling CRUD operations and connecting to a **PostgreSQL** database to persist parsed profiles.

Microservices communicate internally using **Spring Cloud OpenFeign**.

## ✨ Key Features

* **Intelligent Document Chunking:** Uses fuzzy string matching and trigger words to slice chaotic, unformatted text into logical blocks (e.g., separating "Education" from "Experience").
* **State Machine Data Extraction:** Prevents data overwrites by maintaining an active "working memory" state, allowing the system to accurately differentiate between sequential job entries or schools.
* **Advanced Heuristics:** Employs brutal regex patterns and grammar checks to handle edge cases like invisible En-Dashes (`–`), sentence fragments mistaken for companies, and inline date strings.
* **Upsert Duplication Handling:** Safely handles duplicate entries often found in resume footers by intelligently merging missing data rather than duplicating JSON objects.
* **Analytical Computations:** Automatically calculates aggregate metrics, such as total years of professional experience, dynamically.

## 🛠️ Technology Stack

* **Backend Framework:** Java 17+, Spring Boot
* **Microservices Ecosystem:** Spring Cloud Netflix Eureka, Spring Cloud Gateway, OpenFeign
* **Document Processing:** Apache Tika
* **Database:** PostgreSQL, Spring Data JPA
* **Build Tool:** Maven / Gradle

## 🚀 Getting Started

*(Add instructions here on how to run the Eureka Server first, then the Gateway, Candidate Service, and Parser Service locally. Include any required environment variables for the database.)*
