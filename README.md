# Sports Trainings Data Management

#### This project, "Personal Sports Backend," is a Spring Boot application designed to manage personal sports and health-related data (related to sports performance, not specifically with health-related interests). It serves as the backend for a sports health journal, providing APIs for data management, integration with external services like Strava and Aemet openData API (which is horrible) and soon will try to leverage AI capabilities to simplify athletes and trainers workload and/or data management.

##### Technologies Used

* **Java 17**: The core programming language and JDK version. Many real products still use Java 8 or less, I wanted to begin with something more fresh but without moving to the latest versions, which maybe are less adopted yet.  
* **Spring Boot 3.5.0**: The foundational framework for building the application, providing rapid development and a robust ecosystem. 3.5.0 was the latest stable version when I began and also allows me to use the latest Spring AI features.  
* **Spring WebFlux & Reactor Core**: Enables a fully reactive and non-blocking architecture, crucial for efficient and scalable integration with external APIs. When I first implemented them, I used .block() so I wasn’t really taking advantage of the asynchronous flow. After a few headaches I managed to change that, but actually I don’t know if I gained something with that change.  
* **Spring Data JPA**: For data persistence and interaction with relational databases. It amazes me all the facilities that Spring Data JPA offers, the pageable ones are a good example.  
* **PostgreSQL**: The primary relational database for storing application data. I added to it the PGvector extension, for the vector databases mainly RAG systems need.  
* **Spring AI**: Integrates AI capabilities, still in testing/learning phase.  
* **Ollama**: Used for local LLM interactions, for now just tested Gemma3 and snowflake-artic embedding model.  
* **Lombok**: Reduces boilerplate code (getters, setters, constructors, etc.) for cleaner and more concise Java code.  
* **ModelMapper**: Another object mapping library, used alongside MapStruct.  
* **WireMock**: Used for testing external API integrations by mocking HTTP responses.  
* **H2 Database**: An in-memory database used for testing purposes.

##### Project Structure

com.pablodoblado.personal\_sports\_back.backend

* BackendApplication.java: The main entry point of the Spring Boot application.  
* chatControllers: Contains controllers related to chat functionalities, likely leveraging Spring AI for conversational interfaces.  
* config: Configuration classes for various aspects of the application.  
* controller: REST controllers handling incoming HTTP requests and orchestrating responses.  
* dto: Data Transfer Objects (DTOs) for data exchange between layers and with external APIs.  
* entity: JPA entities representing the database schema.  
* repository: Spring Data JPA repositories for database interactions.  
* service: Business logic layer, containing the core functionalities and orchestrating interactions between controllers, repositories, and external services.  
  * AemetService: Handles integration with the Aemet weather API in a reactive and resilient manner.  
  * ApiRateLimiterService: Manages rate limits for external API calls to prevent exceeding usage quotas.  
  * DocumentService: Manages document operations, including embedding and storage in the vector store.  
  * MetricaSaludService: Business logic for health metrics.  
  * RagService: Core service for Retrieval Augmented Generation, combining LLM with stored documents.  
  * StravaTokenService: Handles Strava OAuth token management.  
  * TrainingActivityService: Business logic for training activities, including a fully reactive integration with the Strava API.  
  * UsuarioService: Business logic for user management.  
* tools: Utility classes.

##### Key Features and Functionalities

* User Management: Supports the creation, retrieval, and general management of user profiles.  
* Training Activity Tracking: Provides robust capabilities for recording and managing various sports activities. Basically allows the user to synchronize its Strava activities. I really would like Coros to offer a public API, but don’t. The only closed thing I found is a Github repository to bulk export your activities, but with limited amounts of data.  
* Health Metrics Management: Enables the storage and retrieval of user health-related data.  
* Strava Integration:  
  * Asynchronously fetches user activities directly from the Strava API.  
  * Features automatic handling of Strava OAuth token refreshing to maintain continuous access.  
* Aemet (Weather) Integration:  
  * Retrieves relevant meteorological data from the Aemet API, correlated with activity locations and times.  
  * Incorporates a resilient retry mechanism with exponential backoff to effectively handle transient API errors.  
* Reactive and Asynchronous Architecture:  
  * Built on Spring WebFlux and Project Reactor, ensuring long-running operations are handled non-blockingly, which maintains high application responsiveness.  
  * For example, the /fetchStravaActivities endpoint immediately returns a 202 Accepted response, with the data fetching and processing occurring in the background.  
* Resilience and Error Handling:  
  * Includes graceful handling for unknown activity types encountered from the Strava API, mapping them to an UNKNOWN category and logging warnings. This is basically in case Strava adds new types without me noticing it.  
  * Designed to be highly resilient to failures from external APIs, preventing cascading failures and ensuring application stability.  
* AI Capabilities (Spring AI & RAG):  
  * Still exploring them…

  #### 

##### Testing

Here I have to express thanks to Ham Vocke [^1] with his amazing article about testing strategies[^2]. With that in mind, I think I managed (unless I tried to) to design a Pyramid Testing Strategy, with both unity and integration tests. Here, mainly I used: 

* StepVerifier: From Reactor Test, used for testing reactive streams (Mono and Flux).  
* WireMock: Used for mocking external API responses to ensure reliable and isolated testing of services.  
* Mockito: Used to mock my services and repositories when needed.

I also tested the repository interfaces. As Ham Vocke said in the above mentioned article: “...you need to test the custom methods and make sure Spring wiring can connect to your database…”.

##### Future Enhancements

* Find ways to implement useful IA-driven functionalities. I don’t wan’t an assistant chatbot for the User manual. Here, I am thinking a lot about context engineering (from code assistants) and how we could replicate our trainer ideas with this to automate the day-to-day suggestions, which tend to be useless sometimes.  
* Integration with a well-designed but I am afraid not available for free, weather API. The Aemet one limits its measures to certain stations and doesn’t provide some measures often.  
* Enhanced security features (e.g., re-enabling Spring Security). Also important when defining the roles.  
* Development of a frontend application to provide a complete user experience. I am already working on it, using Angular.  
* Studying physiological studies and tips on how to improve the performance of athletes. Learn from people like Killian, Anna Carceller, Aitor Viribay and try to implement some of it’s suggestions and knowledge.  
* Marketing ideas on how to promote the application: I don’t have any idea about these, so it would be cool learning a bit.  
* Business plans, studying the market space…

[^1]:  You can visit his website: https://hamvocke.com/

[^2]:  https://martinfowler.com/articles/practical-test-pyramid.html
