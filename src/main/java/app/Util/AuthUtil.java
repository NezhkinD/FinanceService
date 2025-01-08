package app.Util;

import app.Entity.UserEntity;
import app.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthUtil {

    @Autowired
    private UserRepository userRepository;

    public UserEntity auth(String login, String password) {
        Optional<UserEntity> user = userRepository.findOneByLogin(login);
        if (user.isEmpty()) {
            throw new RuntimeException("Пользователь не найден " + login);
        }

        if (HashUtil.verifyHashString(password, user.get().getPassword())) {
            throw new RuntimeException("Введен неверный пароль!");
        }

        return user.get();
    }
}
