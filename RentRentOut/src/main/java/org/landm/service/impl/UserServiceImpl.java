package org.landm.service.impl;

import org.landm.entity.User;
import org.landm.repository.UserRepository;
import org.landm.service.UserService;

public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

     public void addUser(User user){
        userRepository.save(user);
     }
}
