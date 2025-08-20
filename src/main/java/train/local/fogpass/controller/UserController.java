package train.local.fogpass.controller;

import train.local.fogpass.entity.User;
import train.local.fogpass.service.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {


    private final UserServiceImpl userServiceImpl;

    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userServiceImpl.save(user));
    }

    // READ ALL
    @GetMapping
    public List<User> getAllUsers() {
        return userServiceImpl.findAll();
    }

    // READ by ID
    @GetMapping("id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userServiceImpl.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("id/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userServiceImpl.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setPassword(updatedUser.getPassword());
                    return ResponseEntity.ok(userServiceImpl.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("id/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (userServiceImpl.findById(id).isPresent()) {
            userServiceImpl.deleteById(id);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
