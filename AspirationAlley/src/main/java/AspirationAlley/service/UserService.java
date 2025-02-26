package AspirationAlley.service;

import AspirationAlley.model.User;
import AspirationAlley.repository.UserRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String password, String phoneNumber, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    public boolean usernameExceedsChars(String username) {
        if(username.length()>15) {
        	return true;
        }
        return false;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    public List<Boolean> calculateActiveStreak(User user) {
        // Get today's date and the start of the current week (most recent Sunday)
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        // Get the user's last post date and streak count
        LocalDate lastPostDate = user.getLastPostDate();
        int streakCount = user.getStreakCount();

        // Initialize active streak days
        List<Boolean> activeStreak = new ArrayList<>(List.of(false, false, false, false, false, false, false));

        // Validate inputs
        if (lastPostDate == null || streakCount <= 0) {
            System.out.println("No valid streak data available.");
            return activeStreak; // All false if no streak data
        }

        // Iterate through the streak count and mark active days
        LocalDate currentStreakDate = lastPostDate;
        for (int i = 0; i < streakCount; i++) {
            if (currentStreakDate.isBefore(startOfWeek) || currentStreakDate.isAfter(startOfWeek.plusDays(6))) {
                break; // Skip dates outside the current week
            }
            int dayIndex = (currentStreakDate.getDayOfWeek().getValue() % 7); // Map Sunday = 0
            activeStreak.set(dayIndex, true); // Mark the day as active
            currentStreakDate = currentStreakDate.minusDays(1); // Move to the previous day in the streak
        }

        // Debug logs
//        System.out.println("Start of Week: " + startOfWeek);
//        System.out.println("Last Post Date: " + lastPostDate);
//        System.out.println("Streak Count: " + streakCount);
//        System.out.println("Active Streak: " + activeStreak);

        return activeStreak;
    }



    public List<String> getStreakDates() {
        // Generate days of the week starting from Sunday
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);
        List<String> streakDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            streakDates.add(
                startOfWeek.plusDays(i)
                    .getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    .substring(0, 1)
            );
        }
        return streakDates;
    }
    public boolean updateUserProfile(Long userId, String email, String phoneNumber) {
        try {
            // Validate email
            if (!isValidEmail(email)) {
                return false;
            }

            if (!isValidPhoneNumber(phoneNumber)) {
            	return false;
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            userRepository.save(user); // Save updated user to the database
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to validate email
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    // Helper method to validate phone number
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Assuming a valid phone number is 10 digits long
        String phoneRegex = "^[0-9]{10}$";
        return phoneNumber.matches(phoneRegex);
    }

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
    public boolean doesUserExist(String author) {
        return userRepository.existsByUsername(author);
    }
    public boolean doesEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }
}
