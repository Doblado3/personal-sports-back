[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Doblado3/personal-sports-back)
# Sports Trainings Data Management

#### This project, "Personal Sports Backend," is a Spring Boot application designed to manage personal sports and health-related data (related to sports performance, not specifically with health-related interests). It serves as the backend for a sports health journal, providing APIs for data management, integration with external services like Strava and Aemet openData API (which is horrible) and soon will try to leverage AI capabilities to simplify athletes and trainers workload and/or data management.

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
