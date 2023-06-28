package com.example.asyncmethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class GitHubLookupService {

  private static final Logger logger = LoggerFactory.getLogger(GitHubLookupService.class);

  private final RestTemplate restTemplate;

  public GitHubLookupService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  @Async
  public CompletableFuture<User> findUser(String user) throws InterruptedException {
    logger.info("Looking up " + user);
    String url = String.format("https://api.github.com/users/%s", user);
    User results = restTemplate.getForObject(url, User.class);
    // Artificial delay of 1s for demonstration purposes
    Thread.sleep(1000L);
    return CompletableFuture.completedFuture(results);
  }


  public List<User> findUserAlternate(List<String> users) throws Exception {
    logger.info("Looking up users");

    List<CompletableFuture<User>> completableFeatures = new ArrayList<>();

    for (int i = 0; i < users.size(); i++) {
      final String userName = users.get(i);
      completableFeatures.add(CompletableFuture.supplyAsync(() -> apiCall(userName)));
    }
    CompletableFuture<Void> result = CompletableFuture.allOf(completableFeatures.toArray(new CompletableFuture[0]));

    CompletableFuture<List<User>> allFutureResults = result.thenApply(t -> completableFeatures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList()));
    return allFutureResults.get();
  }


  private User apiCall(String user) {
    String url = String.format("https://api.github.com/users/%s", user);
    User results = restTemplate.getForObject(url, User.class);
    // Artificial delay of 1s for demonstration purposes
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return results;
  }

}
