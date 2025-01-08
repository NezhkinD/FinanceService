package app.Controller;

import app.Entity.UserEntity;
import app.Repository.UserRepository;
import app.Util.HashUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Регистрация пользователя")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Неверный запрос")
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam @Parameter(description = "Логин") String login,
            @RequestParam @Parameter(description = "Пароль") String password
    ) {
        try {
            String hashedPassword = HashUtil.hashString(password);
            UserEntity newUser = UserEntity.create(
                    login,
                    hashedPassword
            );
            if (userRepository.findOneByLogin(newUser.getLogin()).isPresent()) {
                throw new RuntimeException("Пользователь с логином " + newUser.getLogin() + " уже существует, введите другой логин.");
            }

            if (HashUtil.verifyHashString(password, hashedPassword)) {
                throw new Exception("Ошибка при регистрации, повторите позже.");
            }
            UserEntity save = userRepository.save(newUser);

            return ResponseEntity.status(HttpStatus.OK).body("Пользователь " + save.getLogin() + " зарегистрирован!");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + ex.getMessage());
        }
    }
}
