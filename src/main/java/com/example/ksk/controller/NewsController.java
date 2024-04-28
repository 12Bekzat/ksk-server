package com.example.ksk.controller;

import com.example.ksk.dto.NewsDto;
import com.example.ksk.entity.News;
import com.example.ksk.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NewsController {
    private final NewsRepository newsRepository;

    @GetMapping("/news")
    public ResponseEntity<?> getAllNews() {
        List<News> all = newsRepository.findAll();

        return ResponseEntity.ok(all);
    }

    @GetMapping("/news/{id}")
    public ResponseEntity<?> getAllNews(@PathVariable(name = "id") String id) {
        News news = newsRepository.findById(Long.parseLong(id)).get();

        return ResponseEntity.ok(news);
    }

    @PostMapping("/news/create")
    public ResponseEntity<?> createNews(@RequestBody NewsDto newsDto) {
        News news = new News(newsDto.getTitle(), newsDto.getDate(), newsDto.getText());
        newsRepository.save(news);

        return ResponseEntity.ok(news);
    }

    @GetMapping("/news/remove/{id}")
    public ResponseEntity<?> removeNews(@PathVariable(name = "id") String id) {
        newsRepository.deleteById(Long.parseLong(id));

        return ResponseEntity.ok(id);
    }

    @PostMapping("/news/edit/{id}")
    public ResponseEntity<?> editNews(@PathVariable(name = "id") String id, @RequestBody NewsDto newsDto) {
        News news = newsRepository.findById(Long.parseLong(id)).get();
        news.setTitle(newsDto.getTitle());
        news.setDate(newsDto.getDate());
        news.setText(newsDto.getText());
        newsRepository.save(news);

        return ResponseEntity.ok(news);
    }
}
