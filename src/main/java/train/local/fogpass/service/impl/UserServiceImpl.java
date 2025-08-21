package train.local.fogpass.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import train.local.fogpass.entity.User;
import train.local.fogpass.repository.UserRepository;
import train.local.fogpass.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUserId(userDetails.getUserId());
            user.setUsername(userDetails.getUsername());
            user.setPwd(userDetails.getPwd());
            user.setDes(userDetails.getDes());
            user.setDept(userDetails.getDept());
            user.setBod(userDetails.getBod());
            return userRepository.save(user);
        }
        return null; // or throw an exception
    }

    @Override
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
    }
}
