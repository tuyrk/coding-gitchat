package com.southwind.controller;

import com.southwind.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@RestController
@RequestMapping("/rest")
public class RestHandler {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/findAll")
    public Collection<User> findAll() {
        return restTemplate.getForEntity("http://localhost:8080/user/findAll", Collection.class).getBody();
    }

    @GetMapping("/findAll2")
    public Collection<User> findAll2() {
        return restTemplate.getForObject("http://localhost:8080/user/findAll", Collection.class);
    }

    @GetMapping("/findById/{id}")
    public User findById(@PathVariable("id") Long id) {
        return restTemplate.getForEntity("http://localhost:8080/user/findById/{id}", User.class, id).getBody();
    }

    @GetMapping("/findById2/{id}")
    public User findById2(@PathVariable("id") Long id) {
        return restTemplate.getForObject("http://localhost:8080/user/findById/{id}", User.class, id);
    }

    @PostMapping("/save")
    public Collection<User> save(@RequestBody User user) {
        return restTemplate.postForEntity("http://localhost:8080/user/save", user, Collection.class).getBody();
    }

    @PostMapping("/save2")
    public Collection<User> save2(@RequestBody User user) {
        return restTemplate.postForObject("http://localhost:8080/user/save", user, Collection.class);
    }

    @PutMapping("/update")
    public void update(@RequestBody User user) {
        restTemplate.put("http://localhost:8080/user/update", user);
    }

    @DeleteMapping("/deleteById/{id}")
    public void delete(@PathVariable("id") Long id) {
        restTemplate.delete("http://localhost:8080/user/deleteById/{id}", id);
    }
}
