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

            // Update fields (except primary key id)
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setLocoPilotId(userDetails.getLocoPilotId());
            user.setDateOfBirth(userDetails.getDateOfBirth());
            user.setDesignation(userDetails.getDesignation());
            user.setDepartment(userDetails.getDepartment());
            user.setActive(userDetails.isActive());
            user.setMobNo(userDetails.getMobNo());

            return userRepository.save(user);
        }
        return null; // alternatively, throw a custom exception
    }

    @Override
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
    }
}
