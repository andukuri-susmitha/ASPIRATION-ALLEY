package AspirationAlley.service;

import AspirationAlley.model.Post;
import AspirationAlley.model.User;
import AspirationAlley.repository.PostRepository;
import AspirationAlley.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
    public boolean deletePostById(Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return true;
        }
        return false;
    }
    @Transactional
    public Post savePost(String author, String title, String content, LocalDateTime timestamp, byte[] mediaData) {
        Optional<User> userOptional = userRepository.findByUsername(author);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with username: " + author);
        }

        User authorUsername = userOptional.get();

        // Update the user's streak
        updateUserStreak(authorUsername);

        // Create a new Post object with the User object
        Post post = new Post(authorUsername, title, content, timestamp, mediaData);
        return postRepository.save(post);
    }

    @Transactional
    public void updateUserStreak(User user) {
        LocalDate today = LocalDate.now();

        if (user.getLastPostDate() == null || user.getLastPostDate().isBefore(today.minusDays(1))) {
            // Reset streak if the last post was more than a day ago
            user.setStreakCount(1); // Reset to 1 since the user is posting today
        } else if (user.getLastPostDate().isEqual(today.minusDays(1))) {
            // Increment streak if the user posted yesterday
            user.setStreakCount(user.getStreakCount() + 1);
        } else if (user.getLastPostDate().isEqual(today)) {
            // No changes needed if the last post date is already today
            return;
        }

        // Update last post date
        user.setLastPostDate(today);
        userRepository.save(user);
    }
    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByAuthor_Id(userId);
    }
    @Transactional
    public synchronized Post incrementLikes(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setLikes(post.getLikes() + 1);
        return postRepository.save(post);
    }

    @Transactional
    public synchronized Post decrementLikes(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setLikes(post.getLikes() - 1);
        return postRepository.save(post);
    }
    public String getUserName(Long userId) {
        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return user.getUsername();
    }
    public byte[] getProfilePicture(Long userId) {
        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return user.getProfilePicture();
    }

}
