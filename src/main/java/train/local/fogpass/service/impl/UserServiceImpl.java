package train.local.fogpass.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import train.local.fogpass.entity.User;
import train.local.fogpass.repository.UserRepository;



import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
